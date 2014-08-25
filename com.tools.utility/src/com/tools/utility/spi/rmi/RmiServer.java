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

import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import com.tools.logger.api.ILogger;
import com.tools.logger.api.LoggerFactory;
import com.tools.utility.impl.rmi.RemoteObjectWrapperImpl;

/**
 * RMI服务端
 * 
 * @author wuyuhou
 *
 */
public class RmiServer {

	private static ILogger logger = LoggerFactory.getLogger(RmiServer.class);

	private String host = null;

	/** Well known port 1099 for registry. */
	private int port = Registry.REGISTRY_PORT;

	private RMIClientSocketFactory clientSocketFactory;

	private RMIServerSocketFactory serverSocketFactory;

	private Registry registry;

	private boolean isStarted = false;

	private static Object lock = new Object();

	private static Map<String, RmiServer> rmiCache = new HashMap<String, RmiServer>();

	private Map<String, Remote> remoteObjectMap = new HashMap<String, Remote>();

	/**
	 * 构造方法
	 */
	private RmiServer() {
	}

	/**
	 * 取得rmi服务
	 *
	 * @param host rmi主机名
	 * @param port rmi端口
	 * @param serverSocketFactory
	 * @param clientSocketFactory
	 * @return
	 */
	public static RmiServer getRmiServer(String host, int port,
			RMIServerSocketFactory serverSocketFactory, RMIClientSocketFactory clientSocketFactory) {
		if (port <= 0) {
			throw new IllegalArgumentException("port'" + port + "' is error!");
		}
		String hostPort = String.valueOf(host).trim() + port;
		RmiServer rmi = rmiCache.get(hostPort);
		if (rmi == null) {
			synchronized (lock) {
				if (rmi == null) {
					rmi = new RmiServer();
					rmi.host = host;
					rmi.port = port;
					rmi.serverSocketFactory = serverSocketFactory;
					rmi.clientSocketFactory = clientSocketFactory;
					rmiCache.put(hostPort, rmi);
				}
			}
		}

		return rmi;
	}

	/**
	 * @return Returns the registry.
	 */
	public Registry getRegistry() {
		return registry;
	}

	/**
	 * 启动rmi服务
	 *
	 * @throws RemoteException
	 */
	public void start() throws RemoteException {
		if (isStarted) {
			return;
		}

		// Check socket factories for RMI registry.
		if (this.clientSocketFactory instanceof RMIServerSocketFactory) {
			this.serverSocketFactory = (RMIServerSocketFactory) this.clientSocketFactory;
		}
		if (this.clientSocketFactory == null && this.serverSocketFactory != null) {
			throw new IllegalArgumentException("RMIServerSocketFactory without RMIClientSocketFactory for registry not supported");
		}

		// Determine RMI registry to use.
		if (this.registry == null) {
			this.registry = getRegistry(this.host, this.port, this.clientSocketFactory, this.serverSocketFactory);
		}
		isStarted = true;
	}

	/**
	 * Locate or create the RMI registry for this exporter.
	 *
	 * @param registryHost the registry host to use (if this is specified, no
	 *            implicit creation of a RMI registry will happen)
	 * @param registryPort the registry port to use
	 * @param clientSocketFactory the RMI client socket factory for the registry (if any)
	 * @param serverSocketFactory the RMI server socket factory for the registry (if any)
	 * @return the RMI registry
	 * @throws RemoteException if the registry couldn't be located or created
	 */
	protected Registry getRegistry(final String registryHost, int registryPort, RMIClientSocketFactory clientSocketFactory,
			RMIServerSocketFactory serverSocketFactory) throws RemoteException {

		if (registryHost != null && registryHost.length() > 0) {
			AccessController.doPrivileged(new PrivilegedAction<String>() {
				public String run() {
					System.setProperty("java.rmi.server.hostname", registryHost);
					return null;
				}
			});
			// Host explictly specified: only lookup possible.
			logger.debug("Looking for RMI registry at port '" + registryPort + "' of host [" + registryHost + "]");

			Registry reg = LocateRegistry.getRegistry(registryHost, registryPort, clientSocketFactory);
			if (reg == null) {
				reg = LocateRegistry.createRegistry(registryPort, clientSocketFactory, serverSocketFactory);
				reg = LocateRegistry.getRegistry(registryHost, registryPort, clientSocketFactory);
				logger.debug("Not found in LocateRegistry, create new registry " + reg);
			}

			try {
				reg.list();
			} catch (Exception e) {
				reg = LocateRegistry.createRegistry(registryPort, clientSocketFactory, serverSocketFactory);

				logger.debug("Fails when test registry, create new registry " + reg);

				reg = LocateRegistry.getRegistry(registryHost, registryPort, clientSocketFactory);

				logger.debug("Looking for RMI registry in LocateResistry again ,found registry " + reg);
			}
			return reg;
		} else {
			return getRegistry(registryPort, clientSocketFactory, serverSocketFactory);
		}
	}

