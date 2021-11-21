// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web.pushrefresh;

import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.web.AppConfigurationEndpoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.azure.spring.cloud.config.web.AppConfigurationWebConstants.APPCONFIGURATION_REFRESH;
import static com.azure.spring.cloud.config.web.AppConfigurationWebConstants.VALIDATION_CODE_FORMAT_START;
import static com.azure.spring.cloud.config.web.AppConfigurationWebConstants.VALIDATION_CODE_KEY;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

/**
 * Endpoint for requesting new configurations to be loaded.
 */
@ControllerEndpoint(id = APPCONFIGURATION_REFRESH)
public final class AppConfigurationRefreshEndpoint implements ApplicationEventPublisherAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationRefreshEndpoint.class);

    private final ContextRefresher contextRefresher;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

        String reference = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        JsonNode kvReference = OBJECT_MAPPER.readTree(reference);
        AppConfigurationEndpoint validation = new AppConfigurationEndpoint(kvReference, appConfiguration.getStores(),
            allRequestParams);

        if (!validation.authenticate()) {
            return HttpStatus.UNAUTHORIZED.getReasonPhrase();
        }

        JsonNode validationResponse = kvReference.findValue(VALIDATION_CODE_KEY);
        if (validationResponse != null) {
            // Validating Web Hook
            return String.format("%s%s\"}", VALIDATION_CODE_FORMAT_START, validationResponse.asText());
        } else {
            if (contextRefresher != null) {
                if (validation.triggerRefresh()) {
                    publisher.publishEvent(
                        new AppConfigurationRefreshEvent(validation.getEndpoint()));
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
