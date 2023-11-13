package sapodataconnector.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import sapodataconnector.proxies.constants.Constants;
import system.proxies.Session;

public class SessionCookieStore implements CookieStore {
	private final static ILogNode LOGGER = Core.getLogger(sapodataconnector.proxies.constants.Constants.getLogNode());

	private String sessionId;
	private IContext context;

	public SessionCookieStore(String sessionId, IContext context) {
		this.sessionId = sessionId;
		this.context = context;
	}

	@Override
	public void addCookie(Cookie givenCookie) {
		
		XPath<sapodataconnector.proxies.Cookie> cookies = XPath.create(context, sapodataconnector.proxies.Cookie.class)
				.eq(sapodataconnector.proxies.Cookie.MemberNames.Cookie_Session.toString(), sessionId)
				.eq(sapodataconnector.proxies.Cookie.MemberNames.domain, givenCookie.getDomain())
				.eq(sapodataconnector.proxies.Cookie.MemberNames.path, givenCookie.getPath())
				.eq(sapodataconnector.proxies.Cookie.MemberNames.name, givenCookie.getName());

		try {
			sapodataconnector.proxies.Cookie cookie = cookies.first();
			if (cookie == null) {	
				cookie = new sapodataconnector.proxies.Cookie(context);
			}

			cookie.setdomain(givenCookie.getDomain());
			cookie.setexpiryDate(givenCookie.getExpiryDate());		
			cookie.setisPersistent(givenCookie.isPersistent());
			cookie.setisSecure(givenCookie.isSecure());
			cookie.setname(givenCookie.getName());
			cookie.setpath(givenCookie.getPath());
			cookie.setvalue(givenCookie.getValue());

			// getPorts() method is mentioned as obsolete in the documentation https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/cookie/Cookie.html#getPorts()
			if (givenCookie.getPorts() != null
					&& givenCookie.getPorts().length == 1) {
				cookie.setports(Integer.toString(givenCookie.getPorts()[0]));
			}
			Session session = Session.initialize(context, context.getSession().getMendixObject());	
			cookie.setCookie_Session(session);	
			cookie.commit();
			
		} catch (CoreException exception) {
			Core.getLogger(Constants.getLogNode()).warn("Error adding cookie: " + exception.getMessage(), exception);
		}
	}

	@Override
	public void clear() {
		XPath<sapodataconnector.proxies.Cookie> userCookies = XPath.create(context, sapodataconnector.proxies.Cookie.class)
				.eq(sapodataconnector.proxies.Cookie.MemberNames.Cookie_Session.toString(), sessionId);
		try {
			userCookies.deleteAll();
		} catch (CoreException exception) {
			Core.getLogger(Constants.getLogNode()).warn("Error clearing cookie: " + exception.getMessage(), exception);
		}
	}

	@Override
	public boolean clearExpired(Date expiryDate) {
		XPath<sapodataconnector.proxies.Cookie> userCookies = XPath.create(context, sapodataconnector.proxies.Cookie.class)
				.eq(sapodataconnector.proxies.Cookie.MemberNames.Cookie_Session.toString(), sessionId)
				.compare(sapodataconnector.proxies.Cookie.MemberNames.expiryDate, "<=", expiryDate);
		try {
			userCookies.deleteAll();
		} catch (CoreException exception) {
			Core.getLogger(Constants.getLogNode()).warn("Error clearExpired cookie: " + exception.getMessage(), exception);
		}
		return true;
	}

	@Override
	public List<Cookie> getCookies() {
		XPath<sapodataconnector.proxies.Cookie> cookies = XPath.create(context, sapodataconnector.proxies.Cookie.class)
			.eq(sapodataconnector.proxies.Cookie.MemberNames.Cookie_Session.toString(), sessionId);

		List<Cookie> results = new ArrayList<>(0);
		try {
			for (sapodataconnector.proxies.Cookie storedCookie: cookies.all()) {
				BasicClientCookie target = new BasicClientCookie(storedCookie.getname(), storedCookie.getvalue());
				target.setDomain(storedCookie.getdomain());
				target.setPath(storedCookie.getpath());
				target.setSecure(storedCookie.getisSecure());
				target.setExpiryDate(storedCookie.getexpiryDate());
				target.setAttribute(ClientCookie.DOMAIN_ATTR, storedCookie.getdomain());
				results.add(target);
			}
		} catch (CoreException exception) {
			Core.getLogger(Constants.getLogNode()).warn("Error in  getCookies: " + exception.getMessage(), exception);
		}

		if(LOGGER.isDebugEnabled()) {
			if (results.size()>0) {
				StringBuilder sb = new StringBuilder("Session cookie store: ");
				for (Cookie c : results) {
					sb.append("\n").append(c.getName()).append(":").append(c.getValue());
				}
				LOGGER.debug(sb.toString());
			} else {
				LOGGER.debug("Session cookie store is empty.");
			}
		}
		

		return results;
	}

}
