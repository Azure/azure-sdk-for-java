// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config.web.pushrefresh;

import static com.microsoft.azure.spring.cloud.config.web.Constants.APPCONFIGURATION_REFRESH;
import static com.microsoft.azure.spring.cloud.config.web.Constants.VALIDATION_CODE_FORMAT_START;
import static com.microsoft.azure.spring.cloud.config.web.Constants.VALIDATION_CODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.microsoft.azure.spring.cloud.config.web.AppConfigurationEndpoint;
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

@ControllerEndpoint(id = APPCONFIGURATION_REFRESH)
public class AppConfigurationRefreshEndpoint implements ApplicationEventPublisherAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationRefreshEndpoint.class);

    private ContextRefresher contextRefresher;

    private ObjectMapper objectmapper = new ObjectMapper();

    private AppConfigurationProperties appConfiguration;

    private ApplicationEventPublisher publisher;

    public AppConfigurationRefreshEndpoint(ContextRefresher contextRefresher,
        AppConfigurationProperties appConfiguration) {
        this.contextRefresher = contextRefresher;
        this.appConfiguration = appConfiguration;

    }

    @PostMapping(value = "/")
    @ResponseBody
    public String refresh(HttpServletRequest request, HttpServletResponse response,
        @RequestParam Map<String, String> allRequestParams) throws IOException {

        String reference = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        JsonNode kvReference = objectmapper.readTree(reference);
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
                    // Will just refresh the local configurations
                    // contextRefresher.refresh();
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
