package com.azure.spring.cloud.feature.manager;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

abstract class FeatureDeserializer<T> extends StdDeserializer<T> {

	private static final long serialVersionUID = 1L;

	FeatureDeserializer() {
		this(null);
	}

	FeatureDeserializer(Class<?> vc) {
		super(vc);
	}

	String setupValue(Map<String, JsonNode> variantNameMap, String property) {
		JsonNode defaultNode = JsonNodeFactory.instance.objectNode();

		return variantNameMap.getOrDefault(property, defaultNode).asText(null);
	}

	String translateLowerCaseWithSeparator(final String input) {
		if (input == null) {
			return input;
		}
		final int length = input.length();
		if (length == 0) {
			return input;
		}

		final StringBuilder result = new StringBuilder(length + (length >> 1));

		for (int i = 0; i < length - 1; i++) {
			char ch = input.charAt(i);
			char next = input.charAt(i + 1);
			char uc = Character.toUpperCase(next);

			if ("-".equals(String.valueOf(ch))) {
				result.append(uc);
				i++;
			} else {
				result.append(ch);
			}
		}
		result.append(input.charAt(length - 1));
		return result.toString();
	}

}
