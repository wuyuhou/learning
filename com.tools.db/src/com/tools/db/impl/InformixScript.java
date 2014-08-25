package com.tools.db.impl;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;

public class InformixScript extends SqlScript {

	protected String getNextStatement(BufferedReader r) throws IOException {

		boolean isFinish = false;
		boolean inString = false;
		boolean isComment = false;

		int i = 0;
		int count = 0;
		int countOfBracket = 0;
		int lineLength = 0;

		String line = null;

		CharArrayWriter caw = new CharArrayWriter();

		while (!isFinish) {
			line = r.readLine();
			if (line == null) {
				isFinish = true;
			} else {
				line.trim(); // trim the line;
				lineLength = line.length();
				i = 0;
				while (i < lineLength) {
					char c = line.charAt(i);
					/** comment block process */
					if (isComment) {
						if (c == '*' && i < (lineLength - 1)
								&& line.charAt(i + 1) == '/') {
							isComment = false;
							i += 2;
						} else {
							i++;
						}
						continue;
					}

					/* first check if a string begins */
					if (c == '\'') {
						caw.write(c);
						if (inString) {
							count++;
							if (count >= 2) {
								count = 0;
							}
						} else {
							inString = true;
							count = 0;
						}
					} else {
						if (inString && count == 1) {
							inString = false;
						}
						if (!inString) {
							// check special characters:'/',';','-','(',')';
							if (c == '/') { // end of sql statement;
								if (i == (lineLength - 1)) {
									isFinish = true;
									break;
								} else {
									if (line.charAt(i + 1) == '*') {
										isComment = true;
									}
								}
							} else if (c == ';') {
								/**
								 * check if the statement is closed in the view
								 * of bracket;
								 */
								if (countOfBracket == 0) {
									isFinish = true;
									break;
								} else {
									caw.write(c);
								}
							} else if (c == '-') {
								// check if single line comment;
								if (i < (lineLength - 1)
										&& line.charAt(i + 1) == '-') {
									break;
								}

								// Add By Kevin Yung At 2008-03-20 Start
								caw.write(c);
								// Add By Kevin Yung At 2008-03-20 End
							} else if (c == '(') {
								countOfBracket++;
								caw.write(c);
							} else if (c == ')') {
								countOfBracket--;
								caw.write(c);
							} else {
								caw.write(c);
							}
						} else {
							caw.write(c);
						}
					}
					i++;
				}
			}
			caw.write(' ');
		}

		if (line == null) {
			return null;
		}

		if (caw.size() < 1) {
			return new String("");
		} else {
			return caw.toString();
		}
	}

}
