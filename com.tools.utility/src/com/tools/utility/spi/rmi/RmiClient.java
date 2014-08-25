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
package com.tools.utility.spi.rmi;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.tools.utility.impl.rmi.IRemoteObjectWrapper;
import com.tools.utility.impl.rmi.RemoteInvocationHandler;

/**
 * rmi服务客户端
 * 
 * @author wuyuhou
 *
 */
public class RmiClient {
	/**
	 * 取得远程发布的对象
	 * 
	 * @param serviceUrl a name in URL format (without the scheme component)  , rmi://HOST:1199/AccountService
	 * @param remoteObjectInterface 远程对象接口
     * @return a reference for a remote object
     * @exception NotBoundException if name is not currently bound
     * @exception RemoteException if registry could not be contacted
     * @exception AccessException if this operation is not permitted
     * @exception MalformedURLException if the name is not an appropriately
     *  formatted URL
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getRemoteObject(String serviceUrl, Class<T> remoteObjectInterface) throws MalformedURLException, RemoteException, NotBoundException {
		if (serviceUrl == null || serviceUrl.trim().length() == 0) {
			throw new IllegalArgumentException("serviceUrl is null!");
		}
		if (remoteObjectInterface == null) {
			throw new IllegalArgumentException("remoteObjectInterface is null!");
		}
		if (!remoteObjectInterface.isInterface()) {
			throw new IllegalArgumentException("Not interface:" + remoteObjectInterface.getName());
		}
		Remote target = Naming.lookup(serviceUrl);
		T remoteObject = null;
		if (target instanceof IRemoteObjectWrapper && remoteObjectInterface != null) {
			remoteObject = (T)Proxy.newProxyInstance(remoteObjectInterface.getClassLoader(),
					new Class[]{remoteObjectInterface}, new RemoteInvocationHandler((IRemoteObjectWrapper)target));
		} else {
			remoteObject = (T)target;
		}
		return remoteObject;
	}
	
	/**
	 * 取得远程发布的对象
	 * 
	 * @param host rmi主机名
	 * @param port rmi端口
	 * @param remoteObjectName 远程对象名称
	 * @param remoteObjectInterface 远程对象接口
     * @return a reference for a remote object
     * @exception NotBoundException if name is not currently bound
     * @exception RemoteException if registry could not be contacted
     * @exception AccessException if this operation is not permitted
     * @exception MalformedURLException if the name is not an appropriately
     *  formatted URL
	 */
	public static <T> T getRemoteObject(String host, int port, String remoteObjectName, Class<T> remoteObjectInterface) throws MalformedURLException, RemoteException, NotBoundException {
		if (host == null || host.trim().length() == 0) {
			throw new IllegalArgumentException("host is null!");
		}
		if (port <= 0) {
			throw new IllegalArgumentException("port'" + port + "' is error!");
		}
		if (remoteObjectName == null || remoteObjectName.trim().length() == 0) {
			throw new IllegalArgumentException("remoteObjectName is null!");
		}
		StringBuilder buf = new StringBuilder();
		buf.append("rmi://").append(host).append(":").append(port);
		buf.append("/").append(remoteObjectName);
		return getRemoteObject(buf.toString(), remoteObjectInterface);
	}
}
