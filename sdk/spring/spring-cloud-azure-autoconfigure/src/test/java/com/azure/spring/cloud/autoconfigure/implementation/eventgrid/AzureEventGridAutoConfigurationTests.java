package com.azure.spring.cloud.autoconfigure.implementation.eventgrid;

import com.azure.spring.cloud.autoconfigure.AbstractAzureServiceConfigurationTests;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.eventgrid.properties.AzureEventGridProperties;
import com.azure.spring.cloud.service.implementation.eventgrid.factory.EventGridPublisherClientBuilderFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AzureEventGridAutoConfigurationTests extends AbstractAzureServiceConfigurationTests<
    EventGridPublisherClientBuilderFactory, AzureEventGridProperties> {

    private static final String ENDPOINT = "https://%s.somelocation.eventgrid.azure.net/api/eventseventgrid.azure.net/api/events";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            AzureGlobalPropertiesAutoConfiguration.class,
            AzureEventGridAutoConfiguration.class));


    @Override
    protected ApplicationContextRunner getMinimalContextRunner() {
        return this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventgrid.endpoint=" + String.format(ENDPOINT, "myeg"));
    }

    @Override
    protected String getPropertyPrefix() {
        return AzureEventGridProperties.PREFIX;
    }

    @Override
    protected Class<EventGridPublisherClientBuilderFactory> getBuilderFactoryType() {
        return EventGridPublisherClientBuilderFactory.class;
    }

    @Override
    protected Class<AzureEventGridProperties> getConfigurationPropertiesType() {
        return AzureEventGridProperties.class;
    }
}
