// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapp;

import com.azure.spring.aad.webapi.AADOAuth2OboAuthorizedClientRepository;
import com.azure.spring.autoconfigure.aad.Constants;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An exception handle Conditional Access in On-Behalf-Of flow.
 *
 * <p>
 * On-Behalf-Of allows you to exchange an access token that your API received for an access token to another API. For
 * better understanding On-Behalf-Of, the reference documentation can help us. See the <a
 * href="https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow">Microsoft identity
 * platform and OAuth 2.0 On-Behalf-Of flow</a>
 *
 * <p>
 * Conditional Access is the tool used by Azure Active Directory to bring signals together, to make decisions, and
 * enforce organizational policies. The reference documentation is
 * <a href="https://docs.microsoft.com/en-us/azure/active-directory/conditional-access">Azure AD Conditional Access
 * documentation</a>
 *
 * <p>
 * <img src="../doc-files/ConditionalAccessException.svg" alt="">
 *
 * <p>
 * Step 3,4,5,6  describe Conditional Access(such as multi-factor authentication) in obo flow:
 *
 * <p>
 * step 3:  {@link AADOAuth2OboAuthorizedClientRepository#loadAuthorizedClient(String, Authentication,
 * HttpServletRequest)} sends the OBO Request to AAD.
 *
 * <p>
 * step 4 : AAD Conditional Access occurs and return an error(The claims field in this error is the reauthorization
 * certificate).
 *
 * <p>
 * step 5: {@link AADOAuth2OboAuthorizedClientRepository}get the claims field create a response by {@link
 * #parametersToHttpHeader(Map)}.
 *
 * <p>
 * step 6: {@link AADWebAppConfiguration#conditionalAccessExceptionFilterFunction()} receives the response and convert
 * it into {@link ConditionalAccessException}. {@link ConditionalAccessException} can catch this exception and put the
 * claims field into session. then clear the authorization information and redirect. At last {@link
 * AADOAuth2AuthorizationRequestResolver} intercepts authorization-url, put claims into {@link
 * OAuth2AuthorizationRequest} to reauthorize.
 */
public final class ConditionalAccessException extends RuntimeException {
    private final Map<String, Object> parameters;

    protected ConditionalAccessException(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public static ConditionalAccessException fromHttpHeader(String httpHeader) {
        return new ConditionalAccessException(ConditionalAccessException.httpHeaderToParameters(httpHeader));
    }

    /**
     * Convert httpHeader to map structure.
     *
     * @param httpHeader httpHeader
     * @return Map Object
     */
    public static Map<String, Object> httpHeaderToParameters(String httpHeader) {
        String[] parameters = Optional.of(httpHeader)
                                      .map(str -> str.substring(Constants.BEARER_PREFIX.length() + 1, str.length() - 1))
                                      .map(str -> str.split(", "))
                                      .orElse(null);
        return Arrays.asList(parameters)
                     .stream()
                     .map(elem -> elem.split("="))
                     .collect(Collectors.toMap(e -> e[0], e -> e[1]));
    }

    /**
     * Convert parameters to JsonString structure.
     *
     * @param parameters returned by webApi.
     * @return String Object
     */
    public static String parametersToHttpHeader(Map<String, Object> parameters) {
        return Constants.BEARER_PREFIX + parameters.toString();
    }

    public static boolean isConditionAccessException(String httpHeader) {
        return httpHeader.startsWith(Constants.BEARER_PREFIX)
            && ConditionalAccessException.httpHeaderToParameters(httpHeader)
                                         .get(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS) != null;
    }
}
