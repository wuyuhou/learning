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
package com.tools.utility.spi.json;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its
 * external form is a string wrapped in curly braces with colons between the
 * names and values, and commas between the values and names. The internal form
 * is an object having <code>get</code> and <code>opt</code> methods for
 * accessing the values by name, and <code>put</code> methods for adding or
 * replacing values by name. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the <code>JSONObject.NULL</code>
 * object. A JSONObject constructor can be used to convert an external form
 * JSON text into an internal form whose values can be retrieved with the
 * <code>get</code> and <code>opt</code> methods, or to convert values into a
 * JSON text using the <code>put</code> and <code>toString</code> methods.
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The <code>put</code> methods adds values to an object. For example, <pre>
 *     myString = new JSONObject().put("JSON", "Hello, World!").toString();</pre>
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules.
 * The constructors are more forgiving in the texts they will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 *     before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 *     quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 *     or single quote, and if they do not contain leading or trailing spaces,
 *     and if they do not contain any of these characters:
 *     <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 *     and if they are not the reserved words <code>true</code>,
 *     <code>false</code>, or <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as
 *     by <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small> as
 *     well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
 *     <code>0x-</code> <small>(hex)</small> prefix.</li>
 * </ul>
 * 
 * @author wuyuhou
 */
public class JSONObject {

	/**
	 * JSONObject.NULL is equivalent to the value that JavaScript calls null,
	 * whilst Java's null is equivalent to the value that JavaScript calls
	 * undefined.
	 */
	private static final class Null {

		/**
		 * There is only intended to be a single instance of the NULL object,
		 * so the clone method returns itself.
		 * @return     NULL.
		 */
		protected final Object clone() {
			return this;
		}

		/**
		 * A Null object is equal to the null value and to itself.
		 * @param object    An object to test for nullness.
		 * @return true if the object parameter is the JSONObject.NULL object
		 *  or null.
		 */
		public boolean equals(Object object) {
			return object == null || object == this;
		}

		/**
		 * Get the "null" string value.
		 * @return The string "null".
		 */
		public String toString() {
			return "null";
		}
	}

	/**
	 * The map where the JSONObject's properties are kept.
	 */
	private Map<String, Object> map;

	/**
	 * It is sometimes more convenient and less ambiguous to have a
	 * <code>NULL</code> object than to use Java's <code>null</code> value.
	 * <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>.
	 * <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
	 */
	public static final Object NULL = new Null();
	
	/**
	 * Construct an empty JSONObject.
	 */
	private JSONObject() {
		this.map = new HashMap<String, Object>();
	}

	/**
	 * Construct a JSONObject from a source JSON text string.
	 * This is the most commonly used JSONObject constructor.
	 * @param source    A string beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @exception JSONRuntimeException If there is a syntax error in the source
	 *  string or a duplicated key.
	 */
	public JSONObject(String source) throws JSONRuntimeException {
		this(new JSONTokener(source));
	}

	/**
	 * Construct a JSONObject from a JSONTokener.
	 * @param x A JSONTokener object containing the source string.
	 * @throws JSONRuntimeException If there is a syntax error in the source string
	 *  or a duplicated key.
	 */
	public JSONObject(JSONTokener x) throws JSONRuntimeException {
		this();
		char c;
		String key;

		if (x.nextClean() != '{') {
			throw x.syntaxError("A JSONObject text must begin with '{'");
		}
		for (;;) {
			c = x.nextClean();
			switch (c) {
				case 0:
					throw x.syntaxError("A JSONObject text must end with '}'");
				case '}':
					return;
				default:
					x.back();
					key = x.nextValue().toString();
			}

			/*
			 * The key is followed by ':'. We will also tolerate '=' or '=>'.
			 */

			c = x.nextClean();
			if (c == '=') {
				if (x.next() != '>') {
					x.back();
				}
			} else if (c != ':') {
				throw x.syntaxError("Expected a ':' after a key");
			}
			putOnce(key, x.nextValue());

			/*
			 * Pairs are separated by ','. We will also tolerate ';'.
			 */

			switch (x.nextClean()) {
				case ';':
				case ',':
					if (x.nextClean() == '}') {
						return;
					}
					x.back();
					break;
				case '}':
					return;
				default:
					throw x.syntaxError("Expected a ',' or '}'");
			}
		}
	}

