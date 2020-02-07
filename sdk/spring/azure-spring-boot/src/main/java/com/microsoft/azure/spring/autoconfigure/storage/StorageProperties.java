/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.storage;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("azure.storage")
public class StorageProperties {
    @NotEmpty
    @Getter
    @Setter
    private String accountName;

    @NotEmpty
    @Getter
    @Setter
    private String accountKey;

    @Getter
    @Setter
    private String containerName;

    @Getter
    @Setter
    private boolean allowTelemetry = true;
    
    @Getter
    @Setter
    private boolean enableHttps = false;
}
