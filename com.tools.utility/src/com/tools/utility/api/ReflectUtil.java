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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tools.utility.spi.convert.TypeConvertorManager;


/**
 * 反射工具实现
 * 
 * @author wuyuhou
 * 
 */
public class ReflectUtil {

	/**
	 * 加载类
	 * 
	 * @param loader 加载类的loader，可以为空
	 * @param className 类名称，不可以为空
	 * @return 类
	 * @throws ClassNotFoundException 类找不到
	 */
	public static Class loadClass(ClassLoader loader, String className) throws ClassNotFoundException {

		if (className == null || className.trim().length() == 0) {
			throw new IllegalArgumentException("className is null!");
		}
		className = className.trim();
		Class clazz = null;
		if (className.charAt(0) == '[') {
			className = className.substring(1);
			if (className.charAt(0) == 'L') {
				className = className.substring(1, className.length() - 1);
			}			
			clazz = loadClass(loader, className);
			clazz = Array.newInstance(clazz, 0).getClass();
		} else {
			clazz = primitiveWrapperTypeMap.get(className);
			if (clazz == null) {
				if (loader == null) {
					loader = ReflectUtil.class.getClassLoader();
				}
				try {
					clazz = loader.loadClass(className);
				} catch (Throwable e) {
					clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
				}
			}
		}

		return clazz;
	}

