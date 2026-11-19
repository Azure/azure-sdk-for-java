// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.identity.AzureIdentityEnvVars;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultPropertySourceProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.implementation.passwordless.properties.AzureJdbcPasswordlessProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.profile.AzureProfileConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import com.azure.storage.queue.QueueMessageEncoding;
import jakarta.jms.Connection;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueSession;
import jakarta.jms.TopicConnection;
import jakarta.jms.TopicSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.SessionProxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SpringCloudAzureAutoconfigureRuntimeHintsTests {

    @Test
    void registersHintsForGlobalPropertyBinding() throws NoSuchMethodException {
        assertGlobalBindingHints(loadHints(getClass().getClassLoader()));
    }

    @Test
    void registersHintsForPasswordlessBinding() throws NoSuchMethodException {
        assertPasswordlessBindingHints(loadHints(getClass().getClassLoader()));
    }

    @Test
    void registersHintsForJdbcPasswordlessBinding() throws NoSuchMethodException {
        assertJdbcPasswordlessBindingHints(loadHints(getClass().getClassLoader()));
    }

    @ParameterizedTest
    @MethodSource("connectionDetailsBindingCases")
    void registersHintsForConnectionDetailsBinding(String sdkPackage, Class<?> propertiesType,
        String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
        RuntimeHints hints = loadHints(getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructor(propertiesType.getDeclaredConstructor()).invoke()).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(propertiesType.getMethod(methodName, parameterTypes)).invoke()).accepts(hints);
    }

    @ParameterizedTest
    @MethodSource("connectionDetailsBindingCases")
    void skipsConnectionDetailsBindingHintsWithoutSdk(String sdkPackage, Class<?> propertiesType,
        String methodName, Class<?>[] parameterTypes) throws IOException, NoSuchMethodException {
        try (FilteredClassLoader classLoader = new FilteredClassLoader(sdkPackage)) {
            RuntimeHints hints = loadHints(classLoader);

            assertThat(hints.reflection().getTypeHint(propertiesType)).isNull();
            assertGlobalBindingHints(hints);
            assertJdbcPasswordlessBindingHints(hints);
        }
    }

    @Test
    void registersHintsForKeyVaultPropertyBinding() throws NoSuchMethodException {
        RuntimeHints hints = loadHints(getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(AzureKeyVaultSecretProperties.class, "getPropertySources").invoke()).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructor(AzureKeyVaultPropertySourceProperties.class.getDeclaredConstructor()).invoke())
            .accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(AzureKeyVaultPropertySourceProperties.class, "setEndpoint").invoke()).accepts(hints);
    }

    @Test
    void registersSharedHintsWithoutKeyVaultClient() throws IOException, NoSuchMethodException {
        try (FilteredClassLoader classLoader = new FilteredClassLoader("com.azure.security.keyvault.secrets")) {
            RuntimeHints hints = loadHints(classLoader);

            assertGlobalBindingHints(hints);
            assertPasswordlessBindingHints(hints);
            assertThat(hints.reflection().getTypeHint(AzureKeyVaultSecretProperties.class)).isNull();
            assertThat(hints.reflection().getTypeHint(AzureKeyVaultPropertySourceProperties.class)).isNull();
            assertThat(RuntimeHintsPredicates.reflection()
                .onConstructor(AzureIdentityEnvVars.class.getDeclaredConstructor()).invoke()).accepts(hints);
            assertThat(hints.reflection().getTypeHint(TypeReference.of(
                "com.azure.security.keyvault.secrets.implementation.models.SecretItem"))).isNull();
            assertThat(hints.reflection().getTypeHint(TypeReference.of(
                "com.azure.security.keyvault.secrets.implementation.models.SecretBundle"))).isNull();
        }
    }

    @Test
    void registersHintsForIdentityEnvironmentVariables() throws NoSuchMethodException {
        RuntimeHints hints = loadHints(getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructor(AzureIdentityEnvVars.class.getDeclaredConstructor()).invoke()).accepts(hints);
    }

    @Test
    void skipsMissingIdentityEnvironmentVariables() throws IOException, NoSuchMethodException {
        try (FilteredClassLoader classLoader = new FilteredClassLoader("com.azure.identity")) {
            RuntimeHints hints = loadHints(classLoader);

            assertThat(hints.reflection().getTypeHint(TypeReference.of("com.azure.identity.AzureIdentityEnvVars")))
                .isNull();
            assertGlobalBindingHints(hints);
        }
    }

    @Test
    void registersHintsForQpidJmsUriConfiguration() throws ClassNotFoundException, NoSuchMethodException {
        RuntimeHints hints = loadHints(getClass().getClassLoader());
        Class<?> factoryType = Class.forName("org.apache.qpid.jms.JmsConnectionFactory");
        Class<?> policyType = Class.forName("org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy");

        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(factoryType.getMethod("getPrefetchPolicy")).invoke()).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(policyType.getMethod("setAll", int.class)).invoke()).accepts(hints);
        assertThat(hints.reflection().getTypeHint(factoryType).getMemberCategories())
            .contains(MemberCategory.INVOKE_PUBLIC_METHODS);
        assertThat(hints.reflection().getTypeHint(policyType).getMemberCategories())
            .contains(MemberCategory.INVOKE_PUBLIC_METHODS);
    }

    @Test
    void registersHintsForStandardTierJmsCustomization() throws ClassNotFoundException, NoSuchFieldException {
        RuntimeHints hints = loadHints(getClass().getClassLoader());
        Class<?> serviceBusFactory = Class.forName("com.azure.servicebus.jms.ServiceBusJmsConnectionFactory");
        Class<?> qpidFactory = Class.forName("org.apache.qpid.jms.JmsConnectionFactory");

        assertThat(RuntimeHintsPredicates.reflection()
            .onField(serviceBusFactory.getDeclaredField("factory"))).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onField(qpidFactory.getDeclaredField("extensionMap"))).accepts(hints);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "provider/amqps", "provider/failover", "transports/ssl", "sasl/PLAIN", "sasl/ANONYMOUS", "tracing/noop"
    })
    void registersHintsForQpidFactoryDiscovery(String factoryPath)
        throws IOException, ClassNotFoundException, NoSuchMethodException {
        String resource = "META-INF/services/org/apache/qpid/jms/" + factoryPath;
        Properties descriptor = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertThat(input).isNotNull();
            descriptor.load(input);
        }
        Class<?> factoryType = Class.forName(descriptor.getProperty("class"));
        RuntimeHints hints = loadHints(getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.resource().forResource(resource)).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructor(factoryType.getDeclaredConstructor()).invoke()).accepts(hints);
        assertThat(hints.reflection().getTypeHint(factoryType).getMemberCategories())
            .contains(MemberCategory.INVOKE_PUBLIC_METHODS);
    }

    @ParameterizedTest
    @MethodSource("jmsCachingProxyInterfaces")
    void registersHintsForJmsCachingProxies(List<Class<?>> interfaces) {
        assertThat(RuntimeHintsPredicates.proxies().forInterfaces(interfaces.toArray(new Class<?>[0])))
            .accepts(loadHints(getClass().getClassLoader()));
    }

    @Test
    void skipsJmsCachingProxiesWithoutSpringJms() throws IOException {
        try (FilteredClassLoader classLoader = new FilteredClassLoader(CachingConnectionFactory.class)) {
            assertThat(loadHints(classLoader).proxies().jdkProxyHints()).isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"com.azure.servicebus.jms", "org.apache.qpid.jms"})
    void skipsQpidHintsWithoutJmsDependencies(String excludedPackage) throws IOException, NoSuchMethodException {
        try (FilteredClassLoader classLoader = new FilteredClassLoader(excludedPackage)) {
            RuntimeHints hints = loadHints(classLoader);

            assertThat(hints.reflection().getTypeHint(TypeReference.of(
                "org.apache.qpid.jms.JmsConnectionFactory"))).isNull();
            assertThat(hints.reflection().getTypeHint(TypeReference.of(
                "org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy"))).isNull();
            assertGlobalBindingHints(hints);
            assertJdbcPasswordlessBindingHints(hints);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "com.azure.security.keyvault.secrets.implementation.models.SecretItem",
        "com.azure.security.keyvault.secrets.implementation.models.SecretBundle"
    })
    void registersHintsForSecretResponseSerialization(String typeName)
        throws ClassNotFoundException, NoSuchMethodException {
        Class<?> type = Class.forName(typeName);
        RuntimeHints hints = loadHints(getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(type.getDeclaredMethod("fromJson", JsonReader.class)).invoke()).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(type.getDeclaredMethod("toJson", JsonWriter.class)).invoke()).accepts(hints);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin",
        "com.azure.identity.extensions.jdbc.mysql.AzureMysqlAuthenticationPlugin",
        "com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider"
    })
    void registersHintsForPasswordlessPlugins(String typeName) {
        RuntimeHints hints = loadHints(getClass().getClassLoader());
        TypeHint typeHint = hints.reflection().getTypeHint(TypeReference.of(typeName));

        assertThat(typeHint).isNotNull();
        assertThat(typeHint.getMemberCategories()).contains(
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS);
    }

    @Test
    void skipsMissingPasswordlessPlugins() throws IOException, NoSuchMethodException {
        try (FilteredClassLoader classLoader = new FilteredClassLoader("com.azure.identity.extensions.jdbc",
            "com.azure.spring.cloud.service.implementation.identity.credential.provider")) {
            RuntimeHints hints = loadHints(classLoader);

            assertGlobalBindingHints(hints);
            assertPasswordlessBindingHints(hints);
            assertThat(hints.reflection().typeHints().filter(typeHint ->
                typeHint.getType().getName().startsWith("com.azure.identity.extensions.jdbc.")
                    || typeHint.getType().getName().startsWith(
                        "com.azure.spring.cloud.service.implementation.identity.credential.provider."))).isEmpty();
        }
    }

    private static Stream<Arguments> connectionDetailsBindingCases() {
        return Stream.of(
            Arguments.of("com.azure.cosmos", AzureCosmosProperties.class,
                "setConsistencyLevel", new Class<?>[] {ConsistencyLevel.class}),
            Arguments.of("com.azure.messaging.eventhubs", AzureEventHubsProperties.class,
                "getConsumer", new Class<?>[0]),
            Arguments.of("com.azure.messaging.servicebus", AzureServiceBusProperties.class,
                "getConsumer", new Class<?>[0]),
            Arguments.of("com.azure.storage.blob", AzureStorageBlobProperties.class,
                "setBlobName", new Class<?>[] {String.class}),
            Arguments.of("com.azure.storage.queue", AzureStorageQueueProperties.class,
                "setMessageEncoding", new Class<?>[] {QueueMessageEncoding.class})
        );
    }

    private static Stream<Arguments> jmsCachingProxyInterfaces() {
        return Stream.of(
            Arguments.of(List.of(Connection.class)),
            Arguments.of(List.of(Connection.class, QueueConnection.class)),
            Arguments.of(List.of(Connection.class, TopicConnection.class)),
            Arguments.of(List.of(Connection.class, QueueConnection.class, TopicConnection.class)),
            Arguments.of(List.of(SessionProxy.class)),
            Arguments.of(List.of(SessionProxy.class, QueueSession.class)),
            Arguments.of(List.of(SessionProxy.class, TopicSession.class)),
            Arguments.of(List.of(SessionProxy.class, QueueSession.class, TopicSession.class)));
    }

    private RuntimeHints loadHints(ClassLoader classLoader) {
        List<RuntimeHintsRegistrar> registrars =
            SpringFactoriesLoader.forResourceLocation("META-INF/spring/aot.factories", classLoader)
                .load(RuntimeHintsRegistrar.class).stream()
                .filter(SpringCloudAzureAutoconfigureRuntimeHints.class::isInstance)
                .toList();
        assertThat(registrars).hasSize(1);
        RuntimeHints hints = new RuntimeHints();
        registrars.get(0).registerHints(hints, classLoader);
        return hints;
    }

    private void assertGlobalBindingHints(RuntimeHints hints) throws NoSuchMethodException {
        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructor(AzureGlobalProperties.class.getDeclaredConstructor()).invoke()).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(AzureGlobalProperties.class, "getCredential").invoke()).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(TokenCredentialConfigurationProperties.class, "setClientId").invoke()).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(AzureProfileConfigurationProperties.class, "setTenantId").invoke()).accepts(hints);
    }

    private void assertPasswordlessBindingHints(RuntimeHints hints) throws NoSuchMethodException {
        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructor(AzurePasswordlessProperties.class.getDeclaredConstructor()).invoke()).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(AzurePasswordlessProperties.class, "setPasswordlessEnabled").invoke()).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(AzurePasswordlessProperties.class, "setScopes").invoke()).accepts(hints);
    }

    private void assertJdbcPasswordlessBindingHints(RuntimeHints hints) throws NoSuchMethodException {
        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructor(AzureJdbcPasswordlessProperties.class.getDeclaredConstructor()).invoke()).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(AzureJdbcPasswordlessProperties.class, "setPasswordlessEnabled").invoke()).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethod(AzureJdbcPasswordlessProperties.class, "setScopes").invoke()).accepts(hints);
    }
}
