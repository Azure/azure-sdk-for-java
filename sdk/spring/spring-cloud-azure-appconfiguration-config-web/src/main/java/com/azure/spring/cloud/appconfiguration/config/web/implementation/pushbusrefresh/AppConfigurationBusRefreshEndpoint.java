// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.web.implementation.pushbusrefresh;

import static com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationWebConstants.APPCONFIGURATION_REFRESH_BUS;
import static com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationWebConstants.VALIDATION_CODE_FORMAT_START;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.cloud.bus.endpoint.AbstractBusEndpoint;
import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.event.PathDestinationFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationEndpoint;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Endpoint for requesting new configurations to be loaded in all registered instances on the Bus.
 */
@ControllerEndpoint(id = APPCONFIGURATION_REFRESH_BUS)
public final class AppConfigurationBusRefreshEndpoint extends AbstractBusEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationBusRefreshEndpoint.class);

    private final AppConfigurationProperties appConfiguration;

    /**
     * Endpoint for triggering a refresh check for a single config store, across all registered instances.
     * 
     * @param context Service Bus event publisher
     * @param appId Service Bus app id
     * @param destinationFactory service bus destination factory
     * @param appConfiguration properties set for client library.
     */
    public AppConfigurationBusRefreshEndpoint(ApplicationEventPublisher context, String appId,
        Destination.Factory destinationFactory,
        AppConfigurationProperties appConfiguration) {
        super(context, appId, destinationFactory);
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

        if (!endpoint.authenticate()) {
            return HttpStatus.UNAUTHORIZED.getReasonPhrase();
        }

        String syncToken = endpoint.getSyncToken();

        JsonNode validationResponse = endpoint.getValidationResponse();
        if (validationResponse != null) {
            // Validating Web Hook
            return VALIDATION_CODE_FORMAT_START + validationResponse.asText() + "\"}";
        } else {
            if (endpoint.triggerRefresh()) {
                // Spring Bus is in use, will publish a RefreshRemoteApplicationEvent

                publish(new AppConfigurationBusRefreshEvent(endpoint.getEndpoint(), syncToken, this, getInstanceId(),
                        new PathDestinationFactory().getDestination(null)));
                return HttpStatus.OK.getReasonPhrase();
            } else {
                LOGGER.debug("Non Refreshable notification");
                return HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
            }
        }
    }
}
