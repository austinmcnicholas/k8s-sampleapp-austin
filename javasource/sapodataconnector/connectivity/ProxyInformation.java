package sapodataconnector.connectivity;

import java.util.Collections;
import java.util.Map;

public class ProxyInformation {

	private String sourceName;
	private String proxyHost;
	private int proxyPort;
	private String proxyCredentials;

	private Map<String, String> requiredHeaders;

	public ProxyInformation(String sourceName, String proxyHost, int proxyPort, String proxyCredentials, Map<String, String> requiredHeaders) {
		this.sourceName = sourceName;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyCredentials = proxyCredentials;
		this.requiredHeaders = requiredHeaders != null ? requiredHeaders : Collections.emptyMap();
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public String getProxyCredentials() {
		return proxyCredentials;
	}

	public Map<String, String> getRequiredHeaders() {
		return requiredHeaders;
	}

}
