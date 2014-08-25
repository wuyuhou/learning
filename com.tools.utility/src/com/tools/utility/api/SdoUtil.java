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
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * sdo操作工具类，不对sdo产生直接依赖
 *
 * @author wuyuhou
 *
 */
public class SdoUtil {
	
	private static final String DEFAULT_ANY_TYPE_SDO_PACKAGE_QNAME = SdoUtil.class.getPackage().getName();
	
	private static Class sdoClass = null;
	
	private static Method getTypeMethod = null;
	
	private static Method deleteMethod = null;
	
	private static Method getPropertyMethod = null;
	
	private static Method getPropertyValueMethod = null;
	
	private static Method getPropertiesMethod = null;
	
	private static Method setPropertyValueMethod = null;
	
	private static Class sdoTypeClass = null;
	
	private static Method getTypeInstanceClassMethod = null;
	
	private static Class sdoPropertyClass = null;
	
	private static Method getPropertyTypeMethod = null;
	
	private static Method getPropertyNameMethod = null;
	
	private static Object eINSTANCE = null;
	
	private static Method createSdoMethod = null;
	
	private static Method sdoLoadMethod = null;
	
	static {
		try {
			sdoClass = loadClass("commonj.sdo.DataObject");
			getTypeMethod = getMethod(sdoClass, "getType", new Class[0]);
			deleteMethod = getMethod(sdoClass, "delete", new Class[0]);
			getPropertyMethod = getMethod(sdoClass, "getProperty", new Class[]{String.class});
			getPropertyValueMethod = getMethod(sdoClass, "get", new Class[]{String.class});
			getPropertiesMethod = getMethod(sdoClass, "getInstanceProperties", new Class[0]);
			setPropertyValueMethod = getMethod(sdoClass, "set", new Class[]{String.class, Object.class});
			
			sdoPropertyClass = loadClass("commonj.sdo.Property");
			getPropertyTypeMethod = getMethod(sdoPropertyClass, "getType", new Class[0]);
			getPropertyNameMethod = getMethod(sdoPropertyClass, "getName", new Class[0]);
			
			sdoTypeClass = loadClass("commonj.sdo.Type");
			getTypeInstanceClassMethod = getMethod(sdoTypeClass, "getInstanceClass", new Class[0]);
			
		} catch (Throwable e) {
		}
		
		try {
			if (sdoClass != null) {
				Class esdoFactoryClass = loadClass("com.primeton.ext.data.sdo.helper.ExtendedDataFactory");
				eINSTANCE = ReflectUtil.getFieldValue(esdoFactoryClass, "eINSTANCE", true, null);
				createSdoMethod = getMethod(eINSTANCE.getClass(), "create", new Class[]{String.class});
			}
		} catch (Throwable e) {
		}
		try {
			if (eINSTANCE != null) {
				StringBuilder buf = new StringBuilder();
				buf.append("<xsd:schema xmlns=\"com.primeton.ibs.sdo\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
				buf.append("        targetNamespace=\"").append(DEFAULT_ANY_TYPE_SDO_PACKAGE_QNAME).append("\">");
				buf.append("	<xsd:complexType name=\"AnyType\">");
				buf.append("		<xsd:sequence>");
				buf.append("			<xsd:any maxOccurs=\"unbounded\" minOccurs=\"0\" namespace=\"##other\" processContents=\"skip\"/>");
				buf.append("		</xsd:sequence>");
				buf.append("	</xsd:complexType>");
				buf.append("</xsd:schema>");
				
				Class sdoLoaderClass = loadClass("com.eos.data.sdo.DynamicXSDLoader");
				sdoLoadMethod = getMethod(sdoLoaderClass, "load", new Class[]{InputStream.class, String.class, boolean.class});
				sdoLoadMethod.invoke(null, new Object[]{new ByteArrayInputStream(buf.toString().getBytes()), null});
			}
		} catch (Throwable e) {
		}
	}
	
	private static Class loadClass(String className) {
		try {
			return ReflectUtil.loadClass(null, className);
		} catch (Throwable e) {
			return null;
		}
	}
	
	private static Method getMethod(Class clazz, String methodName, Class[] paramClasses) {
		if (clazz == null) {
			return null;
		}
		try {
			return ReflectUtil.getMethod(clazz, methodName, paramClasses, true);
		} catch (Throwable e) {
			return null;
		}
	}
	
	public static boolean isSdoType(Class clazz) {
		return sdoClass != null && sdoClass.isAssignableFrom(clazz);
	}
	
	public static Object create(Class toClass) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		try {
			return ReflectUtil.newInstance(toClass);
		} catch (Throwable t) {
			try {
				Object factory = ReflectUtil.getFieldValue(toClass, "FACTORY", true, null);
				Method createMethod = ReflectUtil.getMethod(factory.getClass(), "create", new Class[0], true);
				return createMethod.invoke(factory, new Object[0]);
			} catch (Throwable t1) {
				return create("");
			}
			
		}
	}
	
