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
package com.tools.utility.api.config;

import java.io.Serializable;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.tools.utility.api.XmlUtil;
import com.tools.utility.impl.config.ConfigurationHelper;

/**
 * Value对应三段式配置的&lt;configValue key="xxx"&gt;yyy&lt;/configValue&gt;
 *
 * @author wuyuhou
 *
 */
public class Value implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 4595254574057653627L;

	/** 名称 */
	private String name;

	/** 健值 */
	private String value;
	
	//描述
	private String description;
	
	Properties _prop;
	
	/**
	 * 构造方法
	 */
	public Value() {
		this("");
	}

	/**
	 * 构造方法
	 *
	 * @param valueName 名称
	 */
	public Value(String valueName) {
		this(valueName, "");
	}

	/**
	 * 构造方法
	 *
	 * @param keyName  名称
	 * @param keyValue 键值
	 */
	public Value(String keyName, String keyValue) {
		name = keyName;
		value = keyValue;
		_prop = new Properties();
	}

	/**
	 * 构造方法
	 *
	 * @param element Value元素
	 */
	public Value(Element valueElement) {
		this(valueElement, null);
	}
	
	/**
	 * 构造方法
	 *
	 * @param element Value元素
	 * @param prop 属性信息
	 */
	public Value(Element valueElement, Properties prop) {
		if (prop == null) {
			_prop = new Properties();
		} else {
			_prop = prop;
		}
		this.parse(valueElement);
	}
	
	/**
	 * 深度拷贝
	 */
	public Value clone() {
		Value result = null;
		try {
			result = (Value)super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return result;
	}

	/**
	 * 解析Value元素
	 */
	private void parse(Element valueElement) {
		this.name = valueElement.getAttribute(Configuration.VALUE_KEY);
		this.description = valueElement.getAttribute(Configuration.DESCRIPTION);
		if (ConfigurationHelper.isSimpleNode(valueElement)) {
			this.value = XmlUtil.getNodeValue(valueElement, null);
		} else {
			String complexValue = XmlUtil.node2String(valueElement, false, null);	
			this.value = complexValue.substring(complexValue.indexOf('>') + 1, complexValue.lastIndexOf("</"));
		}
		if (this.value == null) {
			this.value = "";
		}
	}

	/**
	 * 取得名称
	 *
	 * @return 名称
	 */
	public String getName() {
		return name;
	}

	public void setName(String valueName) {
		name = valueName;
	}

	/**
	 * 取得键值
	 *
	 * @return 键值
	 */
	public String getValue() {
		try {
			return ConfigurationHelper.getValueContainVars(value, _prop);
		} catch (Throwable t) {
			return value;
		}			
	}

	/**
	 * 更新键值
	 *
	 * @param value 键值
	 */
	public void setValue(String value) {
		if (value == null) {
			value = "";
		}
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 把Configuration对象转换为Element对象
	 *
	 * @return Element对象
	 */
	public Element toElement() {
		Document document = XmlUtil.newDocument();
		Element valueElem = document.createElement(Configuration.VALUE);
		valueElem.setAttribute(Configuration.VALUE_KEY, name);
		valueElem.setAttribute(Configuration.DESCRIPTION, description);
		valueElem.setNodeValue(value);
		return valueElem;
	}

	/**
	 * Document格式的字符串化
	 *
	 * @return Document格式的信息
	 */
	public String toString() {
		return XmlUtil.node2String(toElement(), true, null);
	}
	
	/**
	 * 相等比较
	 * 
	 * @return true:相等
	 */
	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof Value)) {
			return false;
		}
		Value t = (Value)o;
		if (!ConfigurationHelper.equal(name, t.name)) {
			return false;
		}
		if (!ConfigurationHelper.equal(description, t.description)) {
			return false;
		}
		if (!ConfigurationHelper.equal(value, t.value)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 计算hashCode
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + (name == null ? 0 : name.hashCode());
		result = 37 * result + (description == null ? 0 : description.hashCode());
		result = 37 * result + (value == null ? 0 : value.hashCode());
		return result;
	}
}