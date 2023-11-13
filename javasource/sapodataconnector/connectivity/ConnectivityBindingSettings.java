package sapodataconnector.connectivity;

import java.util.Map;

import com.mendix.thirdparty.org.json.JSONArray;
import com.mendix.thirdparty.org.json.JSONObject;

/**
 * <pre>"credentials": {
     "clientid": "sb-clone3742766613!b380|connectivity!b17",
     "clientsecret": "bCcHkQdedV7m",
     "identityzone": "p1942443769trial",
     "onpremise_proxy_host": "10.0.85.1",
     "onpremise_proxy_port": "20003",
     "tenantid": "71d1ae35-d917-4d18-9b07-9044bc880a45",
     "tenantmode": "dedicated",
     "uaadomain": "authentication.eu10.hana.ondemand.com",
     "url": "https://p1942443769trial.authentication.eu10.hana.ondemand.com",
     "verificationkey": "-----BEGIN PUBLIC KEY-----MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAwThn6OO9kj0bchkOGkqYBnV1dQ3zU/xtj7Kj7nDd8nyRMcEWCtVzrzjzhiisRhlrzlRIEY82wRAZNGKMnw7cvCwNixcfcDJnjzgr2pJ+5/yDZUc0IXXyIWPZD+XdL+0EogC3d4+fqyvg/BF/F0t2hKHWr/UTXE6zrGhBKaL0d8rKfYd6olGWigFd+3+24CKI14zWVxUBtC+P9Fhngc9DRzkXqhxOK/EKn0HzSgotf5duq6Tmk9DCNM4sLW4+ERc6xzrgbeEexakabvax/Az9WZ4qhwgw+fwIhKIC7WLwCEJaRsW4m7NKkv+eJR2LKYesuQ9SVAJ3EXV86RwdnH4uAv7lQHsKURPVAQBlranSqyQu0EXs2N9OlWTxe+FyNkIvyZvoLrZl/CdlYc8AKxRm5rn2/88nkrYQ0XZSrnICM5FRWgVF2hn5KfZGwtBN85/D4Yck6B3ocMfyX7e4URUm9lRPQFUJGTXaZnEIge0R159HUwhTN1HvyXrs6uT1ZZmW+c3p47dw1+LmUf/hIf8zd+uvHQjIeHEJqxjqfyA8yqAFKRHKVFrwnwdMHIsRap2EKBhHMfeVf0P2th5C9MggYoGCvdIaIUgMBX3TtCdvGrcWML7hnyS2zkrlA8SoKJnRcRF2KxWKs355FhpHpzqyZflO5l98+O8wOsFjGpL9d0ECAwEAAQ==-----END PUBLIC KEY-----",
     "xsappname": "clone3742766613614e3494c27140ddb5be4a!b380|connectivity!b17"
    }</pre>
 * @author Chris
 *
 */
public class ConnectivityBindingSettings {

	private final static String VCAP_SERVICES = "VCAP_SERVICES";
	private final static String CONNECTIVITY = "connectivity";
	private final static String CREDENTIALS = "credentials";
	private final static String CLIENT_ID = "clientid";
	private final static String CLIENT_SECRET = "clientsecret";
	private final static String IDENTY_ZONE = "identityzone";
	private final static String ONPREMISE_PROXY_HOST = "onpremise_proxy_host";
	private final static String ONPREMISE_PROXY_PORT = "onpremise_proxy_port";
	private final static String URL = "url";
	private final static String VERIFICATION_KEY = "verificationkey";
	private final static String XSAPPNAME = "xsappname";

	private String proxyHost;
	private int proxyPort;
	private String clientId;
	private String clientSecret;
	private String identityZone;
	private String url;
	private String verificationKey;
	private String xsappname;

	/**
	 * Hide the constructor. Use getConnectivityBindingSettings to create an instance
	 * based on VCAP settings. The object is inmutable (hence no setters are defined).
	 */
	private ConnectivityBindingSettings() {
		// hide constructor
	}

	public static ConnectivityBindingSettings createFromEnvironmentSettings() {
		Map<String, String> systemProperties = System.getenv();
		String vcapApplicationSettings = systemProperties.get(VCAP_SERVICES);
		return getConnectivityBindingSettings(vcapApplicationSettings);			
	}

	/**
	 * Extract the connectivity settings from the VCAP settings and create a Java POJO for it.
	 *
	 * @param vcapSettings
	 * @return A ConnectivityBindingSettings containing the values needed by the Mendix App.
	 * @throws IllegalArgumentException if needed JSON elements can't be found.
	 */
	public static ConnectivityBindingSettings getConnectivityBindingSettings(String vcapSettings) {
		if (vcapSettings == null || vcapSettings.trim().equals("")) {
			return null;
		}
		
		JSONObject vcap = new JSONObject(vcapSettings);

		if (!vcap.has(CONNECTIVITY)) {
			throw new IllegalArgumentException("connectivity not found in the provided vcap settings");
		}
		JSONArray connectivityArray = vcap.getJSONArray(CONNECTIVITY);
		if (connectivityArray.length() != 1) {
			throw new IllegalArgumentException("connectivity array contains zero or multiple elements.");
		}
		JSONObject connectivityFirst = connectivityArray.getJSONObject(0);

		JSONObject credentials = connectivityFirst.getJSONObject(CREDENTIALS);

		ConnectivityBindingSettings settings = new ConnectivityBindingSettings();
		settings.clientId = credentials.getString(CLIENT_ID);
		settings.clientSecret = credentials.getString(CLIENT_SECRET);
		settings.identityZone = credentials.getString(IDENTY_ZONE);
		settings.proxyHost = credentials.getString(ONPREMISE_PROXY_HOST);
		settings.proxyPort = credentials.getInt(ONPREMISE_PROXY_PORT);
		settings.url = credentials.getString(URL);
		settings.verificationKey = credentials.getString(VERIFICATION_KEY);
		settings.xsappname = credentials.getString(XSAPPNAME);
		return settings;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getIdentityZone() {
		return identityZone;
	}

	public String getUrl() {
		return url;
	}

	public String getVerificationKey() {
		return verificationKey;
	}

	public String getXsappname() {
		return xsappname;
	}

}
