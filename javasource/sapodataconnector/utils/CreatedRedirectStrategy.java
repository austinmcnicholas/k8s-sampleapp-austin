package sapodataconnector.utils;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class CreatedRedirectStrategy extends LaxRedirectStrategy {

	@Override
	public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
			throws ProtocolException {
		if (request == null) {
			throw new IllegalArgumentException("HTTP request may not be null");
		}
		if (response == null) {
			throw new IllegalArgumentException("HTTP response may not be null");
		}

		int statusCode = response.getStatusLine().getStatusCode();
		String method = request.getRequestLine().getMethod();
		Header locationHeader = response.getFirstHeader("location");
		switch (statusCode) {
		case 201:
			return isRedirectable(method) && locationHeader != null;

		default:
			return super.isRedirected(request, response, context);
		}

	}



}
