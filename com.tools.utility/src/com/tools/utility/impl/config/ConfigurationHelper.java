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
package com.tools.utility.impl.config;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.tools.utility.api.SystemProperties;

/**
 * 配置助手类
 *
 * @author wuyuhou 
 */

public class ConfigurationHelper {
	
	protected static String DELIM_START = "${";
	protected static char   DELIM_STOP  = '}';
	protected static int DELIM_START_LEN = 2;
	protected static int DELIM_STOP_LEN = 1;

	/**
	 * 取得含有变量的属性信息，使用${var_name}形式，允许递归<p>
	 * 
	 * Perform variable substitution in string <code>val</code> from the values
	 * of keys found in the system propeties.
	 * 
	 * <p>
	 * The variable substitution delimeters are <b>${</b> and <b>}</b>.
	 * 
	 * <p>
	 * For example, if the System properties contains "key=value", then the call
	 * 
	 * <pre>
	 * String s = getValueContainVars(&quot;Value of key is ${key}.&quot;);
	 * </pre>
	 * 
	 * will set the variable <code>s</code> to "Value of key is value.".
	 * 
	 * <p>
	 * If no value could be found for the specified key, then the
	 * <code>props</code> parameter is searched, if the value could not be found
	 * there, then substitution defaults to the empty string.
	 * 
	 * <p>
	 * For example, if system propeties contains no value for the key
	 * "inexistentKey", then the call
	 * 
	 * <pre>
	 * String s = getValueContainVars(&quot;Value of inexistentKey is [${inexistentKey}]&quot;);
	 * </pre>
	 * 
	 * will set <code>s</code> to "Value of inexistentKey is []"
	 * 
	 * <p>
	 * An {@link java.lang.IllegalArgumentException} is thrown if
	 * <code>val</code> contains a start delimeter "${" which is not balanced by
	 * a stop delimeter "}".
	 * </p>
	 * 
	 * <p>
	 * <b>Author</b> Avy Sharell</a>
	 * </p>
	 * 
	 * @param val
	 *            The string on which variable substitution is performed.
	 * @throws IllegalArgumentException
	 *             if <code>val</code> is malformed.
	 */
	public static String getValueContainVars(String val, Properties props) throws IllegalArgumentException {

		StringBuffer sbuf = new StringBuffer();

		int i = 0;
		int j, k;

		while (true) {
			j = val.indexOf(DELIM_START, i);
			if (j == -1) {
				// no more variables
				if (i == 0) { // this is a simple string
					return val;
				} else { // add the tail string which contails no variables and
							// return the result.
					sbuf.append(val.substring(i, val.length()));
					return sbuf.toString();
				}
			} else {
				sbuf.append(val.substring(i, j));
				k = val.indexOf(DELIM_STOP, j);
				if (k == -1) {
					throw new IllegalArgumentException(
							'"'		+ val
									+ "\" has no closing brace. Opening brace at position "
									+ j + '.');
				} else {
					j += DELIM_START_LEN;
					String key = val.substring(j, k);
					String replacement = null;
					if (props != null) {
						replacement = props.getProperty(key);
					}					
					if (replacement == null) {
						replacement = SystemProperties.getProperty(key);
					}

					if (replacement != null) {
						// Do variable substitution on the replacement string
						// such that we can solve "Hello ${x2}" as "Hello p1"
						// the where the properties are
						// x1=p1
						// x2=${x1}
						String recursiveReplacement = getValueContainVars(replacement, props);
						sbuf.append(recursiveReplacement);
					}
					i = k + DELIM_STOP_LEN;
				}
			}
		}
	}
	
	public static boolean isSimpleNode(Node node) {
		NodeList childList = node.getChildNodes();
		for (int i = 0; i < childList.getLength(); i++) {
			short type = childList.item(i).getNodeType();
			if ((type != Node.COMMENT_NODE) && (type != Node.TEXT_NODE)) {
				return false;
			}
		}
		return true;
	}
	
	//删除一个孩子节点
	public static void removeChild(Element parent, Element child) {
		if (child == null || parent == null) {
			return;
		}
		Document doc = parent.getOwnerDocument();
		
		if (doc == child.getOwnerDocument()) {
			parent.removeChild(child);
		} else {
			parent.removeChild(doc.importNode(child, true));
		}
	}
	
	//增加一个孩子节点
	public static Element appendChild(Element parent, String childNodeName, String childNodeAttriName, String childNodeAttriValue) {
		Element child = parent.getOwnerDocument().createElement(childNodeName);
		parent.appendChild(child);
		child.setAttribute(childNodeAttriName, childNodeAttriValue);		
		return child;
	}

	public static Element removeChild(Element parent, String childNodeName, String childNodeAttriName, String childNodeAttriValue) {
		NodeList childList = parent.getElementsByTagName(childNodeName);
		for (int i = 0; i < childList.getLength(); i++) {
			Element child = (Element)childList.item(i);
			if (equal(childNodeAttriValue, child.getAttribute(childNodeAttriName))) {
				parent.removeChild(child);
				return child;
			}
		}
		return null;
	}
	
	//取得孩子节点
	public static Element getChild(Element parent, String childNodeName, String childNodeAttriName, String childNodeAttriValue) {
		NodeList childList = parent.getElementsByTagName(childNodeName);
		for (int i = 0; i < childList.getLength(); i++) {
			Element child = (Element) childList.item(i);
			if (equal(childNodeAttriValue, child.getAttribute(childNodeAttriName))) {
				return child;
			}
		}
		return null;
	}
	
	public static boolean equal(String s1, String s2) {
		if (s1 == s2) {
			return true;
		}

		if (s1 == null) {
			s1 = "";
		}

		if (s2 == null) {
			s2 = "";
		}

		s1 = s1.trim();
		s2 = s2.trim();

		if (s1.equals(s2)) {
			return true;
		}
		return false;
	}

}