	/**
	 * Put a key/value pair in the JSONObject. If the value is null,
	 * then the key will be removed from the JSONObject if it is present.
	 * @param key   A key string.
	 * @param value An object which is the value. It should be of one of these
	 *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
	 *  or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONRuntimeException If the value is non-finite number
	 *  or if the key is null.
	 */
	private void put(String key, Object value) throws JSONRuntimeException {
		if (key == null) {
			throw new JSONRuntimeException("Null key.");
		}
		if (value != null) {
			testValidity(value);
			this.map.put(key, value);
		} else {
			this.map.remove(key);
		}
	}

	/**
	 * Put a key/value pair in the JSONObject, but only if the key and the
	 * value are both non-null, and only if there is not already a member
	 * with that name.
	 * @param key
	 * @param value
	 * @return his.
	 * @throws JSONRuntimeException if the key is a duplicate
	 */
	private void putOnce(String key, Object value) throws JSONRuntimeException {
		if (key != null && value != null) {
			if (opt(key) != null) {
				throw new JSONRuntimeException("Duplicate key \"" + key + "\"");
			}
			put(key, value);
		}
	}

	/**
	 * Get the value object associated with a key.
	 *
	 * @param key   A key string.
	 * @return      The object associated with the key.
	 * @throws   JSONRuntimeException if the key is not found.
	 */
	public Object get(String key) throws JSONRuntimeException {
		Object o = opt(key);
		if (o == null) {
			throw new JSONRuntimeException("JSONObject[" + quote(key) + "] not found.");
		}
		return o;
	}

	/**
	 * Get the boolean value associated with a key.
	 *
	 * @param key   A key string.
	 * @return      The truth.
	 * @throws   JSONRuntimeException
	 *  if the value is not a Boolean or the String "true" or "false".
	 */
	public boolean getBoolean(String key) throws JSONRuntimeException {
		Object o = get(key);
		if (o.equals(Boolean.FALSE) || (o instanceof String && ((String) o).equalsIgnoreCase("false"))) {
			return false;
		} else if (o.equals(Boolean.TRUE) || (o instanceof String && ((String) o).equalsIgnoreCase("true"))) {
			return true;
		}
		throw new JSONRuntimeException("JSONObject[" + quote(key) + "] is not a Boolean.");
	}

	/**
	 * Get the double value associated with a key.
	 * @param key   A key string.
	 * @return      The numeric value.
	 * @throws JSONRuntimeException if the key is not found or
	 *  if the value is not a Number object and cannot be converted to a number.
	 */
	public double getDouble(String key) throws JSONRuntimeException {
		Object o = get(key);
		try {
			return o instanceof Number ? ((Number) o).doubleValue() : Double.valueOf((String) o).doubleValue();
		} catch (Exception e) {
			throw new JSONRuntimeException("JSONObject[" + quote(key) + "] is not a number.");
		}
	}

	/**
	 * Get the int value associated with a key. If the number value is too
	 * large for an int, it will be clipped.
	 *
	 * @param key   A key string.
	 * @return      The integer value.
	 * @throws   JSONRuntimeException if the key is not found or if the value cannot
	 *  be converted to an integer.
	 */
	public int getInt(String key) throws JSONRuntimeException {
		Object o = get(key);
		return o instanceof Number ? ((Number) o).intValue() : (int) getDouble(key);
	}

	/**
	 * Get the long value associated with a key. If the number value is too
	 * long for a long, it will be clipped.
	 *
	 * @param key   A key string.
	 * @return      The long value.
	 * @throws   JSONRuntimeException if the key is not found or if the value cannot
	 *  be converted to a long.
	 */
	public long getLong(String key) throws JSONRuntimeException {
		Object o = get(key);
		return o instanceof Number ? ((Number) o).longValue() : (long) getDouble(key);
	}

	/**
	 * Get the JSONArray value associated with a key.
	 *
	 * @param key   A key string.
	 * @return      A JSONArray which is the value.
	 * @throws   JSONRuntimeException if the key is not found or
	 *  if the value is not a JSONArray.
	 */
	public JSONArray getJSONArray(String key) throws JSONRuntimeException {
		Object o = get(key);
		if (o instanceof JSONArray) {
			return (JSONArray) o;
		}
		throw new JSONRuntimeException("JSONObject[" + quote(key) + "] is not a JSONArray.");
	}

