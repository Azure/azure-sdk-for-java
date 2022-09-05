// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.filter;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AuthorityPrefix;
import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.Constants;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.proc.BadJWTException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.constants.Constants.DEFAULT_AUTHORITY_SET;

/**
 * A stateless authentication filter which uses app roles feature of Azure Active Directory. Since it's a stateless
 * implementation so the principal will not be stored in session. By using roles claim in the token it will not call
 * Microsoft Graph to retrieve users' groups.
 *
 * @see OncePerRequestFilter
 */
public class AadAppRoleStatelessAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadAppRoleStatelessAuthenticationFilter.class);

    private final UserPrincipalManager principalManager;

    /**
     * Creates a new instance of {@link AadAppRoleStatelessAuthenticationFilter}.
     *
     * @param principalManager the user principal manager
     */
    public AadAppRoleStatelessAuthenticationFilter(UserPrincipalManager principalManager) {
        this.principalManager = principalManager;
    }

    /**
     * Do filter.
     *
     * @param httpServletRequest the httpServletRequest
     * @param httpServletResponse the httpServletResponse
     * @param filterChain the filterChain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        String aadIssuedBearerToken = Optional.of(httpServletRequest)
                                              .map(r -> r.getHeader(HttpHeaders.AUTHORIZATION))
                                              .map(String::trim)
                                              .filter(s -> s.startsWith(Constants.BEARER_PREFIX))
                                              .map(s -> s.replace(Constants.BEARER_PREFIX, ""))
                                              .filter(principalManager::isTokenIssuedByAad)
                                              .orElse(null);
        if (aadIssuedBearerToken == null || alreadyAuthenticated()) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        try {
            final UserPrincipal userPrincipal = principalManager.buildUserPrincipal(aadIssuedBearerToken);
            final Authentication authentication = new PreAuthenticatedAuthenticationToken(
                userPrincipal,
                null,
                toSimpleGrantedAuthoritySet(userPrincipal)
            );
            LOGGER.info("Request token verification success. {}", authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            } finally {
                //Clear context after execution
                SecurityContextHolder.clearContext();
            }
        } catch (BadJWTException ex) {
            // Invalid JWT. Either expired or not yet valid.
            httpServletResponse.sendError(HttpStatus.UNAUTHORIZED.value());
        } catch (ParseException | BadJOSEException | JOSEException ex) {
            throw new ServletException("Failed to initialize UserPrincipal.", ex);
        }
    }

    private boolean alreadyAuthenticated() {
        return Optional.of(SecurityContextHolder.getContext())
                       .map(SecurityContext::getAuthentication)
                       .map(Authentication::isAuthenticated)
                       .orElse(false);
    }

    /**
     * Converts to user principal to a set of simple granted authorities.
     *
     * @param userPrincipal the user principal
     * @return A set of SimpleGrantedAuthoritys
     */
    protected Set<SimpleGrantedAuthority> toSimpleGrantedAuthoritySet(UserPrincipal userPrincipal) {
        Set<SimpleGrantedAuthority> simpleGrantedAuthoritySet =
            Optional.of(userPrincipal)
                    .map(UserPrincipal::getRoles)
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .filter(StringUtils::hasText)
                    .map(s -> new SimpleGrantedAuthority(AuthorityPrefix.ROLE + s))
                    .collect(Collectors.toSet());
        return Optional.of(simpleGrantedAuthoritySet)
                       .filter(r -> !r.isEmpty())
                       .orElse(DEFAULT_AUTHORITY_SET);

    }
}
