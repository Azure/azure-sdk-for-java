//  Copyright (c) Microsoft Corporation. All rights reserved.
//  Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka.jaas;

import com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.JAAS_OPTIONS_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JaasTest {

	@Test
	void testToString() {
		Jaas jaas = new Jaas("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule");
		String jaasStr = jaas.toString();
		assertEquals(AzureKafkaPropertiesUtils.SASL_JAAS_CONFIG_OAUTH_PREFIX + AzureKafkaConfigurationUtils.AZURE_CONFIGURED_JAAS_OPTIONS + ";", jaasStr);
	}

	@Test
	void testToStringWithOptions() {
		Jaas jaas = new Jaas("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule");
		jaas.getOptions().put("azure.credential.client-id", "test");
		jaas.getOptions().put("azure.credential.managed-identity-enabled", "true");
		String jaasStr = jaas.toString();
		assertThat(jaasStr.startsWith(AzureKafkaPropertiesUtils.SASL_JAAS_CONFIG_OAUTH_PREFIX));
		assertThat(jaasStr.contains(String.format(JAAS_OPTIONS_PATTERN, "azure.credential.client-id", "test")));
		assertThat(jaasStr.contains(String.format(JAAS_OPTIONS_PATTERN, "azure.credential.managed-identity-enabled", "true")));
		assertThat(jaasStr.contains(AzureKafkaConfigurationUtils.AZURE_CONFIGURED_JAAS_OPTIONS));
	}
}
