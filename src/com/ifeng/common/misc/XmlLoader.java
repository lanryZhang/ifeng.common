package com.ifeng.common.misc;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.dom.NodeImpl;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XNIException;

/**
 * 实现优化的XML parser loader，并提供得到Element location(文件名、行号、列号)的方法 这个类不要有过多的依赖关系。
 * 
 * @author jinmy
 */
public final class XmlLoader {

	// UserData的名字，记录一个Element的位置信息
	static final String SYSTEMID = "rwu_systemid";

	static final String COLUMNNO = "rwu_columnno";

	static final String LINENO = "rwu_lineno";

	public static final String LOCATION_UNKNOWN = "(location unknown)";

	/**
	 * An extension of the Xerces DOM parser that puts the location of each node
	 * in that node's UserData. jinmy: 来自 Cocoon Forms的DomHelper 避免引入依赖关系。
	 */
	public static class LocationTrackingDOMParser extends DOMParser {
		private XMLLocator locator;

		public void startDocument(XMLLocator xmlLocator, String s,
				NamespaceContext namespaceContext, Augmentations augmentations)
				throws XNIException {
			super.startDocument(xmlLocator, s, namespaceContext, augmentations);
			this.locator = xmlLocator;
			setLocation();
		}

		public void startElement(QName qName, XMLAttributes xmlAttributes,
				Augmentations augmentations) throws XNIException {
			super.startElement(qName, xmlAttributes, augmentations);
			setLocation();
		}

		private final void setLocation() {
			if (this.locator == null) {
				throw new RuntimeException("Error: locator is null. "
						+ "Check that you have the correct version of Xerces");
			}

			NodeImpl node = null;
			try {
				node = (NodeImpl) getProperty("http://apache.org/xml/properties/dom/current-element-node");
			} catch (org.xml.sax.SAXException ex) {
				System.err.println("exception " + ex);
			}
			if (node != null) {
				setUserData(node, SYSTEMID, locator.getLiteralSystemId());
				setUserData(node, LINENO, new Integer(locator.getLineNumber()));
				setUserData(node, COLUMNNO, new Integer(locator
						.getColumnNumber()));
			}
		}

