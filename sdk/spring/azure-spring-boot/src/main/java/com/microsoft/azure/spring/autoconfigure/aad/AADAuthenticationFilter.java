// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import com.microsoft.aad.msal4j.MsalServiceException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.util.ResourceRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
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

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_TYPE = "Bearer ";

    private AADAuthenticationProperties aadAuthProps;
    private ServiceEndpointsProperties serviceEndpointsProps;
    private UserPrincipalManager principalManager;

    public AADAuthenticationFilter(AADAuthenticationProperties aadAuthProps,
                                   ServiceEndpointsProperties serviceEndpointsProps,
                                   ResourceRetriever resourceRetriever) {
        this(aadAuthProps, serviceEndpointsProps, new UserPrincipalManager(serviceEndpointsProps,
                aadAuthProps,
                resourceRetriever,
                false));
    }

    public AADAuthenticationFilter(AADAuthenticationProperties aadAuthProps,
                                   ServiceEndpointsProperties serviceEndpointsProps,
                                   ResourceRetriever resourceRetriever,
                                   JWKSetCache jwkSetCache) {
        this(aadAuthProps, serviceEndpointsProps, new UserPrincipalManager(serviceEndpointsProps,
                aadAuthProps,
                resourceRetriever,
                false,
                jwkSetCache));
    }

    public AADAuthenticationFilter(AADAuthenticationProperties aadAuthProps,
                                   ServiceEndpointsProperties serviceEndpointsProps,
                                   UserPrincipalManager userPrincipalManager) {
        this.aadAuthProps = aadAuthProps;
        this.serviceEndpointsProps = serviceEndpointsProps;
        this.principalManager = userPrincipalManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(TOKEN_HEADER);

        if (!alreadyAuthenticated() && authHeader != null && authHeader.startsWith(TOKEN_TYPE)) {
            verifyToken(request.getSession(), authHeader.replace(TOKEN_TYPE, ""));
        }

        filterChain.doFilter(request, response);
    }

    private boolean alreadyAuthenticated() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    private void verifyToken(HttpSession session, String token) throws IOException, ServletException {
        if (!principalManager.isTokenIssuedByAAD(token)) {
            LOGGER.info("Token {} is not issued by AAD", token);
            return;
        }

        try {
            final String currentToken = (String) session.getAttribute(CURRENT_USER_PRINCIPAL_JWT_TOKEN);
            UserPrincipal principal = (UserPrincipal) session.getAttribute(CURRENT_USER_PRINCIPAL);
            String graphApiToken = (String) session.getAttribute(CURRENT_USER_PRINCIPAL_GRAPHAPI_TOKEN);

            final AzureADGraphClient client = new AzureADGraphClient(aadAuthProps.getClientId(),
                aadAuthProps.getClientSecret(), aadAuthProps, serviceEndpointsProps);

            if (principal == null || graphApiToken == null || graphApiToken.isEmpty() || !token.equals(currentToken)) {
                principal = principalManager.buildUserPrincipal(token);

                final String tenantId = principal.getClaim().toString();
                graphApiToken = client.acquireTokenForGraphApi(token, tenantId).accessToken();

                principal.setUserGroups(client.getGroups(graphApiToken));

                session.setAttribute(CURRENT_USER_PRINCIPAL, principal);
                session.setAttribute(CURRENT_USER_PRINCIPAL_GRAPHAPI_TOKEN, graphApiToken);
                session.setAttribute(CURRENT_USER_PRINCIPAL_JWT_TOKEN, token);
            }

            final Authentication authentication = new PreAuthenticatedAuthenticationToken(
                principal, null, client.convertGroupsToGrantedAuthorities(principal.getUserGroups()));

            authentication.setAuthenticated(true);
            LOGGER.info("Request token verification success. {}", authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (MalformedURLException | ParseException | BadJOSEException | JOSEException ex) {
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
    }
}
