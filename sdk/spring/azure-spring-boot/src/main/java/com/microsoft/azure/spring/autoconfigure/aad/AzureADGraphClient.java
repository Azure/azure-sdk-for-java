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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private final String clientId;
    private final String clientSecret;
    private final ServiceEndpoints serviceEndpoints;
    private final AADAuthenticationProperties aadAuthenticationProperties;

    private static final String V2_VERSION_ENV_FLAG = "v2-graph";
    private boolean aadMicrosoftGraphApiBool;

    public AzureADGraphClient(String clientId, String clientSecret, AADAuthenticationProperties aadAuthProps,
                              ServiceEndpointsProperties serviceEndpointsProps) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.aadAuthenticationProperties = aadAuthProps;
        this.serviceEndpoints = serviceEndpointsProps.getServiceEndpoints(aadAuthProps.getEnvironment());

        this.initAADMicrosoftGraphApiBool(aadAuthProps.getEnvironment());
    }

    private void initAADMicrosoftGraphApiBool(String endpointEnv) {
        this.aadMicrosoftGraphApiBool = endpointEnv.contains(V2_VERSION_ENV_FLAG);
    }

    private String getUserMemberships(String accessToken, String odataNextLink) throws IOException {
        final URL url = buildUrl(odataNextLink);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // Set the appropriate header fields in the request header.

        if (this.aadMicrosoftGraphApiBool) {
            conn.setRequestMethod(HttpMethod.GET.toString());
            conn.setRequestProperty(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
            conn.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        } else {
            conn.setRequestMethod(HttpMethod.GET.toString());
            conn.setRequestProperty("api-version", "1.6");
            conn.setRequestProperty(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
            conn.setRequestProperty(HttpHeaders.ACCEPT, "application/json;odata=minimalmetadata");
        }
        final String responseInJson = getResponseStringFromConn(conn);
        final int responseCode = conn.getResponseCode();
        if (responseCode == HTTPResponse.SC_OK) {
            return responseInJson;
        } else {
            throw new IllegalStateException("Response is not "
                + HTTPResponse.SC_OK + ", response json: " + responseInJson);
        }
    }

    private String getSkipTokenFromLink(String odataNextLink) {
        String[] parts = odataNextLink.split("/memberOf\\?");
        return parts[1];
    }

    private URL buildUrl(String odataNextLink) throws MalformedURLException {
        URL url;
        if (odataNextLink != null) {
            if (this.aadMicrosoftGraphApiBool) {
                url = new URL(odataNextLink);
            } else {
                String skipToken = getSkipTokenFromLink(odataNextLink);
                url = new URL(serviceEndpoints.getAadMembershipRestUri() + "&" + skipToken);
            }
        } else {
            url = new URL(serviceEndpoints.getAadMembershipRestUri());
        }
        return url;
    }

    private static String getResponseStringFromConn(HttpURLConnection conn) throws IOException {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            final StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
            }
            return stringBuffer.toString();
        }
    }

    public List<UserGroup> getGroups(String graphApiToken) throws IOException {
        return loadUserGroups(graphApiToken);
    }

    private List<UserGroup> loadUserGroups(String graphApiToken) throws IOException {
        String responseInJson = getUserMemberships(graphApiToken, null);
        final List<UserGroup> lUserGroups = new ArrayList<>();
        final ObjectMapper objectMapper = JacksonObjectMapperFactory.getInstance();
        UserGroups groupsFromJson = objectMapper.readValue(responseInJson, UserGroups.class);

        if (groupsFromJson.getValue() != null) {
            lUserGroups.addAll(groupsFromJson.getValue().stream().filter(this::isMatchingUserGroupKey)
                .collect(Collectors.toList()));
        }
        while (groupsFromJson.getOdataNextLink() != null) {
            responseInJson = getUserMemberships(graphApiToken, groupsFromJson.getOdataNextLink());
            groupsFromJson = objectMapper.readValue(responseInJson, UserGroups.class);
            lUserGroups.addAll(groupsFromJson.getValue().stream().filter(this::isMatchingUserGroupKey)
                .collect(Collectors.toList()));
        }

        return lUserGroups;
    }

    /**
     * Checks that the UserGroup has a Group object type.
     *
     * @param node - json node to look for a key/value to equate against the
     *             {@link AADAuthenticationProperties.UserGroupProperties}
     * @return true if the json node contains the correct key, and expected value to identify a user group.
     */
    private boolean isMatchingUserGroupKey(final UserGroup group) {
        return group.getObjectType().equals(aadAuthenticationProperties.getUserGroup().getValue());
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
        final Set<GrantedAuthority> mappedAuthorities = groups.stream().filter(this::isValidUserGroupToGrantAuthority)
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
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);

            final ConfidentialClientApplication application = ConfidentialClientApplication
                .builder(clientId, clientCredential)
                .authority(serviceEndpoints.getAadSigninUri() + tenantId + "/")
                .correlationId(getCorrelationId())
                .build();

            final Set<String> scopes = new HashSet<>();
            scopes.add(aadMicrosoftGraphApiBool ? MICROSOFT_GRAPH_SCOPE : AAD_GRAPH_API_SCOPE);

            final OnBehalfOfParameters onBehalfOfParameters = OnBehalfOfParameters
                .builder(scopes, assertion)
                .build();

            final CompletableFuture<IAuthenticationResult> future = application.acquireToken(onBehalfOfParameters);
            result = future.get();
        } catch (ExecutionException | InterruptedException | MalformedURLException e) {
            // handle conditional access policy
            final Throwable cause = e.getCause();
            if (cause instanceof MsalServiceException) {
                final MsalServiceException exception = (MsalServiceException) cause;
                if (exception.claims() != null && !exception.claims().isEmpty()) {
                    throw exception;
                }
            }
            LOGGER.error("acquire on behalf of token for graph api error", e);
        } finally {
            if (service != null) {
                service.shutdown();
            }
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
