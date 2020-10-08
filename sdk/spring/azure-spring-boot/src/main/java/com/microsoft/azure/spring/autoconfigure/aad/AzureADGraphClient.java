// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.naming.ServiceUnavailableException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Microsoft Graph client encapsulation.
 */
public class AzureADGraphClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureADGraphClient.class);
    private static final SimpleGrantedAuthority DEFAULT_AUTHORITY = new SimpleGrantedAuthority("ROLE_USER");
    private static final String DEFAULT_ROLE_PREFIX = "ROLE_";
    private static final String MICROSOFT_GRAPH_SCOPE = "https://graph.microsoft.com/user.read";
    private static final String AAD_GRAPH_API_SCOPE = "https://graph.windows.net/user.read";
    // We use "aadfeed5" as suffix when client library is ADAL, upgrade to "aadfeed6" for MSAL
    private static final String REQUEST_ID_SUFFIX = "aadfeed6";
    private static final String V2_VERSION_ENV_FLAG = "v2-graph";

    private final String clientId;
    private final String clientSecret;
    private final ServiceEndpoints serviceEndpoints;
    private final AADAuthenticationProperties aadAuthenticationProperties;
    private final boolean graphApiVersionIsV2;

    public AzureADGraphClient(String clientId,
                              String clientSecret,
                              AADAuthenticationProperties aadAuthProps,
                              ServiceEndpointsProperties serviceEndpointsProps) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.aadAuthenticationProperties = aadAuthProps;
        this.serviceEndpoints = serviceEndpointsProps.getServiceEndpoints(aadAuthProps.getEnvironment());
        this.graphApiVersionIsV2 = aadAuthProps.getEnvironment().contains(V2_VERSION_ENV_FLAG);
    }

    private String getUserMemberships(String accessToken, String urlString) throws IOException {
        URL url = new URL(urlString);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Set the appropriate header fields in the request header.
        if (this.graphApiVersionIsV2) {
            connection.setRequestMethod(HttpMethod.GET.toString());
            connection.setRequestProperty(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
            connection.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        } else {
            connection.setRequestMethod(HttpMethod.GET.toString());
            connection.setRequestProperty("api-version", "1.6");
            connection.setRequestProperty(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
            connection.setRequestProperty(HttpHeaders.ACCEPT, "application/json;odata=minimalmetadata");
        }
        final String responseInJson = getResponseString(connection);
        final int responseCode = connection.getResponseCode();
        if (responseCode == HTTPResponse.SC_OK) {
            return responseInJson;
        } else {
            throw new IllegalStateException(
                "Response is not " + HTTPResponse.SC_OK + ", response json: " + responseInJson);
        }
    }

    private String getUrlStringFromODataNextLink(String odataNextLink) {
        if (this.graphApiVersionIsV2) {
            return odataNextLink;
        } else {
            String skipToken = odataNextLink.split("/memberOf\\?")[1];
            return serviceEndpoints.getAadMembershipRestUri() + "&" + skipToken;
        }
    }

    private static String getResponseString(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            final StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
            }
            return stringBuffer.toString();
        }
    }

    public List<UserGroup> getGroups(String graphApiToken) throws IOException {
        final List<UserGroup> userGroupList = new ArrayList<>();
        final ObjectMapper objectMapper = JacksonObjectMapperFactory.getInstance();
        String urlString = serviceEndpoints.getAadMembershipRestUri();
        while (urlString != null) {
            String responseInJson = getUserMemberships(graphApiToken, urlString);
            UserGroups userGroups = objectMapper.readValue(responseInJson, UserGroups.class);
            userGroups.getValue()
                      .stream()
                      .filter(this::isMatchingUserGroupKey)
                      .forEach(userGroupList::add);
            urlString = Optional.of(userGroups)
                                .map(UserGroups::getOdataNextLink)
                                .map(this::getUrlStringFromODataNextLink)
                                .orElse(null);
        }
        return userGroupList;
    }

    /**
     * Checks that the UserGroup has a Group object type.
     *
     * @param userGroup - userGroup
     * @return true if the json node contains the correct key, and expected value to identify a user group.
     */
    private boolean isMatchingUserGroupKey(final UserGroup userGroup) {
        return userGroup.getObjectType().equals(aadAuthenticationProperties.getUserGroup().getValue());
    }

    public Set<GrantedAuthority> getGrantedAuthorities(String graphApiToken) throws IOException {
        // Fetch the authority information from the protected resource using accessToken
        final List<UserGroup> groups = getGroups(graphApiToken);
        // Map the authority information to one or more GrantedAuthority's and add it to mappedAuthorities
        return convertGroupsToGrantedAuthorities(groups);
    }


    /**
     * Converts UserGroup list to Set of GrantedAuthorities
     *
     * @param groups user groups
     * @return granted authorities
     */
    public Set<GrantedAuthority> convertGroupsToGrantedAuthorities(final List<UserGroup> groups) {
        // Map the authority information to one or more GrantedAuthority's and add it to mappedAuthorities
        final Set<GrantedAuthority> mappedAuthorities =
            groups.stream()
                  .filter(this::isValidUserGroupToGrantAuthority)
                  .map(userGroup -> new SimpleGrantedAuthority(DEFAULT_ROLE_PREFIX + userGroup.getDisplayName()))
                  .collect(Collectors.toCollection(LinkedHashSet::new));
        if (mappedAuthorities.isEmpty()) {
            mappedAuthorities.add(DEFAULT_AUTHORITY);
        }
        return mappedAuthorities;
    }

    /**
     * Determines if this is a valid {@link UserGroup} to build to a GrantedAuthority.
     * <p>
     * If the {@link AADAuthenticationProperties.UserGroupProperties#getAllowedGroups()}
     *  contains the {@link UserGroup#getDisplayName()} return
     * true.
     *
     * @param group - User Group to check if valid to grant an authority to.
     * @return true if allowed-groups contains the UserGroup display name
     */
    private boolean isValidUserGroupToGrantAuthority(final UserGroup group) {
        return aadAuthenticationProperties.getUserGroup().getAllowedGroups().contains(group.getDisplayName());
    }

    public IAuthenticationResult acquireTokenForGraphApi(String idToken, String tenantId)
        throws ServiceUnavailableException {
        final IClientCredential clientCredential = ClientCredentialFactory.createFromSecret(clientSecret);
        final UserAssertion assertion = new UserAssertion(idToken);
        IAuthenticationResult result = null;
        try {
            final ConfidentialClientApplication application = ConfidentialClientApplication
                .builder(clientId, clientCredential)
                .authority(serviceEndpoints.getAadSigninUri() + tenantId + "/")
                .correlationId(getCorrelationId())
                .build();
            final Set<String> scopes = new HashSet<>();
            scopes.add(graphApiVersionIsV2 ? MICROSOFT_GRAPH_SCOPE : AAD_GRAPH_API_SCOPE);
            final OnBehalfOfParameters onBehalfOfParameters = OnBehalfOfParameters.builder(scopes, assertion).build();
            result = application.acquireToken(onBehalfOfParameters).get();
        } catch (ExecutionException | InterruptedException | MalformedURLException e) {
            // Handle conditional access policy
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
            throw new ServiceUnavailableException("unable to acquire on-behalf-of token for client " + clientId);
        }
        return result;
    }

    private static String getCorrelationId() {
        final String uuid = UUID.randomUUID().toString();
        return uuid.substring(0, uuid.length() - REQUEST_ID_SUFFIX.length()) + REQUEST_ID_SUFFIX;
    }
}
