package com.azure.spring.core.credential.resolver;

import com.azure.spring.core.credential.provider.AzureConnectionStringProvider;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.aware.credential.ConnectionStringAware;
import org.springframework.util.StringUtils;

/**
 * Resolve the token credential according to the azure properties.
 */
public class AzureConnectionStringResolver implements AzureCredentialResolver<AzureConnectionStringProvider> {

    @Override
    public AzureConnectionStringProvider resolve(AzureProperties properties) {
        if (!isResolvable(properties)) {
            return null;
        }

        String connectionString = ((ConnectionStringAware) properties).getConnectionString();
        if (!StringUtils.hasText(connectionString)) {
            return null;
        }

        return new AzureConnectionStringProvider(connectionString);
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return properties instanceof ConnectionStringAware;
    }

}
