package sapodataconnector.actions;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.thirdparty.org.json.JSONObject;

public class TypeMapping {

	private static ConcurrentHashMap<String, Map<String, String>> entityMapping = new ConcurrentHashMap<>();
	private final static String METAMODEL = ".MetaModel";
	private final static String TYPEMAPPING_ATTR = "TypeMapping";

	public static Map<String, String> getEntityTypes(IContext context, IMendixObject odataEntity) {
		Map<String, String> entityType = entityMapping.get(odataEntity.getMetaObject().getName().toLowerCase());
		if (entityType == null) {
			// parse MetaModel
			parseMetaModel(context, odataEntity.getMetaObject().getModuleName());
			entityType = entityMapping.computeIfAbsent(odataEntity.getMetaObject().getName().toLowerCase(),
					(key) -> { return Collections.EMPTY_MAP; });
		}

		return entityType;
	}

	protected static void parseMetaModel(IContext context, String module) {
		IMendixObject metaModel = Core.instantiate(context, module + METAMODEL);
		if (metaModel == null) {
			return;
		}

		String typeMappingInJson = metaModel.getValue(context, TYPEMAPPING_ATTR);

		JSONObject typeMapping = new JSONObject(typeMappingInJson);
		typeMapping.keys().forEachRemaining((entityName) -> {
			JSONObject entity = typeMapping.getJSONObject(entityName);

			Map<String, String> entityAttributes = new ConcurrentHashMap<>();
			entity.keys().forEachRemaining((attributeName) -> {
				entityAttributes.put(attributeName.toLowerCase(), entity.getString(attributeName));
			});

			entityMapping.put(module.toLowerCase() + "." + entityName.toLowerCase(), entityAttributes);
		});
	}
}
