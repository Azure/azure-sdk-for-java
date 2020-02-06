/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.mediaservices;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("azure.mediaservices")
public class MediaServicesProperties {
    @NotEmpty(message = "azure.mediaservices.tenant property must be configured.")
    private String tenant;

    /**
     * Media service Azure Active Directory client-id(application id).
     */
    @NotEmpty(message = "azure.mediaservices.client-id property must be configured.")
    private String clientId;

    /**
     * Media service Azure Active Directory client secret.
     */
    @NotEmpty(message = "azure.mediaservices.client-secret property must be configured.")
    private String clientSecret;

    /**
     * Media service REST API endpoint.
     */
    @NotEmpty(message = "azure.mediaservices.rest-api-endpoint property must be configured.")
    private String restApiEndpoint;

    /**
     * Proxy host if to use proxy.
     */
    private String proxyHost;

    /**
     * Proxy port if to use proxy.
     */
    private Integer proxyPort;

    /**
     * Proxy scheme if to use proxy. Default is http.
     */
    private String proxyScheme = "http";

    /**
     * Whether allow telemetry collecting.
     */
    private boolean allowTelemetry = true;

    /**
     * Socket connect timeout
     */
    private Integer connectTimeout;

    /**
     * Socket read timeout
     */
    private Integer readTimeout;

}