	/**
	 * Locate or create the RMI registry for this exporter.
	 *
	 * @param registryPort the registry port to use
	 * @param clientSocketFactory the RMI client socket factory for the registry
	 *            (if any)
	 * @param serverSocketFactory the RMI server socket factory for the registry
	 *            (if any)
	 * @return the RMI registry
	 * @throws RemoteException if the registry couldn't be located or created
	 */
	protected Registry getRegistry(int registryPort, RMIClientSocketFactory clientSocketFactory, RMIServerSocketFactory serverSocketFactory)
			throws RemoteException {

		if (clientSocketFactory != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Looking for RMI registry at port '" + registryPort + "', using custom socket factory");
			}
			try {
				// Retrieve existing registry.
				Registry reg = LocateRegistry.getRegistry(null, registryPort, clientSocketFactory);
				reg.list();
				return reg;
			} catch (RemoteException ex) {
				logger.debug("RMI registry access threw exception", ex);
				logger.warn("Could not detect RMI registry - creating new one");
				// Assume no registry found -> create new one.
				LocateRegistry.createRegistry(registryPort, clientSocketFactory, serverSocketFactory);

				return LocateRegistry.getRegistry(null, registryPort, clientSocketFactory); 
			}
		}

		else {
			return getRegistry(registryPort);
		}
	}

	/**
	 * Locate or create the RMI registry for this exporter.
	 *
	 * @param registryPort the registry port to use
	 * @return the RMI registry
	 * @throws RemoteException if the registry couldn't be located or created
	 */
	protected Registry getRegistry(int registryPort) throws RemoteException {
		if (logger.isInfoEnabled()) {
			logger.info("Looking for RMI registry at port '" + registryPort + "'");
		}
		try {
			// Retrieve existing registry.
			Registry reg = LocateRegistry.getRegistry(registryPort);
			reg.list();
			return reg;
		} catch (RemoteException ex) {
			// logger.debug("RMI registry access threw exception", ex);
			// logger.warn("Could not detect RMI registry - creating new one");
			// Assume no registry found -> create new one.
			LocateRegistry.createRegistry(registryPort);
			return LocateRegistry.getRegistry(registryPort); // bug 11590
		}
	}

	/**
	 * 关闭rmi服务
	 */
	public void stop() throws RemoteException {
		if (isStarted == false) {
			return;
		}
		// 关闭rmi注册的服务
		for (String remoteObjectName : registry.list()) {
			try {
				UnicastRemoteObject.unexportObject(registry.lookup(remoteObjectName), true);
			} catch (Exception t) {
				logger.debug(t);
			}
			try {
				registry.unbind(remoteObjectName);
			} catch (Exception t) {
				logger.debug(t);
			}
		}

		// 强制关闭rmi服务
		AccessController.doPrivileged(new PrivilegedAction<String>() {  // 通过WAS控制台重启应用，报端口已经使用异常，应用没有正常启动
			public String run() {
				try {
					Field implTableField = sun.rmi.transport.ObjectTable.class.getDeclaredField("implTable");
					implTableField.setAccessible(true);
					Map targetMaps = (Map)implTableField.get(null);

					Field transportField = Target.class.getDeclaredField("exportedTransport");
					transportField.setAccessible(true);

					Field serverSocketField = sun.rmi.transport.tcp.TCPTransport.class.getDeclaredField("server");
					serverSocketField.setAccessible(true);

					for (Object ref : targetMaps.keySet().toArray()) {
						sun.rmi.transport.Target target = (sun.rmi.transport.Target)targetMaps.get(ref);
						if (target.getStub() instanceof Registry) {
							sun.rmi.transport.tcp.TCPTransport transport = (sun.rmi.transport.tcp.TCPTransport)transportField.get(target);
							ServerSocket server = (ServerSocket)serverSocketField.get(transport);
							String ipAddress = server.getInetAddress().getHostAddress();
							if (("0.0.0.0".equals(ipAddress) || "127.0.0.1".equals(ipAddress) || ipAddress.equals(host))
									&& server.getLocalPort() == port) {
								try {
									sun.rmi.transport.ObjectTable.unexportObject((Remote)((WeakReference)ref).get(), true);
								} catch (Throwable t) {
									logger.debug(t);
								}
								try {
									server.close();
								} catch (Throwable t) {
									logger.debug(t);
								}
								try {
									serverSocketField.set(transport, null);
								} catch (Throwable t) {
									logger.debug(t);
								}
							}
						}
					}
					try {
						Field reaperField = sun.rmi.transport.ObjectTable.class.getDeclaredField("reaper");
						reaperField.setAccessible(true);

						Object reaperThread = reaperField.get(null);
						if (reaperThread != null) {
							((Thread)reaperThread).interrupt();
						}
					} catch (Throwable t) {
					}
				} catch (Throwable t) {
					logger.debug(t);
				}
				return null;
			}
		});

		registry = null;
		clientSocketFactory = null;
		serverSocketFactory = null;
		isStarted = false;
	}

	/**
	 *
	 * 发布rmi对象
	 *
	 * @param remoteObjectName
	 * @param remoteObject
	 * @throws RemoteException
	 */
	public void publish(String remoteObjectName, Object remoteObject) throws RemoteException {
		if (remoteObjectName == null || remoteObjectName.trim().length() == 0) {
			throw new IllegalArgumentException("remoteObjectName is null!");
		}
		if (remoteObject == null) {
			throw new IllegalArgumentException("remoteObject is null!");
		}
		if (registry == null) {
			throw new NullPointerException("registry is null, so remote object publish is impossiable!");
		}
		Remote exportedObject = getObjectToPublish(remoteObject);
		remoteObjectMap.put(remoteObjectName, exportedObject);

		if (clientSocketFactory != null) {
			UnicastRemoteObject.exportObject(exportedObject, port, clientSocketFactory, this.serverSocketFactory);
		} else {
			UnicastRemoteObject.exportObject(exportedObject, port);
		}

		// Bind RMI object to registry.
		registry.rebind(remoteObjectName, exportedObject);
	}

	/**
	 * 取消发布rmi对象
	 *
	 * @param remoteObjectName
	 * @throws RemoteException
	 */
	public void unpublish(String remoteObjectName) throws RemoteException {
		if (remoteObjectName == null || remoteObjectName.trim().length() == 0) {
			throw new IllegalArgumentException("remoteObjectName is null!");
		}
		if (registry == null) {
			throw new NullPointerException("registry is null, so remote object unpublish is impossiable!");
		}
		try {
			registry.unbind(remoteObjectName);
		} catch (NotBoundException ignore) {
		}

		try {  
			if (remoteObjectMap.containsKey(remoteObjectName)) {
				UnicastRemoteObject.unexportObject(remoteObjectMap.get(remoteObjectName), true);
				remoteObjectMap.remove(remoteObjectName);
			}

		} catch (Exception t) {
			logger.debug(t);
		}
	}

	private static Remote getObjectToPublish(Object remoteObject) {
		if (remoteObject instanceof Remote) {
			// conventional RMI service
			return (Remote) remoteObject;
		} else {
			return new RemoteObjectWrapperImpl(remoteObject);
		}
	}
}
