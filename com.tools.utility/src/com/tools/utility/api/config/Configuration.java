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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.tools.utility.api.IOUtil;
import com.tools.utility.api.XmlUtil;
import com.tools.utility.impl.config.ConfigurationHelper;

/**
 * 三段式配置信息类。<br>
 * <p>
 * 配置文件为 xml 文件, 格式如下:
 * <br>
 * <pre><code>
 * 	&lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;
 * 	&lt;configuration description="XXX" &gt;
 * 		&lt;property name="version" value="6.0"/&gt;
 * 		&lt;module name="global" description="XXX" &gt;
 * 			&lt;group name="productInfo" description="XXX" &gt;
 * 				&lt;configValue key="productname" description="XXX" &gt;Primeton Studio&lt;/configValue&gt;
 * 				&lt;configValue key="info"&gt;普元&lt;/configValue&gt;
 * 				&lt;configValue key="version"&gt;${version}&lt;/configValue&gt;
 * 				&lt;configValue key="modifydate"&gt;2003/12/18&lt;/configValue&gt;
 * 			&lt;/group&gt;
 * 			&lt;group name="DBConnect"&gt;
 * 				&lt;configValue key="context-initial-factory"&gt;org.apache.naming.java.javaURLContextFactory&lt;/configValue&gt;
 * 				&lt;configValue key="dbprovider"&gt;t3://localhost:8080&lt;/configValue&gt;
 * 				&lt;configValue key="single"&gt;true&lt;/configValue&gt;
 * 				&lt;configValue key="username"&gt;jiaoly2&lt;/configValue&gt;
 * 				&lt;configValue key="password"&gt;jiaoly2&lt;/configValue&gt;
 * 				&lt;configValue key="jdbcurl"&gt;jdbc:oracle:thin:@192.168.1.250:1521:eos12&lt;/configValue&gt;
 * 				&lt;configValue key="jdbcdriver"&gt;oracle.jdbc.driver.OracleDriver&lt;/configValue&gt;
 * 			&lt;/group&gt;
 * 		&lt;/module&gt;
 * 	&lt;/configuration&gt;
 * </code></pre>
 * 使用示例: <br>
 * <pre>
 * String filePath = "d:\\test.xml";
 * Configuration config = new Configuration(filePath);
 * String value = config.getConfigValue("global", "productInfo", "productname");
 * //value == "Primeton EOS Studio"
 * value = "test";
 * //改变指定 config 的值
 * config.setConfigValue("global", "productInfo", "productname", value);
 * </pre>
 * 
 * @author wuyuhou
 */

