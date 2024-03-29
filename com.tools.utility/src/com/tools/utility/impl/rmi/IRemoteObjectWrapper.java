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
package com.tools.utility.impl.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * remote对象包裹
 * 
 * @author wuyuhou
 *
 */
public interface IRemoteObjectWrapper extends Remote {	
	public Object invoke(String methodName, Class[] types, Object[] args) throws RemoteException, Throwable;
}
