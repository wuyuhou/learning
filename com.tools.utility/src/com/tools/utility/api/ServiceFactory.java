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
package com.tools.utility.api;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务工厂类（根据配置）
 *
 * @author wuyuhou
 *
 */
public class ServiceFactory {
	
	private static ServiceFactory INSTANCE = null;
	
	private ConcurrentHashMap<Class, Object> serviceMap = new ConcurrentHashMap<Class, Object>();
	
	public static ServiceFactory getInstance() {
		if (INSTANCE == null) {
			synchronized(ServiceFactory.class) {
				if (INSTANCE == null) {
					INSTANCE = new ServiceFactory();
				}
			}
		}
		return INSTANCE;
	}
	
	/**
	 * 取得服务实例
	 * 
	 * @param serviceClass 服务类型
	 * @return 服务实例
	 */
	@SuppressWarnings("unchecked")
	public <S> S getService(Class<S> serviceClass) {
		if (serviceClass == null) {
			throw new IllegalArgumentException("serviceClass is null");
		}
		if (serviceMap.get(serviceClass) == null) {
			synchronized(ServiceFactory.class) {
				if (serviceMap.get(serviceClass) == null) {
					ServiceExtensionLoader<S> loader = ServiceExtensionLoader.load(serviceClass);
					List<S> serviceList = loader.getExtensions();
					if (serviceList.size() > 0) {
						serviceMap.put(serviceClass, serviceList.get(0));
					}
				}
			}
		}
		return (S)serviceMap.get(serviceClass);
	}
	
	/**
	 * 删除某一个服务
	 * 
	 * @param serviceClass
	 */
	public void removeService(Class serviceClass) {
		if (serviceClass == null) {
			return;
		}
		serviceMap.remove(serviceClass);
	}
	
	/**
	 * 清空所有服务
	 */
	public void clearServices() {
		serviceMap.clear();
	}

}