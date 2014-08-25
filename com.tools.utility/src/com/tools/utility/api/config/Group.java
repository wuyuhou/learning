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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.tools.utility.api.XmlUtil;
import com.tools.utility.impl.config.ConfigurationHelper;

/**
 * Group类: 对应三段式配置的&lt;group name="productInfo"/&gt;
 *
 * @author wuyuhou
 *
 */
public class Group implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -7503847652819439395L;

	/** Group名称 */
	private String name;
	
	//描述
	private String description;
	
	Properties _prop;

	/** Value哈希表 */
	private Map<String, Value> values = new LinkedHashMap<String, Value>();

	/**
	 * 构造方法
	 *
	 */
	public Group() {
		this("");
	}

	/**
	 * 构造方法
	 *
	 * @param groupName Group名称
	 */
	public Group(String groupName) {
		name = groupName;
		_prop = new Properties();
	}

	/**
	 * 构造方法
	 *
	 * @param element Group元素
	 * 
	 * @throws ConfigurationRuntimeException Document解析出错
	 */
	public Group(Element groupElement) throws ConfigurationRuntimeException {
		this(groupElement, null);
	}
	
	/**
	 * 构造方法
	 *
	 * @param element Group元素
	 * @param prop 属性信息
	 * 
	 * @throws ConfigurationRuntimeException Document解析出错
	 */
	public Group(Element groupElement, Properties prop) throws ConfigurationRuntimeException {
		if (prop == null) {
			_prop = new Properties();
		} else {
			_prop = prop;
		}
		this.parse(groupElement);
	}

	/**
	 * 解析Group元素操作
	 *
	 * @throws ConfigurationRuntimeException Document解析出错
	 */
	private void parse(Element groupElement) throws ConfigurationRuntimeException {
		this.name = groupElement.getAttribute(Configuration.GROUP_NAME);
		this.description = groupElement.getAttribute(Configuration.DESCRIPTION);
		try {
			NodeList nodeList = XmlUtil.findNodes(groupElement,
					Configuration.VALUE);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element valueEle = (Element) nodeList.item(i);
				Value value = new Value(valueEle, _prop);

				this.values.put(value.getName(), value);
			}
		} catch (Exception e) {
			throw new ConfigurationRuntimeException("the element parse error!", e);
		}
	}

	/**
	 * 取得Group名称
	 *
	 * @return Group名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设定Group名称
	 *
	 * @param groupName Group名称
	 */
	public void setName(String groupName) {
		name = groupName;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 自融合
	 *
	 * @param group Group对象
	 *
	 * @return 融合后的Group对象
	 */
	public Group mergeGroup(Group group) {
		for (Value value : group.values.values()) {
			mergeValue(value);
		}
		return this;
	}

	/**
	 * 向Value哈希表添加Value
	 *
	 * @param key   主健名称
	 * @param value 主健值
	 *
	 * @return 添加后的Value对象
	 *
	 */
	public Value addValue(String key, String value) {

		return addValue(new Value(key, value));
	}

	/**
	 * 向Value哈希表添加Value
	 *
	 * @param value Value对象
	 *
	 * @return 添加后的Value对象
	 *
	 */
	public Value addValue(Value value) {
		Value valueInstance = this.getValue(value.getName());
		if (valueInstance != null) {
			return null;
		} else {
			this.values.put(value.getName(), value);
			value._prop = _prop;
			return value;
		}
	}

	/**
	 * 为指定 configValue 设置值, 如果不存在, 将创建节点
	 *
	 * @param key   主健名称
	 * @param value 主健值
	 *
	 * @return 设定前的Value对象
	 */
	public Value setValue(String key, String value) {

		return setValue(new Value(key, value));
	}
	
	/**
	 * 融合Value
	 *
	 * @param value Value对象
	 *
	 * @return 融合后的Value对象
	 */
	public Value mergeValue(Value value) {
		value._prop = _prop;
		this.values.put(value.getName(), value);
		return getValue(value.getName());
	}

	/**
	 * 设定Value
	 *
	 * @param value Value对象
	 *
	 * @return 设定前的Value对象
	 */
	public Value setValue(Value value) {
		if (value == null) {
			return null;
		}
		Value bef = this.values.get(value.getName());
		deleteValue(value.getName());
		addValue(value);
		value._prop = _prop;
		return bef;
	}

	/**
	 * 从Value哈希表中删除Value
	 *
	 * @param key   主健名称
	 *
	 * @return 被删除的Value对象
	 */
	public Value deleteValue(String key) {
		Value value = this.getValue(key);
		if (value == null) {
			return null;
		} else {
			this.values.remove(key);
		}
		return value;
	}

	/**
	 * 取得Value哈希表
	 *
	 * @return Value哈希表
	 */
	public Map<String, Value> getValues() {
		return values;
	}

	/**
	 * 从Value哈希表中查找指定的 configValue 值
	 *
	 * @param key 主健名称
	 *
	 * @return configValue值
	 */
	public Value getValue(String key) {
		return (Value) this.values.get(key);
	}

	/**
	 * 获得指定 configValue 的值
	 *
	 * @param keyName    主健名称
	 *
	 * @return configValue的值
	 */
	public String getConfigValue(String keyName) {
		Value value = this.values.get(keyName);
		if (value == null) {
			return null;
		} else {
			return value.getValue();
		}
	}

	/**
	 * 取得Value名称列表
	 *
	 * @return Value名称列表
	 */
	public String[] valueNames() {

		return this.values.keySet().toArray(new String[0]);
	}

	/**
	 * 转化为Map<String, String>实例
	 *
	 * @return Map<String, String>实例
	 */
	public Map<String, String> toMapValues() {
		Collection<Value> col = this.values.values();
		Map<String, String> mapvalues = new LinkedHashMap<String, String>();
		for (Value v : col) {
			mapvalues.put(v.getName(), v.getValue());
		}
		return mapvalues;
	}

	/**
	 * 转化为Properties实例
	 *
	 * @return Properties实例
	 */
	public Properties toProperties() {
		Collection<Value> col = this.values.values();
		Properties props = new Properties();
		for (Value v : col) {
			props.setProperty(v.getName(), v.getValue());
		}
		return props;
	}

	/**
	 * 把Configuration对象转换为Element对象
	 *
	 * @return Element对象
	 * @throws ParserConfigurationException 
	 */
	public Element toElement() {
		Document document = XmlUtil.newDocument();
		Element groupElem = document.createElement(Configuration.GROUP);
		groupElem.setAttribute(Configuration.GROUP_NAME, name);
		groupElem.setAttribute(Configuration.DESCRIPTION, description);
		for (Value value : this.getValues().values()) {
			Element valueElem = XmlUtil.createChild(groupElem, Configuration.VALUE, value.getValue());
			valueElem.setAttribute(Configuration.VALUE_KEY, value.getName());
		}
		return groupElem;
	}
	
	/**
	 * 深度拷贝
	 */
	public Group clone() {
		Group group = null;
		try {
			group = (Group)super.clone();
			group.values = new LinkedHashMap<String, Value>();
			for (Entry<String, Value> entry : values.entrySet()) {
				group.values.put(entry.getKey(), entry.getValue().clone());
			}
		} catch (CloneNotSupportedException e) {
		}
		return group;
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
		if ((o == null) || !(o instanceof Group)) {
			return false;
		}
		Group t = (Group) o;
		if (!ConfigurationHelper.equal(name, t.name)) {
			return false;
		}
		if (!ConfigurationHelper.equal(description, t.description)) {
			return false;
		}
		if (values.size() != t.values.size()) {
			return false;
		}
		for (String key : t.values.keySet()) {
			Value value1 = t.values.get(key);
			Value value2 = values.get(key);
			if (value1 == null) {
				if (value2 == null) {
					continue;
				} else {
					return false;
				}
			}
			if (!value1.equals(value2)) {
				return false;
			}
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
		result = 37 * result + values.size();
		for (Value value : values.values()) {
			result = 37 * result + (value == null ? 0 : value.hashCode());
		}
		
		return result;
	}
}