// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.naming.ServiceUnavailableException;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.azure.spring.autoconfigure.aad.Scopes.OPENID_PERMISSIONS;

public class AccessTokenManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenManager.class);
    // We use "aadfeed5" as suffix when client library is ADAL, upgrade to "aadfeed6" for MSAL
    private static final String REQUEST_ID_SUFFIX = "aadfeed6";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN_";
    private static final long ACCESS_TOKEN_MIN_LIVE_TIME = 60 * 1000;
    private final ServiceEndpoints serviceEndpoints;
    private final AADAuthenticationProperties aadAuthenticationProperties;

    public AccessTokenManager(ServiceEndpoints serviceEndpoints,
                              AADAuthenticationProperties aadAuthenticationProperties) {
        this.serviceEndpoints = serviceEndpoints;
        this.aadAuthenticationProperties = aadAuthenticationProperties;
    }

    /**
     * Acquire AccessToken token for Microsoft Graph with expected permissions.
     *
     * @param permissions The expected permissions of resources.
     * @return AccessToken
     * @throws ServiceUnavailableException If fail to acquire the token.
     */
    public AccessToken getAccessTokenForMicrosoftGraph(Set<String> permissions) throws ServiceUnavailableException {
        return getAccessToken(aadAuthenticationProperties.getGraphApiUri(), permissions);
    }

    /**
     * Acquire AccessToken token for a resource server with expected permissions.
     *
     * @param applicationIdUri The Application ID URI of resource server, e.g., https://graph.microsoft.com for
     * Microsoft Graph API.
     * @param permissions The expected permissions of resources.
     * @return AccessToken
     * @throws ServiceUnavailableException If fail to acquire the token.
     */
    public AccessToken getAccessToken(String applicationIdUri,
                                      Set<String> permissions) throws ServiceUnavailableException {
        AccessToken accessToken = loadAccessTokenFromSession(applicationIdUri);
        Set<String> uniformedPermissions = permissions.stream()
                                                      .map(String::trim)
                                                      .map(s -> s.toLowerCase(Locale.ENGLISH))
                                                      .filter(s -> !s.isEmpty())
                                                      .collect(Collectors.toSet());
        if (accessToken.permissions.containsAll(uniformedPermissions)) {
            return accessToken;
        } else {
            // TODO: incremental consent.
            String idToken = getIdTokenFromSecurityContext();
            return getAccessToken(idToken, applicationIdUri, uniformedPermissions);
        }
    }

    private String getIdTokenFromSecurityContext() {
        return Optional.of(SecurityContextHolder.getContext())
                       .map(SecurityContext::getAuthentication)
                       .map(Authentication::getPrincipal)
                       .map(p -> (OidcUser) p)
                       .map(OidcUser::getIdToken)
                       .map(AbstractOAuth2Token::getTokenValue)
                       .orElse(null);
    }

    /**
     * Get AccessToken token for Microsoft Graph with expected permissions.
     *
     * @param idToken The token used to perform an get token request.
     * @param permissions The permissions of resources to be authorized with, need to be formatted as lowercase.
     * @return The access token for Microsoft Graph service.
     * @throws ServiceUnavailableException If fail to acquire the token.
     * @throws MsalServiceException If {@link MsalServiceException} has occurred.
     */
    public AccessToken getAccessTokenForMicrosoftGraph(String idToken,
                                                       Set<String> permissions) throws ServiceUnavailableException {
        return getAccessToken(idToken, aadAuthenticationProperties.getGraphApiUri(), permissions);
    }

    /**
     * Get AccessToken for a resource server.
     *
     * @param idToken The token used to perform an get token request.
     * @param applicationIdUri The Application ID URI of resource server, e.g., https://graph.microsoft.com for
     * Microsoft Graph API.
     * @param permissions The permissions of resources to be authorized with, need to be formatted as lowercase.
     * @return The access token for resource server.
     * @throws ServiceUnavailableException If fail to acquire the token.
     * @throws MsalServiceException If {@link MsalServiceException} has occurred.
     */
    public AccessToken getAccessToken(String idToken,
                                      String applicationIdUri,
                                      Set<String> permissions) throws ServiceUnavailableException {
        IAuthenticationResult result = getIAuthenticationResult(idToken, applicationIdUri, permissions);
        Set<String> uniformedPermissions =
            Arrays.stream(result.scopes().toLowerCase(Locale.ENGLISH).split(" "))
                  .map(s -> s.startsWith(applicationIdUri) ? s.split(applicationIdUri)[1] : s)
                  .collect(Collectors.toSet());
        AccessToken accessToken = new AccessToken(
            applicationIdUri,
            uniformedPermissions,
            result.expiresOnDate(),
            result.accessToken()
        );
        accessToken.getAccessTokenWithRefreshAutomatically();
        return accessToken;
    }

    /**
     * Acquire IAuthenticationResult for a resource server.
     *
     * @param idToken The token used to perform an get token request.
     * @param applicationIdUri The Application ID URI of resource server, e.g., https://graph.microsoft.com for
     * Microsoft Graph API.
     * @param permissions The permissions of resources to be authorized with, need to be formatted as lowercase.
     * @return The access token for Graph service.
     * @throws ServiceUnavailableException If fail to acquire the token.
     * @throws MsalServiceException If {@link MsalServiceException} has occurred.
     */
    private IAuthenticationResult getIAuthenticationResult(String idToken,
                                                           String applicationIdUri,
                                                           Set<String> permissions) throws ServiceUnavailableException {
        final IClientCredential clientCredential =
            ClientCredentialFactory.createFromSecret(aadAuthenticationProperties.getClientSecret());
        final UserAssertion assertion = new UserAssertion(idToken);
        IAuthenticationResult result = null;
        try {
            final ConfidentialClientApplication application = ConfidentialClientApplication
                .builder(aadAuthenticationProperties.getClientId(), clientCredential)
                .authority(serviceEndpoints.getAadSigninUri() + aadAuthenticationProperties.getTenantId() + "/")
                .correlationId(getCorrelationId())
                .build();
            Set<String> scopes = toScopeSet(applicationIdUri, permissions);
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
            LOGGER.error("acquire on behalf of token error", e);
        }
        if (result == null) {
            throw new ServiceUnavailableException("unable to acquire on-behalf-of token for client "
                + aadAuthenticationProperties.getClientId());
        }
        return result;
    }

    private static String getCorrelationId() {
        final String uuid = UUID.randomUUID().toString();
        return uuid.substring(0, uuid.length() - REQUEST_ID_SUFFIX.length()) + REQUEST_ID_SUFFIX;
    }

    private Set<String> toScopeSet(String applicationIdUri, Set<String> permissions) {
        return permissions.stream()
                          .map(scope -> OPENID_PERMISSIONS.contains(scope) ? scope : applicationIdUri + scope)
                          .collect(Collectors.toSet());
    }

    private AccessToken loadAccessTokenFromSession(String applicationIdUri) {
        return (AccessToken) Optional.of(RequestContextHolder.currentRequestAttributes())
                                     .map(a -> (ServletRequestAttributes) a)
                                     .map(ServletRequestAttributes::getRequest)
                                     .map(HttpServletRequest::getSession)
                                     .map(s -> s.getAttribute(ACCESS_TOKEN + applicationIdUri))
                                     .orElseGet(() -> new AccessToken(applicationIdUri,
                                         new HashSet<>(),
                                         new Date(),
                                         "")
                                     );
    }

    private void saveAccessTokenToSession(AccessToken accessToken) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        attr.getRequest().getSession(false).setAttribute(ACCESS_TOKEN + accessToken.applicationIdUri, accessToken);
    }

    /**
     * AccessToken will cached in session. So AccessToken only work for session supported web service.
     */
    public class AccessToken {
        private final String applicationIdUri;
        private final Set<String> permissions;
        private String accessToken;
        private Date expiredTime;

        AccessToken(String applicationIdUri,
                    Set<String> scopes,
                    Date expiredTime,
                    String accessToken) {
            this.applicationIdUri = applicationIdUri;
            this.permissions = scopes;
            this.expiredTime = expiredTime;
            this.accessToken = accessToken;
        }

        public boolean needRefresh() {
            Date currentTime = new Date();
            return expiredTime.getTime() - currentTime.getTime() < ACCESS_TOKEN_MIN_LIVE_TIME;
        }

        public void refresh() throws ServiceUnavailableException {
            String idToken = getIdTokenFromSecurityContext();
            IAuthenticationResult result = getIAuthenticationResult(idToken, applicationIdUri, permissions);
            accessToken = result.accessToken();
            expiredTime = result.expiresOnDate();
        }

        public String getAccessTokenWithRefreshAutomatically() throws ServiceUnavailableException {
            if (needRefresh()) {
                refresh();
                saveAccessTokenToSession(this);
            }
            return accessToken;
        }
    }
}
