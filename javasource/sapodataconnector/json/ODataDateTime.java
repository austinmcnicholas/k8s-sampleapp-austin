package sapodataconnector.json;

import java.util.Date;

import com.mendix.thirdparty.org.json.JSONString;

/**
 * For representing a date time value as oData string.
 *
 * Will represent the value in json as
 * <pre>"\/Date(timestamp)\/"</pre>
 *
 * @author Chris
 *
 */
public class ODataDateTime implements JSONString {

	private Date dateTime;

	public ODataDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	@Override
	public String toJSONString() {
		return new StringBuilder("\"\\/Date(")
				.append(dateTime.getTime())
				.append(")\\/\"")
				.toString();
	}

	@Override
	public String toString() {
		return toJSONString();
	}

}
