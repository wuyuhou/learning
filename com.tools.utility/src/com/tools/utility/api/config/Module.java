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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.tools.utility.api.XmlUtil;
import com.tools.utility.impl.config.ConfigurationHelper;

/**
 * 模块类:对应EOS三段式配置的&lt;module name="global"/&gt;
 *
 * @author wuyuhou
 *
 */
public class Module implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 6755737425219904258L;

	/** 模块名称 */
	private String name;
	
	//描述
	private String description;
	
	Properties _prop;

	/** Group哈希表 */
	Map<String, Group> groups = new LinkedHashMap<String, Group>();

	/**
	 * 构造方法
	 */
	public Module() {
		this("");
	}

	/**
	 * 构造方法
	 *
	 * @param moduleName 模块名称
	 */
	public Module(String moduleName) {
		name = moduleName;
		_prop = new Properties();
	}

	/**
	 * 构造方法
	 *
	 * @param element 模块元素
	 *
	 * @throws ConfigurationRuntimeException Document解析出错
	 */
	public Module(Element moduleElement) throws ConfigurationRuntimeException {
		this(moduleElement, null);
	}

	/**
	 * 构造方法
	 *
	 * @param element 模块元素
	 * @param prop 属性信息
	 * 
	 * @throws ConfigurationRuntimeException Document解析出错
	 */
	public Module(Element moduleElement, Properties prop) throws ConfigurationRuntimeException {
		if (prop == null) {
			_prop = new Properties();
		} else {
			_prop = prop;
		}
		this.parse(moduleElement);			
	}
	
	/**
	 * 解析模块元素操作
	 *
	 * @throws ConfigurationRuntimeException Document解析出错
	 */
	private void parse(Element moduleElement) throws ConfigurationRuntimeException {
		this.name = moduleElement.getAttribute(Configuration.MODULE_NAME);
		this.description = moduleElement.getAttribute(Configuration.DESCRIPTION);
		try {
			NodeList nodeList = XmlUtil.findNodes(moduleElement, Configuration.GROUP);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element groupEle = (Element) nodeList.item(i);
				Group group = new Group(groupEle, _prop);
				this.groups.put(group.getName(), group);
			}
		} catch (Exception e) {
			throw new ConfigurationRuntimeException("the element parse error!", e);
		}
	}

	/**
	 * 取得模块名称
	 *
	 * @return 模块名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设定模块名称
	 * @param moduleName 模块名称
	 */
	public void setName(String moduleName) {
		name = moduleName;
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
	 * @param module module对象
	 *
	 * @return 融合后的module对象
	 */
	public Module mergeModule(Module module) {
		for (Group group : module.groups.values()) {
			mergeGroup(group);
		}
		return this;
	}

	/**
	 * 向Group哈希表添加Group
	 *
	 * @param groupName group名称
	 *
	 * @return 添加后的Group对象
	 */
	public Group addGroup(String groupName) {
		Group group = this.getGroup(groupName);
		if (group != null) {
			return null;
		}
		group = new Group(groupName);
		this.groups.put(groupName, group);

		return group;
	}

	/**
	 * 向Group哈希表添加Group
	 *
	 * @param group Group对象
	 *
	 * @return 添加后的Group对象
	 */
	public Group addGroup(Group group) {
		String groupName = group.getName();
		Group groupRet = this.getGroup(groupName);
		if (groupRet != null) {
			return null;
		} else {
			this.groups.put(groupName, group);
			group._prop = _prop;
			return group;
		}
	}
	
	/**
	 * 融合Group
	 *
	 * @param group Group对象
	 *
	 * @return 融合后的Group对象
	 */
	public Group mergeGroup(Group group) {
		String groupName = group.getName();
		Group groupRet = this.getGroup(groupName);
		if (groupRet == null) {
			this.groups.put(groupName, group);
			group._prop = _prop;
		} else {
			for (Value value : group.getValues().values()) {
				groupRet.mergeValue(value);
			}
			this.groups.put(groupName, groupRet);
		}
		return this.getGroup(groupName);
	}

	/**
	 * 设定group
	 *
	 * @param group Group对象
	 * @return 设定前的Group对象
	 */
	public Group setGroup(Group group) {
		if (group == null) {
			return null;
		}
		
		Group bef = this.groups.get(group.getName());
		deleteGroup(group.getName());
		addGroup(group);
		group._prop = _prop;
		return bef;
	}

	/**
	 * 从Group哈希表删除一个Group
	 *
	 * @param groupName group名称
	 *
	 * @return 被删除的Group对象
	 */
	public Group deleteGroup(String groupName) {
		Group group = this.getGroup(groupName);
		if (group == null) {
			return null;
		}
		this.groups.remove(groupName);
		return group;
	}

	/**
	 * 从Group哈希表取得一个Group
	 *
	 * @param groupName group名称
	 *
	 * @return Group对象
	 */
	public Group getGroup(String groupName) {
		return this.groups.get(groupName);
	}

	/**
	 * 取得Group名称列表
	 *
	 * @return Group名称列表
	 */
	public String[] groupNames() {

		return this.groups.keySet().toArray(new String[0]);
	}

	/**
	 * 取得Group哈希表
	 *
	 * @return Group哈希表
	 */
	public Map<String, Group> getGroups() {
		return groups;
	}

	/**
	 * 获取指定 group 中, 所有 configValue 名称列表
	 *
	 * @param groupName  group名称
	 *
	 * @return configValue名称列表
	 */
	public String[] valueNames(String groupName) {
		Group group = this.groups.get(groupName);
		if (group == null) {
			return new String[0];
		} else {
			return group.valueNames();
		}
	}

	/**
	 * 获得指定 configValue 的值
	 *
	 * @param groupName  group名称
	 * @param key        主健名称
	 *
	 * @return configValue的值
	 */
	public String getConfigValue(String groupName, String key) {
		Group group = this.groups.get(groupName);
		if (group == null)
			return null;

		return group.getConfigValue(key);
	}

	/**
	 * 添加一个 configValue 配置项,  groupName 如果不存在, 将被创建
	 *
	 * @param groupName  group名称
	 * @param key        主健名称
	 * @param value      主健值
	 *
	 * @return  添加后的Value对象
	 */
	public Value addValue(String groupName, String key, String value) {

		return addValue(groupName, new Value(key, value));
	}

	/**
	 * 添加一个 configValue 配置项, moduleName groupName 如果不存在, 将被创建<br>
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 * @param value      Value对象
	 *
	 * @return  添加后的Value对象
	 */
	public Value addValue(String groupName, Value value) {
		Group group = this.groups.get(groupName);
		if (group == null) {
			group = addGroup(groupName);
		}
		return group.addValue(value);
	}
	
	/**
	 * 融合一个 configValue 配置项, moduleName groupName 如果不存在, 将被创建<br>
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 * @param value      Value对象
	 *
	 * @return  添加后的Value对象
	 */
	public Value mergeValue(String groupName, Value value) {
		Group group = this.groups.get(groupName);
		if (group == null) {
			group = addGroup(groupName);
		}
		return group.mergeValue(value);
	}

	/**
	 * 为指定的 configValue 赋值, 如果groupName不存在, 将默认创建<br>
	 *
	 * @param groupName  group名称
	 * @param key        主健名称
	 * @param value      主健值
	 * 
	 * @return 设定前的Value对象
	 */
	public Value setValue(String groupName, String key, String value) {
		return setValue(groupName, new Value(key, value));
	}

	/**
	 * 设定一个 configValue 配置项, groupName 如果不存在, 将被创建
	 *
	 * @param groupName  group名称
	 * @param value      Value对象
	 *
	 * @return 设定前的Value对象
	 */
	public Value setValue(String groupName, Value value) {
		Group group = this.groups.get(groupName);
		if (group == null) {
			group = addGroup(groupName);
		}
		return group.setValue(value);
	}

	/**
	 * 删除指定的
	 *
	 * @param groupName  group名称
	 * @param key        主健名称
	 *
	 * @return 被删除的Value对象
	 */
	public Value deleteValue(String groupName, String key){
		Group group = this.groups.get(groupName);
		if (group == null) {
			return null;
		}
		return group.deleteValue(key);
	}

	/**
	 * 把Configuration对象转换为Element对象
	 *
	 * @return Element对象
	 */
	public Element toElement() {
		Document document = XmlUtil.newDocument();
		Element moduleElem = document.createElement(Configuration.MODULE);
		moduleElem.setAttribute(Configuration.MODULE_NAME, name);
		moduleElem.setAttribute(Configuration.DESCRIPTION, description);

		for (Group group : this.getGroups().values()) {
			Element groupElem = XmlUtil.createChild(moduleElem, Configuration.GROUP, null);
			groupElem.setAttribute(Configuration.GROUP_NAME, group.getName());
			for (Value value : group.getValues().values()) {
				Element valueElem = XmlUtil.createChild(groupElem, Configuration.VALUE, value.getValue());
				valueElem.setAttribute(Configuration.VALUE_KEY, value.getName());
			}
		}

		return moduleElem;
	}
	
	/**
	 * 深度拷贝
	 */
	public Module clone() {
		Module module = null;
		try {
			module = (Module)super.clone();
			module.groups = new LinkedHashMap<String, Group>();
			for (Entry<String, Group> entry : groups.entrySet()) {
				module.groups.put(entry.getKey(), entry.getValue().clone());
			}
		} catch (CloneNotSupportedException e) {
		}			
		return module;
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
		if ((o == null) || !(o instanceof Module)) {
			return false;
		}
		Module t = (Module) o;
		if (!ConfigurationHelper.equal(name, t.name)) {
			return false;
		}
		if (!ConfigurationHelper.equal(description, t.description)) {
			return false;
		}
		if (groups.size() != t.groups.size()) {
			return false;
		}
		for (String key : t.groups.keySet()) {
			Group group1 = t.groups.get(key);
			Group group2 = groups.get(key);
			if (group1 == null) {
				if (group2 == null) {
					continue;
				} else {
					return false;
				}
			}
			if (!group1.equals(group2)) {
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
		result = 37 * result + groups.size();
		for (Group group : groups.values()) {
			result = 37 * result + (group == null ? 0 : group.hashCode());
		}
		
		return result;
	}
}