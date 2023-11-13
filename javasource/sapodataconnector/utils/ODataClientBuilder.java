package sapodataconnector.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import sapodataconnector.connectivity.SAPProxyInformationProvider;
import sapodataconnector.proxies.Authentication;
import sapodataconnector.proxies.Destination;
import sapodataconnector.proxies.ProxyType;
import sapodataconnector.proxies.RequestParams;
import system.proxies.Session;

/**
 * Helper class to build a CloseableHttpClient using a fluent syntax
 * using ODataConnector input.
 *
 */
public class ODataClientBuilder {

	private final static ILogNode LOGGER = Core.getLogger(sapodataconnector.proxies.constants.Constants.getLogNode());

	private ODataClientBuilder() {
		// hide default constructor
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {
			// hide default constructor
		}

		private IContext context;
		private RequestParams requestParameters;
		private Destination destination;

		public Builder setContext(IContext context) {
			this.context = context;
			return this;
		}

		public Builder setRequestParameters(RequestParams requestParameters) {
			this.requestParameters = requestParameters;
			return this;
		}

		public Builder setDestination(Destination destination) {
			this.destination = destination;
			return this;
		}
		
		//creating requestinterceptor to register with client.
		HttpRequestInterceptor requestInterceptor = new HttpRequestInterceptor() {
			
			@Override
			public void process(HttpRequest request, HttpContext httpContext) throws HttpException, IOException {
				Header [] requestHeaders = request.getAllHeaders();
				StringBuilder requestLoggingBuilder = new StringBuilder(context.getSession().getId().toString());
				requestLoggingBuilder.append("|").append("Request URI: ")
				.append(request.getRequestLine().getUri());
				LOGGER.info(requestLoggingBuilder.toString());	
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Request Headers starts:: ");
					if(null != requestHeaders && requestHeaders.length > 0) {
						for (int i = 0; i < requestHeaders.length; i++) {
							Header reqHeader = requestHeaders[i];
							if(StringUtils.isNotBlank(reqHeader.getName())) {
								LOGGER.debug(reqHeader.getName()+ " : "+reqHeader.getValue());
							}
						}
					}
					LOGGER.debug("Request Headers ends:: ");
				}
			}
		};
		
		HttpResponseInterceptor responseInterceptor = new HttpResponseInterceptor() {
			
			@Override
			public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
				Header [] responseHeaders = httpResponse.getAllHeaders();
				StringBuilder responseLoggingBuilder = new StringBuilder(context.getSession().getId().toString());
				responseLoggingBuilder.append("|").append("Response Received Status Code: ")
				.append(httpResponse.getStatusLine());
				LOGGER.info(responseLoggingBuilder.toString());	
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Response Headers starts:: ");
					if(null != responseHeaders && responseHeaders.length > 0) {
						for (int i = 0; i < responseHeaders.length; i++) {
							Header resHeader = responseHeaders[i];
							if(StringUtils.isNotBlank(resHeader.getName())) {
								LOGGER.debug(resHeader.getName()+ " : "+resHeader.getValue());
							}
						}
					}
					LOGGER.debug("Response Headers ends:: ");
						
				}
			}
		};

		public CloseableHttpClient build() {
			HttpClientBuilder clientBuilder = HttpClients.custom().addInterceptorLast(requestInterceptor)
					.addInterceptorLast(responseInterceptor);
			// Redirect automatically, our own implementation to follow 201 Created as well
			final RedirectStrategy redirectStrategy = new LaxRedirectStrategy();
			clientBuilder = clientBuilder.setRedirectStrategy(redirectStrategy);
			
			// initialize the cookie store;
			if (context != null) { 
				IMendixObject mendixSession = context.getSession().getMendixObject();
				if (mendixSession != null) {
					// initialize proxy object 
					Session session = Session.initialize(context, mendixSession);					
					clientBuilder = clientBuilder.setDefaultCookieStore(new SessionCookieStore(Long.toString(session.getMendixObject().getId().toLong()), context));
				} else {	
					clientBuilder = clientBuilder.setDefaultCookieStore(new ContextCookieStore(context));
				}
			} else {
				// maybe IlligalStateException?
				LOGGER.error("New OdatahttpClient is built but context is null!");
			}

			// set time outs
			RequestParams parameters = Optional.ofNullable(requestParameters).orElse(new RequestParams(context));
			clientBuilder.setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(parameters.getconnectTimeout() * 1000)
                    .setSocketTimeout(parameters.getreadTimeout() * 1000)
                    .setExpectContinueEnabled(true)
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .build());

			// Determine the proxy, if Cloud Connector is configured search for the cloud connector proxy in the
			// VCAP settings
			if (this.destination != null && destination.getProxyType() == ProxyType.OnPremise) {

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(String.format("Using proxy %s:%d from %s",
							destination.getProxyHost(), destination.getProxyPort(), destination.getName()));
				}
	
				final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(
		                new AuthScope(destination.getProxyHost(),  destination.getProxyPort()),
		                // Add SAP-Connectivity-Proxy Credentials by the Connectivity service biding in the VCAP Services
		                new UsernamePasswordCredentials(destination.getSapConnectivityProxyCredentials(), StringUtils.EMPTY));

		        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				clientBuilder = clientBuilder.setProxy(new HttpHost(destination.getProxyHost(), destination.getProxyPort()));

				// Adding headers
				if (destination.getAuthentication() == Authentication.PrincipalPropagation) {
					List<BasicHeader> defaultHeaders = new ArrayList<BasicHeader>();
					LOGGER.debug(String.format("Setting Proxy Header %s: %s", SAPProxyInformationProvider.SAP_CONNECTIVITY_AUTHENTICATION, destination.getSapConnectivityAuthentication()));
					defaultHeaders.add(new BasicHeader(SAPProxyInformationProvider.SAP_CONNECTIVITY_AUTHENTICATION, destination.getSapConnectivityAuthentication()));
					if(destination.getSapConnectivitySccLocationId() != null && !destination.getSapConnectivitySccLocationId().isEmpty()) {
						LOGGER.debug(String.format("Setting Proxy Header %s: %s", SAPProxyInformationProvider.SAP_CONNECTIVITY_SCC_LOCATION_ID, destination.getSapConnectivitySccLocationId()));
						defaultHeaders.add(new BasicHeader(SAPProxyInformationProvider.SAP_CONNECTIVITY_SCC_LOCATION_ID, destination.getSapConnectivitySccLocationId()));
					}
					clientBuilder = clientBuilder.setDefaultHeaders(defaultHeaders);
				}
			} else {
				clientBuilder = clientBuilder.useSystemProperties();
			}

			// done
			return clientBuilder.build();
		}
	}

}
