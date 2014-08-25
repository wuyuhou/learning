package com.tools.db.impl;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;

public class OracleScript extends SqlScript {

	protected String getNextStatement(BufferedReader r) throws IOException {

		boolean isFinish = false;//�Ƿ���ɱ�־λ
		boolean inString = false;//�Ƿ����ַ�����־λ
		boolean isComment = false;//�Ƿ���ע�ͱ�ʾλ

		int i = 0;
		int count = 0;
		int lineLength = 0;//��ǰ���ַ�����

		String line = null;//SQL ���

		CharArrayWriter caw = new CharArrayWriter();//��� SQL ���

		//��Ե�ǰ�н��в���
		while (!isFinish) {
			line = r.readLine();

			if (line == null) {
				isFinish = true;
			} else {
				line = line.trim();
				lineLength = line.length();

				i = 0;

				while (i < lineLength) {
					//һ���ַ�һ���ַ��ļ��
					char c = line.charAt(i);

					//�����ע���еĻ�������Ƿ��� '*/'���еĻ�������2λ������ֻ��1λ�ַ�
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

					//�������ע���еĻ�������Ƿ��� '\' ��ͷ
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
