package com.azure.spring.cloud.appconfiguration.config.implementation;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;

public class Supplier implements InstanceSupplier<AppConfigurationKeyVaultClientFactory>{

    @Override
    public AppConfigurationKeyVaultClientFactory get(BootstrapContext context) {
        // TODO Auto-generated method stub
        return null;
    }

}
