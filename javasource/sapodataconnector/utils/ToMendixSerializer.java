package sapodataconnector.utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaAssociation;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive;
import com.mendix.thirdparty.org.json.JSONArray;
import com.mendix.thirdparty.org.json.JSONObject;

import sapodataconnector.proxies.OdataObject;
import sapodataconnector.proxies.ResultInfo;

public class ToMendixSerializer {

	// this method is being called both when we want to do a getlist and when we get a list of results after executing a function
	// TODO: refactor and merge with addComplexOrExtractedODataObject
	public static void createNewMendixObjectList(IContext context, String responseType, List<IMendixObject> responseList, ResultInfo targetRoot,
			String responseJson) {
		JSONObject jsonD = new JSONObject(responseJson).getJSONObject("d");
		handleCount(jsonD, targetRoot);

		JSONArray responseJsonArray = jsonD.getJSONArray("results");
		for (JSONObject jsonItem : responseJsonArray.toJSONObjectCollection()) {

			IMendixObject newOdataObject = Core.instantiate(context, responseType);
			IMendixObject optionalTargetRoot = targetRoot != null ? targetRoot.getMendixObject() : null;
			updateMendixObject(context, jsonItem, newOdataObject, optionalTargetRoot);
			if (responseList != null) {
				responseList.add(newOdataObject);
			}
		}
	}

	public static void updateMendixObject(IContext context, JSONObject item, IMendixObject newOdataObject, IMendixObject targetRoot) {
		if (targetRoot != null) {
			newOdataObject.setValue(context, OdataObject.MemberNames.OdataObject_ResultInfo.toString(), targetRoot.getId());
		}
		updateMendixObject(context, item, newOdataObject);
	}

	public static void updateMendixObject(IContext context, JSONObject item, IMendixObject newOdataObject) {
		IMetaObject metaObject = newOdataObject.getMetaObject();

		for (Iterator<String> jsonNames = item.keys(); jsonNames.hasNext(); ) {
			String jsonName = jsonNames.next();
			if (item.isNull(jsonName)) {
				// we can skip null json value
				continue;
			}

			// find the Mendix Object attribute (primitive, deferred, meta object, or complex type)
			if (jsonName.equals("__metadata")) {
				// handle meta data attributes
				JSONObject metadata = item.getJSONObject("__metadata");
				if (metadata.has("uri")) {
					newOdataObject.setValue(context, OdataObject.MemberNames.meta_objectURI.toString(), metadata.get("uri"));
				}
				if (metadata.has("etag")) {
					newOdataObject.setValue(context, OdataObject.MemberNames.meta_etag.toString(), metadata.get("etag"));
				}


			} else if (item.isJSONObject(jsonName)) {
				// json attribute is an object which could be a deferred, embedded or complex type
				JSONObject attributeObject = item.getJSONObject(jsonName);
				if (attributeObject.has("__deferred")) {
					// handle deferred type
					JSONObject deferred = attributeObject.getJSONObject("__deferred");
					String mendixAttrName = jsonName + "Deferred";
					if (metaObject.getMetaPrimitive(mendixAttrName) != null) {
						newOdataObject.setValue(context, mendixAttrName, deferred.get("uri"));
					}

				} else if (attributeObject.has("__metadata")) {
					// handle complex type. TODO: check if this condition ever happens
					addComplexOrExtractedODataObject(context, newOdataObject, metaObject, attributeObject, jsonName);
				} else if (attributeObject.has("results")) {
					// this is for the case that the response has an expanded list
					 JSONArray resultList = attributeObject.getJSONArray("results");
					for (JSONObject expandedItem : resultList.toJSONObjectCollection()) {
						// get the type of the item, we can in future expand this to the whole connector so that we don't have to ask the users to provide us with the type
						// TODO: merge this at some point when we get the type from the response with createNewMendixObjectList method
						addComplexOrExtractedODataObject(context, newOdataObject, metaObject, expandedItem, jsonName);
					}
				}

			} else {
				// handle primitives

				// find JSON attribute in the Mendix Object
				IMetaPrimitive mendixAttrPrimitive = metaObject.getMetaPrimitive(jsonName);
				if (mendixAttrPrimitive == null) {
					mendixAttrPrimitive = metaObject.getMetaPrimitive("odata_" + jsonName);
				}

				if (mendixAttrPrimitive != null) {
					switch (mendixAttrPrimitive.getType()) {
					case Boolean:
						Boolean booleanValue = item.getBoolean(jsonName);
						newOdataObject.setValue(context, mendixAttrPrimitive.getName(), booleanValue);
						break;
					case DateTime:
						String timestamp = item.getString(jsonName).replace("/Date(", "").replace(")/", "");
						newOdataObject.setValue(context, mendixAttrPrimitive.getName(), new Date(Long.valueOf(timestamp)));
						break;
					case Decimal:
						newOdataObject.setValue(context, mendixAttrPrimitive.getName(), new BigDecimal(item.getString(jsonName)));
						break;
					case Enum:
						break;
					case Long:
						newOdataObject.setValue(context, mendixAttrPrimitive.getName(), Long.valueOf(item.getString(jsonName)));
						break;
					case String:
						newOdataObject.setValue(context, mendixAttrPrimitive.getName(), item.getString(jsonName));
						break;
					case AutoNumber:
						break;
					case Binary:
						break;
					case HashString:
						break;
					case Integer:
						newOdataObject.setValue(context, mendixAttrPrimitive.getName(), item.getInt(jsonName));
						break;
					default:
						break;
					}
				}

			}
		}
	}

