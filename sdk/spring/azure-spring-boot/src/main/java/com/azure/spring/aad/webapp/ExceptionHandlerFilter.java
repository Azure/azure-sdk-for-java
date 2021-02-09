// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.aad.webapi.AADOAuth2OboAuthorizedClientRepository;
import com.azure.spring.autoconfigure.aad.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handle the specified exception.
 */
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADOAuth2OboAuthorizedClientRepository.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            WebClientResponseException webClientResponseException =
                Optional.of(exception)
                        .map(Throwable::getCause)
                        .filter(e -> e instanceof WebClientResponseException)
                        .map(e -> (WebClientResponseException) e)
                        .filter(ExceptionHandlerFilter::isConditionalAccessExceptionFromObo)
                        .orElse(null);
            if (webClientResponseException != null) {
                handleConditionalAccess(webClientResponseException, request, response);
                return;
            }
            throw exception;
        }
    }

    private static boolean isConditionalAccessExceptionFromObo(WebClientResponseException exception) {
        String result = Optional.of(exception)
                                .map(WebClientResponseException::getHeaders)
                                .map(httpHeaders -> httpHeaders.get(HttpHeaders.WWW_AUTHENTICATE))
                                .map(list -> list.get(0))
                                .filter(value -> value.contains(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS))
                                .orElse(null);
        return result != null;
    }

    static void handleConditionalAccess(WebClientResponseException exception, HttpServletRequest request,
                                        HttpServletResponse response) {
        Map<String, String> parameters =
            Optional.of(exception)
                    .map(WebClientResponseException::getHeaders)
                    .map(httpHeaders -> httpHeaders.get(HttpHeaders.WWW_AUTHENTICATE))
                    .map(list -> list.get(0))
                    .map(ExceptionHandlerFilter::parseAuthParameters)
                    .orElse(null);
        request.getSession().setAttribute(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS,
            parameters.get(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS));
        response.setStatus(302);
        try {
            response.sendRedirect(Constants.DEFAULT_AUTHORITY_ENDPOINT_URI);
        } catch (IOException e) {
            LOGGER.error("Failed to load authorized client.", exception);
        }
    }

    private static Map<String, String> parseAuthParameters(String wwwAuthenticateHeader) {
        return Stream.of(wwwAuthenticateHeader)
                     .filter(header -> !StringUtils.isEmpty(header))
                     .map(str -> str.substring(Constants.BEARER_PREFIX.length() + 1, str.length() - 1))
                     .map(str -> str.split(", "))
                     .flatMap(Stream::of)
                     .map(parameter -> parameter.split("="))
                     .filter(parameter -> parameter.length > 1)
                     .collect(Collectors.toMap(
                         parameters -> parameters[0],
                         parameters -> parameters[1]));
    }
}
