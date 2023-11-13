package sapodataconnector.utils;

import java.util.Base64;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import sapodataconnector.proxies.Authentication;
import sapodataconnector.proxies.Destination;
import sapodataconnector.proxies.CSRFToken;
import sapodataconnector.proxies.Header;
import sapodataconnector.proxies.OdataObject;
import sapodataconnector.proxies.RequestParams;

public class ODataRequestBuilder {

	private ODataRequestBuilder() {
		// hiding default constructor d
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {
			// hiding default constructor
		}

		private static final String BEARER_PREFIX = "Bearer ";
		private static final String BASIC_PREFIX = "Basic ";
		private IContext context;
		private HttpRequestBase request;
		private RequestParams requestParameters;
		private boolean fetchCSRF = false;
		private String ifMatchEtag;
		private Destination destination;
		private java.lang.String bodyContent;

		public Builder get(String url) {
			this.request = new HttpGet(url);
			return this;
		}

		public Builder get(Destination destination, String resource) {
			this.destination = destination;
			this.request = new HttpGet(getRestUri(destination, resource));
			return this;
		}

		public Builder post(String url) {
			this.request = new HttpPost(url);
			return this;
		}

		public Builder post(Destination destination, String resource) {
			this.destination = destination;
			this.request = new HttpPost(getRestUri(destination, resource));
			return this;
		}

		public Builder put(String url) {
			this.request = new HttpPut(url);
			return this;
		}

		public Builder put(Destination destination, String resource) {
			this.destination = destination;
			this.request = new HttpPut(getRestUri(destination, resource));
			return this;
		}

		public Builder delete(String url) {
			this.request = new HttpDelete(url);
			return this;
		}

		public Builder delete(Destination destination, String resource) {
			this.destination = destination;
			this.request = new HttpDelete(getRestUri(destination, resource));
			return this;
		}

		public Builder setContext(IContext context) {
			this.context = context;
			return this;
		}

		public Builder setRequestHeader(RequestParams requestParameters) {
			this.requestParameters = requestParameters;
			return this;
		}

		public Builder withIfMatchEtagFrom(OdataObject subject) {
			if (subject != null) {
				ifMatchEtag = subject.getmeta_etag();
			}
			return this;
		}

		public Builder fetchCSRFToken() {
			fetchCSRF = true;
			return this;
		}
		
		public Builder setBodyContent(String bodyContent) {
			this.bodyContent = bodyContent;
			return this;
		}
		
		private String getRestUri(Destination destination, String resource) {
			String uri = destination != null ? destination.getUrl() : "";
			return uri + resource;
		}

		private CSRFToken getCSRFToken() {
			if (context.getSession().getMendixObject() == null) {
				// retrieve token from context
				CSRFToken token = (CSRFToken) context.getData()
						.get(sapodataconnector.utils.ODataResponseHandler.CSRF_TOKEN);
				return token;
			}

			List<IMendixObject> tokens = Core.retrieveByPath(context, context.getSession().getMendixObject(),
					CSRFToken.MemberNames.CSRFToken_Session.toString());
			return !tokens.isEmpty() ? CSRFToken.initialize(context, tokens.get(0)) : null;
		}

		public <T extends HttpRequestBase> T build() {
			if (request == null) {
				throw new IllegalStateException("build called without a method set (get, post, put, delete).");
			}

			// check CSRF
			if (fetchCSRF) {
				request.addHeader("x-csrf-token", "fetch");
			} else if (!request.getMethod().equals("GET") && context != null) {
				// check the context for a CSRF token and add it
				CSRFToken csrfToken = getCSRFToken();
				if (csrfToken != null) {
					request.addHeader("x-csrf-token", csrfToken.getcsrfTokenValue());
				}
			}

			// etag
			if (ifMatchEtag != null && !ifMatchEtag.isEmpty()) {
				request.addHeader("If-Match", ifMatchEtag);
			}

			boolean acceptHeaderGiven = false;
			boolean contentTypeHeaderGiven = false;

			// given destination
			if (destination != null) {
				Authentication authentication = destination.getAuthentication();

				switch (authentication) {
				case BasicAuthentication:
					final String credentials = Base64.getEncoder()
							.encodeToString((destination.getUser() + ':' + destination.getPassword()).getBytes());
					request.addHeader(HttpHeaders.AUTHORIZATION, BASIC_PREFIX + credentials);
					break;
				case OAuth2SAMLBearerAssertion:
					request.addHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + destination.getSamlBearer());
					break;
				}
			}

			// given headers
			if (requestParameters != null) {
				if (context == null) {
					throw new IllegalStateException(
							"Can't set parameters without having a context, please call setContext");
				}

				List<IMendixObject> headers = Core.retrieveByPath(context, requestParameters.getMendixObject(),
						Header.MemberNames.Header_RequestParams.toString());
				for (IMendixObject headerMendixObject : headers) {
					Header header = Header.initialize(context, headerMendixObject);
					request.addHeader(header.getname(), header.getvalue());

					acceptHeaderGiven = acceptHeaderGiven || header.getname().equalsIgnoreCase("accept");
					contentTypeHeaderGiven = contentTypeHeaderGiven
							|| header.getname().equalsIgnoreCase("content-type");
				}
			}

			// default headers
			if (!acceptHeaderGiven) {
				request.addHeader("Accept", "application/json");
			}
			if (!contentTypeHeaderGiven) {
				request.addHeader("Content-Type", "application/json");
			}
			request.addHeader("Pragma", "no-cache");
			request.addHeader("Cache-Control", "no-cache");

			// body content
			if (bodyContent != null && request instanceof HttpPost) {
				HttpPost postRequest = (HttpPost) request;
				postRequest.setEntity(EntityBuilder.create()
						.setText(bodyContent)
						.setContentType(ContentType.APPLICATION_JSON.withCharset(Consts.UTF_8))
						.build());
			}
			
			return (T) request;
		}
	}

}
