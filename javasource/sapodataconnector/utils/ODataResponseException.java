package sapodataconnector.utils;

import java.io.IOException;
/**
 * 
 * This class is a wrapper around the IOException class and is used to clarify the type of exception that is caught in the Mendix side.
 *
 */
public class ODataResponseException extends IOException{

	public ODataResponseException(){
		super();
	}
	public ODataResponseException(String message){
		super(message);
	}
}
