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
import java.util.Map;
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
            Map<String, Object> parameters = Optional.of(exception)
                                                     .map(Throwable::getCause)
                                                     .filter(e -> e instanceof ConditionalAccessException)
                                                     .map(e -> (ConditionalAccessException) e)
                                                     .map(ConditionalAccessException::getParameters)
                                                     .orElse(null);
            if (parameters != null && parameters.get(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS) != null) {
                request.getSession().setAttribute(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS,
                    parameters.get(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS));
                response.setStatus(302);
                response.sendRedirect(parameters.get(Constants.DEFAULT_AUTHORITY_ENDPOINT_URI).toString());
                return;
            }
            throw exception;
        }
    }
}
