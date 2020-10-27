// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.naming.ServiceUnavailableException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.azure.spring.autoconfigure.aad.Constants.DEFAULT_AUTHORITY_SET;
import static com.azure.spring.autoconfigure.aad.Constants.ROLE_PREFIX;
import static com.azure.spring.autoconfigure.aad.Scopes.AAD_GRAPH_API_URI;
import static com.azure.spring.autoconfigure.aad.Scopes.MICROSOFT_GRAPH_URI;
import static com.azure.spring.autoconfigure.aad.Scopes.OPENID_PERMISSIONS;


/**
 * Microsoft Graph client encapsulation.
 */
public class AzureADGraphClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureADGraphClient.class);
    // We use "aadfeed5" as suffix when client library is ADAL, upgrade to "aadfeed6" for MSAL
    private static final String REQUEST_ID_SUFFIX = "aadfeed6";
    private static final String V2_VERSION_ENV_FLAG = "v2-graph";
    private static final String OBO_TOKEN_MAP = "oboTokenMap";
    private static final String SUPPORTED_PERMISSIONS = "supportedPermissions";
    private static final long TIME_INTERNAL_FOR_OBO_TOKEN_EXPIRATION = 60 * 1000;

    private final ServiceEndpoints serviceEndpoints;
    private final AADAuthenticationProperties aadAuthenticationProperties;
    private final boolean graphApiVersionIsV2;
    private Map<String, AccessToken> oboTokenMap; // applicationIdUri -> oboToken
    private Map<String, Set<String>> supportedPermissions; //applicationIdUri -> scopes

    public AzureADGraphClient(AADAuthenticationProperties aadAuthenticationProperties,
                              ServiceEndpointsProperties serviceEndpointsProps) {
        this.aadAuthenticationProperties = aadAuthenticationProperties;
        this.serviceEndpoints = serviceEndpointsProps.getServiceEndpoints(aadAuthenticationProperties.getEnvironment());
        this.graphApiVersionIsV2 = Optional.of(aadAuthenticationProperties)
                                           .map(AADAuthenticationProperties::getEnvironment)
                                           .map(environment -> environment.contains(V2_VERSION_ENV_FLAG))
                                           .orElse(false);
        oboTokenMap = new HashMap<>();
        supportedPermissions = new HashMap<>();
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

    /**
     * @param graphApiToken token used to access graph api.
     * @return groups in graph api.
     * @throws IOException throw exception if get groups failed by IOException.
     */
    public Set<String> getGroups(String graphApiToken) throws IOException {
        final Set<String> groups = new LinkedHashSet<>();
        final ObjectMapper objectMapper = JacksonObjectMapperFactory.getInstance();
        String aadMembershipRestUri = serviceEndpoints.getAadMembershipRestUri();
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
                                           .map(this::getUrlStringFromODataNextLink)
                                           .orElse(null);
        }
        return groups;
    }

    private boolean isGroupObject(final Membership membership) {
        return membership.getObjectType().equals(aadAuthenticationProperties.getUserGroup().getValue());
    }

    /**
     * @param graphApiToken token of graph api.
     * @return set of SimpleGrantedAuthority
     * @throws IOException throw exception if get groups failed by IOException.
     */
    public Set<SimpleGrantedAuthority> getGrantedAuthorities(String graphApiToken) throws IOException {
        return toGrantedAuthoritySet(getGroups(graphApiToken));
    }

    public Set<SimpleGrantedAuthority> toGrantedAuthoritySet(final Set<String> groups) {
        Set<SimpleGrantedAuthority> grantedAuthoritySet =
            groups.stream()
                  .filter(aadAuthenticationProperties::isAllowedGroup)
                  .map(group -> new SimpleGrantedAuthority(ROLE_PREFIX + group))
                  .collect(Collectors.toSet());
        return Optional.of(grantedAuthoritySet)
                       .filter(g -> !g.isEmpty())
                       .orElse(DEFAULT_AUTHORITY_SET);
    }

    private static String getCorrelationId() {
        final String uuid = UUID.randomUUID().toString();
        return uuid.substring(0, uuid.length() - REQUEST_ID_SUFFIX.length()) + REQUEST_ID_SUFFIX;
    }

    /**
     * Acquire OBO token for a web-hosted resource with expected permissions.
     *
     * @param permissions The expected permissions of Graph API, only support permissions of {openid, profile, email,
     * offline_access}.
     * @return The OBO token for Graph service.
     * @throws ServiceUnavailableException If fail to acquire the token.
     */
    public String getOboToken(Set<String> permissions) throws ServiceUnavailableException {
        if (!OPENID_PERMISSIONS.containsAll(permissions)) {
            throw new IllegalArgumentException("Permissions should be a sub collection of {openid, profile, email, "
                + "offline_access}");
        }
        return getOboToken(MICROSOFT_GRAPH_URI, permissions);
    }

    /**
     * Acquire OBO token for a web-hosted resource with expected permissions.
     *
     * @param applicationIdUri The Application ID URI of web-hosted resource, e.g., https://graph.microsoft.com for
     * Microsoft Graph API.
     * @param permissions The expected permissions of resources.
     * @return The OBO token for Graph service.
     * @throws ServiceUnavailableException If fail to acquire the token.
     */
    public String getOboToken(String applicationIdUri, Set<String> permissions) throws ServiceUnavailableException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OidcUser principal = ((OidcUser) authentication.getPrincipal());
        String idToken = principal.getIdToken().getTokenValue();
        String tenantId = aadAuthenticationProperties.getTenantId();
        loadOBOTokenFromSession();

        Set<String> uniformedPermissionSet = permissions.stream()
                                                        .map(String::trim)
                                                        .map(s -> s.toLowerCase(Locale.ENGLISH))
                                                        .filter(s -> !s.isEmpty())
                                                        .collect(Collectors.toSet());

        boolean isScopeSupported = Optional.of(applicationIdUri)
                                           .map(uri -> supportedPermissions.getOrDefault(uri, null))
                                           .map(permissionSet -> permissionSet.containsAll(uniformedPermissionSet))
                                           .orElse(false);
        if (isScopeSupported) {
            AccessToken accessToken = oboTokenMap.get(applicationIdUri);
            if (accessToken.needRefresh()) {
                accessToken.refresh(idToken, tenantId, applicationIdUri, uniformedPermissionSet);
            }
            return accessToken.getAccessToken();
        } else {
            // TODO: incremental consent.
            return getAccessToken(idToken, tenantId, applicationIdUri, uniformedPermissionSet);
        }
    }

    private Set<String> convertToScope(String applicationIdUri, Set<String> permissions) {
        return permissions.stream()
                          .map(scope -> OPENID_PERMISSIONS.contains(scope) ? scope : applicationIdUri + scope)
                          .collect(Collectors.toSet());
    }

    public String getGraphApiUri() {
        boolean isGraphApiVersionIsV2 = Optional.of(aadAuthenticationProperties)
                                                .map(AADAuthenticationProperties::getEnvironment)
                                                .map(environment -> environment.contains("v2-graph"))
                                                .orElse(false);
        return isGraphApiVersionIsV2 ? MICROSOFT_GRAPH_URI : AAD_GRAPH_API_URI;
    }

    /**
     * Acquire access token for a web-hosted resource.
     *
     * @param idToken The token used to perform an OBO request.
     * @param tenantId The tenant id.
     * @param applicationIdUri The Application ID URI of web-hosted resource, e.g., https://graph.microsoft.com for
     * Microsoft Graph API.
     * @param permissions The permissions of resources to be authorized with, need to be formatted as lowercase.
     * @return The access token for Graph service.
     * @throws ServiceUnavailableException If fail to acquire the token.
     * @throws MsalServiceException If {@link MsalServiceException} has occurred.
     */
    public String getAccessToken(String idToken,
                                 String tenantId,
                                 String applicationIdUri,
                                 Set<String> permissions) throws ServiceUnavailableException {
        return getIAuthenticationResult(idToken, tenantId, applicationIdUri, permissions).accessToken();
    }

    /**
     * Acquire IAuthenticationResult for a web-hosted resource.
     *
     * @param idToken The token used to perform an OBO request.
     * @param tenantId The tenant id.
     * @param applicationIdUri The Application ID URI of web-hosted resource, e.g., https://graph.microsoft.com for
     * Microsoft Graph API.
     * @param permissions The permissions of resources to be authorized with, need to be formatted as lowercase.
     * @return The access token for Graph service.
     * @throws ServiceUnavailableException If fail to acquire the token.
     * @throws MsalServiceException If {@link MsalServiceException} has occurred.
     */
    public IAuthenticationResult getIAuthenticationResult(String idToken,
                                                          String tenantId,
                                                          String applicationIdUri,
                                                          Set<String> permissions) throws ServiceUnavailableException {
        final IClientCredential clientCredential =
            ClientCredentialFactory.createFromSecret(aadAuthenticationProperties.getClientSecret());
        final UserAssertion assertion = new UserAssertion(idToken);
        IAuthenticationResult result = null;
        try {
            final ConfidentialClientApplication application = ConfidentialClientApplication
                .builder(aadAuthenticationProperties.getClientId(), clientCredential)
                .authority(serviceEndpoints.getAadSigninUri() + tenantId + "/")
                .correlationId(getCorrelationId())
                .build();
            Set<String> scopes = convertToScope(applicationIdUri, permissions);
            final OnBehalfOfParameters onBehalfOfParameters = OnBehalfOfParameters.builder(scopes, assertion).build();
            result = application.acquireToken(onBehalfOfParameters).get();

            Set<String> acquiredPermissions = Arrays.stream(result.scopes().toLowerCase(Locale.ENGLISH).split(" "))
                                                    .map(s -> s.startsWith(applicationIdUri)
                                                        ? s.split(applicationIdUri)[1] : s)
                                                    .collect(Collectors.toSet());
            AccessToken accessToken = new AccessToken(result.expiresOnDate(), result.accessToken());
            oboTokenMap.put(applicationIdUri, accessToken);
            if (supportedPermissions.containsKey(applicationIdUri)) {
                supportedPermissions.get(applicationIdUri).addAll(acquiredPermissions);
            } else {
                supportedPermissions.put(applicationIdUri, acquiredPermissions);
            }
            storeOBOTokenInSession();
        } catch (ExecutionException | InterruptedException | MalformedURLException e) {
            // Handle conditional access policy
            final Throwable cause = e.getCause();
            if (cause instanceof MsalServiceException) {
                final MsalServiceException exception = (MsalServiceException) cause;
                if (exception.claims() != null && !exception.claims().isEmpty()) {
                    throw exception;
                }
            }
            LOGGER.error("acquire on behalf of token error", e);
        }
        if (result == null) {
            throw new ServiceUnavailableException("unable to acquire on-behalf-of token for client "
                + aadAuthenticationProperties.getClientId());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void loadOBOTokenFromSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        oboTokenMap = (Map<String, AccessToken>) attr.getRequest()
                                                     .getSession(false)
                                                     .getAttribute(OBO_TOKEN_MAP);
        supportedPermissions = (Map<String, Set<String>>) attr.getRequest()
                                                              .getSession(false)
                                                              .getAttribute(SUPPORTED_PERMISSIONS);
    }

    private void storeOBOTokenInSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        attr.getRequest().getSession(false).setAttribute(OBO_TOKEN_MAP, oboTokenMap);
        attr.getRequest().getSession(false).setAttribute(SUPPORTED_PERMISSIONS, supportedPermissions);
    }

    private class AccessToken {
        private String accessToken;
        private Date expiredTime;

        AccessToken(Date expiredTime, String accessToken) {
            this.expiredTime = expiredTime;
            this.accessToken = accessToken;
        }

        public boolean needRefresh() {
            Date currentTime = new Date();
            return expiredTime.getTime() - currentTime.getTime() < TIME_INTERNAL_FOR_OBO_TOKEN_EXPIRATION;
        }

        public void refresh(String idToken,
                            String tenantId,
                            String applicationIdUri,
                            Set<String> permissions) throws ServiceUnavailableException {
            IAuthenticationResult result = getIAuthenticationResult(idToken, tenantId, applicationIdUri, permissions);
            accessToken = result.accessToken();
            expiredTime = result.expiresOnDate();
            storeOBOTokenInSession();
        }

        public String getAccessToken() {
            return accessToken;
        }
    }
}
