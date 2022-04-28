package com.azure.spring.cloud.feature.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.azure.spring.cloud.feature.manager.entities.FeatureVariant;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FeatureVariantDeserializer extends FeatureDeserializer<FeatureVariant> {

	private static final long serialVersionUID = 1L;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public FeatureVariantDeserializer() {
		this(null);
	}

	protected FeatureVariantDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public FeatureVariant deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		JsonNode node = jp.getCodec().readTree(jp);

		FeatureVariant variant = new FeatureVariant();

		Map<String, JsonNode> variantPropertyMap = new HashMap<>();

		node.fieldNames().forEachRemaining(variantName -> {
			variantPropertyMap.put(translateLowerCaseWithSeparator(variantName), node.get(variantName));
		});

		variant.setName(setupValue(variantPropertyMap, "name"));
		variant.setConfigurationReference(setupValue(variantPropertyMap, "configurationReference"));
		variant.setAssignmentParameters(
				MAPPER.convertValue(variantPropertyMap.get("assignmentParameters"), AssignmentParameterType.getType()));

		JsonNode defaultNode = variantPropertyMap.get("default");
		Boolean isDefault = false;

		if (defaultNode != null) {
			isDefault = defaultNode.asBoolean();
		}

		variant.setDefault(isDefault);

		return variant;
	}
	
	static class AssignmentParameterType {
		
		static TypeReference<LinkedHashMap<String, Object>> getType(){
			return new TypeReference<LinkedHashMap<String, Object>>() {
			};
		}
		
	}

}