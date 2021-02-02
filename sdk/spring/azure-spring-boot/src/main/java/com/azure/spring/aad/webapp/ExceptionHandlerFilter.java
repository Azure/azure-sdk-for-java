// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.Constants;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Handle the specified exception.
 */
public class ExceptionHandlerFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            String claims = Optional.of(exception)
                                    .map(Throwable::getCause)
                                    .filter(e -> e instanceof ConditionalAccessException)
                                    .map(e -> (ConditionalAccessException) e)
                                    .map(ConditionalAccessException::getClaims)
                                    .orElse(null);
            if (claims != null) {
                request.getSession().setAttribute(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS,
                    claims);
                response.setStatus(302);
                response.sendRedirect(Constants.DEFAULT_AUTHORITY_ENDPOINT_URL);
                return;
            }
            throw exception;
        }
    }
}
