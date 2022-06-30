// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryCredentialsPolicy;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryTokenRequestContext;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryTokenService;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContainerRegistryCredentialPolicySyncTests {

    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String AUTHENTICATE_HEADER = "Bearer realm=\"https://mytest.azurecr.io/oauth2/token\",service=\"mytest.azurecr.io\",scope=\"registry:catalog:*\",error=\"invalid_token\"";
    public static final Integer UNAUTHORIZED = 401;
    public static final Integer SUCCESS = 200;
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer";
    public static final String SERVICENAME = "mytest.azurecr.io";
    public static final String SCOPENAME = "registry:catalog:*";

    private ContainerRegistryTokenService service;
    private HttpRequest request;
    private HttpResponse unauthorizedHttpResponse;
    private HttpResponse unauthorizedHttpResponseWithoutHeader;
    private HttpPipelineCallContext callContext;
    private HttpResponse successResponse;
    private HttpPipelineNextSyncPolicy nextPolicy;
    private HttpPipelineNextSyncPolicy nextClonePolicy;

    @BeforeEach
    public void setup() {
        AccessToken accessToken = new AccessToken("tokenValue", OffsetDateTime.now().plusMinutes(30));

        ContainerRegistryTokenService mockService = mock(ContainerRegistryTokenService.class);
        when(mockService.getTokenSync(any(ContainerRegistryTokenRequestContext.class))).thenReturn(accessToken);

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://mytest.azurecr.io");

        HttpPipelineCallContext context = mock(HttpPipelineCallContext.class);
        when(context.getHttpRequest()).thenReturn(request);

        MockHttpResponse unauthorizedResponseWithHeader = new MockHttpResponse(
            mock(HttpRequest.class),
            UNAUTHORIZED,
            new HttpHeaders().set(WWW_AUTHENTICATE, AUTHENTICATE_HEADER)
        );

        MockHttpResponse unauthorizedResponseWithoutHeader = new MockHttpResponse(
            mock(HttpRequest.class),
            UNAUTHORIZED);

        MockHttpResponse successResponse = new MockHttpResponse(
            mock(HttpRequest.class),
            SUCCESS
        );

        HttpPipelineNextSyncPolicy mockNextClone = mock(HttpPipelineNextSyncPolicy.class);
        when(mockNextClone.processSync()).thenReturn(successResponse);
        HttpPipelineNextSyncPolicy mockNext = mock(HttpPipelineNextSyncPolicy.class);
        when(mockNext.clone()).thenReturn(mockNextClone);
        when(mockNext.processSync()).thenReturn(unauthorizedResponseWithHeader);

        this.service = mockService;
        this.unauthorizedHttpResponse = unauthorizedResponseWithHeader;
        this.unauthorizedHttpResponseWithoutHeader = unauthorizedResponseWithoutHeader;
        this.callContext = context;
        this.request = request;
        this.successResponse = successResponse;
        this.nextClonePolicy = mockNextClone;
        this.nextPolicy = mockNext;
    }

    @Test
    public void requestNoRetryOnOtherErrorCodes() {
        ContainerRegistryCredentialsPolicy policy = new ContainerRegistryCredentialsPolicy(this.service);
        ContainerRegistryCredentialsPolicy spyPolicy = Mockito.spy(policy);

        when(nextPolicy.processSync()).thenReturn(successResponse);
        policy.processSync(this.callContext, this.nextPolicy);

        // Make sure no call being done to the authorize request.
        verify(spyPolicy, times(0)).setAuthorizationHeader(any(HttpPipelineCallContext.class), any(ContainerRegistryTokenRequestContext.class));

        when(nextPolicy.processSync()).thenReturn(unauthorizedHttpResponseWithoutHeader);
        policy.processSync(this.callContext, this.nextPolicy);

        // Make sure no call being done to the authorize request.
        verify(spyPolicy, times(0)).setAuthorizationHeaderSync(any(HttpPipelineCallContext.class), any(ContainerRegistryTokenRequestContext.class));
    }

    @Test
    public void requestAddBearerTokenToRequest() {
        ContainerRegistryCredentialsPolicy policy = new ContainerRegistryCredentialsPolicy(this.service);
        ContainerRegistryCredentialsPolicy spyPolicy = Mockito.spy(policy);
        boolean onChallenge = spyPolicy.authorizeRequestOnChallengeSync(this.callContext, this.unauthorizedHttpResponse);

        // Validate that the onChallenge ran successfully.
        assertTrue(onChallenge);

        String tokenValue = this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
        assertTrue(tokenValue.endsWith(tokenValue));

        // Validate that the token creation was called with the correct arguments.
        ArgumentCaptor<ContainerRegistryTokenRequestContext> argument = ArgumentCaptor.forClass(ContainerRegistryTokenRequestContext.class);
        verify(spyPolicy).setAuthorizationHeaderSync(any(HttpPipelineCallContext.class), argument.capture());

        ContainerRegistryTokenRequestContext requestContext = argument.getValue();
        assertEquals(SERVICENAME, requestContext.getServiceName());
        assertEquals(SCOPENAME, requestContext.getScope());
    }
}
