// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.converter;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.cosmos.CosmosClientProperties;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a {@link CosmosClientProperties.DirectConnectionProperties} to a {@link DirectConnectionConfig}.
 */
public final class GatewayConnectionConfigConverter implements Converter<CosmosClientProperties.GatewayConnectionProperties, GatewayConnectionConfig> {

    public static final GatewayConnectionConfigConverter GATEWAY_CONNECTION_CONFIG_CONVERTER = new GatewayConnectionConfigConverter();

    private GatewayConnectionConfigConverter() {

    }

    @Override
    public GatewayConnectionConfig convert(CosmosClientProperties.GatewayConnectionProperties source) {
        GatewayConnectionConfig result = new GatewayConnectionConfig();

        PropertyMapper mapper = new PropertyMapper();

        mapper.from(source.getIdleConnectionTimeout()).to(result::setIdleConnectionTimeout);
        mapper.from(source.getMaxConnectionPoolSize()).to(result::setMaxConnectionPoolSize);

        return result;
    }
}
