package com.azure.spring.cloud.feature.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.azure.spring.cloud.feature.manager.entities.DynamicFeature;
import com.azure.spring.cloud.feature.manager.entities.FeatureVariant;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

class DynamicFeatureDeserializer extends FeatureDeserializer<DynamicFeature> {

	private static final long serialVersionUID = 1L;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	DynamicFeatureDeserializer() {
		this(null);

		SimpleModule module = new SimpleModule();
		module.addDeserializer(FeatureVariant.class, new FeatureVariantDeserializer());
		MAPPER.registerModule(module);
	}

	DynamicFeatureDeserializer(Class<?> vc) {
		super(vc);

		SimpleModule module = new SimpleModule();
		module.addDeserializer(FeatureVariant.class, new FeatureVariantDeserializer());
		MAPPER.registerModule(module);
	}

	@Override
	public DynamicFeature deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		JsonNode node = jp.getCodec().readTree(jp);

		DynamicFeature dynamicFeature = new DynamicFeature();

		Map<String, JsonNode> variantNameMap = new HashMap<>();

		Map<String, FeatureVariant> variantMap = new LinkedHashMap<>();

		// Converts Values from kabab case to camel case
		node.fieldNames().forEachRemaining(variantName -> {
			variantNameMap.put(translateLowerCaseWithSeparator(variantName), node.get(variantName));
		});

		dynamicFeature.setAssigner(setupValue(variantNameMap, "assigner"));

		if (variantNameMap.containsKey("variants")) {
			
			JsonNode variants = variantNameMap.get("variants");
			
			variants.fieldNames().forEachRemaining(variant -> {
				System.out.println(variant);
				variantMap.put(variant, MAPPER.convertValue(variants.get(variant), FeatureVariant.class));
			});
		}

		dynamicFeature.setVariants(variantMap);

		return dynamicFeature;
	}

}
