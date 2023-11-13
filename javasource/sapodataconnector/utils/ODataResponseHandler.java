package sapodataconnector.utils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import sapodataconnector.proxies.CSRFToken;
import sapodataconnector.proxies.constants.Constants;
import system.proxies.HttpHeader;
import system.proxies.HttpResponse;
import system.proxies.Session;

public class ODataResponseHandler implements ResponseHandler<String> {

	private static final ILogNode LOGGER = Core.getLogger(Constants.getLogNode());
	public static final String HTTP_RESPONSE = "Http_Response";
	public static final String CSRF_TOKEN = "Mendix_CSRFToken";

	private String operation;
	private ILogNode logNode;
	private IContext context;
	private Integer expectedHttpCode;

	public ODataResponseHandler(String operation, ILogNode logNode, IContext context) {
		this.operation = operation;
		this.logNode = logNode;
		this.context = context;
	}

	public ODataResponseHandler(String operation, ILogNode logNode, IContext context, int expectedHttpCode) {
		this.operation = operation;
		this.logNode = logNode;
		this.context = context;
		this.expectedHttpCode = expectedHttpCode > 0 ? expectedHttpCode : null;
	}

	@Override
	public String handleResponse(org.apache.http.HttpResponse response) throws ClientProtocolException, IOException {

		final int responseCode = response.getStatusLine().getStatusCode();
		String responseText = "";
		if (response.getEntity() != null){
			responseText = EntityUtils.toString(response.getEntity());
		}
		setHttpResponse(response, responseText);

		if (!Optional.ofNullable(expectedHttpCode).map(expected -> expected == responseCode)
                .orElse(responseCode >= 200 && responseCode < 400)) {
			String exception = new StringBuilder(operation)
					.append("|Response code: ")
					.append(responseCode)
					.append(", Response text: ")
					.append(responseText)
					.toString();
			logNode.warn(new StringBuilder(context.getSession().getId().toString()).append("|").append(exception));
			throw new ODataResponseException(exception);
		}

		if(LOGGER.isDebugEnabled()) {
			logNode.debug(new StringBuilder(context.getSession().getId().toString())
					.append("|")
					.append(operation)
					.append("|Response code: ")
					.append(responseCode)
					.append(", Response text: ")
					.append(responseText)
					.toString());
		}

		if (response.getFirstHeader("x-csrf-token") != null && context.getSession() != null) {
			CSRFToken token;
			
			if (context.getSession().getMendixObject() != null) {
				final List<IMendixObject> tokens = Core.retrieveByPath(context, context.getSession().getMendixObject(), CSRFToken.MemberNames.CSRFToken_Session.toString());

				if (tokens.isEmpty()) {
					token = new CSRFToken(context);
					token.setcsrfTokenValue(response.getFirstHeader("x-csrf-token").getValue());
					token.setCSRFToken_Session(Session.initialize(context, context.getSession().getMendixObject()));
				} else {
					token = CSRFToken.initialize(context, tokens.get(0));
					token.setcsrfTokenValue(response.getFirstHeader("x-csrf-token").getValue());
				}

				try {
					token.commit();
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}
			} else {
				// put token to context if session is not instantiated
				token = new CSRFToken(context);
				token.setcsrfTokenValue(response.getFirstHeader("x-csrf-token").getValue());
				context.getData().put(CSRF_TOKEN, token);
			}
		}

		return responseText;
	}

	private void setHttpResponse(org.apache.http.HttpResponse response, String responseText) {
		HttpResponse httpResponse = new HttpResponse(context);
		httpResponse.setHttpVersion(context, response.getProtocolVersion().toString());
		httpResponse.setReasonPhrase(context, response.getStatusLine().getReasonPhrase().toString());
		httpResponse.setStatusCode(context, response.getStatusLine().getStatusCode());
		httpResponse.setContent(context, responseText);
		for (Header iterable_element : response.getAllHeaders()) {
			HttpHeader httpHeader = new HttpHeader(context);
			httpHeader.setKey(context, iterable_element.getName());
			httpHeader.setValue(context, iterable_element.getValue());
			httpHeader.setHttpHeaders(context, httpResponse);
		}
		context.getData().put(HTTP_RESPONSE, httpResponse);
	}
}