	/**
	 * Get the JSONObject value associated with a key.
	 *
	 * @param key   A key string.
	 * @return      A JSONObject which is the value.
	 * @throws   JSONRuntimeException if the key is not found or
	 *  if the value is not a JSONObject.
	 */
	public JSONObject getJSONObject(String key) throws JSONRuntimeException {
		Object o = get(key);
		if (o instanceof JSONObject) {
			return (JSONObject) o;
		}
		throw new JSONRuntimeException("JSONObject[" + quote(key) + "] is not a JSONObject.");
	}

	/**
	 * Get an array of field names from a JSONObject.
	 *
	 * @return An array of field names, or null if there are no names.
	 */
	public static String[] getNames(JSONObject jo) {
		int length = jo.length();
		Iterator i = jo.keys();
		String[] names = new String[length];
		int j = 0;
		while (i.hasNext()) {
			names[j] = (String) i.next();
			j += 1;
		}
		return names;
	}

	/**
	 * Get an array of field names from an Object.
	 *
	 * @return An array of field names, or null if there are no names.
	 */
	public static String[] getNames(Object object) {
		if (object == null) {
			return null;
		}
		Class klass = object.getClass();
		Field[] fields = klass.getFields();
		int length = fields.length;
		String[] names = new String[length];
		for (int i = 0; i < length; i += 1) {
			names[i] = fields[i].getName();
		}
		return names;
	}

	/**
	 * Get the string associated with a key.
	 *
	 * @param key   A key string.
	 * @return      A string which is the value.
	 * @throws   JSONRuntimeException if the key is not found.
	 */
	public String getString(String key) throws JSONRuntimeException {
		return get(key).toString();
	}

	/**
	 * Determine if the JSONObject contains a specific key.
	 * @param key   A key string.
	 * @return      true if the key exists in the JSONObject.
	 */
	public boolean has(String key) {
		return this.map.containsKey(key);
	}

	/**
	 * Determine if the value associated with the key is null or if there is
	 *  no value.
	 * @param key   A key string.
	 * @return      true if there is no value associated with the key or if
	 *  the value is the JSONObject.NULL object.
	 */
	public boolean isNull(String key) {
		return JSONObject.NULL.equals(opt(key));
	}

	/**
	 * Get an enumeration of the keys of the JSONObject.
	 *
	 * @return An iterator of the keys.
	 */
	public Iterator keys() {
		return this.map.keySet().iterator();
	}

	/**
	 * Get the number of keys stored in the JSONObject.
	 *
	 * @return The number of keys in the JSONObject.
	 */
	public int length() {
		return this.map.size();
	}

	/**
	 * Get an optional value associated with a key.
	 * @param key   A key string.
	 * @return      An object which is the value, or null if there is no value.
	 */
	public Object opt(String key) {
		return key == null ? null : this.map.get(key);
	}

	/**
	 * Get an optional boolean associated with a key.
	 * It returns false if there is no such key, or if the value is not
	 * Boolean.TRUE or the String "true".
	 *
	 * @param key   A key string.
	 * @return      The truth.
	 */
	public boolean optBoolean(String key) {
		return optBoolean(key, false);
	}

