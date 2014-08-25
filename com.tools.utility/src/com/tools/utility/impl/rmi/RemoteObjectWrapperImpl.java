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

import java.rmi.RemoteException;

/**
 * remote对象包裹实现
 * 
 * @author wuyuhou
 *
 */
public class RemoteObjectWrapperImpl implements IRemoteObjectWrapper {
	
	private static final long serialVersionUID = 3100547696763659024L;
	
	private Object target;
	
	private Class targetClass;
	
	public RemoteObjectWrapperImpl(Object target) {
		if (target == null) {
			throw new IllegalArgumentException("target is null!");
		}
		this.target = target;
		this.targetClass = target.getClass();
	}
	
	public Object invoke(String methodName, Class[] types, Object[] args) throws RemoteException, Throwable {
		return targetClass.getMethod(methodName, types).invoke(target, args);
	}
}
