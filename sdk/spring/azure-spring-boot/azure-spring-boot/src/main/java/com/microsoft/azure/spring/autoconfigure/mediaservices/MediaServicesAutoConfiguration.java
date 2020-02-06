/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.mediaservices;

import com.microsoft.azure.telemetry.TelemetrySender;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.authentication.AzureAdClientSymmetricKey;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenCredentials;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenProvider;
import com.microsoft.windowsazure.services.media.authentication.AzureEnvironments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.microsoft.azure.telemetry.TelemetryData.SERVICE_NAME;
import static com.microsoft.azure.telemetry.TelemetryData.getClassPackageSimpleName;
import static com.microsoft.windowsazure.Configuration.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Configuration
@ConditionalOnResource(resources = "classpath:mediaservices.enable.config")
@ConditionalOnMissingBean(MediaContract.class)
@EnableConfigurationProperties(MediaServicesProperties.class)
@ConditionalOnProperty(prefix = "azure.mediaservices",
        value = {"tenant", "client-id", "client-secret", "rest-api-endpoint"})
public class MediaServicesAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(MediaServicesAutoConfiguration.class);

    private final MediaServicesProperties properties;

    public MediaServicesAutoConfiguration(MediaServicesProperties mediaServicesProperties) {
        this.properties = mediaServicesProperties;
    }

    @Bean
    public MediaContract mediaContract() throws ServiceException, MalformedURLException, URISyntaxException {
        LOG.debug("mediaContract called");
        return createMediaContract();
    }

    private MediaContract createMediaContract() throws ServiceException, MalformedURLException, URISyntaxException {
        LOG.debug("createMediaContract called");

        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        final AzureAdTokenCredentials credentials = new AzureAdTokenCredentials(properties.getTenant(),
                new AzureAdClientSymmetricKey(properties.getClientId(), properties.getClientSecret()),
                AzureEnvironments.AZURE_CLOUD_ENVIRONMENT);

        final AzureAdTokenProvider tokenProvider = new AzureAdTokenProvider(credentials, executorService);

        final com.microsoft.windowsazure.Configuration configuration = MediaConfiguration
                .configureWithAzureAdTokenProvider(new URI(properties.getRestApiEndpoint()), tokenProvider);

        if (properties.getConnectTimeout() != null) {
            configuration.getProperties().put(PROPERTY_CONNECT_TIMEOUT, properties.getConnectTimeout());
        }
        if (properties.getReadTimeout() != null) {
            configuration.getProperties().put(PROPERTY_READ_TIMEOUT, properties.getReadTimeout());
        }

        if (!StringUtils.isEmpty(properties.getProxyHost()) && nonNull(properties.getProxyPort())) {
            configuration.getProperties().put(PROPERTY_HTTP_PROXY_HOST, properties.getProxyHost());
            configuration.getProperties().put(PROPERTY_HTTP_PROXY_PORT, properties.getProxyPort());
            configuration.getProperties().put(PROPERTY_HTTP_PROXY_SCHEME, properties.getProxyScheme());
        } else if (!StringUtils.isEmpty(properties.getProxyHost()) && isNull(properties.getProxyPort())) {
            throw new ServiceException("Please configure azure.mediaservices.proxy-port");
        } else if (nonNull(properties.getProxyPort()) && StringUtils.isEmpty(properties.getProxyHost())) {
            throw new ServiceException("Please configure azure.mediaservices.proxy-host");
        }

        return MediaService.create(configuration);
    }

    @PostConstruct
    private void sendTelemetry() {
        if (properties.isAllowTelemetry()) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();

            events.put(SERVICE_NAME, getClassPackageSimpleName(MediaServicesAutoConfiguration.class));

            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }
}
