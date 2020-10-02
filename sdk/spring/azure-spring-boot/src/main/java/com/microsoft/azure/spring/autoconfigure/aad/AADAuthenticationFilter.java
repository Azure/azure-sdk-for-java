// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import com.microsoft.aad.msal4j.MsalServiceException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.proc.BadJWTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.naming.ServiceUnavailableException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Optional;

import static com.microsoft.azure.spring.autoconfigure.aad.Constants.BEARER_PREFIX;

/**
 * A stateful authentication filter which uses Microsoft Graph groups to authorize. Both ID token and access token are
 * supported. In the case of access token, only access token issued for the exact same application this filter used for
 * could be accepted, e.g. access token issued for Microsoft Graph could not be processed by users' application.
 */
public class AADAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AADAuthenticationFilter.class);

    private static final String CURRENT_USER_PRINCIPAL = "CURRENT_USER_PRINCIPAL";
    private static final String CURRENT_USER_PRINCIPAL_GRAPHAPI_TOKEN = "CURRENT_USER_PRINCIPAL_GRAPHAPI_TOKEN";
    private static final String CURRENT_USER_PRINCIPAL_JWT_TOKEN = "CURRENT_USER_PRINCIPAL_JWT_TOKEN";

    private final AADAuthenticationProperties aadAuthenticationProperties;
    private final ServiceEndpointsProperties serviceEndpointsProperties;
    private final UserPrincipalManager userPrincipalManager;

    public AADAuthenticationFilter(AADAuthenticationProperties aadAuthenticationProperties,
                                   ServiceEndpointsProperties serviceEndpointsProperties,
                                   ResourceRetriever resourceRetriever) {
        this(
            aadAuthenticationProperties,
            serviceEndpointsProperties,
            new UserPrincipalManager(
                serviceEndpointsProperties,
                aadAuthenticationProperties,
                resourceRetriever,
                false
            )
        );
    }

    public AADAuthenticationFilter(AADAuthenticationProperties aadAuthenticationProperties,
                                   ServiceEndpointsProperties serviceEndpointsProperties,
                                   ResourceRetriever resourceRetriever,
                                   JWKSetCache jwkSetCache) {
        this(
            aadAuthenticationProperties,
            serviceEndpointsProperties,
            new UserPrincipalManager(
                serviceEndpointsProperties,
                aadAuthenticationProperties,
                resourceRetriever,
                false,
                jwkSetCache
            )
        );
    }

    public AADAuthenticationFilter(AADAuthenticationProperties aadAuthenticationProperties,
                                   ServiceEndpointsProperties serviceEndpointsProperties,
                                   UserPrincipalManager userPrincipalManager) {
        this.aadAuthenticationProperties = aadAuthenticationProperties;
        this.serviceEndpointsProperties = serviceEndpointsProperties;
        this.userPrincipalManager = userPrincipalManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        String aadIssuedBearerToken = Optional.of(httpServletRequest)
                                              .map(r -> r.getHeader(HttpHeaders.AUTHORIZATION))
                                              .map(String::trim)
                                              .filter(s -> s.startsWith(BEARER_PREFIX))
                                              .map(s -> s.replace(BEARER_PREFIX, ""))
                                              .filter(userPrincipalManager::isTokenIssuedByAAD)
                                              .orElse(null);
        if (aadIssuedBearerToken == null || alreadyAuthenticated()) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        try {
            HttpSession httpSession = httpServletRequest.getSession();
            String currentToken = getStringAttribute(httpSession, CURRENT_USER_PRINCIPAL_JWT_TOKEN);
            String graphApiToken = getStringAttribute(httpSession, CURRENT_USER_PRINCIPAL_GRAPHAPI_TOKEN);
            UserPrincipal principal = (UserPrincipal) httpSession.getAttribute(CURRENT_USER_PRINCIPAL);
            final AzureADGraphClient azureADGraphClient = new AzureADGraphClient(
                aadAuthenticationProperties.getClientId(),
                aadAuthenticationProperties.getClientSecret(),
                aadAuthenticationProperties,
                serviceEndpointsProperties
            );
            if (principal == null || graphApiToken == null || !aadIssuedBearerToken.equals(currentToken)) {
                principal = userPrincipalManager.buildUserPrincipal(aadIssuedBearerToken);
                String tenantId = principal.getClaim().toString();
                graphApiToken = azureADGraphClient.acquireTokenForGraphApi(aadIssuedBearerToken, tenantId)
                                                  .accessToken();
                principal.setUserGroups(azureADGraphClient.getGroups(graphApiToken));
                httpSession.setAttribute(CURRENT_USER_PRINCIPAL, principal);
                httpSession.setAttribute(CURRENT_USER_PRINCIPAL_GRAPHAPI_TOKEN, graphApiToken);
                httpSession.setAttribute(CURRENT_USER_PRINCIPAL_JWT_TOKEN, aadIssuedBearerToken);
            }
            final Authentication authentication = new PreAuthenticatedAuthenticationToken(
                principal,
                null,
                azureADGraphClient.convertGroupsToGrantedAuthorities(principal.getUserGroups())
            );
            LOGGER.info("Request token verification success. {}", authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadJWTException ex) {
            // Invalid JWT. Either expired or not yet valid.
            httpServletResponse.sendError(HttpStatus.UNAUTHORIZED.value());
            return;
        } catch (MalformedURLException | ParseException | JOSEException | BadJOSEException ex) {
            LOGGER.error("Failed to initialize UserPrincipal.", ex);
            throw new ServletException(ex);
        } catch (ServiceUnavailableException ex) {
            LOGGER.error("Failed to acquire graph api token.", ex);
            throw new ServletException(ex);
        } catch (MsalServiceException ex) {
            if (ex.claims() != null && !ex.claims().isEmpty()) {
                throw new ServletException("Handle conditional access policy", ex);
            } else {
                throw ex;
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private boolean alreadyAuthenticated() {
        return Optional.of(SecurityContextHolder.getContext())
                       .map(SecurityContext::getAuthentication)
                       .map(Authentication::isAuthenticated)
                       .orElse(false);
    }

    private String getStringAttribute(HttpSession httpSession, String attributeName) {
        return Optional.ofNullable(httpSession)
                       .map(s -> (String) s.getAttribute(attributeName))
                       .map(String::trim)
                       .filter(s -> !s.isEmpty())
                       .orElse(null);
    }
}
