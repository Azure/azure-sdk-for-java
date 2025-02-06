// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventgrid;

import com.azure.core.util.BinaryData;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.EventGridServiceVersion;
import com.azure.spring.cloud.autoconfigure.implementation.AbstractAzureServiceConfigurationTests;
import com.azure.spring.cloud.autoconfigure.implementation.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.eventgrid.properties.AzureEventGridProperties;
import com.azure.spring.cloud.service.implementation.eventgrid.factory.EventGridPublisherClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void withoutClientBuilderShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventGridPublisherClientBuilder.class))
            .withPropertyValues("spring.cloud.azure.eventgrid.endpoint=" + String.format(ENDPOINT, "myeg"))
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventGridAutoConfiguration.class));
    }

    @Test
    void disableEventGridShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventgrid.enabled=false",
                "spring.cloud.azure.eventgrid.endpoint=" + String.format(ENDPOINT, "myeg")
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventGridAutoConfiguration.class));
    }

    @Test
    void withoutEndpointShouldNotConfigure() {
        this.contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventGridAutoConfiguration.class));
    }

    @Test
    void withEndpointShouldConfigure() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventgrid.endpoint=" + String.format(ENDPOINT, "myeg"))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventGridAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureEventGridProperties.class);
                assertThat(context).hasSingleBean(EventGridPublisherClientBuilderFactory.class);
                assertThat(context).hasSingleBean(EventGridPublisherClientBuilder.class);
                assertThat(context).hasSingleBean(EventGridPublisherClient.class);
                assertThat(context).hasSingleBean(EventGridPublisherAsyncClient.class);
            });
    }

    @Test
    void builderFactoryCanOverride() {
        AzureEventGridProperties properties = new AzureEventGridProperties();
        properties.setEndpoint(String.format(ENDPOINT, "myeg"));
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventgrid.endpoint=" + String.format(ENDPOINT, "myeg"))
            .withBean("myFactory", EventGridPublisherClientBuilderFactory.class, () -> new EventGridPublisherClientBuilderFactory(properties))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventGridAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureEventGridProperties.class);
                assertThat(context).hasSingleBean(EventGridPublisherClientBuilderFactory.class);
                assertThat(context).hasSingleBean(EventGridPublisherClientBuilder.class);
                assertThat(context).hasSingleBean(EventGridPublisherClient.class);
                assertThat(context).hasSingleBean(EventGridPublisherAsyncClient.class);

                assertThat(context).hasBean("myFactory");
            });
    }

    @Test
    void builderCanOverride() {
        EventGridPublisherClientBuilder myBuilder = new EventGridPublisherClientBuilder()
            .endpoint(String.format(ENDPOINT, "myeg"))
            .credential(new DefaultAzureCredentialBuilder().build());

        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventgrid.endpoint=" + String.format(ENDPOINT, "myeg"))
            .withBean("myBuilder", EventGridPublisherClientBuilder.class, () -> myBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventGridAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureEventGridProperties.class);
                assertThat(context).hasSingleBean(EventGridPublisherClientBuilderFactory.class);
                assertThat(context).hasSingleBean(EventGridPublisherClientBuilder.class);
                assertThat(context).hasSingleBean(EventGridPublisherClient.class);
                assertThat(context).hasSingleBean(EventGridPublisherAsyncClient.class);

                assertThat(context).hasBean("myBuilder");
            });
    }

    @Test
    @SuppressWarnings("unchecked")
    void clientCanOverride() {
        EventGridPublisherClient<BinaryData> myCustomClient = new EventGridPublisherClientBuilder()
            .endpoint(String.format(ENDPOINT, "myeg"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildCustomEventPublisherClient();

        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventgrid.endpoint=" + String.format(ENDPOINT, "myeg"))
            .withBean("myCustomClient", EventGridPublisherClient.class, () -> myCustomClient)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventGridAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureEventGridProperties.class);
                assertThat(context).hasSingleBean(EventGridPublisherClientBuilderFactory.class);
                assertThat(context).hasSingleBean(EventGridPublisherClientBuilder.class);
                assertThat(context).hasSingleBean(EventGridPublisherClient.class);
                assertThat(context).hasSingleBean(EventGridPublisherAsyncClient.class);
                assertThat(context).hasBean("myCustomClient");
                assertThat(context.getBean("myCustomClient")).isSameAs(myCustomClient);
                assertThat(context.getBean(EventGridPublisherClient.class)).isSameAs(myCustomClient);
            });
    }

    @Test
    void customizerShouldBeCalled() {
        EventGridBuilderCustomizer customizer = new EventGridBuilderCustomizer();
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventgrid.endpoint=" + String.format(ENDPOINT, "myeg"))
            .withBean("customizer1", EventGridBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", EventGridBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        EventGridBuilderCustomizer customizer = new EventGridBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventgrid.endpoint=" + String.format(ENDPOINT, "myeg"))
            .withBean("customizer1", EventGridBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", EventGridBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    @Test
    void configurationPropertiesShouldBind() {
        String endpoint = String.format(ENDPOINT, "mykv");
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventgrid.endpoint=" + endpoint,
                "spring.cloud.azure.eventgrid.service-version=V2018_01_01",
                "spring.cloud.azure.eventgrid.key=some-key",
                "spring.cloud.azure.eventgrid.sas-token=some-sas-token",
                "spring.cloud.azure.eventgrid.event-schema=CLOUD_EVENT"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventGridProperties.class);
                AzureEventGridProperties properties = context.getBean(AzureEventGridProperties.class);
                assertEquals(endpoint, properties.getEndpoint());
                assertEquals(EventGridServiceVersion.V2018_01_01, properties.getServiceVersion());
                assertEquals("some-key", properties.getKey());
                assertEquals("some-sas-token", properties.getSasToken());
                assertEquals(AzureEventGridProperties.EventSchema.CLOUD_EVENT, properties.getEventSchema());
            });
    }

    private static class EventGridBuilderCustomizer extends TestBuilderCustomizer<EventGridPublisherClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }
}
