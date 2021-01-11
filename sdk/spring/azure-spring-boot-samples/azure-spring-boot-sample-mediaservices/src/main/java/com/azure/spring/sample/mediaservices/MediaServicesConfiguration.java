// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.mediaservices;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.authentication.AzureAdClientSymmetricKey;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenCredentials;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.microsoft.windowsazure.Configuration.PROPERTY_CONNECT_TIMEOUT;
import static com.microsoft.windowsazure.Configuration.PROPERTY_HTTP_PROXY_HOST;
import static com.microsoft.windowsazure.Configuration.PROPERTY_HTTP_PROXY_PORT;
import static com.microsoft.windowsazure.Configuration.PROPERTY_HTTP_PROXY_SCHEME;
import static com.microsoft.windowsazure.Configuration.PROPERTY_READ_TIMEOUT;
import static com.microsoft.windowsazure.services.media.authentication.AzureEnvironments.AZURE_CLOUD_ENVIRONMENT;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;

@Configuration
@EnableConfigurationProperties(MediaServicesProperties.class)
public class MediaServicesConfiguration {

    private final MediaServicesProperties properties;

    public MediaServicesConfiguration(MediaServicesProperties mediaServicesProperties) {
        this.properties = mediaServicesProperties;
    }

    /**
     * Configure the media service bean with provided properties.
     *
     * @return The media service bean.
     * @throws ServiceException If proxy is enabled but not configured right.
     * @throws MalformedURLException If provide Azure environment is not valid.
     * @throws URISyntaxException If the rest API endpoint URI is not valid.
     */
    @Bean
    public MediaContract mediaContract() throws ServiceException, MalformedURLException, URISyntaxException {
        final AzureAdTokenCredentials credentials = new AzureAdTokenCredentials(properties.getTenant(),
            new AzureAdClientSymmetricKey(properties.getClientId(), properties.getClientSecret()),
            AZURE_CLOUD_ENVIRONMENT);

        final AzureAdTokenProvider tokenProvider = new AzureAdTokenProvider(credentials, newFixedThreadPool(1));

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

}
