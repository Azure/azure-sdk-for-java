/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web.pushbusrefresh;

import static com.microsoft.azure.spring.cloud.config.web.Constants.APPCONFIGURATION_REFRESH_BUS;
import static com.microsoft.azure.spring.cloud.config.web.Constants.VALIDATION_CODE_FORMAT_START;
import static com.microsoft.azure.spring.cloud.config.web.Constants.VALIDATION_CODE_KEY;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.cloud.bus.endpoint.AbstractBusEndpoint;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.microsoft.azure.spring.cloud.config.web.AppConfigurationEndpoint;

@ControllerEndpoint(id = APPCONFIGURATION_REFRESH_BUS)
public class AppConfigurationBusRefreshEndpoint extends AbstractBusEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationBusRefreshEndpoint.class);

    private ObjectMapper objectmapper = new ObjectMapper();

    private AppConfigurationProperties appConfiguration;

    public AppConfigurationBusRefreshEndpoint(ApplicationEventPublisher context, String appId,
            AppConfigurationProperties appConfiguration) {
        super(context, appId);
        this.appConfiguration = appConfiguration;
    }

    @PostMapping(value = "/")
    @ResponseBody
    public String refresh(HttpServletRequest request, HttpServletResponse response,
            @RequestParam Map<String, String> allRequestParams) throws IOException {
        String reference = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        JsonNode kvReference = objectmapper.readTree(reference);

        AppConfigurationEndpoint validation;
        try {
            validation = new AppConfigurationEndpoint(kvReference, appConfiguration.getStores(),
                    allRequestParams);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        }
        
        if (!validation.authenticate()) {
            return HttpStatus.UNAUTHORIZED.getReasonPhrase();
        }

        JsonNode validationResponse = kvReference.findValue(VALIDATION_CODE_KEY);
        if (validationResponse != null) {
            // Validating Web Hook
            return VALIDATION_CODE_FORMAT_START + validationResponse.asText() + "\"}";
        } else {
            if (validation.triggerRefresh()) {
                // Spring Bus is in use, will publish a RefreshRemoteApplicationEvent
                publish(new AppConfigurationBusRefreshEvent(validation.getEndpoint(), this, getInstanceId()));
                return HttpStatus.OK.getReasonPhrase();
            } else {
                LOGGER.debug("Non Refreshable notification");
                return HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
            }
        }
    }
}
