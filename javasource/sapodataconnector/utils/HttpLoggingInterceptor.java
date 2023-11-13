package sapodataconnector.utils;

import java.io.IOException;
import java.util.Set;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import sapodataconnector.proxies.constants.Constants;

public class HttpLoggingInterceptor implements Interceptor {
	
	private static final ILogNode LOGGER = Core.getLogger(Constants.getLogNode());

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request originalRequest = chain.request();
		Set<String> headerKeys = originalRequest.headers().names();
		LOGGER.debug("HttpLoggingInterceptor Request Headers STARTS::");
		LOGGER.debug("Sending request:: "+originalRequest.url());

		for (String headerKey : headerKeys) {
			LOGGER.debug(headerKey+" :: "+originalRequest.header(headerKey));			
		}

		LOGGER.debug("HttpLoggingInterceptor Request Headers ENDS::");
		Response response = chain.proceed(originalRequest);

		LOGGER.debug("Received response for :: "+originalRequest.url());

		Set<String> responseHeaderKeys = response.headers().names();
		LOGGER.debug("HttpLoggingInterceptor Response Headers STARTS::");
		for (String responseHeaderKey : responseHeaderKeys) {
			LOGGER.debug(responseHeaderKey+" :: "+response.header(responseHeaderKey));
		}
		LOGGER.debug("HttpLoggingInterceptor Response Headers ENDS::");
		return response;
	}

}
