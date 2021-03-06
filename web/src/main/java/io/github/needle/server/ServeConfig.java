package io.github.needle.server;

import io.github.kits.Props;
import io.github.kits.Strings;
import io.github.needle.bean.Singleton;
import io.github.needle.constants.Consts;

/**
 * @author whimthen
 * @version 1.0.0
 * @since 1.0.0
 */
public class ServeConfig {

	/**
	 * 绑定端口
	 * binding port
	 */
	private int port;

	/**
	 * 服务名称
	 * server name
	 */
	private String serverName;

	private String inetAddress;

	private String serverVersion;

	public String getServerVersion() {
		return serverVersion;
	}

	public String getInetAddress() {
		return inetAddress;
	}

	public ServeConfig setInetAddress(String inetAddress) {
		this.inetAddress = inetAddress;
		return this;
	}

	public String getServerName() {
		if (Strings.isNullOrEmpty(serverName))
			setServerName(Consts.DEFAULT_SERVER_NAME);
		return serverName.toUpperCase();
	}

	public ServeConfig setServerName(String serverName) {
		this.serverName = serverName;
		return this;
	}

	public int getPort() {
		return port;
	}

	public ServeConfig setPort(int port) {
		this.port = port;
		return this;
	}

	private ServeConfig() {
		serverVersion = Props.getString("maven-version", "server.version").orElse("1.0.0-RELEASE");
	}

	private static class ServeConfigHolder {
		private static final ServeConfig INSTANCE = new ServeConfig();
	}

	public static ServeConfig getInstance() {
		Singleton.register(ServeConfigHolder.INSTANCE);
		return ServeConfigHolder.INSTANCE;
	}

}
