package sapodataconnector.connectivity;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.thirdparty.org.json.JSONObject;

public class SAPProxyInformationProvider
		implements ProxyInformationProvider {

	public static final String SAP_CONNECTIVITY_AUTHENTICATION = "SAP-Connectivity-Authentication";
	public static final String SAP_CONNECTIVITY_SCC_LOCATION_ID = "SAP-Connectivity-SCC-Location_ID";

	private static final String XSUAA_USER_TOKEN_MF = "SapAuthentication.ACT_GetUserToken";

	private static final String OAUTH_TOKEN_PATH = "/oauth/token";
	private static final String GRANT_TYPE = "grant_type";
	private static final String CLIENT_CREDENTIALS = "client_credentials";
	private static final String AUTHORIZATION = "Authorization";
	private static final String BASIC = "Basic ";
	private static final String TOKEN_FORMAT = "token_format";
	private static final String JWT = "jwt";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String EXPIRES_IN = "expires_in";
	private static final int MS_PER_SEC = 1000;

	private static final String BEARER = "Bearer ";

	private static ConnectivityBindingSettings bindingSettings;
	private static ProxyAuthorizationToken token;

	private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private static final Lock readLock = readWriteLock.readLock();
	private static final Lock writeLock = readWriteLock.writeLock();

	private static final ILogNode LOGGER = Core.getLogger(sapodataconnector.proxies.constants.Constants.getLogNode());

	@Override
	public ProxyInformation getProxyInformation(IContext context) {
		ProxyAuthorizationToken proxyAccessToken = getToken();
		if (bindingSettings != null) {
			if (proxyAccessToken == null) {
				throw new IllegalStateException("Unable to retrieve token for Proxy Authorization");
			}

			// get XSUAA user token
			Map<String, String> headers = new HashMap<>(0);
			String userToken = getUserXSUAAToken(context);
			if (userToken != null) {
				headers.put(SAP_CONNECTIVITY_AUTHENTICATION, BEARER + userToken);
			}

			ProxyInformation proxyInformation = new ProxyInformation(
					this.getClass().getSimpleName(),
					bindingSettings.getProxyHost(),
					bindingSettings.getProxyPort(),
					proxyAccessToken.token,
					java.util.Collections.unmodifiableMap(headers));
			return proxyInformation;
		}
		return null;
	}

	ProxyAuthorizationToken getToken() {
		readLock.lock();
		try {
			if (token == null || System.currentTimeMillis() >= token.expiresAt) {
				// unlock the readlock before acquire a write locks
				readLock.unlock();
				writeLock.lock();
				try {
					// acquire the readlock again
					readLock.lock();

					// test again to prevent multiple token refreshes
					if (bindingSettings == null) {
						bindingSettings = ConnectivityBindingSettings.createFromEnvironmentSettings();
					}

					if (bindingSettings != null && (token == null || System.currentTimeMillis() >= token.expiresAt)) {
						token = fetchProxyAuthorizationToken();
					}

				} finally {
					writeLock.unlock();
				}
			}
			return token;
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * This will fetch a Bearer token for the proxy authorization from the exposed
	 * url in the VCAP settings.
	 * @return
	 */
	ProxyAuthorizationToken fetchProxyAuthorizationToken() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Fetchning new proxy-authentication token.");
		}
		String tokenUrl = new StringBuilder(bindingSettings.getUrl())
				.append(OAUTH_TOKEN_PATH)
				.toString();

		String authenticationSecret = Base64.getEncoder().encodeToString(
				new StringBuilder(bindingSettings.getClientId())
					.append(":")
					.append(bindingSettings.getClientSecret()).toString().getBytes());
		String authenticationHeader = new StringBuilder(BASIC).append(authenticationSecret).toString();

		try {
			HttpResponse response = Request.Post(tokenUrl)
					.addHeader(AUTHORIZATION, authenticationHeader)
					.bodyForm(Form.form()
							.add(GRANT_TYPE, CLIENT_CREDENTIALS)
							.add(TOKEN_FORMAT, JWT)
							.build(),
							Charset.forName("UTF-8"))
					.execute()
					.returnResponse();

			String responseString = EntityUtils.toString(response.getEntity());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Proxy token request HTTP result: %d, reponse: %s",
						response.getStatusLine().getStatusCode(), responseString));
			}
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new RuntimeException("Got invalidate response code from Proxy-Authorization token request: " + response.getStatusLine().getStatusCode()
						+ " - " + responseString);
			}

			return parseToken(responseString);
		} catch (Exception ex) {
			throw new RuntimeException("Caught Exception during Proxy-Authorization token request", ex);
		}
	}

	ProxyAuthorizationToken parseToken(String tokenString) {
		JSONObject jsonToken = new JSONObject(tokenString);

		ProxyAuthorizationToken proxyAccessToken = new ProxyAuthorizationToken();
		proxyAccessToken.token = jsonToken.getString(ACCESS_TOKEN);
		proxyAccessToken.expiresAt = System.currentTimeMillis() + jsonToken.getLong(EXPIRES_IN) * MS_PER_SEC;

		LOGGER.info(String.format("Proxy token refreshed. Token: %s.... Expires in %d.",
				proxyAccessToken.token.substring(0, 25),
				jsonToken.getLong(EXPIRES_IN)));

		return proxyAccessToken;
	}

	String getUserXSUAAToken(IContext context) {
		if (!Core.getMicroflowNames().contains(XSUAA_USER_TOKEN_MF)) {
			LOGGER.warn(String.format("Unable to fetch user XSUAA token because microflow %s "
					+ "doesn't exists. Please check SapAuthentication module is included into your projects", XSUAA_USER_TOKEN_MF));
			return null;
		}

		return (String) Core.microflowCall(XSUAA_USER_TOKEN_MF).withParams(Collections.emptyMap()).execute(context);
	}

	private class ProxyAuthorizationToken {
		long expiresAt = 0;
		String token;
	}
}
