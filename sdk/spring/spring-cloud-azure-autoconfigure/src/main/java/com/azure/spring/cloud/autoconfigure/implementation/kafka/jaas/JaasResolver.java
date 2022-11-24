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

}
