package sapodataconnector.utils;

import sapodataconnector.proxies.RequestParams;

public class ExpectedHttpResultOptional {

	public static ExpectedHttpResultOptional ofNullable(RequestParams parameters) {
		ExpectedHttpResultOptional optional = new ExpectedHttpResultOptional();
		if (parameters != null && parameters.getexpectedHttpResult() > 0) {
			optional.expectedHttpResult = parameters.getexpectedHttpResult();
		}
		return optional;
	}

	private Integer expectedHttpResult = null;

	public ExpectedHttpResultOptional orDefault(int expected) {
		if (expectedHttpResult == null) {
			this.expectedHttpResult = expected;
		}
		return this;
	}

	/**
	 * Return the the expected value set via the headers, the default or 0 is none is set
	 * @return
	 */
	public int getExpectedHttpResult() {
		return expectedHttpResult != null ? expectedHttpResult : 0;
	}

}
