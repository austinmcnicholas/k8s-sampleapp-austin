package sapodataconnector.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

public class ContextCookieStore implements CookieStore {
	private IContext context;
	// key for context data
	private static final String COOKIES = "Mendix_Cookies";
	
	private final static ILogNode LOGGER = Core.getLogger(sapodataconnector.proxies.constants.Constants.getLogNode());

	@Override
	public void addCookie(Cookie givenCookie) {
		HashMap<String, Cookie> cookiesMap = getCookiesMap();
		cookiesMap.put(givenCookie.getName(), givenCookie);
	}

	public ContextCookieStore(IContext context) {
		super();
		// initialize cookies map to context, so they will be there for every next call
		if (context.getData().get(COOKIES) == null) {
			context.getData().put(COOKIES, new HashMap<String, Cookie>());
		}
		this.context = context;
	}

	@Override
	public void clear() {
		context.getData().put(COOKIES, new HashMap<String, Cookie>());
	}

	@Override
	public boolean clearExpired(Date expiryDate) {
		HashMap<String, Cookie> cookiesMap = getCookiesMap();
		boolean result = false;
		Iterator<Cookie> i = cookiesMap.values().iterator();
		while (i.hasNext()) {
			Cookie cookie = i.next();
			if (cookie.isExpired(expiryDate)) {
				i.remove();
				result = true;
			}
		}
		context.getData().put(COOKIES, cookiesMap);
		
		return result;
	}

	@Override
	public List<Cookie> getCookies() {
		HashMap<String, Cookie> cookiesMap = getCookiesMap();

		if(LOGGER.isDebugEnabled()) {
			if (cookiesMap.size()>0) {
				StringBuilder sb = new StringBuilder("Context cookie store: ");
				for (Cookie c : cookiesMap.values()) {
					sb.append("\n").append(c.getName()).append(":").append(c.getValue());
				}
				LOGGER.debug(sb.toString());
			} else {
				LOGGER.debug("Context cookie store is empty.");
			}
		}
		
		return new ArrayList<Cookie>(cookiesMap.values());
	}
	
	// extract cookies map from context
	@SuppressWarnings("unchecked")
	private HashMap<String, Cookie> getCookiesMap() {
		return (HashMap<String, Cookie>) context.getData().get(COOKIES);
	}

}