	public static Object create(String entityName) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		if (createSdoMethod == null) {
			throw new UnsupportedOperationException("Lack sdo create jar, so unsupported create sdo!");
		}
		if (entityName == null || entityName.trim().length() == 0) {
			entityName = DEFAULT_ANY_TYPE_SDO_PACKAGE_QNAME + ".AnyType";
		}
		try {
			return createSdoMethod.invoke(null, new Object[]{entityName});
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	public static void delete(Object sdo) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		try {
			deleteMethod.invoke(sdo, new Object[0]);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	public static void load(InputStream in, boolean isAdded) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		if (sdoLoadMethod == null) {
			throw new UnsupportedOperationException("Lack sdo load jar, so unsupported load sdo!");
		}
		try {
			sdoLoadMethod.invoke(null, new Object[]{in, null, isAdded});
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	public static void load(File xsdPath, boolean isAdded) {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		if (sdoLoadMethod == null) {
			throw new UnsupportedOperationException("Lack sdo load jar, so unsupported load sdo!");
		}
		if (xsdPath == null) {
			throw new IllegalArgumentException("xsdPath is null!");
		}
		if (!xsdPath.exists()) {				
			throw new IllegalArgumentException("xsdPath'" + xsdPath.getAbsolutePath() + "' is not existed!");
		}
		if (xsdPath.isFile()) {
			doLoad(xsdPath, isAdded);
		} else {
			List<File> sdoList = IOUtil.listFiles(xsdPath, new FileFilter(){
				public boolean accept(File file) {
					if (file.isFile()) {
						if (file.getName().toLowerCase().endsWith(".xsd")) {
							return true;
						}
					}
					return false;
				}				
			});
			for (File sdoFile : sdoList) {
				doLoad(sdoFile, isAdded);
			}
		}
	}
	
	private static void doLoad(File xsdFile, boolean isAdded) {
		FileInputStream in = null;
		try {
			in = new FileInputStream(xsdFile);
			load(in, isAdded);
		} catch (Throwable e) {
			throw new IllegalArgumentException("cannot load xsd:" + xsdFile.getAbsolutePath(), e);
		} finally {
			IOUtil.closeQuietly(in);
		}
	}
	
	public static List getInstanceProperties(Object sdo) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		try {
			return (List)getPropertiesMethod.invoke(sdo, new Object[0]);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	public static Object getType(Object sdo) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		try {
			return getTypeMethod.invoke(sdo, new Object[0]);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	public static Class getSdoInstanceClass(Object sdo) throws Throwable {
		return getTypeInstanceClass(getType(sdo));
	}
	
	public static Object getProperty(Object sdo, String propertyName) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		try {
			return getPropertyMethod.invoke(sdo, new Object[]{propertyName});
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	public static String getPropertyName(Object sdoProperty) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		try {
			return (String)getPropertyNameMethod.invoke(sdoProperty, new Object[0]);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	public static Object getPropertyType(Object sdoProperty) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		try {
			return getPropertyValueMethod.invoke(getPropertyTypeMethod, new Object[0]);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	public static Class getTypeInstanceClass(Object sdoType) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		try {
			return (Class)getTypeInstanceClassMethod.invoke(sdoType, new Object[0]);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	public static Class getPropertyInstanceClass(Object sdo, String propertyName) throws Throwable {
		return getTypeInstanceClass(getPropertyType(getProperty(sdo, propertyName)));
	}
	
	public static Object getPropertyValue(Object sdo, String propertyName) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		try {
			return (String)getPropertyValueMethod.invoke(sdo, new Object[]{propertyName});
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	public static void setPropertyValue(Object sdo, String propertyName, Object propertyValue) throws Throwable {
		if (sdoClass == null) {
			throw new UnsupportedOperationException("Lack sdo jar, so unsupported sdo!");
		}
		try {
			setPropertyValueMethod.invoke(sdo, new Object[]{propertyName, propertyValue});
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
}