	private static void addComplexOrExtractedODataObject(IContext context, IMendixObject newOdataObject,
			IMetaObject metaObject, JSONObject expandedItem, String jsonName) {
		JSONObject metadata = expandedItem.getJSONObject("__metadata");
		String expandTypeName = metadata.has("type") ? metadata.getString("type") : null;
		IMendixObject newOdataExpandObject = getTargetMendixEntity(context, metaObject.getModuleName(), expandTypeName);
		updateMendixObject(context, expandedItem, newOdataExpandObject);
		
		String targetEntityName = StringUtils.substringAfterLast(newOdataExpandObject.getType(), ".");
		String parentEntityName = StringUtils.substringAfterLast(newOdataObject.getType(), ".");
		
		String itemAssociationName = jsonName + "_" + targetEntityName + "_" + parentEntityName;
		Boolean foundAssociation = updateParentAssociationUsingAssociationName(context, newOdataObject, newOdataExpandObject, itemAssociationName);
		if (!foundAssociation) {
			// TODO: discuss a better way for supporting both sides of the association
			// It will try to find the association in the reverse order, if it was unable to find the association at the first try.
			itemAssociationName = jsonName + "_" + parentEntityName + "_" + targetEntityName;
			updateParentAssociationUsingAssociationName(context, newOdataExpandObject, newOdataObject, itemAssociationName);
		}
	}
	
	public static void setAssociationWithParent(IContext context, List<IMendixObject> resultList, OdataObject parent, String associationName) {
		if(parent!=null) {
			associationName = StringUtils.substringAfterLast(associationName, ".");
			for (IMendixObject odataObject : resultList) {
				if (associationName != null && !associationName.isEmpty()) {
					updateParentAssociationUsingAssociationName(context, parent.getMendixObject(), odataObject, associationName);
				}
				else { 
					updateParentAssociation(context, parent.getMendixObject(), odataObject);
				}
			}
		}
	}
	
	protected static boolean updateParentAssociationUsingAssociationName(IContext context, IMendixObject parent, IMendixObject target, String itemAssociationName) {
		for (IMetaAssociation association : target.getMetaObject().getDeclaredMetaAssociationsParent()) {
			String associationName = StringUtils.substringAfterLast(association.getName(),".");
			if ( associationName.equals(itemAssociationName) ) {			
				target.setValue(context, association.getName(), parent.getId());
				return true;	
			}
		}
		return false;
	}

	protected static boolean updateParentAssociation(IContext context, IMendixObject parent, IMendixObject target) {
		for (IMetaAssociation association : target.getMetaObject().getDeclaredMetaAssociationsParent()) {

			if ( association.getChild().getName().equals(parent.getType())
					&& !(parent.getType().equals(target.getType())) ) { 	// Skip when 'parent' and 'target' are of same type
				target.setValue(context, association.getName(), parent.getId());
				return true;
			}
		}
		return false;
	}

	/**
	 * The JSON __metadata object exposes the type name from the schema as MODULE_NAME.TYPE_NAME. Remove the type
	 * name and then try to find an entity with TypeName in this module.
	 *
	 * @param context The current mendix context.
	 * @param moduleName The module in which to search the target entity type
	 * @param jsonTypeName The JSON type name
	 * @param An instance of the target type
	 */
	protected static IMendixObject getTargetMendixEntity(IContext context, String moduleName, String jsonTypeName) {
		String mendixEntityName = moduleName + jsonTypeName.substring(jsonTypeName.indexOf("."));
		return Core.instantiate(context, mendixEntityName);
	}

	protected static void handleCount(JSONObject d, ResultInfo odataRoot) {
		if (d.has("__count")) {
			String countStr = d.optString("__count");
			try {
				odataRoot.settotalCount(Integer.valueOf(countStr));
			} catch (Exception ex) {
				// can't parse count value, ignore
			}

		}
	}


}
