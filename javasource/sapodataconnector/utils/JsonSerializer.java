package sapodataconnector.utils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mendix.core.Core;
import com.mendix.core.objectmanagement.member.MendixEnum;
import com.mendix.core.objectmanagement.member.MendixObjectReference;
import com.mendix.core.objectmanagement.member.MendixObjectReferenceSet;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;
import com.mendix.systemwideinterfaces.core.meta.IMetaAssociation;
import com.mendix.systemwideinterfaces.core.meta.IMetaAssociation.AssociationOwner;
import com.mendix.systemwideinterfaces.core.meta.IMetaAssociation.AssociationType;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;
import com.mendix.thirdparty.org.json.JSONArray;
import com.mendix.thirdparty.org.json.JSONObject;

import sapodataconnector.json.ODataDateTime;
import sapodataconnector.proxies.BooleanValue;
import sapodataconnector.proxies.ComplexType;

public class JsonSerializer {

	public static JSONObject writeMendixObjectToJson(IContext context, IMendixObject view, Boolean groupSupport) throws Exception {
		if (context == null) {
			throw new IllegalStateException("Context is null");
		}
		if (view == null) {
			throw new IllegalArgumentException("Mendix to JSON conversion expects an object");
		}

		if (!hasDataAccess(view.getMetaObject(), context)) {
			throw new IllegalStateException("During JSON serialization: Object of type '" + view.getType() + "' has no readable members for users with role(s) " + context.getSession().getUserRolesNames() + ". Please check the security rules");
		}

		// empty values should be included in the update when updating object and not for 'new' entities or complex types
		boolean skipEmpty = view.hasMember("meta_objectURI") ? view.getMember(context, "meta_objectURI").getValue(context) == null : true;

		JSONObject res = new JSONObject();
		Map<String, ? extends IMendixObjectMember<?>> members = view.getMembers(context);
		IMetaObject metaObject=view.getMetaObject();
		Collection<? extends IMetaAssociation> memberAssociationList=metaObject.getDeclaredMetaAssociationsChild();
		for (Map.Entry<String, ? extends IMendixObjectMember<?>> e : members.entrySet()) {
			if (skipEmpty && isNullOrEmpty(e.getValue().getValue(context)) && !e.getKey().endsWith("Deferred")) {
				continue;
			}

			serializeMember(context, res, getTargetMemberName(context, view, e.getKey()), e.getValue(), view.getMetaObject(), memberAssociationList, view, groupSupport);
		}

		return res;
	}

	private static String getTargetMemberName(IContext context, IMendixObject view, String sourceAttr ) {
		String name = getShortMemberName(sourceAttr);
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalStateException("During JSON serialization: Object of type '" + view.getType() + "', member '" + sourceAttr + "' has a corresponding '_jsonkey' attribute, but its value is empty.");
		}

