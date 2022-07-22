// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.filter;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.constants.Constants.BEARER_PREFIX;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Optional;

import javax.naming.ServiceUnavailableException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.aad.implementation.graph.AadGraphClient;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationServerEndpoints;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.proc.BadJWTException;

/**
 * A stateful authentication filter which uses Microsoft Graph groups to authorize. Both ID token and access token are
 * supported. In the case of access token, only access token issued for the exact same application this filter used for
 * could be accepted, e.g. access token issued for Microsoft Graph could not be processed by users' application.
 *
 * @see OncePerRequestFilter
 */
public class AadAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AadAuthenticationFilter.class);
    private static final String CURRENT_USER_PRINCIPAL = "CURRENT_USER_PRINCIPAL";

    private final UserPrincipalManager userPrincipalManager;
    private final AadGraphClient aadGraphClient;

    /**
     * Creates a new instance of {@link AadAuthenticationFilter}.
     *
     * @param aadAuthenticationProperties the AAD authentication properties
     * @param endpoints the AAD authorization server endpoints
     * @param resourceRetriever the resource retriever
     */
    public AadAuthenticationFilter(AadAuthenticationProperties aadAuthenticationProperties,
                                   AadAuthorizationServerEndpoints endpoints,
                                   ResourceRetriever resourceRetriever) {
        this(
            aadAuthenticationProperties,
            endpoints,
            new UserPrincipalManager(
                endpoints,
                aadAuthenticationProperties,
                resourceRetriever,
                false
            )
        );
    }

    /**
     * Creates a new instance of {@link AadAuthenticationFilter}.
     *
     * @param aadAuthenticationProperties the AAD authentication properties
     * @param endpoints the AAD authorization server endpoints
     * @param resourceRetriever the resource retriever
     * @param jwkSetCache the JWK set cache
     */
    public AadAuthenticationFilter(AadAuthenticationProperties aadAuthenticationProperties,
                                   AadAuthorizationServerEndpoints endpoints,
                                   ResourceRetriever resourceRetriever,
                                   JWKSetCache jwkSetCache) {
        this(
            aadAuthenticationProperties,
            endpoints,
            new UserPrincipalManager(
                endpoints,
                aadAuthenticationProperties,
                resourceRetriever,
                false,
                jwkSetCache
            )
        );
    }

    /**
     * Creates a new instance of {@link AadAuthenticationFilter}.
     *
     * @param aadAuthenticationProperties the AAD authentication properties
     * @param endpoints the AAD authorization server endpoints
     * @param userPrincipalManager the user principal manager
     */
    public AadAuthenticationFilter(AadAuthenticationProperties aadAuthenticationProperties,
                                   AadAuthorizationServerEndpoints endpoints,
                                   UserPrincipalManager userPrincipalManager) {
        this.userPrincipalManager = userPrincipalManager;
        this.aadGraphClient = new AadGraphClient(
            aadAuthenticationProperties.getCredential().getClientId(),
            aadAuthenticationProperties.getCredential().getClientSecret(),
            aadAuthenticationProperties,
            endpoints
        );
    }

    /**
     * Do filter.
     *
     * @param httpServletRequest the http servlet request
     * @param httpServletResponse the http servlet responce
     * @param filterChain the filter chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        String aadIssuedBearerToken = Optional.of(httpServletRequest)
                                              .map(r -> r.getHeader(HttpHeaders.AUTHORIZATION))
                                              .map(String::trim)
                                              .filter(s -> s.startsWith(BEARER_PREFIX))
                                              .map(s -> s.replace(BEARER_PREFIX, ""))
                                              .filter(userPrincipalManager::isTokenIssuedByAad)
                                              .orElse(null);
        if (aadIssuedBearerToken == null || alreadyAuthenticated()) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        try {
            HttpSession httpSession = httpServletRequest.getSession();
            UserPrincipal userPrincipal = (UserPrincipal) httpSession.getAttribute(CURRENT_USER_PRINCIPAL);
            if (userPrincipal == null
                || !userPrincipal.getAadIssuedBearerToken().equals(aadIssuedBearerToken)
                || userPrincipal.getAccessTokenForGraphApi() == null
            ) {
                userPrincipal = userPrincipalManager.buildUserPrincipal(aadIssuedBearerToken);
                String tenantId = userPrincipal.getClaim(AadJwtClaimNames.TID).toString();
                String accessTokenForGraphApi = aadGraphClient
                    .acquireTokenForGraphApi(aadIssuedBearerToken, tenantId)
                    .accessToken();
                userPrincipal.setAccessTokenForGraphApi(accessTokenForGraphApi);
                userPrincipal.setGroups(aadGraphClient.getGroups(accessTokenForGraphApi));
                httpSession.setAttribute(CURRENT_USER_PRINCIPAL, userPrincipal);
            }
            final Authentication authentication = new PreAuthenticatedAuthenticationToken(
                userPrincipal,
                null,
                aadGraphClient.toGrantedAuthoritySet(userPrincipal.getGroups())
            );
            LOGGER.info("Request token verification success. {}", authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadJWTException ex) {
            // Invalid JWT. Either expired or not yet valid.
            httpServletResponse.sendError(HttpStatus.UNAUTHORIZED.value());
            return;
        } catch (MalformedURLException | ParseException | JOSEException | BadJOSEException ex) {
            throw new ServletException("Failed to initialize UserPrincipal.", ex);
        } catch (ServiceUnavailableException ex) {
            throw new ServletException("Failed to acquire graph api token.", ex);
        } catch (MsalServiceException ex) {
            // Handle conditional access policy, step 2.
            // No step 3 any more, because ServletException will not be caught.
            // TODO: Do we need to return 401 instead of 500?
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
}
