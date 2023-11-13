package sapodataconnector.request;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mendix.systemwideinterfaces.core.IContext;

import sapodataconnector.proxies.Header;
import sapodataconnector.proxies.RequestParams;

public class RequestParameterUtil {

	private RequestParameterUtil() {
		// hide constructor
	}

	private IContext context;
	private RequestParams requestParams;
	private Map<String, String> newHeaders = new HashMap<>();

	public static RequestParameterUtil create(IContext context) {
		RequestParameterUtil requestParameterUtil = new RequestParameterUtil();
		requestParameterUtil.context = context;
		requestParameterUtil.requestParams = new RequestParams(context);

		return requestParameterUtil;
	}

	public static RequestParameterUtil change(IContext context, RequestParams parameters) {
		RequestParameterUtil util = new RequestParameterUtil();
		util.context = context;
		util.requestParams = parameters;
		return util;
	}

	public RequestParameterUtil addHeader(String name, String value) {
		newHeaders.put(name, value);
		return this;
	}

	public RequestParameterUtil addBasicAuthentication(String username, String password) {
		StringBuilder secret = new StringBuilder(username)
				.append(":")
				.append(password);
		String base64Secret = Base64.getEncoder().encodeToString(secret.toString().getBytes());

		StringBuilder authorization = new StringBuilder("Basic ")
				.append(base64Secret);
		newHeaders.put("Authorization", authorization.toString());
		return this;
	}

	public RequestParams build() {
		for (Entry<String, String> entry: newHeaders.entrySet()) {
			Header header = new Header(context);
			header.setname(entry.getKey());
			header.setvalue(entry.getValue());
			header.setHeader_RequestParams(requestParams);
		}

		return requestParams;
	}

}
