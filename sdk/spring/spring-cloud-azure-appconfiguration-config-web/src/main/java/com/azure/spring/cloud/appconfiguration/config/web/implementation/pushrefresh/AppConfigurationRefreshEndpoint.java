// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.web.implementation.pushrefresh;

import static com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationWebConstants.APPCONFIGURATION_REFRESH;
import static com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationWebConstants.VALIDATION_CODE_FORMAT_START;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationEndpoint;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Endpoint for requesting new configurations to be loaded.
 */
@SuppressWarnings("removal")
@ControllerEndpoint(id = APPCONFIGURATION_REFRESH)
public class AppConfigurationRefreshEndpoint implements ApplicationEventPublisherAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationRefreshEndpoint.class);

    private final ContextRefresher contextRefresher;

    private final AppConfigurationProperties appConfiguration;

    private ApplicationEventPublisher publisher;

    /**
     * Endpoint for triggering a refresh check for a single config store.
     * 
     * @param contextRefresher Used to verify refresh is available.
     * @param appConfiguration properties set for client library.
     */
    public AppConfigurationRefreshEndpoint(ContextRefresher contextRefresher,
        AppConfigurationProperties appConfiguration) {
        this.contextRefresher = contextRefresher;
        this.appConfiguration = appConfiguration;

    }

    /**
     * Checks a HttpServletRequest to see if it is a refresh event. Validates token information. If request is a
     * validation request returns validation code.
     *
     * @param request Request checked for refresh.
     * @param response Response for request.
     * @param allRequestParams request parameters needs to contain validation token.
     * @return 200 if refresh event triggered. 500 if invalid for any reason. Validation response if requested.
     * @throws IOException Unable to parse request info for validation.
     */
    @PostMapping(value = "/")
    @ResponseBody
    public String refresh(HttpServletRequest request, HttpServletResponse response,
        @RequestParam Map<String, String> allRequestParams) throws IOException {
        
        AppConfigurationEndpoint endpoint;
        try {
            endpoint = new AppConfigurationEndpoint(request, appConfiguration.getStores(),
                allRequestParams);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        }

        String syncToken = endpoint.getSyncToken();

        JsonNode validationResponse = endpoint.getValidationResponse();
        if (validationResponse != null) {
            // Validating Web Hook
            return String.format("%s%s\"}", VALIDATION_CODE_FORMAT_START, validationResponse.asText());
        } else {
            if (!endpoint.authenticate()) {
                return HttpStatus.UNAUTHORIZED.getReasonPhrase();
            }
            
            if (contextRefresher != null) {
                if (endpoint.triggerRefresh()) {
                    publisher.publishEvent(new AppConfigurationRefreshEvent(endpoint.getEndpoint(), syncToken));
                    return HttpStatus.OK.getReasonPhrase();
                } else {
                    LOGGER.debug("Non Refreshable notification");
                    return HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
                }
            } else {
                LOGGER.error("ContextRefresher Not Found. Unable to Refresh.");
                return HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
            }
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

}
