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

import java.util.ArrayList;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 *     before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there
 *     is <code>,</code>&nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 *     quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 *     or single quote, and if they do not contain leading or trailing spaces,
 *     and if they do not contain any of these characters:
 *     <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 *     and if they are not the reserved words <code>true</code>,
 *     <code>false</code>, or <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as
 *     well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
 *     <code>0x-</code> <small>(hex)</small> prefix.</li>
 * </ul>
 * 
 * @author wuyuhou
 */
public class JSONArray {

	/**
	 * The arrayList where the JSONArray's properties are kept.
	 */
	private ArrayList myArrayList;

	/**
	 * Construct an empty JSONArray.
	 */
	private JSONArray() {
		this.myArrayList = new ArrayList();
	}

	/**
	 * Construct a JSONArray from a source JSON text.
	 * @param source     A string that begins with
	 * <code>[</code>&nbsp;<small>(left bracket)</small>
	 *  and ends with <code>]</code>&nbsp;<small>(right bracket)</small>.
	 *  @throws JSONRuntimeException If there is a syntax error.
	 */
	public JSONArray(String source) throws JSONRuntimeException {
		this(new JSONTokener(source));
	}

	/**
	 * Construct a JSONArray from a JSONTokener.
	 * @param x A JSONTokener
	 * @throws JSONRuntimeException If there is a syntax error.
	 */
	@SuppressWarnings("unchecked")
	public JSONArray(JSONTokener x) throws JSONRuntimeException {
		this();
		char c = x.nextClean();
		char q;
		if (c == '[') {
			q = ']';
		} else if (c == '(') {
			q = ')';
		} else {
			throw x.syntaxError("A JSONArray text must start with '['");
		}
		if (x.nextClean() == ']') {
			return;
		}
		x.back();
		for (;;) {
			if (x.nextClean() == ',') {
				x.back();
				this.myArrayList.add(null);
			} else {
				x.back();
				this.myArrayList.add(x.nextValue());
			}
			c = x.nextClean();
			switch (c) {
				case ';':
				case ',':
					if (x.nextClean() == ']') {
						return;
					}
					x.back();
					break;
				case ']':
				case ')':
					if (q != c) {
						throw x.syntaxError("Expected a '" + new Character(q) + "'");
					}
					return;
				default:
					throw x.syntaxError("Expected a ',' or ']'");
			}
		}
	}

	/**
	 * Get the object value associated with an index.
	 * @param index
	 *  The index must be between 0 and length() - 1.
	 * @return An object value.
	 * @throws JSONRuntimeException If there is no value for the index.
	 */
	public Object get(int index) throws JSONRuntimeException {
		Object o = opt(index);
		if (o == null) {
			throw new JSONRuntimeException("JSONArray[" + index + "] not found.");
		}
		return o;
	}

	/**
	 * Get the boolean value associated with an index.
	 * The string values "true" and "false" are converted to boolean.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      The truth.
	 * @throws JSONRuntimeException If there is no value for the index or if the
	 *  value is not convertable to boolean.
	 */
	public boolean getBoolean(int index) throws JSONRuntimeException {
		Object o = get(index);
		if (o.equals(Boolean.FALSE) || (o instanceof String && ((String) o).equalsIgnoreCase("false"))) {
			return false;
		} else if (o.equals(Boolean.TRUE) || (o instanceof String && ((String) o).equalsIgnoreCase("true"))) {
			return true;
		}
		throw new JSONRuntimeException("JSONArray[" + index + "] is not a Boolean.");
	}

	/**
	 * Get the double value associated with an index.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      The value.
	 * @throws   JSONRuntimeException If the key is not found or if the value cannot
	 *  be converted to a number.
	 */
	public double getDouble(int index) throws JSONRuntimeException {
		Object o = get(index);
		try {
			return o instanceof Number ? ((Number) o).doubleValue() : Double.valueOf((String) o).doubleValue();
		} catch (Exception e) {
			throw new JSONRuntimeException("JSONArray[" + index + "] is not a number.");
		}
	}

	/**
	 * Get the int value associated with an index.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      The value.
	 * @throws   JSONRuntimeException If the key is not found or if the value cannot
	 *  be converted to a number.
	 *  if the value cannot be converted to a number.
	 */
	public int getInt(int index) throws JSONRuntimeException {
		Object o = get(index);
		return o instanceof Number ? ((Number) o).intValue() : (int) getDouble(index);
	}

	/**
	 * Get the JSONArray associated with an index.
	 * @param index The index must be between 0 and length() - 1.
	 * @return      A JSONArray value.
	 * @throws JSONRuntimeException If there is no value for the index. or if the
	 * value is not a JSONArray
	 */
	public JSONArray getJSONArray(int index) throws JSONRuntimeException {
		Object o = get(index);
		if (o instanceof JSONArray) {
			return (JSONArray) o;
		}
		throw new JSONRuntimeException("JSONArray[" + index + "] is not a JSONArray.");
	}

	/**
	 * Get the JSONObject associated with an index.
	 * @param index subscript
	 * @return      A JSONObject value.
	 * @throws JSONRuntimeException If there is no value for the index or if the
	 * value is not a JSONObject
	 */
	public JSONObject getJSONObject(int index) throws JSONRuntimeException {
		Object o = get(index);
		if (o instanceof JSONObject) {
			return (JSONObject) o;
		}
		throw new JSONRuntimeException("JSONArray[" + index + "] is not a JSONObject.");
	}

