package sapodataconnector.utils;

import java.util.Map;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.thirdparty.org.json.JSONArray;
import com.mendix.thirdparty.org.json.JSONObject;

import sapodataconnector.proxies.constants.Constants;

public class AppEnvironmentSettings {

	private static final Map<String, String> environment = System.getenv();
    private static final String vcapApplication = environment.get("VCAP_APPLICATION");
    private static final String vcapServices = environment.get("VCAP_SERVICES") == null ? "{}" :  environment.get("VCAP_SERVICES");
    private static final JSONObject vcapServicesObject = new JSONObject(vcapServices);
    private static final ILogNode LOGGER = Core.getLogger(Constants.getLogNode());

    public static String getAuthenticationEndpoint() {
    	final JSONArray xsuaaArray = vcapServicesObject.getJSONArray("xsuaa");
        final JSONObject xsuaaConfigurationObject = xsuaaArray.getJSONObject(0);

        final JSONObject credentialsObject = xsuaaConfigurationObject.getJSONObject("credentials");
        String authenticationEndpoint = credentialsObject.getString("url");
        LOGGER.debug("authenticationEndpoint URL :: "+authenticationEndpoint);
        return authenticationEndpoint;
    } 

    public static String getDestinationClientId() {
    	final JSONArray xsuaaArray = vcapServicesObject.getJSONArray("destination");
        final JSONObject xsuaaConfigurationObject = xsuaaArray.getJSONObject(0);

        final JSONObject credentialsObject = xsuaaConfigurationObject.getJSONObject("credentials");
        String clientId = credentialsObject.getString("clientid");
        LOGGER.debug("Destination clientId :: "+clientId);
        return clientId;
    }

    public static String getDestinationClientSecret() {
    	final JSONArray xsuaaArray = vcapServicesObject.getJSONArray("destination");
        final JSONObject xsuaaConfigurationObject = xsuaaArray.getJSONObject(0);

        final JSONObject credentialsObject = xsuaaConfigurationObject.getJSONObject("credentials");
    	return credentialsObject.getString("clientsecret");
    }

    public static String geDestinationEndpoint() {
    	final JSONArray xsuaaArray = vcapServicesObject.getJSONArray("destination");
        final JSONObject xsuaaConfigurationObject = xsuaaArray.getJSONObject(0);

        final JSONObject credentialsObject = xsuaaConfigurationObject.getJSONObject("credentials");
        String destinationEndPoint = credentialsObject.getString("uri");
        LOGGER.debug("DestinationEndpoint URI:: "+destinationEndPoint);
    	return destinationEndPoint;
    }

}
