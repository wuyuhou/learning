
package com.tools.db.api;

import java.io.InputStream;
import java.sql.Connection;

/**
 * ���ݿ��ʼ���ӿ�
 *
 * @author yourname (mailto:yourname@primeton.com)
 */
public interface IDatabaseInitializer {
	/**
	 * ��ʼ��
	 * 
	 * @param connection ����
	 * @param sqlScriptIn sql�ű���
	 * param encoding sql�ű�����
	 * @return ��ʼ����������Ϊ�գ����ʼ���ɹ�������Ϊ������Ϣ
	 */
	String initialize(Connection connection, InputStream sqlScriptIn, String encoding);
}
/*
 * 
 * $Log: IDatabaseInitializer.java,v $
 * Revision 1.1  2012/08/14 08:34:08  wuyh
 * Update:�������ݿ��ʼ������
 *
 * 
 */
