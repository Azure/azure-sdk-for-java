// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.filter;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.graph.Membership;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.graph.Memberships;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AuthorityPrefix;
import com.azure.spring.cloud.autoconfigure.implementation.aad.utils.JacksonObjectMapperFactory;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.properties.AadAuthorizationServerEndpoints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.client.RestOperations;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.utils.AadRestTemplateCreator.createRestTemplate;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.Constants.DEFAULT_AUTHORITY_SET;


/**
 * Microsoft Graph client encapsulation.
 */
class AadGraphClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadGraphClient.class);
    private static final String MICROSOFT_GRAPH_SCOPE = "User.Read";
    // We use "aadfeed5" as suffix when client library is ADAL, upgrade to "aadfeed6" for MSAL
    private static final String REQUEST_ID_SUFFIX = "aadfeed6";

    private final String clientId;
    private final String clientSecret;
    private final AadAuthorizationServerEndpoints endpoints;
    private final AadAuthenticationProperties aadAuthenticationProperties;
    private RestOperations operations;

    /**
     * Creates a new instance of {@link AadGraphClient}.
     *
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @param aadAuthenticationProperties the AAD authentication properties
     * @param endpoints the AAF authorization server endpoints
     */
    AadGraphClient(String clientId,
                          String clientSecret,
                          AadAuthenticationProperties aadAuthenticationProperties,
                          AadAuthorizationServerEndpoints endpoints,
                          RestTemplateBuilder restTemplateBuilder) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.aadAuthenticationProperties = aadAuthenticationProperties;
        this.endpoints = endpoints;
        this.operations = createRestTemplate(restTemplateBuilder);
    }

    void setRestOperations(RestOperations operations) {
        this.operations = operations;
    }

    private String getUserMemberships(String accessToken, String urlString) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = operations.exchange(urlString, HttpMethod.GET, entity, String.class);
        String responseInJson = response.getBody();
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new IllegalStateException(
                    "Response is not " + HttpStatus.OK + ", response json: " + responseInJson);
        }
    }

    /**
     * @param graphApiToken token used to access graph api.
     * @return groups in graph api.
     * @throws IOException throw exception if get groups failed by IOException.
     */
    public Set<String> getGroups(String graphApiToken) throws IOException {
        final Set<String> groups = new LinkedHashSet<>();
        final ObjectMapper objectMapper = JacksonObjectMapperFactory.getInstance();
        String aadMembershipRestUri = this.aadAuthenticationProperties.getGraphMembershipUri();
        while (aadMembershipRestUri != null) {
            String membershipsJson = getUserMemberships(graphApiToken, aadMembershipRestUri);
            Memberships memberships = objectMapper.readValue(membershipsJson, Memberships.class);
            memberships.getValue()
                       .stream()
                       .filter(this::isGroupObject)
                       .map(Membership::getDisplayName)
                       .forEach(groups::add);
            aadMembershipRestUri = Optional.of(memberships)
                                           .map(Memberships::getOdataNextLink)
                                           .orElse(null);
        }
        return groups;
    }

    private boolean isGroupObject(final Membership membership) {
        return membership.getObjectType().equals(Membership.OBJECT_TYPE_GROUP);
    }

    /**
     * Converts a set of groups to their granted authority set.
     *
     * @param groups a set of groups
     * @return the granted authority set
     */
    public Set<SimpleGrantedAuthority> toGrantedAuthoritySet(final Set<String> groups) {
        Set<SimpleGrantedAuthority> grantedAuthoritySet =
            groups.stream()
                  .filter(aadAuthenticationProperties::isAllowedGroup)
                  .map(group -> new SimpleGrantedAuthority(AuthorityPrefix.ROLE + group))
                  .collect(Collectors.toSet());
        return Optional.of(grantedAuthoritySet)
                       .filter(g -> !g.isEmpty())
                       .orElse(DEFAULT_AUTHORITY_SET);
    }

    /**
     * Acquire access token for calling Graph API.
     *
     * @param idToken The token used to perform an OBO request.
     * @param tenantId The tenant id.
     * @return The access token for Graph service.
     * @throws ServiceUnavailableException If fail to acquire the token.
     * @throws MsalServiceException If {@link MsalServiceException} has occurred.
     */
    public IAuthenticationResult acquireTokenForGraphApi(String idToken, String tenantId)
        throws ServiceUnavailableException {
        final IClientCredential clientCredential =
            ClientCredentialFactory.createFromSecret(clientSecret);
        final UserAssertion assertion = new UserAssertion(idToken);
        IAuthenticationResult result = null;
        try {
            final ConfidentialClientApplication application = ConfidentialClientApplication
                .builder(clientId, clientCredential)
                .authority(endpoints.getBaseUri() + tenantId + "/")
                .correlationId(getCorrelationId())
                .build();
            final Set<String> scopes = new HashSet<>();
            scopes.add(MICROSOFT_GRAPH_SCOPE);
            final OnBehalfOfParameters onBehalfOfParameters = OnBehalfOfParameters.builder(scopes, assertion).build();
            result = application.acquireToken(onBehalfOfParameters).get();
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted during acquiring token for graph API!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (ExecutionException | MalformedURLException e) {
            // Handle conditional access policy, step 1.
            final Throwable cause = e.getCause();
            if (cause instanceof MsalServiceException) {
                final MsalServiceException exception = (MsalServiceException) cause;
                if (exception.claims() != null && !exception.claims().isEmpty()) {
                    throw exception;
                }
            }
            LOGGER.error("acquire on behalf of token for graph api error", e);
        }
        if (result == null) {
            throw new ServiceUnavailableException("unable to acquire on_behalf_of token for client "
                + clientId);
        }
        return result;
    }

    private static String getCorrelationId() {
        final String uuid = UUID.randomUUID().toString();
        return uuid.substring(0, uuid.length() - REQUEST_ID_SUFFIX.length()) + REQUEST_ID_SUFFIX;
    }
}
