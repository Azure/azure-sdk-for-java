package com.azure.spring.cloud.autoconfigure.aadb2c.implementation.config;

@FunctionalInterface
public interface AadB2cClientRegistrationRepositoryBuilderConfigurer {

    void configure(AadB2cClientRegistrationRepositoryBuilder builder);

}