	/**
	 * Get an optional boolean associated with a key.
	 * It returns the defaultValue if there is no such key, or if it is not
	 * a Boolean or the String "true" or "false" (case insensitive).
	 *
	 * @param key              A key string.
	 * @param defaultValue     The default.
	 * @return      The truth.
	 */
	public boolean optBoolean(String key, boolean defaultValue) {
		try {
			return getBoolean(key);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get an optional double associated with a key,
	 * or NaN if there is no such key or if its value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A string which is the key.
	 * @return      An object which is the value.
	 */
	public double optDouble(String key) {
		return optDouble(key, Double.NaN);
	}

	/**
	 * Get an optional double associated with a key, or the
	 * defaultValue if there is no such key or if its value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      An object which is the value.
	 */
	public double optDouble(String key, double defaultValue) {
		try {
			Object o = opt(key);
			return o instanceof Number ? ((Number) o).doubleValue() : new Double((String) o).doubleValue();
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get an optional int value associated with a key,
	 * or zero if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @return      An object which is the value.
	 */
	public int optInt(String key) {
		return optInt(key, 0);
	}

	/**
	 * Get an optional int value associated with a key,
	 * or the default if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      An object which is the value.
	 */
	public int optInt(String key, int defaultValue) {
		try {
			return getInt(key);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get an optional JSONArray associated with a key.
	 * It returns null if there is no such key, or if its value is not a
	 * JSONArray.
	 *
	 * @param key   A key string.
	 * @return      A JSONArray which is the value.
	 */
	public JSONArray optJSONArray(String key) {
		Object o = opt(key);
		return o instanceof JSONArray ? (JSONArray) o : null;
	}

	/**
	 * Get an optional JSONObject associated with a key.
	 * It returns null if there is no such key, or if its value is not a
	 * JSONObject.
	 *
	 * @param key   A key string.
	 * @return      A JSONObject which is the value.
	 */
	public JSONObject optJSONObject(String key) {
		Object o = opt(key);
		return o instanceof JSONObject ? (JSONObject) o : null;
	}

	/**
	 * Get an optional long value associated with a key,
	 * or zero if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @return      An object which is the value.
	 */
	public long optLong(String key) {
		return optLong(key, 0);
	}

	/**
	 * Get an optional long value associated with a key,
	 * or the default if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      An object which is the value.
	 */
	public long optLong(String key, long defaultValue) {
		try {
			return getLong(key);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get an optional string associated with a key.
	 * It returns an empty string if there is no such key. If the value is not
	 * a string and is not null, then it is coverted to a string.
	 *
	 * @param key   A key string.
	 * @return      A string which is the value.
	 */
	public String optString(String key) {
		return optString(key, "");
	}

	/**
	 * Get an optional string associated with a key.
	 * It returns the defaultValue if there is no such key.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      A string which is the value.
	 */
	public String optString(String key, String defaultValue) {
		Object o = opt(key);
		return o != null ? o.toString() : defaultValue;
	}

	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within </, allowing JSON
	 * text to be delivered in HTML. In JSON text, a string cannot contain a
	 * control character or an unescaped quote or backslash.
	 * @param string A String
	 * @return  A String correctly formatted for insertion in a JSON text.
	 */
	private static String quote(String string) {
		if (string == null || string.length() == 0) {
			return "\"\"";
		}

		char b;
		char c = 0;
		int i;
		int len = string.length();
		StringBuffer sb = new StringBuffer(len + 4);
		String t;

		sb.append('"');
		for (i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
				case '\\':
				case '"':
					sb.append('\\');
					sb.append(c);
					break;
				case '/':
					if (b == '<') {
						sb.append('\\');
					}
					sb.append(c);
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\r':
					sb.append("\\r");
					break;
				default:
					if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
						t = "000" + Integer.toHexString(c);
						sb.append("\\u" + t.substring(t.length() - 4));
					} else {
						sb.append(c);
					}
			}
		}
		sb.append('"');
		return sb.toString();
	}

	/**
	 * Get an enumeration of the keys of the JSONObject.
	 * The keys will be sorted alphabetically.
	 *
	 * @return An iterator of the keys.
	 */	
	public Iterator<String> sortedKeys() {
		return new TreeSet<String>(this.map.keySet()).iterator();
	}

	/**
	 * Throw an exception if the object is an NaN or infinite number.
	 * @param o The object to test.
	 * @throws JSONRuntimeException If o is a non-finite number.
	 */
	static void testValidity(Object o) throws JSONRuntimeException {
		if (o != null) {
			if (o instanceof Double) {
				if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
					throw new JSONRuntimeException("JSON does not allow non-finite numbers.");
				}
			} else if (o instanceof Float) {
				if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
					throw new JSONRuntimeException("JSON does not allow non-finite numbers.");
				}
			}
		}
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{");
		StringBuffer buftmp = new StringBuffer();
		for (String key : map.keySet()) {
			buftmp.append(",\"").append(key).append("\":");
			Object obj = map.get(key);
			if (obj == null) {
				buftmp.append("\"\"");
			} else if (obj instanceof Character || Character.TYPE.isAssignableFrom(obj.getClass()) || obj instanceof String) {
				buftmp.append(quote(String.valueOf(obj)));
			} else {
				buftmp.append(obj);
			}
		}
		if (buftmp.length() > 0) {
			buf.append(buftmp.substring(1));
		}
		buf.append("}");
		return buf.toString();
	}
}