public class Configuration implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -3467623613804896461L;

	/** 配置文件路径 */
	private String filePath = null;
	
	// 描述
	private String description = null;
	
	private String encoding = null;

	/** 文档对象 */
	private transient Document document = null;
	
	/** 模块哈希表 */
	private Map<String, Module> modules = new LinkedHashMap<String, Module>();
	
	/** 属性信息 */
	private Properties _prop = new Properties();
	
	////////////////////////////////////////////////////////////
	//下面是定义的常量
	final static String MODULE = "module";

	final static String GROUP = "group";

	final static String VALUE = "configValue";
	
	final static String DESCRIPTION = "description";

	final static String MODULE_NAME = "name";

	final static String GROUP_NAME = "name";

	final static String VALUE_KEY = "key";
	
	//属性信息节点常量	
	final static String PROPERTY = "property";
	
	final static String PROPERTY_NAME = "name";
	
	final static String PROPERTY_VALUE = "value";
	
	/**
	 * 
	 * 构造方法（默认）
	 *
	 */
	public Configuration() {
		this(new ByteArrayInputStream("<configuraion/>".getBytes()), "UTF-8");
	}
	
	/**
	 * 构造方法
	 *
	 * @param configPath 配置文件绝对路径
	 * @throws ConfigurationRuntimeException 抛出条件：解析出错
	 */
	public Configuration(File configFile, String encoding) throws ConfigurationRuntimeException {
		if (configFile == null) {
			throw new IllegalArgumentException("ConfigFile is null!");
		}
		if (!configFile.exists()) {
			throw new IllegalArgumentException("ConfigFile'" + configFile + "' is not existed!");
		}
		if (configFile.isDirectory()) {
			throw new IllegalArgumentException("ConfigFile'" + configFile + "' is dir, not file!");
		}

		filePath = configFile.getAbsolutePath();
		this.encoding = encoding;
		parse();
	}
	
	private void parse() {
		InputStream in = null;
		try {
			in = new FileInputStream(filePath);
			document = XmlUtil.parse(in, encoding, null);
			parse(document, modules);
		} catch (Exception e) {
			throw new ConfigurationRuntimeException("File[{0}] parse error!", new Object[] {filePath}, e);
		} finally {
			IOUtil.closeQuietly(in);
		}
	}

	/**
	 * 构造方法
	 * 
	 * @param in 配置文件流
	 * @throws ConfigurationRuntimeException 抛出条件：解析出错
	 */
	public Configuration(InputStream in, String encoding) throws ConfigurationRuntimeException {
		if (in == null) {
			throw new IllegalArgumentException("InputStream is null!");
		}
		this.encoding = encoding;
		try {
			document = XmlUtil.parse(in, encoding, null);
			parse(document, modules);
		} catch (Exception e) {
			throw new ConfigurationRuntimeException("Document parse error!", e);
		}
	}

	/**
	 * 构造方法
	 * 
	 * @param conf
	 * @throws ConfigurationRuntimeException 抛出条件：解析出错
	 */
	public Configuration(Document doc) throws ConfigurationRuntimeException {
		if(doc == null) {
			throw new IllegalArgumentException("Document is null!");
		}
		try {
			document = doc;
			parse(document, modules);
		} catch (Exception e) {
			throw new ConfigurationRuntimeException("Document parse error!", e);
		}
	}
	
	static boolean isNullOrBlank(String str) {
		if (str == null || str.trim().length() == 0 || str.equals("null")) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * 解析操作
	 *
	 * @throws ConfigurationRuntimeException 抛出条件：解析出错
	 */
	private void parse(Document doc, Map<String, Module> moduleMap) throws Exception {
		Element root = doc.getDocumentElement();
		description = root.getAttribute(Configuration.DESCRIPTION);
		// 解析属性信息
		NodeList list = XmlUtil.findNodes(root, Configuration.PROPERTY);
		for (int i = 0; i < list.getLength(); i++) {
			Element propEle = (Element) list.item(i);
			// 解析<property name="src" value="./src"/>
			String propName = propEle.getAttribute(Configuration.PROPERTY_NAME);
			if (!isNullOrBlank(propName)) {
				_prop.setProperty(propName, propEle.getAttribute(Configuration.PROPERTY_VALUE));
			}
		}

		// 解析三段式节点
		NodeList listModule = XmlUtil.findNodes(root, Configuration.MODULE);
		for (int i = 0; i < listModule.getLength(); i++) {
			Element moduleEle = (Element) listModule.item(i);
			Module module = new Module(moduleEle, _prop);
			moduleMap.put(module.getName(), module);
		}
	}
	
	/**
	 * 取得配置文件的全路径
	 *
	 * @return 配置文件的全路径
	 */
	public String getConfigFilePath() {
		return filePath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 获取所有 module 的名称列表
	 *
	 * @return 模块名称列表
	 */
	public String[] moduleNames() {

		return this.modules.keySet().toArray(new String[0]);
	}
	
	/**
	 * 自融合
	 *
	 * @param config config对象
	 * @return 融合后的config对象
	 */
	public Configuration mergeConfiguration(Configuration config) {
		for(Module module : config.modules.values()) {
			mergeModule(module);
		}
		return this;
	}

	/**
	 * 获得指定的 module
	 *
	 * @param moduleName 模块名称
	 *
	 * @return 模块对象
	 */
	public Module getModule(String moduleName) {
		return (Module) this.modules.get(moduleName);
	}

	/**
	 * 取得Module哈希表
	 *
	 * @return Module哈希表
	 */
	public Map<String, Module> getModules() {
		return modules;
	}

	/**
	 * 添加指定名称的 module, 如果module已经存在, 不做处理<br>
	 *
	 * @param moduleName 模块名称
	 * @return 添加后的模块对象
	 */
	public Module addModule(String moduleName) {
		if (this.getModule(moduleName) != null) {
			return null;
		}
		Module newModule = new Module(moduleName);
		this.modules.put(moduleName, newModule);
		return newModule;
	}

	/**
	 * 添加指定的 module
	 *
	 * @param module module对象
	 * @return 添加后的module对象
	 */
	public Module addModule(Module module) {
		String moduleName = module.getName();
		Module moduleRet = this.getModule(moduleName);
		if (moduleRet != null) {
			return null;
		} else {
			this.modules.put(moduleName, module);
			module._prop = _prop;
			return module;
		}
	}
	
	/**
	 * 融合指定的 module，如果已经存在，则更新，否则，增加
	 *
	 * @param module module对象
	 * @return 融合后的module对象
	 */
	public Module mergeModule(Module module) {
		String moduleName = module.getName();
		Module moduleRet = this.getModule(moduleName);
		if (moduleRet == null) {
			this.modules.put(moduleName, module);
			module._prop = _prop;
		} else {
			for (Group group : module.groups.values()) {
				moduleRet.mergeGroup(group);
			}
			this.modules.put(moduleName, moduleRet);
		}
		return this.getModule(moduleName);
	}

	/**
	 * 设定指定的 module
	 *
	 * @param module module对象
	 * @return 设定前的module对象
	 */
	public Module setModule(Module module) {
		if (module == null) {
			return null;
		}
		Module bef = this.modules.get(module.getName());
		this.deleteModule(module.getName());
		this.addModule(module);
		module._prop = _prop;
		return bef;
	}

	/**
	 * 从配置信息中删除指定的 module<br>
	 *
	 * @param moduleName 模块名称
	 * @return 被删除的模块对象
	 */
	public Module deleteModule(String moduleName) {
		Module module = this.getModule(moduleName);
		if (module == null) {
			return null;
		}
		this.modules.remove(moduleName);
		return module;
	}

	/**
	 * 获取指定 module 中, 所有 group 名称列表
	 *
	 * @param moduleName 模块名称
	 * @return group名称列表
	 */
	public String[] groupNames(String moduleName) {
		Module module = this.getModule(moduleName);
		if (module == null) {
			return new String[0];
		} else {
			return module.groupNames();
		}
	}

	/**
	 * 获得指定的 Group
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 * @return Group对象
	 */
	public Group getGroup(String moduleName, String groupName) {
		Module module = this.getModule(moduleName);
		if (module == null)
			return null;
		return module.getGroup(groupName);
	}

	/**
	 * 添加一个Group, 如果 module 不存在, 将默认创建<br>
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 *
	 * @return 添加后的Group对象
	 */
	public Group addGroup(String moduleName, String groupName) {
		Module module = this.getModule(moduleName);
		if (module == null) {
			module = this.addModule(moduleName);
		}
		return module.addGroup(groupName);
	}

	/**
	 * 添加一个Group
	 *
	 * @param moduleName module名称
	 * @param group  group对象
	 *
	 * @return 添加后的Group对象
	 */
	public Group addGroup(String moduleName, Group group) {
		Module module = this.getModule(moduleName);
		if (module == null) {
			module = this.addModule(moduleName);
		}
		return module.addGroup(group);
	}
	
	/**
	 * 融合一个Group
	 *
	 * @param moduleName module名称
	 * @param group  group对象
	 *
	 * @return 融合后的Group对象
	 */
	public Group mergeGroup(String moduleName, Group group) {
		Module module = this.getModule(moduleName);
		if (module == null) {
			module = this.addModule(moduleName);
		}
		return module.mergeGroup(group);
	}

	/**
	 * 设定一个Group
	 *
	 * @param moduleName module名称
	 * @param group  group对象
	 *
	 * @return 设定前的Group对象
	 */
	public Group setGroup(String moduleName, Group group) {
		Module module = this.getModule(moduleName);
		if (module == null) {
			module = this.addModule(moduleName);
		}		
		return module.setGroup(group);
	}

	/**
	 * 在指定的 module 中删除 group<br>
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 *
	 * @return 被删除的Group对象
	 */
	public Group deleteGroup(String moduleName, String groupName){
		Module module = this.getModule(moduleName);
		if (module == null) {
			return null;
		}
		return module.deleteGroup(groupName);
	}

	/**
	 * 获取指定 module  group 中, 所有 configValue 名称列表
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 *
	 * @return configValue名称列表
	 */
	public String[] valueNames(String moduleName, String groupName) {
		Group group = this.getGroup(moduleName, groupName);
		if (group == null) {
			return new String[0];
		} else {
			return group.valueNames();
		}
	}

	/**
	 * 获得指定 configValue 的值
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 * @param keyName    主健名称
	 *
	 * @return configValue的值
	 */
	public String getConfigValue(String moduleName, String groupName,
			String keyName) {
		Module module = (Module) this.modules.get(moduleName);
		if (module == null)
			return null;

		return module.getConfigValue(groupName, keyName);
	}
	
	public String getConfigValue(String moduleName, String groupName,
			String keyName, String defaultValue) {
		String value = getConfigValue(moduleName, groupName, keyName);
		if (value == null || value.trim().length() == 0) {
			return defaultValue;
		} else {
			return value;
		}
	}

	/**
	 * 添加一个 configValue 配置项, moduleName groupName 如果不存在, 将被创建<br>
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 * @param key        主健名称
	 * @param value      主健值
	 *
	 * @return  添加后的Value对象
	 */
	public Value addValue(String moduleName, String groupName, String key, String value) {

		return addValue(moduleName, groupName, new Value(key, value));
	}

	/**
	 * 添加一个 configValue 配置项, moduleName groupName 如果不存在, 将被创建<br>
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 * @param value      Value对象
	 *
	 * @return  融合后的Value对象
	 */
	public Value mergeValue(String moduleName, String groupName, Value value) {
		Module module = this.getModule(moduleName);
		if (module == null) {
			module = this.addModule(moduleName);
		}
		return module.mergeValue(groupName, value);
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
	public Value addValue(String moduleName, String groupName, Value value) {
		Module module = this.getModule(moduleName);
		if (module == null) {
			module = this.addModule(moduleName);
		}
		return module.addValue(groupName, value);
	}

	/**
	 * 为指定的 configValue 赋值, 如果moduleName groupName不存在, 将默认创建<br>
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 * @param key        主健名称
	 * @param value      主健值
	 * @return 设定前的Value对象
	 */
	public Value setValue(String moduleName, String groupName, String key, String value) {

		return setValue(moduleName, groupName, new Value(key, value));
	}

	/**
	 * 设定一个 configValue 配置项, moduleName groupName 如果不存在, 将被创建
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 * @param value      Value对象
	 *
	 * @return 设定前的Value对象
	 */
	public Value setValue(String moduleName, String groupName, Value value) {
		Module module = this.getModule(moduleName);
		if (module == null) {
			module = this.addModule(moduleName);
		}
		return module.setValue(groupName, value);
	}

	/**
	 * 删除指定的Value
	 *
	 * @param moduleName 模块名称
	 * @param groupName  group名称
	 * @param key        主健名称
	 *
	 * @return 被删除的Value对象
	 */
	public Value deleteValue(String moduleName, String groupName, String key){
		Module module = this.getModule(moduleName);
		if (module == null) {
			return null;
		}
		return module.deleteValue(groupName, key);
	}

	/**
	 * 保存Configuration
	 *
	 *  @throws ConfigurationRuntimeException 抛出条件：文件路径不存在，或者文件无法保存
	 */
	public void save() throws ConfigurationRuntimeException {
		if (this.document == null) {
			throw new ConfigurationRuntimeException("the document is null!");
		}
		if (this.filePath == null) {
			throw new ConfigurationRuntimeException("filepath is null!");
		}
		
		OutputStream out = null;
		try {
			if (isModifyAndUpdateDocument(this, true)) {
				out = new FileOutputStream(filePath);
				XmlUtil.save(document, out, true, encoding);
			}			
		} catch (Exception e) {
			throw new ConfigurationRuntimeException("the document cannot save to [path={0}]!", new String[]{filePath}, e);
		} finally {
			IOUtil.closeQuietly(out);
		}
	}
 
	/**
	 * 将当前的Configuration保存到指定的文件中
	 *
	 * @param configFile 文件路径
	 * @throws ConfigurationRuntimeException 抛出条件：Document为null，或者文件无法保存
	 */
	public void saveAs(File configFile) throws ConfigurationRuntimeException {
		if (configFile == null) {
			throw new IllegalArgumentException("ConfigFile is null!");
		}
		if (!configFile.exists()) {
			throw new IllegalArgumentException("ConfigFile'" + configFile + "' is not existed!");
		}
		if (configFile.isDirectory()) {
			throw new IllegalArgumentException("ConfigFile'" + configFile + "' is dir, not file!");
		}
		if (this.document == null) {
			throw new ConfigurationRuntimeException("the document is null!");
		}
		OutputStream out = null;
		try {
			isModifyAndUpdateDocument(this, true);
			out = new FileOutputStream(configFile);
			XmlUtil.save(document, out, true, encoding);
		} catch (Exception e) {
			throw new ConfigurationRuntimeException("the document cannot save to [path={0}]!", new Object[]{configFile}, e);
		} finally {
			IOUtil.closeQuietly(out);
		}
	}
	
	/**
	 * 深度拷贝
	 */
	public Configuration clone() {
		Configuration config = null;
		try {
			config = (Configuration)super.clone();
			Document _oldDoc = config.document;
			Document newDoc = XmlUtil.newDocument();
			Node root = newDoc.importNode(_oldDoc.getDocumentElement(), true);
			newDoc.appendChild(root);
			config.document = newDoc;
//			config.document = (Document)document.cloneNode(true);
			config.modules = new LinkedHashMap<String, Module>();
			for (Entry<String, Module> entry : modules.entrySet()) {
				config.modules.put(entry.getKey(), entry.getValue().clone());
			}
		} catch (Exception e) {
		}
		return config;
	}
	
	/**
	 * Document格式的字符串化
	 *
	 * @return Document格式的信息
	 */
	public String toString() {
		return XmlUtil.node2String(toDocument(), true, encoding);
	}
	
	/**
	 * 相等比较(只比较Modules)
	 * 
	 * @return true:相等
	 */
	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof Configuration)) {
			return false;
		}
		Configuration t = (Configuration) o;
		if (!ConfigurationHelper.equal(description, t.description)) {
			return false;
		}
		if (modules.size() != t.modules.size()) {
			return false;
		}
		for (String key : t.modules.keySet()) {
			Module module1 = t.modules.get(key);
			Module module2 = modules.get(key);
			if (module1 == null) {
				if (module2 == null) {
					continue;
				} else {
					return false;
				}
			}
			if (!module1.equals(module2)) {
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
		result = 37 * result + (filePath == null ? 0 : filePath.hashCode());
		result = 37 * result + (description == null ? 0 : description.hashCode());
		result = 37 * result + modules.size();
		for (Module module : modules.values()) {
			result = 37 * result + (module == null ? 0 : module.hashCode());
		}
		
		return result;
	}
	
	/**
	 * 序列化
	 * 
	 * @param s
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		
		s.defaultReadObject();
		
		if (filePath != null) {
			parse();		
		}
	}
	
	/**
	 * 把Configuration对象转换为Document对象
	 */
	private static boolean isModifyAndUpdateDocument(Configuration config, boolean updateFlag) {
		
		Configuration oldConfig = null;
		if (config.filePath == null) {
			oldConfig = new Configuration(config.document);
		} else {
			oldConfig = new Configuration(new File(config.filePath), config.encoding);
		}
		
		boolean modifyFlag = false;
		if (!config.equals(oldConfig)) {
			modifyFlag = true;
			if (updateFlag) {
				Element root = config.document.getDocumentElement();
				root.setAttribute(Configuration.DESCRIPTION, config.description);
				Map<String, Module> oldModuleMap = oldConfig.modules;

				Map<String, Module> newModuleMap = config.clone().getModules();

				// 修改module节点
				for (String moduleName : oldModuleMap.keySet().toArray(new String[0])) {					
					Module newModule = newModuleMap.get(moduleName);
					//newModule不存在
					if (newModule == null) {
						ConfigurationHelper.removeChild(root, Configuration.MODULE, Configuration.MODULE_NAME, moduleName);
						oldModuleMap.remove(moduleName);
					} else {
						//module
						Module oldModule = oldModuleMap.get(moduleName);
						for (String groupName : oldModule.groupNames()) {							
							Group newGroup = newModule.getGroup(groupName);
							//newGroup不存在
							if (newGroup == null) {
								ConfigurationHelper.removeChild(getElement(root, moduleName), getElement(root, moduleName, groupName));
								oldModule.deleteGroup(groupName);
							} else {
								//group
								Group oldGroup = oldModule.getGroup(groupName);
								for (String key : oldGroup.valueNames()) {					
									String value = oldGroup.getConfigValue(key);
									Value newValue = newGroup.getValue(key);
									//删除节点
									if (newValue == null) {
										ConfigurationHelper.removeChild(getElement(root, moduleName, groupName), getElement(root, moduleName, groupName, key));
										oldGroup.deleteValue(key);
									} else {
										if (!ConfigurationHelper.equal(newValue.getValue(), value)) {
											Element valueElem = getElement(root, moduleName, groupName, key);											
											XmlUtil.removeAllChild(valueElem);
											valueElem.setNodeValue(newValue.getValue());
										}
									}
									newGroup.deleteValue(key);
								}
								
								//增加Value节点
								for (Entry<String, Value> addValueEntry : newGroup.getValues().entrySet()) {
									Value value = addValueEntry.getValue();
									Element valueElem = ConfigurationHelper.appendChild(getElement(root, moduleName, groupName), Configuration.VALUE, Configuration.VALUE_KEY, value.getName());
									valueElem.setNodeValue(value.getValue());
								}
							}
							
							newModule.deleteGroup(groupName);					
						}
						
						//增加Group节点
						for (Entry<String, Group> addGroupEntry : newModule.getGroups().entrySet()) {
							String groupName = addGroupEntry.getKey();
							Element groupElem = ConfigurationHelper.appendChild(getElement(root, moduleName), Configuration.GROUP, Configuration.GROUP_NAME, groupName);
							for (Value value : addGroupEntry.getValue().getValues().values()) {
								Element valueElem = ConfigurationHelper.appendChild(groupElem, Configuration.VALUE, Configuration.VALUE_KEY, value.getName());
								valueElem.setNodeValue(value.getValue());
							}
						}
					}
					newModuleMap.remove(moduleName);
				}
				
				//增加Module节点
				for (Entry<String, Module> addModuleEntry : newModuleMap.entrySet()) {
					String moduleName = addModuleEntry.getKey();
					Element moduleElem = ConfigurationHelper.appendChild(root, Configuration.MODULE, Configuration.MODULE_NAME, moduleName);
					for (Group group : addModuleEntry.getValue().getGroups().values()) {
						Element groupElem = ConfigurationHelper.appendChild(moduleElem, Configuration.GROUP, Configuration.GROUP_NAME, group.getName());
						for (Value value : group.getValues().values()) {
							Element valueElem = ConfigurationHelper.appendChild(groupElem, Configuration.VALUE, Configuration.VALUE_KEY, value.getName());
							valueElem.setNodeValue(value.getValue());
						}
					}
				}
			}
		}
		return modifyFlag;
	}
	
	//取得Module元素
	private static Element getElement(Element root, String moduleName) {
		return ConfigurationHelper.getChild(root, Configuration.MODULE, Configuration.MODULE_NAME, moduleName);
	}
	
	//取得Group元素
	private static Element getElement(Element root, String moduleName, String groupName) {
		return ConfigurationHelper.getChild(getElement(root, moduleName), Configuration.GROUP, Configuration.GROUP_NAME, groupName);
	}
	
	//取得Value元素
	private static Element getElement(Element root, String moduleName, String groupName, String valueName) {
		return ConfigurationHelper.getChild(getElement(root, moduleName, groupName), Configuration.VALUE, Configuration.VALUE_KEY, valueName);
	}

	/**
	 * 把Configuration对象转换为Document对象
	 *
	 * @return Document对象
	 */
	public Document toDocument() {
		Configuration clone = this.clone();
		isModifyAndUpdateDocument(clone, true);
		return clone.document;
	}
}