		private static void setUserData(NodeImpl node, String name, Object value) {
			try {
				setUserDataMethod.invoke(node,
						new Object[] { name, value, null });
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private static Method setUserDataMethod = null;
		static {
			Class w3cUserDataHandlerClass = null;
			try {
				w3cUserDataHandlerClass = Class
						.forName("org.w3c.dom.UserDataHandler");
			} catch (ClassNotFoundException e) {
			}
			Class xerces262UserDataHandlerClass = null;
			try {
				xerces262UserDataHandlerClass = Class
						.forName("org.apache.xerces.dom3.UserDataHandler");
			} catch (ClassNotFoundException e) {
			}
			if (xerces262UserDataHandlerClass != null) {
				try {
					setUserDataMethod = NodeImpl.class.getMethod("setUserData",
							new Class[] { String.class, Object.class,
									xerces262UserDataHandlerClass });
				} catch (NoSuchMethodException e) {
				}
			}
			if (setUserDataMethod == null) {
				if (w3cUserDataHandlerClass != null) {
					try {
						setUserDataMethod = NodeImpl.class
								.getMethod("setUserData", new Class[] {
										String.class, Object.class,
										w3cUserDataHandlerClass });
					} catch (NoSuchMethodException e) {
					}
				}
			}
			if (setUserDataMethod == null) {
				System.err.println("Improper libraries for xercesImpl and DOM");
				RuntimeException e = new RuntimeException(
						"Improper libraries for xercesImpl and DOM");
				e.printStackTrace();
				throw e;
			}
		}
	}

	private XmlLoader() {
	}

	/**
	 * Get DOM Document object from specified InputStream. And close the
	 * InputStream when done.
	 * 
	 * @param inputStream
	 *            XML InputStream object.
	 * @return Document object.
	 * @throws SAXException
	 *             When SAX error occurs.
	 * @throws IOException
	 *             When IO error occurs.
	 */
	public static Document parseXMLDocument(InputStream inputStream)
			throws SAXException, IOException {
		return parseXMLDocument(new InputSource(inputStream));
	}

	/**
	 * Get DOM Document object of specified xml file.
	 * 
	 * @param source
	 *            xml file InputSource object.
	 * @return Document object.
	 */
	public static Document parseXMLDocument(InputSource source)
			throws SAXException, IOException {
		DOMParser domParser = new LocationTrackingDOMParser();
		domParser.setFeature(
				"http://apache.org/xml/features/dom/defer-node-expansion",
				false);
		domParser.setFeature(
				"http://apache.org/xml/features/dom/create-entity-ref-nodes",
				false);
		domParser.parse(source);
		return domParser.getDocument();
	}

	/**
	 * 读入一个文件并解析为DOM
	 * 
	 * @param file
	 *            文件名
	 * @param properties
	 *            如果非null，则使用该properties对输入文件进行替换
	 * @return 解析后的文档
	 */
	public static Document parseXMLDocument(String file, Properties properties)
			throws SAXException, IOException {
		InputStream input = new BufferedInputStream(new FileInputStream(file));
		try {
			return parseXMLDocument(input, properties, file);
		} finally {
			input.close();
		}
	}

	/**
	 * 读入一个URL指向的文件并解析为DOM
	 * 
	 * @param url
	 *            指向一个XML文件
	 * @param properties
	 *            如果非null，则使用该properties对输入文件进行替换.
	 * @return 解析后的文档.
	 * @throws SAXException
	 *             When SAX error occurs.
	 * @throws IOException
	 *             When IO error occurs.
	 */
	public static Document parseXMLDocument(URL url, Properties properties)
			throws SAXException, IOException {
		InputStream input = url.openStream();
		try {
			return parseXMLDocument(url.openStream(), properties, url
					.toExternalForm());
		} finally {
			input.close();
		}
	}

	/**
	 * 读入一个文件并解析为DOM
	 * 
	 * @param inputStream
	 *            XML InputStream object.
	 * @param properties
	 *            如果非null，则使用该properties对输入文件进行替换.
	 * @param systemId
	 *            文件名或者其它的标识.
	 * @return 解析后的文档.
	 * @throws SAXException
	 *             When SAX error occurs.
	 * @throws IOException
	 *             When IO error occurs.
	 */
	public static Document parseXMLDocument(InputStream inputStream,
			Properties properties, String systemId) throws SAXException,
			IOException {
		if (properties != null) {
			inputStream = new PropertyReplacingStream(inputStream, properties);
		}
		InputSource inputSource = new InputSource(inputStream);
		inputSource.setSystemId(systemId);
		return parseXMLDocument(inputSource);
	}

	/**
	 * 得到一个Element的“位置信息”，即文件名、行号、列号
	 * 
	 * @param element
	 *            Element object.
	 * @return Location string.
	 */
	public static String getElementLocation(Element element) {
		String systemId = getElementSystemId(element);
		return systemId == null ? LOCATION_UNKNOWN : systemId + ':'
				+ getElementLineNo(element) + ':' + getElementColumnNo(element);
	}

	/**
	 * 得到一个XML 元素的文件名(SystemId)
	 * 
	 * @param element
	 *            Element object.
	 * @return Element system id.
	 */
	public static String getElementSystemId(Element element) {
		return element instanceof NodeImpl ? (String) ((NodeImpl) element)
				.getUserData(SYSTEMID) : null;
	}

	/**
	 * 得到一个XML 元素所在的行号
	 * 
	 * @param element
	 *            Element object.
	 * @return Element line number.
	 */
	public static int getElementLineNo(Element element) {
		if (element instanceof NodeImpl) {
			Integer lineNo = (Integer) ((NodeImpl) element).getUserData(LINENO);
			return lineNo == null ? -1 : lineNo.intValue();
		}
		return -1;
	}

	/**
	 * 得到一个XML 元素所在的列号
	 * 
	 * @param element
	 *            Element object.
	 * @return Element column number.
	 */
	public static int getElementColumnNo(Element element) {
		if (element instanceof NodeImpl) {
			Integer columnNo = (Integer) ((NodeImpl) element)
					.getUserData(COLUMNNO);
			return columnNo == null ? -1 : columnNo.intValue();
		}
		return -1;
	}

	/**
	 * Get all child elements of specified Element.
	 * 
	 * @param element
	 *            Element object.
	 * @return Child elements array, return zero-length array if no child
	 *         elements.
	 */
	public static Element[] getChildElements(Element element) {
		final List result = new ArrayList();
		for (Node node = element.getFirstChild(); node != null; node = node
				.getNextSibling()) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				result.add(node);
			}
		}
		return (Element[]) result.toArray(new Element[0]);
	}

