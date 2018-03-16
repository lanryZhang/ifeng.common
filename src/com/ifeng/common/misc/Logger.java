package com.ifeng.common.misc;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 这个类是log机制的包装。这个接口内部的实现与外部无关。 目前是log4j的实现。今后也可以扩展为其它实现。
 * <p>
 * 使用者可以只用这里的配置。如果一个系统中，大家都用这里的方法进行log配置， 就可以保证这些配置之间是兼容的，而不会产生以往的Appender
 * Closed错误。
 * <p>
 * 另外，这里提供了一个缺省的配置。
 * <p>
 * 使用者可以把它当作一个标准的Logger，也可以不用它，而用commons-logging
 * 
 * 指南：如何避免Logger冲突，要遵循以下原则： a) 不要使某个category中包含其父category相同的appender。
 * 
 * @author jinmy
 */

public class Logger {

	private org.apache.log4j.Logger log;

	private static final String DEFAULT_CONFIG_FILE = "default-log4j-config.xml";

	/**
	 * 用于在root category中作为一个标志，以判断在configure过程中是否重新配置了root
	 */
	private static final Appender APPENDER_TAG = new ConsoleAppender();

	// 先load一个缺省的配置文件，将所有的东西都输出到标准输出
	static {
		try {
			InputStream inputStream = Logger.class
					.getResourceAsStream(DEFAULT_CONFIG_FILE);
			Document doc = XmlLoader.parseXMLDocument(inputStream,
					new Properties(), null);
			DOMConfigurator.configure(doc.getDocumentElement());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 调入一个新的Log4j配置。配置是可以累加的，但要防止冲突
	 * 
	 * @param fullPathName
	 *            配置文件的全路径名
	 * @param properties
	 *            如果非null，用它替换配置文件中的属性引用(或赋值)
	 * @see XmlLoader#parseXMLDocument(String, Properties)
	 */
	public static void configure(String fullPathName, Properties properties) {
		try {
			Document doc = XmlLoader.parseXMLDocument(fullPathName, properties);
			configure(doc.getDocumentElement());
		} catch (Throwable e) {
			System.err.println("Error loading log4j config file: "
					+ fullPathName);
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param url
	 *            配置文件URL
	 * @param properties
	 *            如果非null，用它替换配置文件中的属性引用(或赋值)
	 */
	public static void configure(URL url, Properties properties) {
		try {
			Document doc = XmlLoader.parseXMLDocument(url, properties);
			configure(doc.getDocumentElement());
		} catch (Throwable e) {
			System.err.println("Error loading log4j config file: " + url);
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 用一个DOM element配置log4j。配置是可以累加的，但要防止冲突
	 * 
	 * @param element
	 *            符合log4j规定的DOM element
	 */
	public static void configure(Element element) {
		try {
			// 先得到所有的root category的appender，避免它们被关闭
			LoggerRepository repository = LogManager.getLoggerRepository();
			org.apache.log4j.Logger root = repository.getRootLogger();
			List appenders = new ArrayList();
			for (Enumeration en = root.getAllAppenders(); en.hasMoreElements();) {
				Appender appender = (Appender) en.nextElement();
				appenders.add(appender);
			}
			// 从root中删除所有的appender，这时它们不会被关闭，
			// 而已经获得这些appender的地方可以继续使用它们
			// 不能用removeAllAppenders，因为这个调用会关闭这些appender
			for (Iterator it = appenders.iterator(); it.hasNext();) {
				root.removeAppender((Appender) it.next());
			}
			root.addAppender(APPENDER_TAG);
			DOMConfigurator.configure(element);
			if (root.isAttached(APPENDER_TAG)) {
				// 这次config没有修改root category
				root.removeAllAppenders(); // 只是remove APPENDER_TAG
				for (Iterator it = appenders.iterator(); it.hasNext();) {
					root.addAppender((Appender) it.next());
				}
			}
		} catch (Throwable e) {
			System.err.println("Error loading log4j config: " + element);
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
	}

	public static Logger getLogger(String name) {
		return new Logger(name);
	}

	public static Logger getRootLogger() {
		return new Logger();
	}

	/**
	 * Same as calling <code>getLogger(clazz.getName())</code>.
	 */
	public static Logger getLogger(Class clazz) {
		return new Logger(clazz.getName());
	}

	protected Logger() {
		log = org.apache.log4j.Logger.getRootLogger();
	}

	protected Logger(String name) {
		log = org.apache.log4j.Logger.getLogger(name);
	}

	// ----------------------------------------------------- Logging Properties
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	public boolean isWarnEnabled() {
		return log.isEnabledFor(Level.WARN);
	}

	public boolean isErrorEnabled() {
		return log.isEnabledFor(Level.ERROR);
	}

	public void debug(Object message) {
		log.debug(message);
	}

	public void debug(Object message, Throwable t) {
		log.debug(message, t);
	}

	public void info(Object message) {
		log.info(message);
	}

	public void info(Object message, Throwable t) {
		log.info(message, t);
	}

	public void warn(Object message) {
		log.warn(message);
	}

	public void warn(Object message, Throwable t) {
		log.warn(message, t);
	}

	public void error(Object message) {
		log.error(message);
	}

	public void error(Object message, Throwable t) {
		log.error(message, t);
	}

	public void fatal(Object message) {
		log.fatal(message);
	}

	public void fatal(Object message, Throwable t) {
		log.fatal(message, t);
	}

	// 以下方法是log4j专用的，用于一些特殊情况，比如测试
	public void addAppender(Appender newAppender) {
		log.addAppender(newAppender);
	}

	public Enumeration getAllAppenders() {
		return log.getAllAppenders();
	}

	public Appender getAppender(String name) {
		return log.getAppender(name);
	}

	public void removeAllAppenders() {
		log.removeAllAppenders();
	}

	public void removeAppender(String name) {
		log.removeAppender(name);
	}

	public void removeAppender(Appender appender) {
		log.removeAppender(appender);
	}

	public void setLevel(Level level) {
		log.setLevel(level);
	}
}
