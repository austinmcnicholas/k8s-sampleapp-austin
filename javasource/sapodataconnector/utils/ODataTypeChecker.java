package sapodataconnector.utils;

import com.mendix.systemwideinterfaces.core.meta.IMetaObject;

public class ODataTypeChecker {

	/**
	 * Check if the given target inherits the odata object. If not throw an exception.
	 *
	 * @param targetMetaObject The Mendix Meta Object of the class to be checked.
	 * @param superClass The class which we expected to be inherited.
	 * @throws IllegalArgumentException if the target doesn't in inherit from superClass.
	 */
	public static void checkDescendantOf(IMetaObject targetMetaObject, final String superClass) throws IllegalArgumentException {
		if (targetMetaObject.getSuperObjects()
				.stream()
				.filter(s -> s.getName().equals(superClass))
				.count() != 1) {
			throw new IllegalArgumentException(new StringBuilder("Provided entity type ")
					.append(targetMetaObject.getName())
					.append(" doesn't inherit from ")
					.append(superClass)
					.append(".")
					.toString());
		}
	}

}