	/**
	 * Get the long value associated with an index.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      The value.
	 * @throws   JSONRuntimeException If the key is not found or if the value cannot
	 *  be converted to a number.
	 */
	public long getLong(int index) throws JSONRuntimeException {
		Object o = get(index);
		return o instanceof Number ? ((Number) o).longValue() : (long) getDouble(index);
	}

	/**
	 * Get the string associated with an index.
	 * @param index The index must be between 0 and length() - 1.
	 * @return      A string value.
	 * @throws JSONRuntimeException If there is no value for the index.
	 */
	public String getString(int index) throws JSONRuntimeException {
		return get(index).toString();
	}

	/**
	 * Determine if the value is null.
	 * @param index The index must be between 0 and length() - 1.
	 * @return true if the value at the index is null, or if there is no value.
	 */
	public boolean isNull(int index) {
		return JSONObject.NULL.equals(opt(index));
	}

	/**
	 * Get the number of elements in the JSONArray, included nulls.
	 *
	 * @return The length (or size).
	 */
	public int length() {
		return this.myArrayList.size();
	}

	/**
	 * Get the optional object value associated with an index.
	 * @param index The index must be between 0 and length() - 1.
	 * @return      An object value, or null if there is no
	 *              object at that index.
	 */
	public Object opt(int index) {
		return (index < 0 || index >= length()) ? null : this.myArrayList.get(index);
	}

	/**
	 * Get the optional boolean value associated with an index.
	 * It returns false if there is no value at that index,
	 * or if the value is not Boolean.TRUE or the String "true".
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      The truth.
	 */
	public boolean optBoolean(int index) {
		return optBoolean(index, false);
	}

	/**
	 * Get the optional boolean value associated with an index.
	 * It returns the defaultValue if there is no value at that index or if
	 * it is not a Boolean or the String "true" or "false" (case insensitive).
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @param defaultValue     A boolean default.
	 * @return      The truth.
	 */
	public boolean optBoolean(int index, boolean defaultValue) {
		try {
			return getBoolean(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional double value associated with an index.
	 * NaN is returned if there is no value for the index,
	 * or if the value is not a number and cannot be converted to a number.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      The value.
	 */
	public double optDouble(int index) {
		return optDouble(index, Double.NaN);
	}

	/**
	 * Get the optional double value associated with an index.
	 * The defaultValue is returned if there is no value for the index,
	 * or if the value is not a number and cannot be converted to a number.
	 *
	 * @param index subscript
	 * @param defaultValue     The default value.
	 * @return      The value.
	 */
	public double optDouble(int index, double defaultValue) {
		try {
			return getDouble(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional int value associated with an index.
	 * Zero is returned if there is no value for the index,
	 * or if the value is not a number and cannot be converted to a number.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      The value.
	 */
	public int optInt(int index) {
		return optInt(index, 0);
	}

	/**
	 * Get the optional int value associated with an index.
	 * The defaultValue is returned if there is no value for the index,
	 * or if the value is not a number and cannot be converted to a number.
	 * @param index The index must be between 0 and length() - 1.
	 * @param defaultValue     The default value.
	 * @return      The value.
	 */
	public int optInt(int index, int defaultValue) {
		try {
			return getInt(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional JSONArray associated with an index.
	 * @param index subscript
	 * @return      A JSONArray value, or null if the index has no value,
	 * or if the value is not a JSONArray.
	 */
	public JSONArray optJSONArray(int index) {
		Object o = opt(index);
		return o instanceof JSONArray ? (JSONArray) o : null;
	}

	/**
	 * Get the optional JSONObject associated with an index.
	 * Null is returned if the key is not found, or null if the index has
	 * no value, or if the value is not a JSONObject.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      A JSONObject value.
	 */
	public JSONObject optJSONObject(int index) {
		Object o = opt(index);
		return o instanceof JSONObject ? (JSONObject) o : null;
	}

	/**
	 * Get the optional long value associated with an index.
	 * Zero is returned if there is no value for the index,
	 * or if the value is not a number and cannot be converted to a number.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      The value.
	 */
	public long optLong(int index) {
		return optLong(index, 0);
	}

	/**
	 * Get the optional long value associated with an index.
	 * The defaultValue is returned if there is no value for the index,
	 * or if the value is not a number and cannot be converted to a number.
	 * @param index The index must be between 0 and length() - 1.
	 * @param defaultValue     The default value.
	 * @return      The value.
	 */
	public long optLong(int index, long defaultValue) {
		try {
			return getLong(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional string value associated with an index. It returns an
	 * empty string if there is no value at that index. If the value
	 * is not a string and is not null, then it is coverted to a string.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      A String value.
	 */
	public String optString(int index) {
		return optString(index, "");
	}

	/**
	 * Get the optional string associated with an index.
	 * The defaultValue is returned if the key is not found.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @param defaultValue     The default value.
	 * @return      A String value.
	 */
	public String optString(int index, String defaultValue) {
		Object o = opt(index);
		return o != null ? o.toString() : defaultValue;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		StringBuffer buftmp = new StringBuffer();
		for (int i = 0; i < myArrayList.size(); i++) {
			buftmp.append(",").append(myArrayList.get(i));
		}
		if (buftmp.length() > 0) {
			buf.append(buftmp.substring(1));
		}
		buf.append("]");
		return buf.toString();
	}
}