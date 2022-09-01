// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.configuration;

import com.azure.spring.cloud.autoconfigure.aad.AadAuthenticationFilterAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cResourceServerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AadB2cOAuth2ClientConfiguration;
import com.nimbusds.jose.util.ResourceRetriever;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestOperations;

import java.util.Arrays;

/**
 * Configure RestOperation bean used for accessing Azure AD and Azure AD B2C
 */
@Configuration(proxyBeanMethods = false)
@Import(RestTemplateAutoConfiguration.class)
public class AadRestOperationConfiguration {

    /**
     * Declare {@link RestOperations} bean which will be used when send http request to Azure AD and Azure AD B2C.
     * <br/>
     * For example:
     * <ul>
     *     <li> In {@link ResourceRetriever} declared in {@link AadAuthenticationFilterAutoConfiguration}.
     *     <li> In kinds of {@link OAuth2AccessTokenResponseClient} bean(like
     *     {@link DefaultAuthorizationCodeTokenResponseClient}) declared in {@link AadOAuth2ClientConfiguration} and
     *     {@link AadB2cOAuth2ClientConfiguration}.
     *     <li> In {@link JwtDecoder} declared in {@link AadResourceServerConfiguration} and
     *     {@link AadB2cResourceServerAutoConfiguration}
     * </ul>
     *
     * @param builder The RestTemplateBuilder bean, which can be provided in {@link RestTemplateAutoConfiguration}
     *                if customer not provide this bean.
     * @return RestOperations bean
     */
    @Bean
    @ConditionalOnMissingBean(RestOperations.class)
    public RestOperations aadAuthRestOperations(RestTemplateBuilder builder) {
        return builder.build();
    }

    /**
     * Declare {@link HttpMessageConverters} bean which can be used in {@link RestTemplateAutoConfiguration}.
     * <br/>
     * {@link FormHttpMessageConverter} and {@link OAuth2AccessTokenResponseHttpMessageConverter} is necessary for
     * kinds of {@link OAuth2AccessTokenResponseClient} implementations(like
     * {@link DefaultAuthorizationCodeTokenResponseClient})
     *
     * @return HttpMessageConverters bean
     */
    @Bean
    @ConditionalOnMissingBean(HttpMessageConverters.class)
    public HttpMessageConverters httpMessageConverters() {
        return new HttpMessageConverters(
                Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
    }

}
