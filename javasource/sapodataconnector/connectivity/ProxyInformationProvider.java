package sapodataconnector.connectivity;

import com.mendix.systemwideinterfaces.core.IContext;

public interface ProxyInformationProvider {

	ProxyInformation getProxyInformation(IContext context);

}
