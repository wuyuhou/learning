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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 * 
 * @author wuyuhou
 */
public class JSONTokener {

	private int index;
	private Reader reader;
	private char lastChar;
	private boolean useLastChar;
	
	/**
	 * Construct a JSONTokener from a string.
	 *
	 * @param reader A reader.
	 */
	public JSONTokener(Reader reader) {
		this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
		this.useLastChar = false;
		this.index = 0;
	}

	/**
	 * Construct a JSONTokener from a string.
	 *
	 * @param s     A source string.
	 */
	public JSONTokener(String s) {
		this(new StringReader(s));
	}

	/**
	 * Back up one character. This provides a sort of lookahead capability,
	 * so that you can test for a digit or letter before attempting to parse
	 * the next number or identifier.
	 */
	public void back() throws JSONRuntimeException {
		if (useLastChar || index <= 0) {
			throw new JSONRuntimeException("Stepping back two steps is not supported");
		}
		index -= 1;
		useLastChar = true;
	}

	/**
	 * Get the hex value of a character (base16).
	 * @param c A character between '0' and '9' or between 'A' and 'F' or
	 * between 'a' and 'f'.
	 * @return  An int between 0 and 15, or -1 if c was not a hex digit.
	 */
	public static int dehexchar(char c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		}
		if (c >= 'A' && c <= 'F') {
			return c - ('A' - 10);
		}
		if (c >= 'a' && c <= 'f') {
			return c - ('a' - 10);
		}
		return -1;
	}

	/**
	 * Determine if the source string still contains characters that next()
	 * can consume.
	 * @return true if not yet at the end of the source.
	 */
	public boolean more() throws JSONRuntimeException {
		char nextChar = next();
		if (nextChar == 0) {
			return false;
		}
		back();
		return true;
	}

	/**
	 * Get the next character in the source string.
	 *
	 * @return The next character, or 0 if past the end of the source string.
	 */
	public char next() throws JSONRuntimeException {
		if (this.useLastChar) {
			this.useLastChar = false;
			if (this.lastChar != 0) {
				this.index += 1;
			}
			return this.lastChar;
		}
		int c;
		try {
			c = this.reader.read();
		} catch (IOException exc) {
			throw new JSONRuntimeException(exc);
		}

		if (c <= 0) { // End of stream
			this.lastChar = 0;
			return 0;
		}
		this.index += 1;
		this.lastChar = (char) c;
		return this.lastChar;
	}

	/**
	 * Consume the next character, and check that it matches a specified
	 * character.
	 * @param c The character to match.
	 * @return The character.
	 * @throws JSONRuntimeException if the character does not match.
	 */
	public char next(char c) throws JSONRuntimeException {
		char n = next();
		if (n != c) {
			throw syntaxError("Expected '" + c + "' and instead saw '" + n + "'");
		}
		return n;
	}

	/**
	 * Get the next n characters.
	 *
	 * @param n     The number of characters to take.
	 * @return      A string of n characters.
	 * @throws JSONRuntimeException
	 *   Substring bounds error if there are not
	 *   n characters remaining in the source string.
	 */
	public String next(int n) throws JSONRuntimeException {
		if (n == 0) {
			return "";
		}

		char[] buffer = new char[n];
		int pos = 0;

		if (this.useLastChar) {
			this.useLastChar = false;
			buffer[0] = this.lastChar;
			pos = 1;
		}

		try {
			int len;
			while ((pos < n) && ((len = reader.read(buffer, pos, n - pos)) != -1)) {
				pos += len;
			}
		} catch (IOException exc) {
			throw new JSONRuntimeException(exc);
		}
		this.index += pos;

		if (pos < n) {
			throw syntaxError("Substring bounds error");
		}

		this.lastChar = buffer[n - 1];
		return new String(buffer);
	}

	/**
	 * Get the next char in the string, skipping whitespace.
	 * @throws JSONRuntimeException
	 * @return  A character, or 0 if there are no more characters.
	 */
	public char nextClean() throws JSONRuntimeException {
		for (;;) {
			char c = next();
			if (c == 0 || c > ' ') {
				return c;
			}
		}
	}

	/**
	 * Return the characters up to the next close quote character.
	 * Backslash processing is done. The formal JSON format does not
	 * allow strings in single quotes, but an implementation is allowed to
	 * accept them.
	 * @param quote The quoting character, either
	 *      <code>"</code>&nbsp;<small>(double quote)</small> or
	 *      <code>'</code>&nbsp;<small>(single quote)</small>.
	 * @return      A String.
	 * @throws JSONRuntimeException Unterminated string.
	 */
	public String nextString(char quote) throws JSONRuntimeException {
		char c;
		StringBuffer sb = new StringBuffer();
		for (;;) {
			c = next();
			switch (c) {
				case 0:
				case '\n':
				case '\r':
					throw syntaxError("Unterminated string");
				case '\\':
					c = next();
					switch (c) {
						case 'b':
							sb.append('\b');
							break;
						case 't':
							sb.append('\t');
							break;
						case 'n':
							sb.append('\n');
							break;
						case 'f':
							sb.append('\f');
							break;
						case 'r':
							sb.append('\r');
							break;
						case 'u':
							sb.append((char) Integer.parseInt(next(4), 16));
							break;
						case '"':
						case '\'':
						case '\\':
						case '/':
							sb.append(c);
							break;
						default:
							throw syntaxError("Illegal escape.");
					}
					break;
				default:
					if (c == quote) {
						return sb.toString();
					}
					sb.append(c);
			}
		}
	}

	/**
	 * Get the text up but not including the specified character or the
	 * end of line, whichever comes first.
	 * @param  d A delimiter character.
	 * @return   A string.
	 */
	public String nextTo(char d) throws JSONRuntimeException {
		StringBuffer sb = new StringBuffer();
		for (;;) {
			char c = next();
			if (c == d || c == 0 || c == '\n' || c == '\r') {
				if (c != 0) {
					back();
				}
				return sb.toString().trim();
			}
			sb.append(c);
		}
	}

	/**
	 * Get the text up but not including one of the specified delimiter
	 * characters or the end of line, whichever comes first.
	 * @param delimiters A set of delimiter characters.
	 * @return A string, trimmed.
	 */
	public String nextTo(String delimiters) throws JSONRuntimeException {
		char c;
		StringBuffer sb = new StringBuffer();
		for (;;) {
			c = next();
			if (delimiters.indexOf(c) >= 0 || c == 0 || c == '\n' || c == '\r') {
				if (c != 0) {
					back();
				}
				return sb.toString().trim();
			}
			sb.append(c);
		}
	}

	/**
	 * Get the next value. The value can be a Boolean, Double, Integer,
	 * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
	 * @throws JSONRuntimeException If syntax error.
	 *
	 * @return An object.
	 */
	public Object nextValue() throws JSONRuntimeException {
		char c = nextClean();
		
		switch (c) {
			case '"':
			case '\'':
				return nextString(c);
			case '{':
				back();
				return new JSONObject(this);
			case '[':
			case '(':
				back();
				return new JSONArray(this);
		}

		/*
		 * Handle unquoted text. This could be the values true, false, or
		 * null, or it can be a number. An implementation (such as this one)
		 * is allowed to also accept non-standard forms.
		 *
		 * Accumulate characters until we reach the end of the text or a
		 * formatting character.
		 */

		StringBuffer sb = new StringBuffer();
		while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
			sb.append(c);
			c = next();
		}
		back();
		return stringToValue(sb.toString().trim());
	}

	/**
	 * Try to convert a string into a number, boolean, or null. If the string
	 * can't be converted, return the string.
	 * @param s A String.
	 * @return A simple JSON value.
	 */
	static Object stringToValue(String s) {
		if (s.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		}
		if (s.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		}
		if (s.equals("") || s.equalsIgnoreCase("null")) {
			return JSONObject.NULL;
		}

		/*
		 * If it might be a number, try converting it. We support the 0- and 0x-
		 * conventions. If a number cannot be produced, then the value will just
		 * be a string. Note that the 0-, 0x-, plus, and implied string
		 * conventions are non-standard. A JSON parser is free to accept
		 * non-JSON forms as long as it accepts all correct JSON forms.
		 */

		char b = s.charAt(0);
		if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
			if (b == '0') {
				if (s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
					try {
						return new Integer(Integer.parseInt(s.substring(2), 16));
					} catch (Exception e) {
						/* Ignore the error */
					}
				} else {
					try {
						return new Integer(Integer.parseInt(s, 8));
					} catch (Exception e) {
						/* Ignore the error */
					}
				}
			}
			try {
				if (s.indexOf('.') > -1 || s.indexOf('e') > -1 || s.indexOf('E') > -1) {
					return Double.valueOf(s);
				} else {
					Long myLong = new Long(s);
					if (myLong.longValue() == myLong.intValue()) {
						return new Integer(myLong.intValue());
					} else {
						return myLong;
					}
				}
			} catch (Exception f) {
				/* Ignore the error */
			}
		}
		return s;
	}

	/**
	 * Skip characters until the next character is the requested character.
	 * If the requested character is not found, no characters are skipped.
	 * @param to A character to skip to.
	 * @return The requested character, or zero if the requested character
	 * is not found.
	 */
	public char skipTo(char to) throws JSONRuntimeException {
		char c;
		try {
			int startIndex = this.index;
			reader.mark(Integer.MAX_VALUE);
			do {
				c = next();
				if (c == 0) {
					reader.reset();
					this.index = startIndex;
					return c;
				}
			} while (c != to);
		} catch (IOException exc) {
			throw new JSONRuntimeException(exc);
		}

		back();
		return c;
	}

	/**
	 * Make a JSONException to signal a syntax error.
	 *
	 * @param message The error message.
	 * @return  A JSONException object, suitable for throwing
	 */
	public JSONRuntimeException syntaxError(String message) {
		return new JSONRuntimeException(message + toString());
	}

	/**
	 * Make a printable string of this JSONTokener.
	 *
	 * @return " at character [this.index]"
	 */
	public String toString() {
		return " at character " + index;
	}
}