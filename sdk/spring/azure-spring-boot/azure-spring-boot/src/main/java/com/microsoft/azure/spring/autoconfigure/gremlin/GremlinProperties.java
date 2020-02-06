/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.gremlin;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Validated
@ConfigurationProperties("gremlin")
public class GremlinProperties {

    @NotEmpty
    private String endpoint;

    private int port;

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    private boolean telemetryAllowed = true;

    private boolean sslEnabled = true;
}