	/**
	 * Returns all Element children of an Element that belong to the given
	 * namespace.
	 * 
	 * @param element
	 *            Element object.
	 * @param namespace
	 *            Namespace.
	 * @return Elements with specified namespace.
	 */
	public static Element[] getChildElements(Element element, String namespace) {
		Element[] children = getChildElements(element);
		final List result = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			if (nameSpaceEquals(namespace, children[i].getNamespaceURI())) {
				result.add(children[i]);
			}
		}
		return (Element[]) result.toArray(new Element[0]);
	}

	/**
	 * Returns all Element children of an Element that belong to the given
	 * namespace and have the given local name.
	 */
	public static Element[] getChildElements(Element element, String namespace,
			String localName) {
		List elements = new ArrayList();
		for (Node node = element.getFirstChild(); node != null; node = node
				.getNextSibling()) {
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& nameSpaceEquals(namespace, node.getNamespaceURI())
					&& localName.equals(node.getLocalName())) {
				elements.add(node);
			}
		}
		Element[] result = new Element[elements.size()];
		elements.toArray(result);
		return result;
	}

	/**
	 * Returns the first child element with the given namespace and localName,
	 * or null if there is no such element.
	 */
	public static Element getChildElement(Element element, String namespace,
			String localName) {
		Element node = null;
		try {
			node = getChildElement(element, namespace, localName, false);
		} catch (Exception e) {
			node = null;
		}
		return node;
	}

	/**
	 * Returns the first child element with the given namespace and localName.
	 * return Null if there is no such element and required flag is unset or
	 * throws an RuntimeException if the "required" flag is set.
	 */
	public static Element getChildElement(Element element, String namespace,
			String localName, boolean required) {
		for (Node node = element.getFirstChild(); node != null; node = node
				.getNextSibling()) {
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& nameSpaceEquals(namespace, node.getNamespaceURI())
					&& localName.equals(node.getLocalName())) {
				return (Element) node;
			}
		}
		if (required) {
			throw new RuntimeException("Missing element '" + localName
					+ "' as child of element '" + element.getTagName()
					+ "' at " + getElementLocation(element));
		} else {
			return null;
		}
	}

	/**
	 * Returns the value of an element's attribute, but throws an
	 * RuntimeException if the element has no such attribute.
	 */
	public static String getAttribute(Element element, String attributeName) {
		String attrValue = element.getAttribute(attributeName);
		if (attrValue.length() == 0) {
			throw new RuntimeException("Missing attribute '" + attributeName
					+ "' on element '" + element.getTagName() + "' at "
					+ getElementLocation(element));
		}
		return attrValue;
	}

	/**
	 * Returns the value of an element's attribute, or a default value if the
	 * element has no such attribute.
	 */
	public static String getAttribute(Element element, String attributeName,
			String defaultValue) {
		if (!element.hasAttribute(attributeName)) {
			return defaultValue;
		}
		String attrValue = element.getAttribute(attributeName);
		return attrValue;
	}

	public static int getAttributeAsInteger(Element element,
			String attributeName) {
		String attrValue = getAttribute(element, attributeName);
		try {
			return Integer.decode(attrValue).intValue();
		} catch (NumberFormatException e) {
			throw new RuntimeException("Cannot parse the value '" + attrValue
					+ "' as an integer in the attribute '" + attributeName
					+ "' on the element '" + element.getTagName() + "' at "
					+ getElementLocation(element));
		}
	}

	public static int getAttributeAsInteger(Element element,
			String attributeName, int defaultValue) {
		String attrValue = getAttribute(element, attributeName, null);
		if (attrValue == null) {
			return defaultValue;
		} else {
			try {
				return Integer.decode(attrValue).intValue();
			} catch (NumberFormatException e) {
				throw new RuntimeException("Cannot parse the value '"
						+ attrValue + "' as an integer in the attribute '"
						+ attributeName + "' on the element '"
						+ element.getTagName() + "' at "
						+ getElementLocation(element));
			}
		}
	}

	public static long getAttributeAsLong(Element element, String attributeName) {
		String attrValue = getAttribute(element, attributeName);
		try {
			return Long.decode(attrValue).longValue();
		} catch (NumberFormatException e) {
			throw new RuntimeException("Cannot parse the value '" + attrValue
					+ "' as a long in the attribute '" + attributeName
					+ "' on the element '" + element.getTagName() + "' at "
					+ getElementLocation(element));
		}
	}

	public static long getAttributeAsLong(Element element,
			String attributeName, long defaultValue) {
		String attrValue = getAttribute(element, attributeName, null);
		if (attrValue == null) {
			return defaultValue;
		} else {
			try {
				return Long.decode(attrValue).longValue();
			} catch (NumberFormatException e) {
				throw new RuntimeException("Cannot parse the value '"
						+ attrValue + "' as a long in the attribute '"
						+ attributeName + "' on the element '"
						+ element.getTagName() + "' at "
						+ getElementLocation(element));
			}
		}
	}

	public static double getAttributeAsDouble(Element element,
			String attributeName) {
		String attrValue = getAttribute(element, attributeName);
		try {
			return Double.parseDouble(attrValue);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Cannot parse the value '" + attrValue
					+ "' as a double in the attribute '" + attributeName
					+ "' on the element '" + element.getTagName() + "' at "
					+ getElementLocation(element));
		}
	}

	public static double getAttributeAsDouble(Element element,
			String attributeName, double defaultValue) {
		String attrValue = getAttribute(element, attributeName, null);
		if (attrValue == null) {
			return defaultValue;
		} else {
			try {
				return Double.parseDouble(attrValue);
			} catch (NumberFormatException e) {
				throw new RuntimeException("Cannot parse the value '"
						+ attrValue + "' as a double in the attribute '"
						+ attributeName + "' on the element '"
						+ element.getTagName() + "' at "
						+ getElementLocation(element));
			}
		}
	}

	public static boolean getAttributeAsBoolean(Element element,
			String attributeName, boolean defaultValue) {
		String attrValue = getAttribute(element, attributeName, null);
		if (attrValue == null) {
			return defaultValue;
		}
		return Boolean.valueOf(attrValue).booleanValue();
	}

	public static boolean getAttributeAsBoolean(Element element,
			String attributeName) {
		String attrValue = getAttribute(element, attributeName);
		return booleanValue(attrValue);
	}

	public static Class getAttributeAsClass(Element element,
			String attributeName) {
		String attrValue = getAttribute(element, attributeName);
		try {
			return Class.forName(attrValue);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find the class '" + attrValue
					+ "' in the attribute '" + attributeName
					+ "' on the element '" + element.getTagName() + "' at "
					+ getElementLocation(element));
		}
	}

	public static Class getAttributeAsClass(Element element,
			String attributeName, Class defaultClass) {
		String attrValue = getAttribute(element, attributeName, null);
		if (attrValue == null) {
			return defaultClass;
		}
		try {
			return Class.forName(attrValue);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find the class '" + attrValue
					+ "' in the attribute '" + attributeName
					+ "' on the element '" + element.getTagName() + "' at "
					+ getElementLocation(element));
		}
	}

	public static String getElementText(Element element) {
		StringBuffer value = new StringBuffer();
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Text || node instanceof CDATASection) {
				value.append(node.getNodeValue());
			}
		}
		return value.toString();
	}

	public static int getElementTextAsInteger(Element element) {
		String value = getElementText(element);
		try {
			return Integer.decode(value).intValue();
		} catch (NumberFormatException e) {
			throw new RuntimeException("Cannot parse the value '" + value
					+ "' as an integer in the text of element '"
					+ element.getTagName() + "' at "
					+ getElementLocation(element));
		}
	}

	public static long getElementTextAsLong(Element element) {
		String value = getElementText(element);
		try {
			return Long.decode(value).longValue();
		} catch (NumberFormatException e) {
			throw new RuntimeException("Cannot parse the value '" + value
					+ "' as a long in the text of element '"
					+ element.getTagName() + "' at "
					+ getElementLocation(element));
		}
	}

	public static double getElementTextAsDouble(Element element) {
		String value = getElementText(element);
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Cannot parse the value '" + value
					+ "' as a double in the text of element '"
					+ element.getTagName() + "' at "
					+ getElementLocation(element));
		}
	}

	public static Class getElementTextAsClass(Element element) {
		String attrValue = getElementText(element);
		try {
			return Class.forName(attrValue);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find the class '" + attrValue
					+ "' in the text of element '" + element.getTagName()
					+ "' at " + getElementLocation(element));
		}
	}

	/**
	 * 得到element的text内容，以boolean返回
	 * 
	 * @param element
	 *            Element object.
	 * @return Boolean value.
	 */
	public static boolean getElementTextAsBoolean(Element element) {
		String value = getElementText(element);
		return booleanValue(value);
	}

	// private :
	private static boolean nameSpaceEquals(String namespace1, String namespace2) {
		return namespace1 == null ? namespace2 == null : namespace1
				.equals(namespace2);
	}

	private static boolean booleanValue(String attrValue) {
		// 没有用Boolean.valueOf，它只支持true|false
		return attrValue != null
				&& (attrValue.equalsIgnoreCase("true")
						|| attrValue.equalsIgnoreCase("yes") || attrValue
						.equalsIgnoreCase("1"));
	}

}
