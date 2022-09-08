// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestTemplate;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadOauth2ClientRestOperationConfiguration.AAD_OAUTH_2_CLIENT_REST_OPERATION_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadOauth2RestOperationConfigurationTestUtil.hasMessageConvertersForAccessToken;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadOauth2RestOperationConfigurationTestUtil.hasOAuth2ErrorResponseErrorHandler;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AadOauth2ClientRestOperationConfigurationTest {

    @Test
    public void testRestOperationBeanConfiguration() {
        new ApplicationContextRunner()
                .withUserConfiguration(AadOauth2ClientRestOperationConfiguration.class, RestTemplateAutoConfiguration.class)
                .run((context) -> {
                    assertThat(context).hasBean(AAD_OAUTH_2_CLIENT_REST_OPERATION_BEAN_NAME);
                    RestTemplate retrieverRestTemplate = (RestTemplate) context.getBean(AAD_OAUTH_2_CLIENT_REST_OPERATION_BEAN_NAME);
                    assertTrue(hasOAuth2ErrorResponseErrorHandler(retrieverRestTemplate));
                    assertTrue(hasMessageConvertersForAccessToken(retrieverRestTemplate));
                });
    }

}
