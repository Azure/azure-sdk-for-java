// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.aot.test.generate.TestGenerationContext;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.aot.ApplicationContextAotGenerator;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.test.tools.CompileWithForkedClassLoader;
import org.springframework.core.test.tools.TestCompiler;
import org.springframework.javapoet.ClassName;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Consumer;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.OTHER;
import static org.assertj.core.api.Assertions.assertThat;

class AzureGlobalPropertiesAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureGlobalPropertiesAutoConfiguration.class));

    @Test
    void testAutoConfiguration() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AzureGlobalPropertiesAutoConfiguration.class);
            assertThat(context).hasSingleBean(AzureGlobalProperties.class);
        });
    }

    @Test
    void configurationPropertiesShouldBind() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.client.application-id=fake-application-id",
                "spring.cloud.azure.client.http.headers[0].name=header-name",
                "spring.cloud.azure.client.http.headers[0].values=a,b,c",
                "spring.cloud.azure.client.http.connect-timeout=1m",
                "spring.cloud.azure.client.http.read-timeout=2m",
                "spring.cloud.azure.client.http.response-timeout=3m",
                "spring.cloud.azure.client.http.write-timeout=4m",
                "spring.cloud.azure.client.http.connection-idle-timeout=5m",
                "spring.cloud.azure.client.http.maximum-connection-pool-size=6",
                "spring.cloud.azure.client.amqp.transport-type=AmqpWebSockets",
                "spring.cloud.azure.credential.client-id=fake-client-id",
                "spring.cloud.azure.credential.client-secret=fake-client-secret",
                "spring.cloud.azure.credential.client-certificate-path=fake-cert-path",
                "spring.cloud.azure.credential.client-certificate-password=fake-cert-password",
                "spring.cloud.azure.credential.username=fake-username",
                "spring.cloud.azure.credential.password=fake-password",
                "spring.cloud.azure.credential.managed-identity-enabled=true",
                "spring.cloud.azure.proxy.type=https",
                "spring.cloud.azure.proxy.hostname=proxy-host",
                "spring.cloud.azure.proxy.port=8888",
                "spring.cloud.azure.proxy.username=x-user",
                "spring.cloud.azure.proxy.password=x-password",
                "spring.cloud.azure.proxy.http.non-proxy-hosts=127.0.0.1",
                "spring.cloud.azure.proxy.amqp.authentication-type=basic",
                "spring.cloud.azure.retry.exponential.max-retries=1",
                "spring.cloud.azure.retry.exponential.base-delay=20s",
                "spring.cloud.azure.retry.exponential.max-delay=30s",
                "spring.cloud.azure.retry.fixed.max-retries=4",
                "spring.cloud.azure.retry.fixed.delay=50s",
                "spring.cloud.azure.retry.mode=fixed",
                "spring.cloud.azure.retry.amqp.try-timeout=200s",
                "spring.cloud.azure.profile.tenant-id=fake-tenant-id",
                "spring.cloud.azure.profile.subscription-id=fake-sub-id",
                "spring.cloud.azure.profile.cloud-type=azure_china"
            )
            .run(context -> {
                final AzureGlobalProperties azureProperties = context.getBean(AzureGlobalProperties.class);
                assertThat(azureProperties.getClient().getApplicationId()).isEqualTo("fake-application-id");
                assertThat(azureProperties.getClient().getHttp().getHeaders().get(0).getName()).isEqualTo("header-name");
                assertThat(azureProperties.getClient().getHttp().getHeaders().get(0).getValues()).isEqualTo(Arrays.asList("a", "b", "c"));
                assertThat(azureProperties.getClient().getHttp().getConnectTimeout()).isEqualTo(Duration.ofMinutes(1));
                assertThat(azureProperties.getClient().getHttp().getReadTimeout()).isEqualTo(Duration.ofMinutes(2));
                assertThat(azureProperties.getClient().getHttp().getResponseTimeout()).isEqualTo(Duration.ofMinutes(3));
                assertThat(azureProperties.getClient().getHttp().getWriteTimeout()).isEqualTo(Duration.ofMinutes(4));
                assertThat(azureProperties.getClient().getHttp().getConnectionIdleTimeout()).isEqualTo(Duration.ofMinutes(5));
                assertThat(azureProperties.getClient().getHttp().getMaximumConnectionPoolSize()).isEqualTo(6);
                assertThat(azureProperties.getClient().getAmqp().getTransportType()).isEqualTo(AmqpTransportType.AMQP_WEB_SOCKETS);

                assertThat(azureProperties.getCredential().getClientId()).isEqualTo("fake-client-id");
                assertThat(azureProperties.getCredential().getClientSecret()).isEqualTo("fake-client-secret");
                assertThat(azureProperties.getCredential().getClientCertificatePath()).isEqualTo("fake-cert-path");
                assertThat(azureProperties.getCredential().getClientCertificatePassword()).isEqualTo("fake-cert-password");
                assertThat(azureProperties.getCredential().getUsername()).isEqualTo("fake-username");
                assertThat(azureProperties.getCredential().getPassword()).isEqualTo("fake-password");
                assertThat(azureProperties.getCredential().isManagedIdentityEnabled()).isTrue();

                assertThat(azureProperties.getProxy().getType()).isEqualTo("https");
                assertThat(azureProperties.getProxy().getHostname()).isEqualTo("proxy-host");
                assertThat(azureProperties.getProxy().getPort()).isEqualTo(8888);
                assertThat(azureProperties.getProxy().getUsername()).isEqualTo("x-user");
                assertThat(azureProperties.getProxy().getPassword()).isEqualTo("x-password");
                assertThat(azureProperties.getProxy().getAmqp().getAuthenticationType()).isEqualTo("basic");
                assertThat(azureProperties.getProxy().getHttp().getNonProxyHosts()).isEqualTo("127.0.0.1");

                assertThat(azureProperties.getRetry().getExponential().getMaxRetries()).isEqualTo(1);
                assertThat(azureProperties.getRetry().getExponential().getBaseDelay()).isEqualTo(Duration.ofSeconds(20));
                assertThat(azureProperties.getRetry().getExponential().getMaxDelay()).isEqualTo(Duration.ofSeconds(30));
                assertThat(azureProperties.getRetry().getFixed().getMaxRetries()).isEqualTo(4);
                assertThat(azureProperties.getRetry().getFixed().getDelay()).isEqualTo(Duration.ofSeconds(50));
                assertThat(azureProperties.getRetry().getMode()).isEqualTo(RetryOptionsProvider.RetryMode.FIXED);
                assertThat(azureProperties.getRetry().getAmqp().getTryTimeout()).isEqualTo(Duration.ofSeconds(200));

                assertThat(azureProperties.getProfile().getTenantId()).isEqualTo("fake-tenant-id");
                assertThat(azureProperties.getProfile().getSubscriptionId()).isEqualTo("fake-sub-id");
                assertThat(azureProperties.getProfile().getCloudType()).isEqualTo(AZURE_CHINA);
                assertThat(azureProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint()).isEqualTo(
                    AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint());
            });
    }

    @Test
    void testAzureProfileOtherCouldModifyEndpoint() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.profile.environment.activeDirectoryEndpoint=abc",
                "spring.cloud.azure.profile.cloud-type=other"
            )
            .run(context -> {
                final AzureGlobalProperties azureProperties = context.getBean(AzureGlobalProperties.class);
                assertThat(azureProperties).extracting("profile.cloudType").isEqualTo(OTHER);
                assertThat(azureProperties).extracting("profile.environment.activeDirectoryEndpoint").isEqualTo("abc");
            });
    }

    @Test
    void testAzureProfileAzureCouldModifyEndpoint() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.profile.environment.activeDirectoryEndpoint=abc",
                "spring.cloud.azure.profile.cloud-type=azure"
            )
            .run(context -> {
                final AzureGlobalProperties azureProperties = context.getBean(AzureGlobalProperties.class);
                assertThat(azureProperties).extracting("profile.cloudType").isEqualTo(AZURE);
                assertThat(azureProperties).extracting("profile.environment.activeDirectoryEndpoint")
                                           .isEqualTo("abc");
            });
    }

    @Test
    void testAzureProfileAzureChina() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.profile.cloud-type=azure_china"
            )
            .run(context -> {
                final AzureGlobalProperties azureProperties = context.getBean(AzureGlobalProperties.class);
                assertThat(azureProperties).extracting("profile.cloudType").isEqualTo(AZURE_CHINA);
                assertThat(azureProperties).extracting("profile.environment.activeDirectoryEndpoint")
                                           .isEqualTo(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint());
            });
    }

    @Test
    @CompileWithForkedClassLoader
    void processAheadOfTimeDoesNotRegisterAzureGlobalProperties() {
        GenericApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(AzureGlobalPropertiesAutoConfiguration.class);
        compile(context, (freshContext) -> {
            freshContext.refresh();
            assertThat(freshContext.getBeansOfType(AzureGlobalProperties.class)).isEmpty();
        });
    }

    @SuppressWarnings("unchecked")
    private void compile(GenericApplicationContext context, Consumer<GenericApplicationContext> freshContext) {
        TestGenerationContext generationContext = new TestGenerationContext(
            ClassName.get(getClass().getPackageName(), "TestTarget"));
        ClassName className = new ApplicationContextAotGenerator().processAheadOfTime(context, generationContext);
        generationContext.writeGeneratedContent();
        TestCompiler.forSystem().with(generationContext).compile((compiled) -> {
            GenericApplicationContext freshApplicationContext = new GenericApplicationContext();
            ApplicationContextInitializer<GenericApplicationContext> initializer = compiled
                .getInstance(ApplicationContextInitializer.class, className.toString());
            initializer.initialize(freshApplicationContext);
            freshContext.accept(freshApplicationContext);
        });
    }

}
