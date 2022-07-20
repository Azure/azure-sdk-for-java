// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.converter;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.cosmos.CosmosClientProperties;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a {@link CosmosClientProperties.DirectConnectionProperties} to a {@link DirectConnectionConfig}.
 */
public final class DirectConnectionConfigConverter implements Converter<CosmosClientProperties.DirectConnectionProperties, DirectConnectionConfig> {

    public static final DirectConnectionConfigConverter DIRECT_CONNECTION_CONFIG_CONVERTER = new DirectConnectionConfigConverter();

    private DirectConnectionConfigConverter() {

    }

    @Override
    public DirectConnectionConfig convert(CosmosClientProperties.DirectConnectionProperties source) {
        DirectConnectionConfig result = new DirectConnectionConfig();

        PropertyMapper mapper = new PropertyMapper();

        mapper.from(source.getConnectionEndpointRediscoveryEnabled())
            .to(result::setConnectionEndpointRediscoveryEnabled);

        mapper.from(source.getConnectTimeout()).to(result::setConnectTimeout);
        mapper.from(source.getIdleConnectionTimeout()).to(result::setIdleConnectionTimeout);
        mapper.from(source.getIdleEndpointTimeout()).to(result::setIdleEndpointTimeout);
        mapper.from(source.getNetworkRequestTimeout()).to(result::setNetworkRequestTimeout);
        mapper.from(source.getMaxConnectionsPerEndpoint()).to(result::setMaxConnectionsPerEndpoint);
        mapper.from(source.getMaxRequestsPerConnection()).to(result::setMaxRequestsPerConnection);

        return result;
    }
}
