package com.azure.spring.core.customizer;

public class NoOpAzureServiceClientBuilderCustomizer<T> implements AzureServiceClientBuilderCustomizer<T> {

    @Override
    public void customize(Object builder) {
        // no-op
    }
}
