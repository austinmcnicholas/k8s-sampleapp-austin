package sapodataconnector.connectivity;

import com.mendix.systemwideinterfaces.core.IContext;

public class ProxyInformationFactory {

	private static ProxyInformationProvider instance;
	private final static Integer INSTANCE_LOCK = 1;

	public static ProxyInformation getProxyInformation(IContext context) {
		if (instance == null) {
			synchronized (INSTANCE_LOCK) {
				instance = new SAPProxyInformationProvider();
			}
		}

		return instance.getProxyInformation(context);
	}
}
