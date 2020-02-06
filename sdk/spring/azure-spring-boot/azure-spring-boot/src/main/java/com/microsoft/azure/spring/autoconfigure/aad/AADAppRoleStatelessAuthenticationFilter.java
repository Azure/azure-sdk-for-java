/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import static org.springframework.util.StringUtils.hasText;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.proc.BadJWTException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class AADAppRoleStatelessAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AADAppRoleStatelessAuthenticationFilter.class);


    private static final String TOKEN_TYPE = "Bearer ";
    private static final JSONArray DEFAULT_ROLE_CLAIM = new JSONArray().appendElement("USER");
    private static final String ROLE_PREFIX = "ROLE_";

    private final UserPrincipalManager principalManager;

    public AADAppRoleStatelessAuthenticationFilter(UserPrincipalManager principalManager) {
        this.principalManager = principalManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean cleanupRequired = false;

        if (hasText(authHeader) && authHeader.startsWith(TOKEN_TYPE)) {
            try {
                final String token = authHeader.replace(TOKEN_TYPE, "");
                final UserPrincipal principal = principalManager.buildUserPrincipal(token);
                final JSONArray roles = Optional.ofNullable((JSONArray) principal.getClaims().get("roles"))
                    .filter(r -> !r.isEmpty())
                    .orElse(DEFAULT_ROLE_CLAIM);
                final Authentication authentication = new PreAuthenticatedAuthenticationToken(
                    principal, null, rolesToGrantedAuthorities(roles));
                authentication.setAuthenticated(true);
                log.info("Request token verification success. {}", authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                cleanupRequired = true;
            } catch (BadJWTException ex) {
                final String errorMessage = "Invalid JWT. Either expired or not yet valid. " + ex.getMessage();
                log.warn(errorMessage);
                throw new ServletException(errorMessage, ex);
            } catch (ParseException | BadJOSEException | JOSEException ex) {
                log.error("Failed to initialize UserPrincipal.", ex);
                throw new ServletException(ex);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (cleanupRequired) {
                //Clear context after execution
                SecurityContextHolder.clearContext();
            }
        }
    }

    protected Set<SimpleGrantedAuthority> rolesToGrantedAuthorities(JSONArray roles) {
        return roles.stream()
            .filter(Objects::nonNull)
            .map(s -> new SimpleGrantedAuthority(ROLE_PREFIX + s))
            .collect(Collectors.toSet());
    }
}