		name = name.replace("odata_", "");
		return name;
	}


	@SuppressWarnings("deprecation")
	private static void serializeMember(IContext context, JSONObject target, String targetMemberName,
			IMendixObjectMember<?> member, IMetaObject viewType, Collection<? extends IMetaAssociation> associationList, IMendixObject view, Boolean groupSupport) throws Exception {

		String parentMemberName = StringUtils.substringAfterLast(view.getMetaObject().getName(),".");
		String memberName = member.getName();
		if (memberName.endsWith("Deferred") && groupSupport) { // check for the association and see if
																// there is a member associated to this object...
			String memberDefferedName = memberName.replace("Deferred", "");
			String memberAssociationToMatch = memberDefferedName.concat("_").concat(parentMemberName);
			for (IMetaAssociation iterator : associationList) {
				if (iterator.getName().contains(memberAssociationToMatch)) {
					List<IMendixObject> associatedMembers = Core.retrieveByPath(context, view, iterator.toString());
					if (!associatedMembers.isEmpty()) {
						JSONArray associatedMemberArray = new JSONArray();
						for (IMendixObject asMember : associatedMembers) {
							associatedMemberArray.put(writeMendixObjectToJson(context, asMember, groupSupport));
						}
						
						if(isOneToOneAssociation(iterator)){
							target.put(memberDefferedName, associatedMemberArray.getJSONObject(0));
						}
						else{
							target.put(memberDefferedName, associatedMemberArray);
						}
					}
				}
			}
		}

		if (isSystemAttribute(memberName)) {
			return;
		}

		//Primitive?
		Object value = member.getValue(context);
		if (!(member instanceof MendixObjectReference) && !(member instanceof MendixObjectReferenceSet)) {

			switch(viewType.getMetaPrimitive(member.getName()).getType().name()) {
			case "AutoNumber":
			case "Boolean":
			case "Currency":
			case "Float":
			case "Integer":
				if (value == null) {
					// Numbers or bools could be null in json, technically.
					// Mendix supports it as well. Technically.
					target.put(targetMemberName, "");
				} else {
					target.put(targetMemberName, value);
				}
				break;
			case "Enum":
				//Support for built-in BooleanValue enumeration.
				MendixEnum me = (MendixEnum) member;
				if ("odataconnectors.BooleanValue".equals(me.getEnumeration().getName())) {
					if (BooleanValue._true.toString().equals(me.getValue(context))) {
						target.put(targetMemberName, true);
					} else if (BooleanValue._false.toString().equals(me.getValue(context))) {
						target.put(targetMemberName, false);
					}
					break;
				}
				//other enumeration, fall trough intentional

			case "HashString":
			case "String":
				if (value == null) {
					target.put(targetMemberName, "");
				} else {
					target.put(targetMemberName, value);
				}
				break;
			case "Long":
			case "Decimal":
				if (value == null) {
					target.put(targetMemberName, "");
				} else {
					target.put(targetMemberName,  value.toString());
				}
				break;

			case "DateTime":
				if (value == null) {
					target.put(targetMemberName, JSONObject.NULL);
				} else {
					target.put(targetMemberName, new ODataDateTime((Date) value)); //"/Date(1500415200000)/"
				}
				break;

			case "Binary":
				break;
			default:
				throw new IllegalStateException("Not supported Mendix Membertype for member " + memberName);
			}

		} else if (member instanceof MendixObjectReference) {
			// referenced objects 'can' be complex types. check this.
			if (value == null) {
				return;
			}

			// Getting an object from the reference like this doesn't seem to work, the result is always null
			// MendixObjectReference referenceMember = (MendixObjectReference) member;
			// IMendixObject referencedObject = referenceMember.getValue(context).getObject();

			IMendixObject referencedObject = Core.retrieveId(context, (IMendixIdentifier) value);
			if (referencedObject != null
					&& referencedObject.getMetaObject().getSuperObjects()
						.stream()
						.filter(s -> s.getName().equals(ComplexType.entityName))
						.count() == 1) {

				//convert to JSON object, but only to do this for referenced complex types
				JSONObject complexAttributeJSON = writeMendixObjectToJson(context, referencedObject,groupSupport);
				if (complexAttributeJSON != null) {
					String jsonMemberName = targetMemberName.substring(0, targetMemberName.indexOf("_"));
					target.put(jsonMemberName, complexAttributeJSON);
				}
			}
		}

		else if (member instanceof MendixObjectReferenceSet) {
			//do nothing

		} else {
			throw new IllegalStateException("Unimplemented membertype " + member.getClass().getSimpleName());
		}
	}
	
	private static boolean isOneToOneAssociation(IMetaAssociation association) {
		AssociationOwner associationOwner = association.getOwner();
		AssociationType associationType = association.getType();
		boolean isOneToOne = AssociationOwner.BOTH == associationOwner && AssociationType.REFERENCE == associationType;
		return isOneToOne;
	}
	public static boolean isSystemAttribute(String key) {
		return "createdDate".equals(key)
			|| "changedDate".equals(key)
			|| "System.owner".equals(key)
			|| "System.changedBy".equals(key)
			// odata fields
			|| "meta_objectURI".equals(key)
			|| "meta_etag".equals(key)
			|| key.endsWith("Deferred");
	}

	public static boolean hasDataAccess(IMetaObject meta, IContext context) {
		return meta.hasMetaDataAccess(context);
	}

	public static String getShortMemberName(String memberName) {
		return memberName.replaceFirst("^.+\\.", "");
	}

	protected static boolean isNullOrEmpty(Object value) {
		if (value instanceof String) {
			return isNullOrEmpty((String) value);
		}
		return value == null;
	}

	protected static boolean isNullOrEmpty(String value) {
		return value == null || "".equals(value);
	}

}
