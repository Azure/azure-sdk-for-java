//  Copyright (c) Microsoft Corporation. All rights reserved.
//  Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka.jaas;

import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.convertJaasStringToMap;

public final class JaasResolver {

	private JaasResolver() {
	}

	public static Jaas resolve(String jaasConfig) {
		Jaas jaas = new Jaas(OAuthBearerLoginModule.class.getName());
		jaas.setOptions(convertJaasStringToMap(jaasConfig));
		return jaas;
	}

//	static Map<String, String> convertJaasStringToMap(String source) {
//		if (source == null || !source.startsWith(AzureKafkaPropertiesUtils.SASL_JAAS_CONFIG_OAUTH_PREFIX) || !source.endsWith(";")) {
//			return Collections.EMPTY_MAP;
//		}
//		Map<String, String> map = Arrays.stream(source.substring(0, source.length() - 1).split(" "))
//			.filter(str -> str.contains("="))
//			.map(str -> str.split("=", 2))
//			.collect(Collectors.toMap(s -> s[0], s -> {
//				if (s[1].length() > 2 && s[1].startsWith("\"") && s[1].endsWith("\"")) {
//					return s[1].substring(1, s[1].length() - 1);
//				}
//				return null;
//			}));
//		Map<String, String> converted = new HashMap<>();
//		AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.getPropertyKeys()
//			.forEach(k -> PROPERTY_MAPPER.from(map.get(k)).to(v -> converted.put(k, v)));
//
//		return converted;
//	}
}
