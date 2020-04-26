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
        this.aadAuthProps = aadAuthProps;
        this.serviceEndpointsProps = serviceEndpointsProps;
        this.principalManager = new UserPrincipalManager(serviceEndpointsProps, aadAuthProps, resourceRetriever, false);
    }

    public AADAuthenticationFilter(AADAuthenticationProperties aadAuthProps,
                                   ServiceEndpointsProperties serviceEndpointsProps,
                                   ResourceRetriever resourceRetriever,
                                   JWKSetCache jwkSetCache) {
        this.aadAuthProps = aadAuthProps;
        this.serviceEndpointsProps = serviceEndpointsProps;
        this.principalManager = new UserPrincipalManager(serviceEndpointsProps,
                aadAuthProps,
                resourceRetriever,
                false,
                jwkSetCache);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(TOKEN_HEADER);

        if (authHeader != null && authHeader.startsWith(TOKEN_TYPE)) {
            try {
                final String idToken = authHeader.replace(TOKEN_TYPE, "");
                UserPrincipal principal = (UserPrincipal) request
                    .getSession().getAttribute(CURRENT_USER_PRINCIPAL);
                String graphApiToken = (String) request
                    .getSession().getAttribute(CURRENT_USER_PRINCIPAL_GRAPHAPI_TOKEN);
                final String currentToken = (String) request
                    .getSession().getAttribute(CURRENT_USER_PRINCIPAL_JWT_TOKEN);

                final AzureADGraphClient client = new AzureADGraphClient(aadAuthProps.getClientId(),
                    aadAuthProps.getClientSecret(), aadAuthProps, serviceEndpointsProps);

                if (principal == null
                    || graphApiToken == null
                    || graphApiToken.isEmpty()
                    || !idToken.equals(currentToken)) {
                    principal = principalManager.buildUserPrincipal(idToken);

                    final String tenantId = principal.getClaim().toString();
                    graphApiToken = client.acquireTokenForGraphApi(idToken, tenantId).accessToken();

                    principal.setUserGroups(client.getGroups(graphApiToken));

                    request.getSession().setAttribute(CURRENT_USER_PRINCIPAL, principal);
                    request.getSession().setAttribute(CURRENT_USER_PRINCIPAL_GRAPHAPI_TOKEN, graphApiToken);
                    request.getSession().setAttribute(CURRENT_USER_PRINCIPAL_JWT_TOKEN, idToken);
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

        filterChain.doFilter(request, response);
    }
}
