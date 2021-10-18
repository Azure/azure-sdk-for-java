// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.security.oauth2.client.endpoint;

import com.azure.spring.core.ApplicationId;
import com.azure.spring.security.oauth2.client.OtherClientRefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * This class is used to add "scope" parameter when request for an access token. <br/>
 * <br/>
 *
 * This is mainly used to support {@link OtherClientRefreshTokenOAuth2AuthorizedClientProvider} to make sure scopes
 * from a single resource when request an access token. <br/>
 * Sample code of {@link AddScopeOAuth2AuthorizationCodeGrantRequestEntityConverter} usage:
 * <pre>
 * <code>
 *
 *     public class SampleWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
 *
 *        {@literal @}Override
 *         protected void configure(HttpSecurity http) throws Exception {
 *             http.oauth2Login()
 *                     .tokenEndpoint()
 *                         .accessTokenResponseClient(accessTokenResponseClient())
 *                         .and();
 *         }
 *
 *         protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
 *             Set<String> scopes = new HashSet<>(Arrays.asList("scope1", "scope2"));
 *             DefaultAuthorizationCodeTokenResponseClient result = new DefaultAuthorizationCodeTokenResponseClient();
 *                 result.setRequestEntityConverter(
 *                     new AddScopeOAuth2AuthorizationCodeGrantRequestEntityConverter("client1", scopes);
 *             return result;
 *         }
 *     }
 * </code>
 * </pre>
 *
 * @author RujunChen
 * @see OtherClientRefreshTokenOAuth2AuthorizedClientProvider
 * @since 4.0
 */
public class AddScopeOAuth2AuthorizationCodeGrantRequestEntityConverter
    extends OAuth2AuthorizationCodeGrantRequestEntityConverter {

    private final String targetClientRegistrationId;
    private final Set<String> scopes;

    public AddScopeOAuth2AuthorizationCodeGrantRequestEntityConverter(
        String targetClientRegistrationId, Set<String> scopes) {
        this.targetClientRegistrationId = targetClientRegistrationId;
        this.scopes = scopes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
        RequestEntity<?> requestEntity = super.convert(request);
        Assert.notNull(requestEntity, "requestEntity can not be null");
        HttpHeaders httpHeaders = createHttpHeaders();
        httpHeaders.putAll(requestEntity.getHeaders());
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) requestEntity.getBody();
        Assert.notNull(body, "body can not be null");
        body.putAll(createScopesMap(request));
        return new RequestEntity<>(body, httpHeaders, requestEntity.getMethod(), requestEntity.getUrl());
    }

    /**
     * Additional headers information used to record current client information.
     *
     * @return HttpHeaders
     */
    public HttpHeaders createHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("x-client-SKU", Collections.singletonList(ApplicationId.AZURE_SPRING_AAD));
        httpHeaders.put("x-client-VER", Collections.singletonList(ApplicationId.VERSION));
        httpHeaders.put("client-request-id", Collections.singletonList(UUID.randomUUID().toString()));
        return httpHeaders;
    }

    /**
     * Add "scope" parameter if the clientRegistration is targetClientRegistration
     *
     * @param request OAuth2AuthorizationCodeGrantRequest
     * @return MultiValueMap
     */
    public MultiValueMap<String, String> createScopesMap(OAuth2AuthorizationCodeGrantRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        String scopeValue = String.join(" ", isTargetClientRegistration(request)
            ? scopes
            : request.getClientRegistration().getScopes());
        body.add("scope", scopeValue);
        return body;
    }

    private boolean isTargetClientRegistration(OAuth2AuthorizationCodeGrantRequest request) {
        return request.getClientRegistration().getRegistrationId().equals(targetClientRegistrationId);
    }
}
