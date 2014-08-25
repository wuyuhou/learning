/*
 * Copyright 2013 Primeton.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tools.utility.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.tools.utility.impl.xml.XPathAPI;

/**
 * xml工具(基于W3C标准)
 *
 * @author wuyuhou
 *
 */
public final class XmlUtil {

	private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	static {
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setNamespaceAware(true);
	}

	private static final String XPATH_NODE_DELIM = "/";

	private static final String XPATH_ATTR_DELIM = "@";

	private static final String XPATH_ATTR_LINCL = "[";

	private static final String XPATH_ATTR_RINCL = "]";

	private static final String XPATH_ATTR_QUOTA = "\"";

	private static final String XPATH_ATTR_SQUOTA = "'";

	private static final String XPATH_ATTR_EQ = "=";

	/**
	 * 创建一个XML Document实例
	 *
	 * @return
	 */
	public static Document newDocument() {
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		Document doc = db.newDocument();
		return doc;
	}

	/**
	 * 从InputStream解析一个Document Instance
	 *
	 * @param in 输入流
	 * @param encoding 编码，可以为空
	 * @param systemId schema位置，可以为空
	 * @return document对象
	 */
	public static Document parse(InputStream in, String encoding, String systemId) {
		if (in == null) {
			throw new IllegalArgumentException("InputStream is null!");
		}
		if (null == encoding) {
			// 默认encoding为UTF-8
			encoding = "UTF-8";
		}
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStreamReader isr = new InputStreamReader(in, encoding);
			InputSource isrc = new InputSource(isr);
			return db.parse(isrc);
		} catch (Exception e) {
			throw new IllegalArgumentException("cannot parse inputStream:" + in, e);
		}
	}

	/**
	 * 从InputStream解析一个Document Instance
	 *
	 * @param xmlFile xml文件
	 * @param encoding 编码，可以为空
	 * @param systemId schema位置，可以为空
	 * @return
	 */
	public static Document parse(File xmlFile, String encoding, String systemId) {
		if (xmlFile == null) {
			throw new IllegalArgumentException("xmlFile is null!");
		}
		if (!xmlFile.exists()) {
			throw new IllegalArgumentException("xmlFile'" + xmlFile.getAbsolutePath() + "' is not existed!");
		}
		if (xmlFile.isDirectory()) {
			throw new IllegalArgumentException("xmlFile'" + xmlFile.getAbsolutePath() + "' is dir, not file!");
		}
		FileInputStream in = null;
		try {
			in = new FileInputStream(xmlFile);
			return parse(in, encoding, systemId);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("cannot parse xml:" + xmlFile.getAbsolutePath(), e);
		} finally {
			IOUtil.closeQuietly(in);
		}
	}

	/**
	 * 从xmlString解析一个Document Instance
	 *
	 * @param xmlString xml字符串
	 * @param encoding 编码，可以为空
	 * @param systemId schema位置，可以为空
	 * @return
	 */
	public static Document parse(String xmlString, String encoding, String systemId) {
		if (xmlString == null || xmlString.trim().length() == 0) {
			throw new IllegalArgumentException("xmlString is null!");
		}
		if (null == encoding) {
			// 默认encoding为UTF-8
			encoding = "UTF-8";
		}
		byte[] bytes = null;
		try {
			bytes = xmlString.getBytes(encoding);
		} catch (Exception e) {
			bytes = xmlString.getBytes();
		}
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		return parse(in, encoding, systemId);
	}

	/**
	 * 保存一个Node节点到OutputStream
	 *
	 * @param node node节点
	 * @param out 输出流
	 * @param isFormat 是否格式化（容易阅读）
	 * @param encoding 编码，可以为空
	 * @throws IOException
	 */
	public static void save(Node node, OutputStream out, boolean isFormat, String encoding) {
		if (node == null) {
			throw new IllegalArgumentException("Node is null!");
		}
		if (out == null) {
			throw new IllegalArgumentException("OutputStream is null!");
		}
		try {
			String content = node2String(node, isFormat, encoding);
			out.write(content.getBytes(encoding));
			out.flush();
		} catch (Exception e) {
			throw new IllegalArgumentException("cannot save:" + node, e);
		}
	}

	/**
	 * 保存一个Node节点到OutputStream
	 *
	 * @param node node节点
	 * @param xmlFile xml文件
	 * @param isFormat 是否格式化（容易阅读）
	 * @param encoding 编码，可以为空
	 * @throws IOException
	 */
	public static void save(Node node, File xmlFile, boolean isFormat, String encoding) {
		if (xmlFile == null) {
			throw new IllegalArgumentException("xmlFile is null!");
		}
		if (!xmlFile.exists()) {
			throw new IllegalArgumentException("xmlFile'" + xmlFile.getAbsolutePath() + "' is not existed!");
		}
		if (xmlFile.isDirectory()) {
			throw new IllegalArgumentException("xmlFile'" + xmlFile.getAbsolutePath() + "' is dir, not file!");
		}

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(xmlFile);
			save(node, out, isFormat, encoding);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("cannot parse xml:" + xmlFile.getAbsolutePath(), e);
		} finally {
			IOUtil.closeQuietly(out);
		}
	}

	/**
	 * 把一个Node转化为Xml字符串
	 *
	 * @param node node节点
	 * @param isFormat 是否格式化（容易阅读）
	 * @param encoding 编码，可以为空
	 * @return
	 */
	public static String node2String(Node node, boolean isFormat, String encoding) {
		if (node == null) {
			throw new IllegalArgumentException("Node is null!");
		}
		boolean hasHead = true;
		if (encoding == null || encoding.trim().length() == 0) {
			encoding = "UTF-8";
			hasHead = false;
		}

		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			if (isFormat) {
				tf.setAttribute("indent-number", new Integer(4));
			}
			Transformer transformer = tf.newTransformer();
			if (isFormat) {
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			}
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			transformer.transform(new DOMSource(node), new StreamResult(new OutputStreamWriter(byteOut, encoding)));

			String str = new String(byteOut.toByteArray(), encoding);
			if (hasHead) {
				if (!str.startsWith("<?xml")) {
					str = ("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n").concat(str);
				}
				return str;
			} else {
				if (!str.startsWith("<?xml")) {
					return str;
				}
				int op = str.indexOf("?>");
				str = str.substring(op + "?>".length()).trim();
				if (str.startsWith("\n")) {
					str = str.substring(1);
				}

				return str;
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot convert and format!", e);
		}
	}

	/**
	 * 通过Xpath查找一个节点<br><pre>
	 *
	 * <root><data><info id='123'><message>XXX<message/></info></data></root>
	 * 如果当前节点是root，要查找message节点，则它的xpath是data/info/message
	 *
	 * </pre>
	 *
	 * @param node node节点
	 * @param xpath xpath字符串
	 * @return
	 */
	public static Node findNode(Node node, String xpath) {
		if (node == null) {
			throw new IllegalArgumentException("Node is null!");
		}
		if (xpath == null || xpath.trim().length() == 0) {
			throw new IllegalArgumentException("Xpath is null!");
		}
		return XPathAPI.selectSingleNode(node, xpath);
	}

	/**
	 * 通过Xpath查找所有节点<br><pre>
	 *
	 * <root><data><info id='123'><message>XXX<message/></info></data></root>
	 * 如果当前节点是root，要查找message节点，则它的xpath是data/info/message
	 *
	 * </pre>
	 *
	 * @param node node节点
	 * @param xpath xpath字符串
	 * @return
	 */
	public static NodeList findNodes(Node node, String xpath) {
		if (node == null) {
			throw new IllegalArgumentException("Node is null!");
		}
		if (xpath == null || xpath.trim().length() == 0) {
			throw new IllegalArgumentException("Xpath is null!");
		}
		return XPathAPI.selectNodeList(node, xpath);
	}

	/**
	 * 取得节点值<br><pre>
	 *
	 * <root><data><info id='123'><message>XXX<message/></info></data></root>
	 * 如果当前节点是root，要得到message节点值，则它的xpath是data/info/message
	 *
	 * </pre>
	 *
	 * @param node node节点
	 * @param xpath xpath字符串
	 * @return
	 */
	public static String getNodeValue(Node node, String xpath) {
		if (node == null) {
			throw new IllegalArgumentException("Node is null!");
		}
		if (xpath == null || xpath.trim().length() == 0) {
			return getNodeValue(node);
		}
		Node childNode = findNode(node, xpath);
		if (childNode == null) {
			return null;
		} else {
			return getNodeValue(childNode);
		}
	}

	private static String getNodeValue(Node node) {
		String value = null;
		if (node == null) {
			return null;
		} else {
			switch (node.getNodeType()) {
			case (Node.ELEMENT_NODE):
				StringBuffer contents = new StringBuffer();
				NodeList childNodes = node.getChildNodes();
				int length = childNodes.getLength();
				if (length == 0) {
					return null;
				}
				for (int i = 0; i < length; i++) {
					if (childNodes.item(i).getNodeType() == Node.TEXT_NODE) {
						contents.append(childNodes.item(i).getNodeValue());
					}
					if (childNodes.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
						contents.append(childNodes.item(i).getNodeValue());
					}
				}
				value = contents.toString();
				break;
			case (Node.TEXT_NODE):
			case (Node.CDATA_SECTION_NODE):
				value = node.getNodeValue();
				break;
			case (Node.ATTRIBUTE_NODE):
				value = node.getNodeValue();
				break;
			}
		}
		if (value == null) {
			return null;
		}
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			result.append(c);
		}
		return result.toString().trim();
	}

	/**
	 * 创建一个子结点
	 *
	 * @param parentNode 父节点
	 * @param childName 子结点名字
	 * @param childValue 字结点值（可以为空）
	 * @return 子结点
	 */
	public static Element createChild(Node parentNode, String childName, String childValue) {
		if (parentNode == null) {
			throw new IllegalArgumentException("parentNode is null!");
		}
		if (childName == null || childName.trim().length() == 0) {
			throw new IllegalArgumentException("ChildName is null!");
		}
		if (parentNode.getNodeType() != Node.DOCUMENT_NODE && parentNode.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException("Wrong NodeType:" + parentNode.getNodeType());
		}
		Document doc = null;
		if (parentNode.getNodeType() == Node.DOCUMENT_NODE) {
			doc = (Document)parentNode;
		} else {
			doc = parentNode.getOwnerDocument();
		}
		Element child = doc.createElement(childName);
		setNodeValue(child, childValue);
		parentNode.appendChild(child);
		return child;
	}

	/**
	 * 添加结点
	 *
	 * @param node node节点
	 * @param xpath xpath字符串
	 * @return 添加的结点
	 */
	public static Node appendNode(Node node, String xpath) {
		if (node == null) {
			throw new IllegalArgumentException("Node is null!");
		}
		if (xpath == null || xpath.trim().length() == 0) {
			throw new IllegalArgumentException("xpath is null!");
		}
		if (xpath.startsWith(XPATH_NODE_DELIM)) {
			xpath = xpath.substring(1);
		}
		String[] saXql = xpath.split(XPATH_NODE_DELIM);
		if (saXql.length < 1) {
			throw new IllegalArgumentException("invalid xpath:" + xpath);
		}

		Document doc = null;
		if (node.getNodeType() == Node.DOCUMENT_NODE) {
			doc = (Document) node;
		} else {
			doc = node.getOwnerDocument();
		}

		Node root = node;
		if (node.getNodeType() == Node.DOCUMENT_NODE) {
			root = doc.getDocumentElement();
		}

		Node item = root;
		for (int i = 0; i < saXql.length; i++) {
			String itemXql = subXql(saXql, i);
			item = findNode(root, itemXql);

			if ((item == null) || (i == saXql.length - 1)) {
				Node parentNode = (i == 0 ? null : findNode(root, subXql(saXql, i - 1)));
				if (saXql[i].startsWith(XPATH_ATTR_DELIM)) { // 如果是/root/data/list/@name这样的xpath，创建name属性
					String attrName = saXql[i].substring(1, saXql[i].length());
					if (parentNode == null) {
						((Element) root).setAttribute(attrName, "");
						item = ((Element) root).getAttributeNode(attrName);
					} else {
						((Element) parentNode).setAttribute(attrName, "");
						item = ((Element) parentNode).getAttributeNode(attrName);
					}
				} else {
					item = createNode(doc, saXql[i]);
					if (parentNode == null) {
						root.appendChild(item);
					} else {
						parentNode.appendChild(item);
					}
				}
			}
		}
		return item;
	}

	private static String subXql(String[] saXql, int index) {
		String subXql = "";
		for (int i = 0; i <= index; i++) {
			if (i >= saXql.length) {
				break;
			}
			if (subXql != "") {
				subXql = subXql.concat(XPATH_NODE_DELIM).concat(saXql[i]);
			} else {
				subXql = subXql.concat(saXql[i]);
			}
		}
		return subXql;
	}

	private static Element createNode(Document doc, String path) {
		StringTokenizer stk = new StringTokenizer(path,
				XPATH_ATTR_LINCL.concat(XPATH_ATTR_RINCL).concat(XPATH_ATTR_DELIM).concat(XPATH_ATTR_QUOTA).concat(
				XPATH_ATTR_SQUOTA));

		String eleName = null;
		String eleAttrName = null;
		String eleAttrVal = null;

		if (stk.hasMoreTokens()) {
			eleName = stk.nextToken();
		}

		if (eleName == null) {
			return null;
		}
		Element retElement = doc.createElement(eleName);

		while (stk.hasMoreTokens()) {

			eleAttrName = stk.nextToken();
			int index = eleAttrName.lastIndexOf(XPATH_ATTR_EQ);
			if (index >= 0) {
				eleAttrName = eleAttrName.substring(0, index);
			}
			if (stk.hasMoreTokens()) {
				eleAttrVal = stk.nextToken();
			}
			if (eleAttrName != null) {
				retElement.setAttribute(eleAttrName, eleAttrVal);
			}
		}

		return retElement;
	}

	private static void setNodeValue(Node node, String value) {
		if (value == null || value.trim().length() == 0) {
			return;
		}
		if (node == null) {
			return;
		} else {
			Node childNode = null;
			switch (node.getNodeType()) {
			case (Node.ELEMENT_NODE):
				childNode = node.getFirstChild();
				if (childNode == null) {
					childNode = node.getOwnerDocument().createTextNode(value);
					node.appendChild(childNode);
				} else if (childNode.getNodeType() == Node.TEXT_NODE) {
					childNode.setNodeValue(value);
				} else {
					node.appendChild(node.getOwnerDocument().createTextNode(value));
				}
				return;
			case (Node.TEXT_NODE):
				node.setNodeValue(value);
				return;
			case (Node.ATTRIBUTE_NODE):
				node.setNodeValue(value);
				return;
			}
		}
	}

	/**
	 * 修改某一个节点的值<br><pre>
	 *
	 * <root><data><info id='123'><message>XXX<message/></info></data></root>
	 * 如果当前节点是root，要修改message节点值，则它的xpath是data/info/message
	 *
	 * </pre>
	 * @param node node节点
	 * @param xpath xpath字符串
	 * @param value 值
	 * @return
	 */
	public static Node setNodeValue(Node node, String xpath, String value) {
		if (node == null) {
			throw new IllegalArgumentException("Node is null!");
		}
		if (node.getNodeType() == Node.DOCUMENT_NODE) {
			node = ((Document) node).getDocumentElement();
		}
		if (node == null) {
			throw new IllegalArgumentException("Root element is null!");
		}
		if (xpath == null || xpath.trim().length() == 0) {
			setNodeValue(node, value);
			return node;
		}
		Node targetNode = findNode(node, xpath);
		if (targetNode == null) {
			targetNode = appendNode(node, xpath);
		}
		setNodeValue(targetNode, value);
		return targetNode;
	}

	/**
	 * 删除所有的子结点
	 *
	 * @param node
	 */
	public static void removeAllChild(Node node) {
		if (node == null) {
			return;
		}
		Node child = node.getFirstChild();
		while (child != null) {
			node.removeChild(child);
			child = node.getFirstChild();
		}
	}
}