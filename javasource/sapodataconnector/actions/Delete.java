// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package sapodataconnector.actions;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.webui.CustomJavaAction;
import sapodataconnector.proxies.constants.Constants;
import sapodataconnector.utils.ExpectedHttpResultOptional;
import sapodataconnector.utils.ODataClientBuilder;
import sapodataconnector.utils.ODataRequestBuilder;
import sapodataconnector.utils.ODataResponseHandler;

public class Delete extends CustomJavaAction<java.lang.Boolean>
{
	private IMendixObject __odataObject;
	private sapodataconnector.proxies.OdataObject odataObject;
	private IMendixObject __destination;
	private sapodataconnector.proxies.Destination destination;
	private IMendixObject __requestParameters;
	private sapodataconnector.proxies.RequestParams requestParameters;

	public Delete(IContext context, IMendixObject odataObject, IMendixObject destination, IMendixObject requestParameters)
	{
		super(context);
		this.__odataObject = odataObject;
		this.__destination = destination;
		this.__requestParameters = requestParameters;
	}

	@java.lang.Override
	public java.lang.Boolean executeAction() throws Exception
	{
		this.odataObject = this.__odataObject == null ? null : sapodataconnector.proxies.OdataObject.initialize(getContext(), __odataObject);

		this.destination = this.__destination == null ? null : sapodataconnector.proxies.Destination.initialize(getContext(), __destination);

		this.requestParameters = this.__requestParameters == null ? null : sapodataconnector.proxies.RequestParams.initialize(getContext(), __requestParameters);

		// BEGIN USER CODE
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(new StringBuilder(getContext().getSession().getId().toString())
					.append("|DELETE|object: ")
					.append(odataObject.getmeta_objectURI())
					.toString());
		}
		
		if(destination != null) {
			LOGGER.info("Delete :: Destination found , delete object");
			final HttpDelete httpDelete = ODataRequestBuilder.builder()
					.delete(destination, odataObject.getmeta_objectURI())
					.setContext(getContext())
					.setRequestHeader(requestParameters)
					.withIfMatchEtagFrom(odataObject)
					.build();

			try(final CloseableHttpClient httpclient = ODataClientBuilder.builder()
					.setContext(getContext())
					.setRequestParameters(requestParameters)
					.setDestination(destination)
					.build()) {
				httpclient.execute(httpDelete, new ODataResponseHandler("DELETE", LOGGER, getContext(),
						ExpectedHttpResultOptional.ofNullable(requestParameters).orDefault(204).getExpectedHttpResult()));
			}
		}
		else {
			LOGGER.info("Delete :: Destination not found , delete object.");
			final HttpDelete httpDelete = ODataRequestBuilder.builder()
					.delete(odataObject.getmeta_objectURI())
					.setContext(getContext())
					.setRequestHeader(requestParameters)
					.withIfMatchEtagFrom(odataObject)
					.build();

			try(final CloseableHttpClient httpclient = ODataClientBuilder.builder()
					.setContext(getContext())
					.setRequestParameters(requestParameters)
					.build()) {
				httpclient.execute(httpDelete, new ODataResponseHandler("DELETE", LOGGER, getContext(),
						ExpectedHttpResultOptional.ofNullable(requestParameters).orDefault(204).getExpectedHttpResult()));
				LOGGER.info(odataObject.getmeta_objectURI() + ":: Object deleted successfully");
			}

		}

		return true;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 * @return a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "Delete";
	}

	// BEGIN EXTRA CODE
	private static final ILogNode LOGGER = Core.getLogger(Constants.getLogNode());
	// END EXTRA CODE
}
