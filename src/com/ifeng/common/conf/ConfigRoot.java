package com.ifeng.common.conf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.map.LRUMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ifeng.common.misc.BeanTools;
import com.ifeng.common.misc.FileTools;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.Primitives;
import com.ifeng.common.misc.XmlLoader;
import com.ifeng.common.misc.BeanTools.PropertyInterceptor;
import com.sun.org.apache.xerces.internal.dom.NodeImpl;

/**
 * <title> 配置树 - common.conf核心类 </title>
 * 
 * <pre>
 * 代表一个配置树对象,一般映射一个配置文件.<br>
 * 文件内部为一个树状结构，采用如下的配置格式来完成一个配置文件：
 *   &lt;config&gt;
 *   	&lt;config name=... value=.../&gt;
 *   	&lt;config name=... value=.../&gt;
 *   &lt;/config&gt;
 * <br>
 * 采用配置文件的路径和其以来的环境变量来构造一个配置树实例, 即可完成“依赖注入”的装载工作.
 * 例如：
 *    ConfigRoot config = new ConfigRoot("/data/test/conf","test.xml",System.getProperties());
 *      或者
 *    ConfigRoot config = new ConfigRoot("/data/test/conf/test.xml",System.getProperties());
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class ConfigRoot {
	//空参数 在反射寻找没有参数的方法时使用
	private static final Class[] EMPTY_ARG = {};

	private static final Logger log = Logger.getLogger(ConfigRoot.class);

	private static final String PACKAGE_NAME = ConfigRoot.class.getPackage().getName();

	//Configurable.config的参数列表，或者同样形式的constructor
	private static final Class[] CONFIG_ARGS = { ConfigRoot.class, Object.class, Element.class };

	private static final Class[] SINGLE_STRING = { String.class };

	
	/**
	 * 用于标识一个类型的实际类型由于是null，而无法判断其具体类型. 因为null可以赋值给任何Object类型，这里用它来表示，以便判断可赋值性
	 */
	private static class ClassForNullClass { // null class
	}

	private static final Class NULL_CLASS = ClassForNullClass.class;

	private static final int DECODE_METHOD_CACHE_SIZE = 64;

	private static Map decodeMethodCache = Collections.synchronizedMap(new LRUMap(DECODE_METHOD_CACHE_SIZE));
	 /**
     * 存放当前配置文件名的属性名字，由configRoot在读取配置文件之前设置.
     * 在配置文件中，可以用${config.home}来访问这个属性。
     * 注意：它只是给配置文件用的，在程序中不能用它。
     */
    public static final String CONFIG_CURRENT_CONFIG_FILE_PROP = "config.currentFile";
    
	//存放所有的配置对象
    private static Map configRoots = Collections.synchronizedMap(new HashMap());

	private static final ConfigPropertyInterceptor _configPropertyInterceptor = new ConfigPropertyInterceptor();

	/**
	 * NULL_DECODE_METHOD用于在decodeMethodCache中，表示没有decode方法的缺省decode方法
	 * 并不真的被调用。这里用getMethods()[0]只是为了简化错误处理.
	 */
	private static final Method NULL_DECODE_METHOD = ConfigRoot.class
			.getMethods()[0];

	// 根元素的name属性，可能是空。可用用它来查询全局的ConfigRoot(ConfigRoot.lookup...)
	private String name;

	// 配置文件的路径
	private URL home;

	// 配置文件的名称
	private String rootConfigFile;

	// 配置文件依赖的环境变量
	private Properties properties;

	// 整个config的root
	private Object root;

	private Stack contextStack = new Stack();

	/**
     * 使用规范的xsd，用CONFIG_NS命名空间。用xsi:type指定Configurable类型。
     * 其它模块可以扩展自己的名字空间，写自己的xsd。
     */
    public static final String CONFIG_NS = "java:" + ConfigRoot.class.getPackage().getName();
    /**
     * XML Schema Instance name space
     */
    public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
	/**
	 * 为了能在config getValue/setValue过程中，截获到中间过程的ConfigException
	 */
	private static class ConfigPropertyInterceptor implements
			PropertyInterceptor {
		public void afterGet(Object object, String name, Object key,
				Object value) {
		}

		public void beforeGet(Object object, String name, Object key) {
			if (object instanceof ConfigException) {
				throw (ConfigException) object;
			}
		}
	}

	/**
	 * 需要分开调用init的情况. 有时需要先有configRoot的实例，而在init过程中可能用到这个实例.
	 */
	public ConfigRoot() {
		//
	}

	/**
	 * 初始化一个配置使用的构造函数.
	 * 
	 * @param home config 文件所在的目录，所有的文件名均相对于这个目录。
	 * 这个目录名可以是一个URL字符串(如jar: ...)
	 * @param rootConfigFile 配置文件名
	 * @param properties 输入的properties。如果为空，则内部自动根据系统properties创建
	 */
	public ConfigRoot(String home, String rootConfigFile, Properties properties) {
		init(home, rootConfigFile, properties);
	}

	/**
	 * 同上. 但根据configPath自动计算home和rootConfigFile(根据最后一个'/'符号)
	 * 
	 * @param configPath 一般应当是一个全路径。如果是部分路径，取决于当前目录
	 * @param properties 输入的properties。如果为空，则内部自动根据系统properties创建
	 */
	public ConfigRoot(String configPath, Properties properties) {
		String fullpath = FileTools.formatFilePath(configPath);
		init(FileTools.dirName(fullpath), FileTools.baseName(fullpath),
				properties);
	}

	/**
	 * 同上
	 * 
	 * @param home 与上面的constructor不同的是，这里传一个java.net.URL，以方便配置文件在jar中使用。
	 * 具体使用方法：new ConfigRoot(XXXX.class.getResource(""), 文件名, ...)
	 * @see ConfigRoot#ConfigRoot(String, String, Properties)
	 */
	public ConfigRoot(URL home, String rootConfigFile, Properties properties) {
		try {
			init(home, rootConfigFile, properties);
		} catch (MalformedURLException e) {
			// never occur?
			throw new ConfigException(e);
		}
	}

	/**
	 * 同上. 但根据configURL自动计算home和rootConfigFile(根据最后一个'/'符号)
	 */
	public ConfigRoot(URL configURL, Properties properties) {
		String strURL = configURL.toExternalForm();
		try {
			init(new URL(FileTools.dirName(strURL, true)), FileTools
					.baseName(strURL), properties);
		} catch (MalformedURLException e) {
			// never occur?
			throw new ConfigException(e);
		}
	}

	public void init(String home0, String rootConfigFile0,
			Properties properties0) {
		try {
			init(new URL("file", "", home0), rootConfigFile0, properties0);
		} catch (MalformedURLException e) {
			// never occur?
			throw new ConfigException(e);
		}
	}

	/**
	 * 初试化一个配置,将读取配置文件,构造对象,放入自身configRoots中.
	 * 
	 * @param home0 文件所在路径
	 * @param rootConfigFile0 文件名称
	 * @param properties0 依赖的环境,为空将使用System.getProperties()
	 * 
	 * @throws MalformedURLException 文件路径错误时将抛出此异常.
	 */
	public void init(URL home0, String rootConfigFile0, Properties properties0)
			throws MalformedURLException {
		this.home = home0;
		this.rootConfigFile = rootConfigFile0;
		
		//为空,则使用System.getProperties()
		this.properties = properties0 == null ? System.getProperties()
				: properties0;

		Element docElement = loadConfigFileByURL(FileTools.combineURL(home0,
				rootConfigFile0));
		// 这里将createValueObjectOnly和configValueObject分开，使得在
		// 配置过程中，即可访问root
		// 根元素的缺省类型为.MapConfig
		this.name = XmlLoader.getAttribute(docElement, "name", null);
		this.root = createValueObjectOnly(null, docElement, MapConfig.class);
		if (this.name != null) {
			ConfigRoot old = (ConfigRoot) configRoots.put(this.name, this);
			if (old != null) {
				log.warn("Config in " + this.rootConfigFile
						+ " has the same ConfigRoot name as "
						+ old.getRootConfigFile());
			}
		}
		pushContext(new ConfigContext(this.root, FileTools.combineURL(home0,
				rootConfigFile0)));
		
		//创建内嵌对象
		configValueObject(this.root, null, docElement);
	}

	/**
	 * 得到某个配置项的值. 配置项名称使用BeanUtils支持的嵌套property名字的格式。
	 * 我们把所有的名字格式均用嵌套方式
	 * 表示，而不用下标和Mapped方式。嵌套方式中，如果名字中有"."字符，要把名字用'&lt;' '&gt;'
	 * (尖括号)括起来。getValue中会自动将其转换为 如ConfigRoot.getValue("abc");
	 * ConfigRoot.getValue("abc.def"); key如果以'.'开头，则相对于currentRoot (只在配置过程中有效)
	 * 
	 * TODO 减少同步范围
	 * 
	 * @return 这里不处理错误情况，如果不存在，则返回null 但如果是ConfigException，则throw之
	 */
	public synchronized Object getValue(String key) {
		Object result;
		try {
			if (key.startsWith(".")) {
				result = BeanTools.getProperty(getCurrentFileRoot(), key
						.substring(1), _configPropertyInterceptor);
			} else {
				result = BeanTools.getProperty(this.root, key,
						_configPropertyInterceptor);
			}
		} catch (IllegalArgumentException e) {
			// 当key路径的中间级某级为null时，PropertyUtils会抛出这个异常
			return null;
		} catch (NoSuchMethodException e) {
			// 当key路径的中间级某级不存在时，PropertyUtils会抛出这个异常
			return null;
		} catch (ConfigException e) {
			throw e;
		} catch (Throwable e) {
			throw new ConfigException(e);
		}
		// 读配置文件时缓存的异常
		if (result instanceof ConfigException) {
			throw (ConfigException) result;
		}
		if (result instanceof Throwable) {
			throw new ConfigException((Throwable) result);
		}
		if (result == null) {
			if (log.isInfoEnabled()) {
				log.info("config getValue('" + key + "') return null.");
			}
		} else if (log.isDebugEnabled()) {
			log.debug("config getValue('" + key
					+ "') return a object of class "
					+ result.getClass().getName());
		}
		return result;
	}

	/**
	 * 设置一个key的值. key如果以'.'开头，则相对于currentFileRoot (只在配置过程中有效)
	 * @throws ConfigException (unchecked)
	 */
	public synchronized void setValue(String key, Object value) {
		if (log.isDebugEnabled()) {
			log.debug("setValue: key='" + key + "' value='" + value + "'");
		}
		try {
			if (key.startsWith(".")) {
				BeanTools.setProperty(getCurrentFileRoot(), key.substring(1),
						value, true, _configPropertyInterceptor);
			} else {
				BeanTools.setProperty(this.root, key, value, true,
						_configPropertyInterceptor);
			}
		} catch (Exception e) {
			throw new ConfigException(e);
		}
	}

	

	/**
	 * 配置一个XML子元素.
	 * 
	 * @param parent 当前配置元素所依赖的父对象,一般情况下为调用者.
	 * @param configEle 当前配置元素
	 * @param elementName 子元素的tag名称
	 * @param required 是否必须，如果true而且不存在，则抛异常；如果false而且不存在，返回null
	 * @param defaultType 缺省类型
	 * @return 配置好的子元素对象
	 */
	public Object createChildObject(Object parent, Element configEle,
			String elementName, boolean required, Class defaultType) {
		Element ele = XmlLoader.getChildElement(configEle, null, elementName,
				required);
		if (ele == null) {
			return null;
		}
		return createValueObject(parent, ele, defaultType);
	}

	public Object createChildObject(Object parent, Element configEle,
			String elementName, boolean required) {
		return createChildObject(parent, configEle, elementName, required, null);
	}

	public Object createChildObject(Object parent, Element configEle,
			String elementName) {
		return createChildObject(parent, configEle, elementName, false, null);
	}

	/**
	 * 配置0到多个XML子元素.
	 * 
	 * @param parent 当前配置元素所依赖的父对象,一般情况下为调用者.
	 * @param configEle 当前配置元素
	 * @param elementName 子元素的tag名称
	 * @param defaultType 缺省类型
	 * @return 配置好的子元素对象列表
	 */
	public List createChildObjects(Object parent, Element configEle,
			String elementName, Class defaultType) {
		Element[] children = XmlLoader.getChildElements(configEle, null,
				elementName);
		List result = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			result.add(createValueObject(parent, children[i], defaultType));
		}
		return result;
	}

	public List createChildObjects(Object parent, Element configEle,
			String elementName) {
		return createChildObjects(parent, configEle, elementName, null);
	}

	/**
	 * 提供一个utility function，供其它的Config创建value对象.
	 * 相当于ceateValueObjectOnly之后调用configValueObject。 形式如：&lt;xxxxx
	 * [type="xxx"] value="xxx"&gt; (缺省type为java.lang.String) 或者：
	 * 
	 * <pre>
	 *              &lt;xxxxx xxxxx所需的属性 [type=&quot;xxx&quot;]&gt;
	 *                 由实现了Configurable接口的valueObject处理的内容
	 *              &lt;/xxxxx&gt;
	 * </pre>
	 * 
	 * @param configEle 配置元素
	 * @return 配置好的对象
	 */
	public Object createValueObject(Object parent, Element configEle,
			Class defaultType) {
		Object beforeConfig = createValueObjectOnly(parent, configEle,
				defaultType);
		return configValueObject(beforeConfig, parent, configEle);
	}

	/**
	 * 与createValueObject相比，只是创建对象本身，但并不配置它.
	 * 在某些情况下，我们需要先创建这个对象，并把它登记到配置树中，然后再配置它。 这样可以在配置过程中就可以引用这个对象。
	 * 一般来说我们只是在MapConfig中用这个特性 格式参见@see #createValueObject(Object, Element,
	 * Class)
	 * 
	 * @param configEle 配置元素
	 * @return 创建的对象，但并未配置。
	 */
	public Object createValueObjectOnly(Object parent, Element configEle,
			Class defaultType) {
		//支持新格式
        convertExtension(configEle);
		Class clazz = getTypeClass(configEle, "type", defaultType);

		if (clazz == String.class) {
			// 缺省类型为字符串
			return XmlLoader.getAttribute(configEle, "value", "");
		}

		// 先看是否可以直接通过config constructor初始化
		Object result = createDirectConfigObject(parent, configEle, clazz);
		if (result == null) {
			result = createObject(configEle, clazz);
		}

		return result;
	}
	public Object createValueObjectOnly(Object parent, Element configEle) {
		return createValueObjectOnly(parent, configEle, null);
	}

	/**
	 * 配置一个由createValueObjectOnly创建的对象.
	 * 
	 * @param configObj 由createValueObjectOnly创建的对象
	 * @param configEle 配置元素
	 * @return 配置好的对象
	 */
	public Object configValueObject(Object configObj, Object parent,
			Element configEle) {
		
		//支持Configurable接口
		if (configObj instanceof Configurable) {
			if (log.isDebugEnabled()) {
				log.debug("Configuring Object at "
						+ XmlLoader.getElementLocation(configEle));
			}
			configObj = ((Configurable) configObj).config(this, parent,
					configEle);
		}
		setObjectProperties(configEle, configObj);
		return configObj;
	}

	/**
	 * 得到一个参数元素的类型. 如果指定了declared-type，则使用它。
	 * 如果没有指定，则需要根据argValue来猜测。
	 */
	public Class getArgType(Element argEle, Object argValue) {
		if (argEle.hasAttribute("declared-type")) {
			return getTypeClass(argEle, "declared-type", null);
		}
		// 如果argValue是null，则可以赋值给所有的目标
		if (argValue == null) {
			return NULL_CLASS;
		}
		// 先得到type指定的类型，如果是primitive，则直接返回，否则用实际的对象类型
		Class result = getTypeClass(argEle, "type", null);
		if (result.isPrimitive()) {
			return result;
		}
		return argValue.getClass();
	}

	/**
	 * 得到一个configEle中type属性所指定的type.
	 * 
	 * @param defaultType 缺省的type，如果没有指定type属性，则使用这个type 如果为null，则缺省为String.class
	 * @return Type class.
	 */
	public Class getTypeClass(Element configEle, String attrName,
			Class defaultType) {
		String type = XmlLoader.getAttribute(configEle, attrName, null);
		if (type == null) {
			return defaultType == null ? String.class : defaultType;
		}
		int dotPos = type.indexOf('.');
		// 如果类名以"."开头，则自动加上本类的package名
		Class clazz = null;

		if (dotPos == 0) {
			type = PACKAGE_NAME + type;
		} else if (dotPos == -1) {
			// 没有"."，则猜想是primitive类型
			clazz = Primitives.getPrimitiveTypeClass(type);
		}
		
		if (clazz == null) { // 不是primitive类型
			try {
				clazz = Class.forName(type);
			} catch (ClassNotFoundException e) {
				throw new ConfigException(configEle, e);
			}
		}
		return clazz;
	}

	public static Object decodeString(Class clazz, String valueStr,
			Properties props) throws Exception {
		valueStr = replace(valueStr, props);
		return decodeString(clazz, valueStr);
	}

	/**
	 * 根据类型，对字符串解码. 优先调用该类的decode(String)静态方法。
	 * java.lang中的一些类有这个方法，如Integer、Double等，它们支持更多的格式，如0x等。
	 * 其次调用valueOf(String)静态方法 如果没有decode或valueOf方法，则使用ConvertUtils转换
	 * 
	 * @param valueStr 值字符串
	 * @return 字符串解码的结果
	 * @throws Exception 简易写法，包括在反射和BeanUtils中抛出的任何异常
	 */
	public static Object decodeString(Class clazz, String valueStr)
			throws Exception {
		if (clazz.equals(String.class)) {
			return valueStr;
		}
		if (clazz.isPrimitive()) {
			// 将primitive类型转换为对应的对象类型
			clazz = Primitives.getTypeClass(clazz);
		}
		Method decodeMethod = (Method) decodeMethodCache.get(clazz);
		if (decodeMethod == null) {
			// 没有在cache中
			decodeMethod = findDecodeStringMethod(clazz);
			if (decodeMethod == null) {
				decodeMethod = NULL_DECODE_METHOD;
			}
			decodeMethodCache.put(clazz, decodeMethod);
		}
		if (decodeMethod == NULL_DECODE_METHOD) {
			Object result = ConvertUtils.convert(valueStr, clazz);
			if (!clazz.isInstance(result)) {
				throw new ConfigException("Can't decode '" + valueStr + "' to "
						+ clazz + ". Static decode(String),"
						+ " valueOf(String) or beanutils converter required");
			}
			return result;
		}
		return decodeMethod.invoke(null, new Object[] { valueStr });
	}

	/**
	 * 创建一个对象. 不能直接用clazz.getConstructor，而要逐个匹配，因为 argTypes可能是子类的类型
	 */
	public Object constructObject(Class clazz, Class[] argTypes,
			Object[] argValues) throws Exception {
		if (argTypes == null || argTypes.length == 0) {
			// 使用空参数的constructor，最多只有一个
			return clazz.newInstance();
		}
		// 先找出所有的待选constructor：参数个数和参数类型均匹配
		Constructor[] ctors = clazz.getConstructors();
		Constructor[] candidates = new Constructor[ctors.length];
		int numCandidates = 0;
		for (int i = 0; i < ctors.length; i++) {
			Class[] paramTypes = ctors[i].getParameterTypes();
			if (isAssignable(argTypes, paramTypes)) {
				candidates[numCandidates++] = ctors[i];
			}
		}
		if (numCandidates == 0) {
			throw new ConfigException("Can't find constructor");
		}
		// 如果选出多个待选constructor，则从这些constructor中，
		// 查找类型最“具体”的constructor(最接近于给定的类型)
		Constructor selected = candidates[0];
		for (int i = 1; i < numCandidates; i++) {
			if (isAssignable(candidates[i].getParameterTypes(), selected
					.getParameterTypes())) {
				selected = candidates[i];
			}
		}
		return selected.newInstance(argValues);
	}

	/**
     * 得到一个相对路径的fileName的全路径URL。可以是jar内的文件
     */
    public URL getConfigFileURL(String fileName, String protocol) {
        try {
            // 如果 protocol 为 null ，使用默认的 protocol
            if (protocol == null) {
                // 如果是绝对路径，协议应该是 "file"
                if (FileTools.isAbsolutePath(fileName)) {
                    protocol = "file";
                } else {
                    protocol = this.getCurrentURL().getProtocol();
                }
            }
            
            if (log.isDebugEnabled()) {
                log.debug("getConfigFileURL - fileName: " 
                        + fileName + ", protocol: " + protocol);
            }

            URL parentURL = new URL(this.getCurrentURL().getProtocol(), "", FileTools
                    .dirName(this.getCurrentURL().getFile(), true));

            if (protocol.equals("classpath")) { //伪协议
                URL configFileURLAbsolute = this.getClass().getClassLoader()
                        .getResource(fileName);
                
                if (configFileURLAbsolute != null) {
                    return configFileURLAbsolute;
                } else { //试着返回相对URL
                    return FileTools.combineURL(parentURL, fileName);
                }
            }
            if (!protocol.equals(this.getCurrentURL().getProtocol())) {
                return new URL(protocol, "", fileName);
            } else {
                if (this.getCurrentURL().getProtocol().equals("file")) {
                    if (FileTools.isAbsolutePath(fileName)) {
                        // 一个绝对路径文件名，而非相对于当前的home
                        return new URL(protocol, "", fileName);
                    }
                }
                if (this.getCurrentURL().getProtocol().equals("jar")) {
                    if (fileName.indexOf(".jar!") != -1) {
                        return new URL(protocol, "", fileName);
                    }
                }                
                return FileTools.combineURL(parentURL, fileName);
            }
        } catch (MalformedURLException e) {
            throw new ConfigException(e);
        }
    }
    
    public URL getCurrentURL() {
        return getContext().getCurrentURL();
    }
    /**
     * 给其它Config的utility方法 读入一个配置文件，并启动后续的配置.
     * fileName是一个相对文件名
     */
    public Element loadConfigFile(String fileName, String protocol) {
        // 替换配置文件中的property
        this.properties.setProperty(CONFIG_CURRENT_CONFIG_FILE_PROP, fileName);

        URL configURL = getConfigFileURL(fileName, protocol);
        
        return loadConfigFileByURL(configURL);
    }
    
    /**
     * 配置一个方法调用.
     * 不能直接clazz.getMethod，而要逐个匹配，因为 argTypes可能是子类的类型
     * 配置的格式是：
     * <pre>
     * ....
     *   &lt;arg ... &gt;
     *   &lt;arg ... &gt;
     * </pre>
     */
    public Object configInvoke(Element configEle, Class clazz,
            String methodName, Object object) {
        // 得到所有的参数子元素(arg)
        Element[] argEles = XmlLoader.getChildElements(configEle, null, "arg");
        // 创建参数对象
        Class[] argTypes = null;
        Object[] argValues = null;
        if (argEles.length > 0) {
            argTypes = new Class[argEles.length];
            argValues = new Object[argEles.length];
            for (int i = 0; i < argEles.length; i++) {
                argValues[i] = createValueObject(this, argEles[i],null);
                argTypes[i] = getArgType(argEles[i], argValues[i]);
            }
        }
        
        Method selected = selectMethod(configEle, clazz, methodName, argTypes);
        if (object == null && !Modifier.isStatic(selected.getModifiers())) {
            throw new ConfigException("Can't find static method");
        }
        try {
            return selected.invoke(object, argValues);
        } catch (InvocationTargetException e) {
            // 将InvocationTargetException拨开，去除其内部的异常
            throw new ConfigException(configEle, "Error invoking method "
                    + selected, e.getCause());
        } catch (Exception e) {
            // 
            throw new ConfigException(configEle, e);
        }
    }

    /**
     * 选择合适的方法，根据方法参数的类型.
     * 寻找同名并参数个数相同的方法中，类型匹配的方法中，类型最接近于给定类型的方法
     */
    private Method selectMethod(Element configEle, Class clazz,
            String methodName, Class[] argTypes) {
        if (argTypes == null || argTypes.length == 0) {
            // 寻找空参数的方法，最多只有一个
            try {
                return clazz.getMethod(methodName, EMPTY_ARG);
            } catch (NoSuchMethodException e) {
                throw new ConfigException(configEle, e);
            }
        }
        // 先找出所有的待选方法：方法名、参数个数和参数类型均匹配
        Method[] methods = clazz.getMethods();
        Method[] candidates = new Method[methods.length];
        int numCandidates = 0;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                Class[] paramTypes = methods[i].getParameterTypes();
                if (isAssignable(argTypes, paramTypes)) {
                    candidates[numCandidates++] = methods[i];
                }
            }
        }
        if (numCandidates == 0) {
            throw new ConfigException("Can't find method " + methodName);
        }
        Method selected = candidates[0];
        // 如果选出多个待选方法，则从这些方法中，查找类型最“具体”的方法(最接近于给定的类型)
        for (int i = 1; i < numCandidates; i++) {
            if (isAssignable(candidates[i].getParameterTypes(), selected
                    .getParameterTypes())) {
                selected = candidates[i];
            }
        }
        return selected;
    }
    protected ConfigContext popContext() {
        return (ConfigContext)this.contextStack.pop();
    }
    
	protected void pushContext(ConfigContext context) {
		this.contextStack.push(context);
	}

	/**
	 * 根据文件URL加载一个文件。
	 * 
	 * @param configURL
	 * @return 文件Element
	 */
	private Element loadConfigFileByURL(URL configURL) {
		try {
			// 打开配置文件
			if (log.isInfoEnabled()) {
				log.info("Reading config file: " + configURL);
			}
			Document doc = XmlLoader.parseXMLDocument(configURL,
					this.properties);
			return doc.getDocumentElement();
		} catch (Throwable e) {
			log.error("Error loading config file " + configURL, e);
			throw new ConfigException("Error loading config file " + configURL
					+ e.getMessage(), e);
		}
	}

	/**
	 * 直接创建一个配置对象. 如果对象存在一个constructor，与Configurable.config同样的参数列表，则
	 * 直接用这个constructor来创建对象，直接配置
	 */
	private Object createDirectConfigObject(Object parent, Element configEle,
			Class clazz) {
		try {
			Constructor configConstructor = clazz.getConstructor(CONFIG_ARGS);
			try {
				return configConstructor.newInstance(new Object[] { this,
						parent, configEle });
			} catch (InvocationTargetException e) {
				Throwable e1 = e.getCause();
				if (e1 instanceof ConfigException) {
					throw (ConfigException) e1;
				} else {
					throw new ConfigException(configEle,
							"Error invoking config constructor "
									+ configConstructor, e);
				}
			} catch (Exception e) {
				throw new ConfigException(configEle,
						"Error invoking config constructor "
								+ configConstructor, e);
			}
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * 设置对象的属性，根据set-property、set-properties配置.
	 */
	private void setObjectProperties(Element configEle, Object result) {
		for (Node node = configEle.getFirstChild(); node != null; node = node
				.getNextSibling()) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element subEle = (Element) node;
				String tag = subEle.getTagName();
				if (tag.equals("set-property")) {
					handleSetProperty(subEle, result);
				} else if (tag.equals("set-properties")) {
					handleSetProperties(subEle, result);
				} 
			}
		}
	}

	/**
	 * 处理&lt;set-property name="名字" .../&gt;.
	 */
	private void handleSetProperty(Element ele, Object result) {
		String propName = XmlLoader.getAttribute(ele, "name");
		try {
			Class propType = BeanTools.getBeanPropertyClass(result, propName);
			if (propType == null) {
				throw new ConfigException(ele, "bean has not property named "
						+ propName);
			}
			// 得到缺省类型(如果没有指定type)。如果propType是Object、Serializable
			// 之类的很基本类型，缺省还是String。一般情况下应当指定具体类型
			// 如果指定了具体类型，则忽略propType
			if (propType.isAssignableFrom(String.class)) {
				// propType is String or super class of String
				propType = String.class;
			}
			Object value = createValueObject(result, ele, propType);
			BeanTools.setProperty(result, propName, value);
		} catch (ConfigException e) {
			throw e;
		} catch (Exception e) {
			throw new ConfigException(ele, e);
		}
	}

	/**
	 * 处理&lt;set-properties 属性名="属性值" .../&gt;.
	 */
	private void handleSetProperties(Element ele, Object result) {
		// 遍历所有的attributes
		NamedNodeMap attrs = ele.getAttributes();
		int length = attrs.getLength();
		for (int j = 0; j < length; j++) {
			Attr attr = (Attr) attrs.item(j);
			if (attr.getNamespaceURI() != null) {
				// 忽略有名字空间的属性
				continue;
			}

			String propName = attr.getLocalName();
			String valueStr = attr.getValue();
			try {
				Class propType = BeanTools.getBeanPropertyClass(result,
						propName);
				if (propType == null) {
					throw new ConfigException(ele,
							"bean has not property named " + propName);
				}
				// 得到缺省类型(如果没有指定type)。如果propType是Object、Serializable
				// 之类的很基本类型，缺省还是String。一般情况下应当指定具体类型
				// 如果指定了具体类型，则忽略propType
				if (propType.isAssignableFrom(String.class)) {
					// propType is String or super class of String
					propType = String.class;
				}
				if (propType.isPrimitive()) {
					// 将primitive类型转换为对应的对象类型
					propType = Primitives.getTypeClass(propType);
				}
				Object value = decodeString(propType, valueStr, this
						.getProperties());
				BeanTools.setSimpleProperty(result, propName, value);
			} catch (Exception e) {
				throw new ConfigException(ele, e);
			}
		}
	}

	/**
	 * 创建对象，根据constructor-arg等.
	 */
	private Object createObject(final Element configEle, Class clazz) {
		if (log.isDebugEnabled()) {
			log.debug("Creating Object of " + clazz + " at "
					+ XmlLoader.getElementLocation(configEle));
		}
		Object result = null;
		Element[] ctorArgEles = XmlLoader.getChildElements(configEle, null,
				"constructor-arg");
		Class[] argTypes = null;
		Object[] argValues = null;
		if (ctorArgEles.length > 0) {
			// constructor-arg inject
			argTypes = new Class[ctorArgEles.length];
			argValues = new Object[ctorArgEles.length];
			for (int i = 0; i < ctorArgEles.length; i++) {
				argValues[i] = createValueObject(null, ctorArgEles[i], null);
				argTypes[i] = getArgType(ctorArgEles[i], argValues[i]);
			}
		} else if (!Configurable.class.isAssignableFrom(clazz)) {
			// 如果是Configurable，则不在这里处理，value由它自己解释
			String value = XmlLoader.getAttribute(configEle, "value", null);
			if (value != null) {
				try {
					result = decodeString(clazz, value, this.getProperties());
				} catch (Exception e) {
					throw new ConfigException(configEle, e);
				}
			}
		}
		if (result == null) {
			try {
				// 创建这个实例
				result = constructObject(clazz, argTypes, argValues);
			} catch (Exception e) {
				throw new ConfigException(configEle, e);
			}
		}
		return result;
	}

	/**
	 * 可解析嵌套的属性定义 目前没处理名字包含#{ ,} 的情况。
	 * 
	 * @param orig
	 * @param props
	 * @return
	 */
	private static String replace(String orig, Properties props) {
		int firstOccur = orig.indexOf("#{");
		if (firstOccur == -1) {
			return orig;
		}// else {
		String pre = orig.substring(0, firstOccur);
		String mid = replace(orig.substring(firstOccur + 2), props);
		firstOccur = mid.indexOf("}");
		if (firstOccur == -1) {
			throw new RuntimeException("no match end tag }.");
		}
		String key = mid.substring(0, firstOccur);
		return pre + props.get(key) + mid.substring(firstOccur + 1);
	}

	/**
	 * @see #decodeString(Class, String)
	 */
	private static Method findDecodeStringMethod(Class clazz) {
		Method decodeMethod = null;
		try {
			// 先找 public static xxx decode(String)方法
			decodeMethod = clazz.getMethod("decode", SINGLE_STRING);
			int modifier = decodeMethod.getModifiers();
			if (!Modifier.isStatic(modifier) || !Modifier.isPublic(modifier)) {
				decodeMethod = null;
			}
		} catch (NoSuchMethodException e) {
			// continue
		}
		if (decodeMethod == null) {
			// 没找到decode方法，找 public static xxx valueOf(String)方法
			try {
				decodeMethod = clazz.getMethod("valueOf", SINGLE_STRING);
				int modifier = decodeMethod.getModifiers();
				if (!Modifier.isStatic(modifier)
						|| !Modifier.isPublic(modifier)) {
					decodeMethod = null;
				}
			} catch (NoSuchMethodException e1) {
				// continue, do nothing
			}
		}
		return decodeMethod;
	}

	/**
	 * 判断srcTypes的值是否可以全部赋值给destTypes. 假定它们都是一样长的，而且没有null.
	 */
	private boolean isAssignable(Class[] srcTypes, Class[] destTypes) {
		if (srcTypes.length != destTypes.length) {
			return false;
		}
		for (int i = 0; i < srcTypes.length; i++) {
			if (destTypes[i].isAssignableFrom(srcTypes[i])) {
				// 如果直接可赋值，继续
				continue;
			}
			if (destTypes[i].isPrimitive()) {
				if ((Primitives.getTypeClass(destTypes[i]))
						.isAssignableFrom(srcTypes[i])) {
					// 如果dest是primitive而且primitive对应的类型可以从src赋值，继续
					continue;
				}
			} else if (srcTypes[i] == NULL_CLASS) {
				// 源值为空，可以赋值给任何类型(除了primitive)
				continue;
			}
			return false;
		}
		return true;
	}

	public String getRootConfigFile() {
		return this.rootConfigFile;
	}

	public Object getRoot() {
		return this.root;
	}

	public String getName() {
		return this.name;
	}

	public Properties getProperties() {
		return this.properties;
	}

	/**
	 * 得到this.home，配置树的根URL，所有的Include等都相对于这个根目录 根目录以"/"结尾.
	 * 
	 * @return Home.
	 */
	public URL getHome() {
		return this.home;
	}

	public Object getCurrentFileRoot() {
		return getContext().getCurrentFileRoot();
	}
	public ConfigContext getContext() {
		return (ConfigContext) this.contextStack.peek();
	}
	 /**
     * 将新格式的定义转换为老格式
     * @see #configValueObject(Object, Object, Element)
     * @param configEle 当前配置元素。会对其内容进行修改
     */
    private void convertExtension(Element configEle) {
        String xsiType = configEle.getAttributeNS(XSI_NS, "type");
        if (configEle instanceof NodeImpl && !"".equals(xsiType)) {
            int colonPos = xsiType.indexOf(':');
            if (colonPos <= 0) {
                throw new ConfigException(configEle, 
                        "Element's xsi:type has no namespace prefix");
            }
            String typePrefix = xsiType.substring(0, colonPos);
            String typeNamespace = ((NodeImpl)configEle).lookupNamespaceURI(
                    typePrefix);
            if (typeNamespace == null || !typeNamespace.startsWith("java:")) {
                throw new ConfigException(configEle,
                        "Invalid type prefix: " + typePrefix
                      + ". Namespace must have a namespace starting with 'java:' "
                      + typeNamespace);
            }
            String typeName = xsiType.substring(colonPos + 1);
            String packageName = typeNamespace.substring(5);
            String className = packageName + '.' + typeName;
            // cf:ConfigItem用来在一些xsd中指定了type为确定类型的地方，
            // 强制使用非configurable的对象，应当已经有type属性，不应当覆盖。
            // 例如：&lt;... xsi:type="cf:ConfigItem" type="某种类型"
            if (!className.equals("com.ifeng.common.conf.ConfigItem")) {
                configEle.setAttribute("type", className);
            }
        }
    }
}
