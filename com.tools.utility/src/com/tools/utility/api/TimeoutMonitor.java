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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 超时监控器
 * 
 * @author wuyuhou
 *
 */
public class TimeoutMonitor {
	
	/**
	 * 静态实例，便于使用
	 */
	public static TimeoutMonitor INSTANCE = new TimeoutMonitor();
	
	//超时时间
	private ConcurrentHashMap<Object, Long> timeoutMap = new ConcurrentHashMap<Object, Long>();
	
	//超时的起始计时时间
	private ConcurrentHashMap<Object, Long> beginTimeMap = new ConcurrentHashMap<Object, Long>();
	
	//超时触发策略
	private ConcurrentHashMap<Object, Runnable> timeoutRunableMap = new ConcurrentHashMap<Object, Runnable>();
	
	//超时监控线程
	private Thread timeoutMonitorThread = null;
	
	//是否启动
	private boolean isStarted = false;
	
	//是否停止监控
	private boolean isStopMonitor = false;
	
	//空闲间隔时间
	private int idle = 50;
	
	//超时策略执行线程池
	private ThreadPoolExecutor timeoutExecutor = null;
	
	public int getIdle() {
		return idle;
	}

	public void setIdle(int idle) {
		this.idle = idle;
	}

	public ThreadPoolExecutor getTimeoutExecutor() {
		return timeoutExecutor;
	}

	public void setTimeoutExecutor(ThreadPoolExecutor timeoutExecutor) {
		this.timeoutExecutor = timeoutExecutor;
	}

	/**
	 * 启动
	 */
	public void start() {
		if (isStarted) {
			return;
		}
		if (idle <= 0) {
			idle = 50;
		}
		if (timeoutExecutor == null) {
			timeoutExecutor = new ThreadPoolExecutor(5, 10, 60000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("timeout-run"));
		}
		isStopMonitor = false;
		timeoutMonitorThread = new Thread("timeoutMonitorThread") {
			@Override
			public void run() {
				while(true) {
					if (isStopMonitor) {
						break;
					}
					if (!timeoutMap.isEmpty()) {
						if (!beginTimeMap.isEmpty()) {
							for (Object key : timeoutMap.keySet().toArray()) {
								Long beginTime = beginTimeMap.get(key);
								Runnable runable = timeoutRunableMap.get(key);
								if (beginTime != null && runable != null) {
									long timeout = timeoutMap.get(key);
									//超时
									if (System.currentTimeMillis() - beginTime > timeout) {
										//触发后删除超时监控
										beginTimeMap.remove(key);
										timeout(runable);
									}
								}
							}
						}
					}
					try {
						Thread.sleep(idle);
					} catch (InterruptedException ignore) {
					}
				}
			}
		};
		timeoutMonitorThread.start();
		isStarted = true;
	}
	
	private void timeout(final Runnable runable) {
		if (runable == null) {
			return;
		}
		if (timeoutExecutor == null) {
			runable.run();
		} else {
			timeoutExecutor.execute(runable);
		}		
	}
	
	/**
	 * 停止
	 */
	public void stop() {
		if (!isStarted) {
			return;
		}
		isStopMonitor = true;
		if (timeoutExecutor != null) {
			timeoutExecutor.shutdown();
			timeoutExecutor = null;
		}
		if (timeoutMonitorThread != null) {
			timeoutMonitorThread.interrupt();
			timeoutMonitorThread = null;
		}
		isStarted = false;
	}
	
	/**
	 * 设置超时对象监控
	 * 
	 * @param obj 超时对象
	 * @param timeout 超时时间
	 * @param run 超时后的触发动作
	 */
	public void putTimeoutObject(Object obj, long timeout, Runnable run) {
		if (obj == null) {
			throw new IllegalArgumentException("obj is null!");
		}
		if (timeout <= 0) {
			throw new IllegalArgumentException("timeout cannot less than zero!");
		}
		if (run == null) {
			throw new IllegalArgumentException("runnable is null!");
		}
		timeoutMap.put(obj, timeout);
		timeoutRunableMap.put(obj, run);
	}
	
	/**
	 * 删除超时对象监控
	 * 
	 * @param obj 超时对象
	 */
	public void removeTimeoutObject(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("obj is null!");
		}
		timeoutMap.remove(obj);
		beginTimeMap.remove(obj);
		timeoutRunableMap.remove(obj);
	}
	
	/**
	 * 设置超时对象起始监控时间
	 * 
	 * @param obj 超时对象
	 * @param beginTime 起始时间
	 */
	public void setBeginTime(Object obj, long beginTime) {
		if (obj == null) {
			throw new IllegalArgumentException("obj is null!");
		}
		if (beginTime <= 0) {
			throw new IllegalArgumentException("beginTime cannot less than zero!");
		}
		beginTimeMap.put(obj, beginTime);
	}
}
