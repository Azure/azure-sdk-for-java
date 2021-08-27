package com.azure.spring.core.customizer;

public interface AzureServiceClientBuilderCustomizer<T> {

    void customize(T builder);

}
