// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.filter.UserPrincipal;
import com.azure.spring.cloud.autoconfigure.aad.filter.UserPrincipalManager;
import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.aad.implementation.graph.AadGraphClient;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationServerEndpoints;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.proc.BadJWTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.constants.Constants.BEARER_PREFIX;

/**
 * Repository for the security context.
 */
public class AadReactiveSecurityContextRepository implements ServerSecurityContextRepository {

    private static final String CURRENT_USER_PRINCIPAL = "CURRENT_USER_PRINCIPAL";

    private final UserPrincipalManager userPrincipalManager;
    private final AadGraphClient aadGraphClient;

    private final ReactiveAuthenticationManager authenticationManager;

    /**
     * Creates a new security context repository.
     *
     * @param aadAuthenticationProperties The properties
     * @param endpoints The endpoints.
     * @param userPrincipalManager The principal manager.
     * @param authenticationManager The authentication manager.
     */
    @Autowired()
    public AadReactiveSecurityContextRepository(AadAuthenticationProperties aadAuthenticationProperties,
                                                AadAuthorizationServerEndpoints endpoints,
                                                UserPrincipalManager userPrincipalManager,
                                                ReactiveAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.userPrincipalManager = userPrincipalManager;
        this.aadGraphClient = new AadGraphClient(
            aadAuthenticationProperties.getCredential().getClientId(),
            aadAuthenticationProperties.getCredential().getClientSecret(),
            aadAuthenticationProperties,
            endpoints
        );
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String aadIssuedBearerToken = Optional.of(exchange)
            .map(exc -> exc.getRequest())
            .map(req -> req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
            .map(String::trim)
            .filter(s -> s.startsWith(BEARER_PREFIX))
            .map(s -> s.replace(BEARER_PREFIX, ""))
            .filter(userPrincipalManager::isTokenIssuedByAad)
            .orElse(null);
        return exchange.getSession().flatMap(session -> {
            UserPrincipal userPrincipal = (UserPrincipal) session.getAttribute(CURRENT_USER_PRINCIPAL);
            try {
                userPrincipal = userPrincipalManager.buildUserPrincipal(aadIssuedBearerToken);
                String tenantId = userPrincipal.getClaim(AadJwtClaimNames.TID).toString();
                String accessTokenForGraphApi = aadGraphClient
                    .acquireTokenForGraphApi(aadIssuedBearerToken, tenantId)
                    .accessToken();
                userPrincipal.setAccessTokenForGraphApi(accessTokenForGraphApi);
                userPrincipal.setGroups(aadGraphClient.getGroups(accessTokenForGraphApi));
                session.getAttributes().put(CURRENT_USER_PRINCIPAL, userPrincipal);
                final Authentication authentication = new PreAuthenticatedAuthenticationToken(
                    userPrincipal,
                    null,
                    aadGraphClient.toGrantedAuthoritySet(userPrincipal.getGroups())
                );
                return this.authenticationManager
                    .authenticate(authentication)
                    .map(SecurityContextImpl::new);
            } catch (BadJWTException ex) {
                // Invalid JWT. Either expired or not yet valid.
                throw new RuntimeException(ex);
            } catch (MalformedURLException | ParseException | JOSEException | BadJOSEException ex) {
                throw new RuntimeException(ex);
            } catch (ServiceUnavailableException ex) {
                throw new RuntimeException(ex);
            } catch (MsalServiceException ex) {
                // Handle conditional access policy, step 2.
                // No step 3 any more, because ServletException will not be caught.
                // TODO: Do we need to return 401 instead of 500?
                if (ex.claims() != null && !ex.claims().isEmpty()) {
                    throw new RuntimeException("Handle conditional access policy", ex);
                } else {
                    throw new RuntimeException(ex);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