	/**
	 * 实例化
	 * 
	 * @param clazz 类，不可以为空
	 * @param parameterTypes 类型信息，可以为空
	 * @param isOnlyPublic 是否只查找公有构造方法
	 * @param initArgs 参数值，可以为空
	 * @return 实例对象
	 * @throws InstantiationException 实例化错误
	 * @throws IllegalAccessException 非法访问错误
	 * @throws InvocationTargetException 构造方法执行过程中出错
	 * @throws NoSuchMethodException 没有这个构造方法
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class clazz, Class[] parameterTypes, boolean isOnlyPublic, Object[] initArgs) throws InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz is null!");
		}
		if (initArgs == null) {
			initArgs = new Object[0];
		}
		if (parameterTypes == null) {
			parameterTypes = args2types(initArgs);
		}
		if (parameterTypes.length != initArgs.length) {
			throw new IllegalArgumentException("The length of parameterTypes and initArgs are inconsistent!");
		}

		final Constructor constructor = getConstructor(clazz, parameterTypes, isOnlyPublic);

		if (!Modifier.isPublic(constructor.getModifiers())) {
			final boolean isAccessible = constructor.isAccessible();
			try {
				AccessController.doPrivileged(new PrivilegedAction() {
					public Object run() {
						constructor.setAccessible(true);
						return null;
					}
				});

				return (T) constructor.newInstance(initArgs);
			} finally {
				AccessController.doPrivileged(new PrivilegedAction() {
					public Object run() {
						constructor.setAccessible(isAccessible);
						return null;
					}
				});
			}
		} else {
			return (T) constructor.newInstance(initArgs);
		}
	}
	
	/**
	 * 尽可能的实例化
	 * 
	 * @param clazz 类，不可以为空
	 * @param isOnlyPublic 是否只查找公有构造方法
	 * @return 实例对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class clazz)  {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz is null!");
		}
		Throwable t = null;
		try {
			return (T)clazz.newInstance();
		} catch (Throwable e) {
			t = e;
		}
		for (Constructor constructor : clazz.getConstructors()) {
			try {
				Class[] parameterTypes = constructor.getParameterTypes();
				if (parameterTypes.length == 0) {
					continue;
				}
				Object[] initargs = new Object[parameterTypes.length];
				for (int i = 0; i < initargs.length; i++) {
					initargs[i] = cast(null, parameterTypes[i]);
				}
				return (T)constructor.newInstance(initargs);
			} catch (Throwable ignore) {
				
			}
		}
		throw new IllegalArgumentException(t);
	}

	/**
	 * 查找field
	 * 
	 * @param clazz 类，不可以为空
	 * @param fieldName 字段名称，不可以为空
	 * @param isOnlyPublic 是否只查找公有的字段
	 * @return 字段对象
	 * @throws NoSuchFieldException 没有这个字段
	 */
	public static Field getField(Class clazz, String fieldName, boolean isOnlyPublic) throws NoSuchFieldException {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz is null!");
		}
		if (fieldName == null || fieldName.trim().length() == 0) {
			throw new IllegalArgumentException("fieldName is null!");
		}
		fieldName = fieldName.trim();
		// 支持多层查找
		String[] fields = fieldName.split("[.]");
		Field field = null;
		for (int i = 0; i < fields.length; i++) {
			if (clazz.isArray()) {
				clazz = clazz.getComponentType();
			}
			field = doGetField(clazz, fields[i], isOnlyPublic);
			if (field == null) {
				break;
			}
			clazz = field.getType();
		}
		if (field == null) {
			throw new NoSuchFieldException(fieldName);
		}
		return field;
	}

	private static Field doGetField(Class clazz, String fieldName, boolean isOnlyPublic) {
		for (Field field : clazz.getDeclaredFields()) {
			int modifiers = field.getModifiers();

			if (isOnlyPublic) {
				if (Modifier.isPublic(modifiers)) {
					if (field.getName().equals(fieldName)) {
						return field;
					}
				}
			} else {
				if (field.getName().equals(fieldName)) {
					return field;
				}
			}
		}
		if (clazz.getSuperclass() != null) {
			return doGetField(clazz.getSuperclass(), fieldName, isOnlyPublic);
		}
		return null;
	}

	/**
	 * 取得某个字段的值
	 * 
	 * @param clazz 类，不可以为空
	 * @param fieldName 字段名称，不可以为空
	 * @param isOnlyPublic 是否只查找公有方法
	 * @param target 目标值，如果是静态字段，可以为空，否则不可以为空
	 * @return 字段值
	 * @throws NoSuchFieldException 没有这个字段
	 * @throws IllegalAccessException 非法访问这个字段
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Class clazz, String fieldName, boolean isOnlyPublic, Object target) throws NoSuchFieldException, IllegalAccessException {
		final Field field = getField(clazz, fieldName, isOnlyPublic);
		if (!Modifier.isPublic(field.getModifiers())) {
			final boolean isAccessible = field.isAccessible();
			try {
				AccessController.doPrivileged(new PrivilegedAction() {
					public Object run() {
						field.setAccessible(true);
						return null;
					}
				});
				return (T) doGetFieldValue(clazz, field, target);
			} finally {
				AccessController.doPrivileged(new PrivilegedAction() {
					public Object run() {
						field.setAccessible(isAccessible);
						return null;
					}
				});
			}
		} else {
			return (T) doGetFieldValue(clazz, field, target);
		}
	}

	private static Object doGetFieldValue(Class clazz, Field field, Object target) throws IllegalAccessException {
		if (Modifier.isStatic(field.getModifiers())) {
			return field.get(null);
		} else {
			if (target == null) {
				try {
					target = newInstance(clazz, new Class[0], true, new Object[0]);
				} catch (Exception e) {
					throw new IllegalArgumentException("target is null!");
				}
			}
			return field.get(target);
		}
	}

	/**
	 * 调用某一个方法
	 * 
	 * @param clazz 类，不可以为空
	 * @param methodName 方法名称，不可以为空
	 * @param parameterTypes 类型信息，可以为空
	 * @param isOnlyPublic 是否只查找公有方法
	 * @param target 实例方法时有意义，可以为空
	 * @param args 调用参数
	 * @return
	 * @throws NoSuchMethodException 方法不存在
	 * @throws IllegalAccessException 非法访问
	 * @throws InvocationTargetException 方法调用异常
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Class clazz, String methodName, Class[] parameterTypes, boolean isOnlyPublic, Object target, Object[] args)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (args == null) {
			args = new Object[0];
		}
		if (parameterTypes == null) {
			parameterTypes = args2types(args);
		}
		final Method method = getMethod(clazz, methodName, parameterTypes, isOnlyPublic);
		if (!Modifier.isPublic(method.getModifiers())) {
			final boolean isAccessible = method.isAccessible();
			try {
				AccessController.doPrivileged(new PrivilegedAction() {
					public Object run() {
						method.setAccessible(true);
						return null;
					}
				});

				return (T) doInvokeMethod(clazz, method, target, args);
			} finally {
				AccessController.doPrivileged(new PrivilegedAction() {
					public Object run() {
						method.setAccessible(isAccessible);
						return null;
					}
				});
			}
		} else {
			return (T) doInvokeMethod(clazz, method, target, args);
		}
	}

	private static Object doInvokeMethod(Class clazz, Method method, Object target, Object[] args) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		if (Modifier.isStatic(method.getModifiers())) {
			return method.invoke(null, args);
		} else {
			if (target == null) {
				try {
					target = newInstance(clazz, new Class[0], true, new Object[0]);
				} catch (Exception e) {
					throw new IllegalArgumentException("target is null!");
				}
			}
			return method.invoke(target, args);
		}
	}

	/**
	 * 把obj强转化为指定的类型对象
	 * 
	 * @param value 对象，可以为空；为空时，可以拿到初始值
	 * @param toClass 转化的对象，可以为空
	 * @return 指定类型的对象
	 */
	public static <T> T cast(Object obj, Class<T> toClass) {
		return (T) cast(obj, toClass, null);
	}

	/**
	 * 把obj强转化为指定的类型对象
	 * 
	 * @param value 对象，可以为空；为空时，默认值返回
	 * @param toClass 转化的对象，可以为空
	 * @param defaultValue 默认值
	 * @return 指定类型的对象
	 */
	public static <T> T cast(Object obj, Class<T> toClass, T defaultValue) {
		return (T) TypeConvertorManager.getInstance().convert(obj, toClass, defaultValue, null);
	}

	/**
	 * 把src中的内容强制覆盖填充到dest
	 * 
	 * @param src 源对象
	 * @param dest 目标对象
	 * @return 覆盖后的目标
	 */
	public static <T> T override(Object src, T dest) {
		if (src == null || dest == null) {
			return dest;
		}

		return (T) TypeConvertorManager.getInstance().convert(src, dest.getClass(), null, dest);
	}
	
	/**
	 * 尽可能的设定值
	 * 
	 * @param obj 母对象
	 * @param name 字段名称
	 * @param value 字段值
	 */
	public static void setValue(final Object obj, String name, final Object value) {
		if (obj == null) {
			throw new IllegalArgumentException("obj is null!");
		}
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("name is null!");
		}
		try {
			if (obj instanceof Map) {
				((Map)obj).put(name, value);
			} else if (SdoUtil.isSdoType(obj.getClass())) {// sdo
				SdoUtil.setPropertyValue(obj, name, value);
			} else {
				final Field field = getField(obj.getClass(), name, false);
				AccessController.doPrivileged(new PrivilegedExceptionAction() {
					public Object run() throws Exception {
						boolean access = field.isAccessible();
						try {
							field.setAccessible(true);
							field.set(obj, cast(value, field.getType()));
							return null;
						} finally {
							field.setAccessible(access);
						}
					}
				});
			}
		} catch (Throwable e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * 对象深度克隆
	 * 
	 * @param value 对象，不可以为空
	 * @return 克隆后的对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deepClone(final Object value) {
		if (value == null || value instanceof String || value.getClass().isPrimitive()) {
			return (T)value;
		}
		try {
			if (value instanceof Collection) {
				Object ret = newInstance(value.getClass());
				for (Object e : (Collection)value) {
					((Collection)ret).add(deepClone(e));
				}
				return (T)ret;
			} else if (value instanceof Map) {
				Object ret = newInstance(value.getClass());
				for (Object key : ((Map)value).keySet()) {
					((Map)ret).put(deepClone(key), deepClone(((Map)value).get(key)));
				}
				return (T)ret;
			} else if (value.getClass().isArray()) {
				int length = Array.getLength(value);
				Object ret = Array.newInstance(value.getClass().getComponentType(), length);
				for (int i = 0; i < length; i++) {
					Array.set(ret, i, deepClone(Array.get(value, i)));
				}
				return (T)ret;
			} else if (SdoUtil.isSdoType(value.getClass())) {// sdo
				Object ret = SdoUtil.create(value.getClass());
				List propertyList = SdoUtil.getInstanceProperties(value);
				for (Object property : propertyList) {
					String propertyName = SdoUtil.getPropertyName(property);
					Object propertyValue = SdoUtil.getPropertyValue(value, propertyName);
					SdoUtil.setPropertyValue(ret, propertyName, deepClone(propertyValue));
				}
				return (T)ret;
			} else if (value instanceof Serializable) {
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				new ObjectOutputStream(byteOut).writeObject(value);
				ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
				return (T)new ObjectInputStream(byteIn).readObject();
			} else {
				final Object ret = newInstance(value.getClass());
				Map<String, Field> fieldMap = getAllField(value.getClass());
				for (String fieldName : fieldMap.keySet()) {
					final Field field = fieldMap.get(fieldName);
					if (Modifier.isFinal(field.getModifiers())) {
						continue;
					}
					AccessController.doPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws Exception {
							boolean access = field.isAccessible();
							try {
								field.setAccessible(true);
								field.set(ret, deepClone(field.get(value)));
								return null;
							} finally {
								field.setAccessible(access);
							}
						}
					});
				}
				return (T)ret;
			}
		} catch (Throwable e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	/**
	 * 尽可能判断是否相等，而不仅仅是==
	 * 
	 * @param value1
	 * @param value2
	 * @param isIgnoreType 是否忽略类型
	 * @return true：含有相同的内容
	 */
	@SuppressWarnings("unchecked")
	public static boolean equals(final Object value1, final Object value2, boolean isIgnoreType) {
		if (value1 == value2) {
			return true;
		}

		if (value1 == null || value2 == null) {
			return false;
		}

		Class clazz1 = value1.getClass();
		Class clazz2 = value2.getClass();

		// 如果不是同一种类型
		if (!(clazz1.isAssignableFrom(clazz2) || clazz2.isAssignableFrom(clazz1))) {
			if (isIgnoreType) {
				try {
					if (value1 instanceof String) {
						if (value2 instanceof Number) {
							return equals(cast(value1, double.class), value2, isIgnoreType);
						} else {
							return equals(cast(value1, clazz2), value2, isIgnoreType);
						}
					} else {
						if (value1 instanceof Number) {
							return equals(cast(value2, double.class), value1, isIgnoreType);
						} else {
							return equals(cast(value2, clazz1), value1, isIgnoreType);
						}
					}
				} catch (Exception e) {
					return false;
				}
			} else {
				return false;
			}
		}

		if (Number.class.isAssignableFrom(clazz1)) {
			return cast(value1, double.class).equals(cast(value2, double.class));
		} else if (clazz1.isPrimitive() || value1 instanceof String) {
			return String.valueOf(value1).equals(String.valueOf(value2));
		} else if (clazz1.isArray()) {
			if (Array.getLength(value1) != Array.getLength(value2)) {
				return false;
			}
			for (int i = 0; i < Array.getLength(value1); i++) {
				if (!equals(Array.get(value1, i), Array.get(value2, i), isIgnoreType)) {
					return false;
				}
			}
			return true;
		} else if (value1 instanceof Collection) {
			if (((Collection) value1).size() != ((Collection) value2).size()) {
				return false;
			}
			Object[] valueArray1 = ((Collection) value1).toArray();
			Object[] valueArray2 = ((Collection) value2).toArray();
			return equals(valueArray1, valueArray2, isIgnoreType);
		} else if (value1 instanceof Map) {
			if (((Map) value1).size() != ((Map) value2).size()) {
				return false;
			}
			for (Object key : ((Map) value1).keySet()) {
				if (!equals(((Map) value1).get(key), ((Map) value2).get(key), isIgnoreType)) {
					return false;
				}
			}
			return true;
		} else if (value1 instanceof InputStream || value1 instanceof OutputStream || value1 instanceof Date || value1 instanceof BigInteger
				|| value1 instanceof BigDecimal) {
			return value1.equals(value2);
		} else if (SdoUtil.isSdoType(clazz1)) {// sdo
			try {
				List propertyList1 = SdoUtil.getInstanceProperties(value1);
				List propertyList2 = SdoUtil.getInstanceProperties(value2);
				if (propertyList1.size() != propertyList2.size()) {
					return false;
				}
				for (Object property : propertyList1) {
					String name = SdoUtil.getPropertyName(property);
					Object propertValue1 = SdoUtil.getPropertyValue(value1, name);
					Object propertValue2 = SdoUtil.getPropertyValue(value2, name);
					if (!equals(propertValue1, propertValue2, isIgnoreType)) {
						return false;
					}
				}
				return true;
			} catch (Throwable e) {
				return false;
			}
		} else {// javabean
			try {
				Map<String, Field> fieldMap1 = getAllField(clazz1);

				Map<String, Field> fieldMap2 = getAllField(clazz2);

				if (fieldMap1.size() != fieldMap2.size()) {
					return false;
				}
				for (String fieldName : fieldMap1.keySet()) {
					final Field field1 = fieldMap1.get(fieldName);
					final Field field2 = fieldMap2.get(fieldName);
					Object fieldValue1 = AccessController.doPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws Exception {
							boolean access = field1.isAccessible();
							try {
								field1.setAccessible(true);
								return field1.get(value1);
							} finally {
								field1.setAccessible(access);
							}
						}
					});
					Object fieldValue2 = AccessController.doPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws Exception {
							boolean access = field1.isAccessible();
							try {
								field2.setAccessible(true);
								return field2.get(value2);
							} finally {
								field2.setAccessible(access);
							}
						}
					});
					if (!equals(fieldValue1, fieldValue2, isIgnoreType)) {
						return false;
					}
				}
				return true;
			} catch (Throwable t) {
				return false;
			}
		}
	}
	
	private static ConcurrentHashMap<Class, Map<String, Field>> classFieldMap = new ConcurrentHashMap<Class, Map<String, Field>>();
	
	/**
	 * 取得所有属性字段
	 * 
	 * @param clazz 类
	 * @return 所有属性字段
	 */
	public static Map<String, Field> getAllField(Class clazz) {
		if (clazz == null) {
			return null;
		}
		Map<String, Field> fieldMap = classFieldMap.get(clazz);
		if (fieldMap == null) {
			synchronized (ReflectUtil.class) {
				if (classFieldMap.get(clazz) == null) {
					fieldMap = new HashMap<String, Field>();
					doGetAllField(clazz, fieldMap);
					fieldMap = Collections.unmodifiableMap(fieldMap);
					classFieldMap.put(clazz, fieldMap);
				}
			}
		}
		return fieldMap;
	}

	private static void doGetAllField(Class clazz, Map<String, Field> fieldMap) {
		for (Field field : clazz.getDeclaredFields()) {
			if (fieldMap.containsKey(field.getName())) {
				continue;
			}
			int modifiers = field.getModifiers();
			//不包含常量（final static）
			if (Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) {
				continue;
			}
			fieldMap.put(field.getName(), field);
		}
		if (clazz.getSuperclass() != null) {
			doGetAllField(clazz.getSuperclass(), fieldMap);
		}
	}

	/**
	 * 按照某个字段排序
	 * 
	 * @param value 数组或者List（JavaBean或者sdo或者Map）
	 * @param fieldSort 排序字段，如果为空，则直接对value进行排序
	 * @param c 排序算法，如果为空，则使用默认方式排序
	 * @param isReverse 是否反转，比如从升序变为降序
	 */
	public static void sort(Object value, String fieldSort, Comparator c, boolean isReverse) {
		sort(value, new String[] {
			fieldSort
		}, new Comparator[] {
			c
		}, new boolean[] {
			isReverse
		});
	}

	/**
	 * 按照一些字段排序
	 * 
	 * @param value 数组或者List（JavaBean或者sdo或者Map）
	 * @param fieldSorts 排序字段集合，如果为空，则直接对value进行排序
	 * @param cs 排序算法集合，如果为空，则使用默认方式排序
	 * @param isReverses 是否反转集合，比如从升序变为降序
	 */
	public static void sort(Object value, String[] fieldSorts, Comparator[] cs, boolean[] isReverses) {
		if (value == null) {
			throw new IllegalArgumentException("value is null!");
		}
		if (fieldSorts == null || fieldSorts.length == 0) {
			fieldSorts = new String[] {
				null
			};
		}
		if (cs == null || cs.length == 0) {
			cs = new Comparator[] {
				null
			};
		}
		if (isReverses == null || isReverses.length == 0) {
			isReverses = new boolean[] {
				true
			};
		}
		if (fieldSorts.length != cs.length) {
			throw new IllegalArgumentException("FieldSort's length is not equal Comparator's length!");
		}
		if (fieldSorts.length != isReverses.length) {
			throw new IllegalArgumentException("FieldSort's length is not equal Reverse's length!");
		}

		Comparator comparator = getSortComparator(fieldSorts, cs, isReverses);

		if (value.getClass().isArray()) {
			Object[] valueArray = (Object[]) value;
			Arrays.sort(valueArray, comparator);
		} else if (value instanceof List) {
			Collections.sort((List) value, comparator);
		} else {
			throw new UnsupportedOperationException("The type of value is not supported sort:" + value.getClass());
		}
	}

	private static Comparator getSortComparator(final String[] fieldSorts, final Comparator[] cs, final boolean[] isReverses) {
		return new Comparator() {
			@SuppressWarnings("unchecked")
			public int compare(Object o1, Object o2) {
				int res = 0;
				for (int i = 0; i < fieldSorts.length; i++) {
					res = doCompare(o1, o2, fieldSorts[i], cs[i], isReverses[i]);
					if (res != 0) {
						break;
					}
				}
				return res;
			}

			private int doCompare(Object o1, Object o2, String fieldSort, Comparator c, boolean isReverse) {
				int res = doCompare(o1, o2, fieldSort, c);
				if (isReverse) {
					res = 0 - res;
				}
				return res;
			}

			private int doCompare(Object o1, Object o2, String fieldSort, Comparator c) {
				if (o1 == null) {
					return 1;
				}
				if (o2 == null) {
					return -1;
				}

				Object filedValue1 = getFieldValue(o1, fieldSort);
				Object filedValue2 = getFieldValue(o2, fieldSort);

				if (filedValue1 == null) {
					return 1;
				}
				if (filedValue2 == null) {
					return -1;
				}
				if (c != null) {
					return c.compare(filedValue1, filedValue2);
				}

				if (filedValue1 instanceof Comparable) {
					return ((Comparable) filedValue1).compareTo(filedValue2);
				} else if (filedValue1 instanceof Number) {
					double res = ((Number) filedValue1).doubleValue() - ((Number) filedValue2).doubleValue();
					if (res > 0) {
						return 1;
					} else if (res == 0) {
						return 0;
					} else {
						return -1;
					}
				}
				throw new UnsupportedOperationException("The type of value is not supported sort:" + filedValue1.getClass());
			}

		};
	}

	private static Object getFieldValue(Object value, String fieldName) {
		if (value == null || fieldName == null || fieldName.trim().length() == 0) {
			return value;
		} else if (value instanceof Map) {// Map
			return ((Map) value).get(fieldName);
		} else if (SdoUtil.isSdoType(value.getClass())) {// sdo
			try {
				return SdoUtil.getPropertyValue(value, fieldName);
			} catch (Throwable e) {
				throw new UnsupportedOperationException(e);
			}
		} else { // JavaBean
			try {
				return getFieldValue(value.getClass(), fieldName, false, value);
			} catch (Throwable e) {
				throw new UnsupportedOperationException(e);
			}
		}
	}

	public static final Map<Object, Class> primitiveWrapperTypeMap = new HashMap<Object, Class>(8);

	static {
		primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
		primitiveWrapperTypeMap.put(Byte.class, byte.class);
		primitiveWrapperTypeMap.put(Character.class, char.class);
		primitiveWrapperTypeMap.put(Double.class, double.class);
		primitiveWrapperTypeMap.put(Float.class, float.class);
		primitiveWrapperTypeMap.put(Integer.class, int.class);
		primitiveWrapperTypeMap.put(Long.class, long.class);
		primitiveWrapperTypeMap.put(Short.class, short.class);

		primitiveWrapperTypeMap.put(boolean.class.getName(), boolean.class);
		primitiveWrapperTypeMap.put(byte.class.getName(), byte.class);
		primitiveWrapperTypeMap.put(char.class.getName(), char.class);
		primitiveWrapperTypeMap.put(double.class.getName(), double.class);
		primitiveWrapperTypeMap.put(float.class.getName(), float.class);
		primitiveWrapperTypeMap.put(int.class.getName(), int.class);
		primitiveWrapperTypeMap.put(long.class.getName(), long.class);
		primitiveWrapperTypeMap.put(short.class.getName(), short.class);
		primitiveWrapperTypeMap.put(String.class.getName(), String.class);
		primitiveWrapperTypeMap.put("String", String.class);
		primitiveWrapperTypeMap.put("Object", Object.class);
		primitiveWrapperTypeMap.put(Object.class.getName(), Object.class);
		primitiveWrapperTypeMap.put(String[].class.getName(), String[].class);
	}

	private static Class[] args2types(Object[] args) {
		if (args == null || args.length == 0) {
			return new Class[0];
		}

		Class[] classes = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			Object object = args[i];
			if (object == null) {
				continue;
			} else {
				classes[i] = object.getClass();
				if (primitiveWrapperTypeMap.containsKey(classes[i])) {
					classes[i] = primitiveWrapperTypeMap.get(classes[i]);
				}
			}
		}

		return classes;
	}

	private static Constructor getConstructor(Class clazz, Class[] parameterTypes, boolean isOnlyPublic) throws NoSuchMethodException {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz is null!");
		}
		if (parameterTypes == null) {
			parameterTypes = new Class[0];
		}
		Constructor[] constructors = new Constructor[4];

		doGetConstructor(clazz, parameterTypes, constructors);

		if (constructors[0] != null) {
			return constructors[0];
		}
		if (isOnlyPublic) {
			if (constructors[1] != null) {
				return constructors[1];
			}
		} else {
			if (constructors[2] != null) {
				return constructors[2];
			}
			if (constructors[1] != null) {
				return constructors[1];
			}
			if (constructors[3] != null) {
				return constructors[3];
			}
		}

		throw new NoSuchMethodException(clazz.getName() + argumentTypesToString(".<init>", parameterTypes));

	}

	private static void doGetConstructor(Class clazz, Class[] parameterTypes, Constructor[] constructors) {
		for (Constructor constructor : clazz.getConstructors()) {
			if (Modifier.isPublic(constructor.getModifiers())) {
				// 精确匹配
				if (isMatchClasses(parameterTypes, constructor.getParameterTypes(), true)) {
					if (constructors[0] == null) {
						constructors[0] = constructor;
						break;
					}
				} else if (isMatchClasses(parameterTypes, constructor.getParameterTypes(), false)) {
					if (constructors[1] == null) {
						constructors[1] = constructor;
					}
				}
			} else {
				if (isMatchClasses(parameterTypes, constructor.getParameterTypes(), true)) {
					if (constructors[2] == null) {
						constructors[2] = constructor;
					}
				} else if (isMatchClasses(parameterTypes, constructor.getParameterTypes(), false)) {
					if (constructors[3] == null) {
						constructors[3] = constructor;
					}
				}
			}
		}
	}

	/**
	 * 查找某一个方法
	 * 
	 * @param clazz 类，不可以为空
	 * @param methodName 方法名称，不可以为空
	 * @param parameterTypes 类型信息，可以为空
	 * @param isOnlyPublic 是否只查找公有方法
	 * @return 方法对象
	 * @throws NoSuchMethodException 方法不存在
	 */
	public static Method getMethod(Class clazz, String methodName, Class[] parameterTypes, boolean isOnlyPublic) throws NoSuchMethodException {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz is null!");
		}
		if (methodName == null || methodName.trim().length() == 0) {
			throw new IllegalArgumentException("methodName is null!");
		}
		methodName = methodName.trim();
		if (parameterTypes == null) {
			parameterTypes = new Class[0];
		}

		Method[] methods = new Method[4];

		doGetMethod(clazz, methodName, parameterTypes, methods);

		if (methods[0] != null) {
			return methods[0];
		}
		if (isOnlyPublic) {
			if (methods[1] != null) {
				return methods[1];
			}
		} else {
			if (methods[2] != null) {
				return methods[2];
			}
			if (methods[1] != null) {
				return methods[1];
			}
			if (methods[3] != null) {
				return methods[3];
			}
		}

		throw new NoSuchMethodException(clazz.getName() + argumentTypesToString("." + methodName, parameterTypes));
	}

	private static void doGetMethod(Class clazz, String methodName, Class[] parameterTypes, Method[] methods) {
		for (Method meth : clazz.getDeclaredMethods()) {
			if (Modifier.isPublic(meth.getModifiers())) {
				if (meth.getName().equals(methodName)) {
					if (isMatchClasses(parameterTypes, meth.getParameterTypes(), true)) {
						if (methods[0] == null) {
							methods[0] = meth;
							break;
						}
					} else if (isMatchClasses(parameterTypes, meth.getParameterTypes(), false)) {
						if (methods[1] == null) {
							methods[1] = meth;
						}
					}
				}
			} else {
				if (meth.getName().equals(methodName)) {
					if (isMatchClasses(parameterTypes, meth.getParameterTypes(), true)) {
						if (methods[2] == null) {
							methods[2] = meth;
						}
					} else if (isMatchClasses(parameterTypes, meth.getParameterTypes(), false)) {
						if (methods[3] == null) {
							methods[3] = meth;
						}
					}
				}
			}
		}
		if (methods[0] == null) {
			if (clazz.getSuperclass() != null) {
				doGetMethod(clazz.getSuperclass(), methodName, parameterTypes, methods);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean isMatchClasses(Class[] parameterTypesSrc, Class[] parameterTypesDest, boolean isStrictly) {
		if (parameterTypesSrc == null) {
			parameterTypesSrc = new Class[0];
		}
		if (parameterTypesDest == null) {
			parameterTypesDest = new Class[0];
		}
		if (parameterTypesSrc.length != parameterTypesDest.length) {
			return false;
		}

		for (int i = 0; i < parameterTypesSrc.length; i++) {
			if (isStrictly) {
				if (parameterTypesDest[i] != parameterTypesSrc[i]) {
					return false;
				}
			} else {
				if (!isAssignable(parameterTypesSrc[i], parameterTypesDest[i])) {
					return false;
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static boolean isAssignable(Class srcClass, Class destClass) {
		// 如果为空，在非严格的情况下，认为是任意类型
		if (srcClass == null) {
			return true;
		}
		if (destClass == null) {
			return false;
		}
		if (srcClass == destClass) {
			return true;
		}
		if (srcClass.isPrimitive()) {
			if (destClass.isPrimitive() == false) {
				return false;
			}
			if (Integer.TYPE.equals(srcClass)) {
				return Long.TYPE.equals(destClass) || Float.TYPE.equals(destClass) || Double.TYPE.equals(destClass);
			}
			if (Long.TYPE.equals(srcClass)) {
				return Float.TYPE.equals(destClass) || Double.TYPE.equals(destClass);
			}
			if (Boolean.TYPE.equals(srcClass)) {
				return false;
			}
			if (Double.TYPE.equals(srcClass)) {
				return false;
			}
			if (Float.TYPE.equals(srcClass)) {
				return Double.TYPE.equals(destClass);
			}
			if (Character.TYPE.equals(srcClass)) {
				return Integer.TYPE.equals(destClass) || Long.TYPE.equals(destClass) || Float.TYPE.equals(destClass) || Double.TYPE.equals(destClass);
			}
			if (Short.TYPE.equals(srcClass)) {
				return Integer.TYPE.equals(destClass) || Long.TYPE.equals(destClass) || Float.TYPE.equals(destClass) || Double.TYPE.equals(destClass);
			}
			if (Byte.TYPE.equals(srcClass)) {
				return Short.TYPE.equals(destClass) || Integer.TYPE.equals(destClass) || Long.TYPE.equals(destClass) || Float.TYPE.equals(destClass)
						|| Double.TYPE.equals(destClass);
			}
			return false;
		}
		return destClass.isAssignableFrom(srcClass);
	}

	private static String argumentTypesToString(String methodName, Class[] argTypes) {
		StringBuilder buf = new StringBuilder();
		buf.append(methodName).append("(");
		if (argTypes != null) {
			for (int i = 0; i < argTypes.length; i++) {
				if (i > 0) {
					buf.append(", ");
				}
				Class c = argTypes[i];
				buf.append((c == null) ? "null" : c.getName());
			}
		}
		buf.append(")");
		return buf.toString();
	}
}