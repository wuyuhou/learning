package com.tools.db.impl;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;

public class OracleScript extends SqlScript {

	protected String getNextStatement(BufferedReader r) throws IOException {

		boolean isFinish = false;//是否完成标志位
		boolean inString = false;//是否是字符串标志位
		boolean isComment = false;//是否是注释表示位

		int i = 0;
		int count = 0;
		int lineLength = 0;//当前行字符长度

		String line = null;//SQL 语句

		CharArrayWriter caw = new CharArrayWriter();//存放 SQL 语句

		//针对当前行进行操作
		while (!isFinish) {
			line = r.readLine();

			if (line == null) {
				isFinish = true;
			} else {
				line = line.trim();
				lineLength = line.length();

				i = 0;

				while (i < lineLength) {
					//一个字符一个字符的检查
					char c = line.charAt(i);

					//如果是注释行的话，检查是否有 '*/'，有的话就跳过2位，否则只跳1位字符
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

					//如果不是注释行的话，检查是否以 '\' 开头
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
								isFinish = true;
								break;
							} else if (c == '-') {
								// check if single line comment;
								if (i < (lineLength - 1)
										&& line.charAt(i + 1) == '-') {
									break;
								}
								
								//Add By Kevin Yung At 2008-03-20 Start
								caw.write(c);
								//Add By Kevin Yung At 2008-03-20 End
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
