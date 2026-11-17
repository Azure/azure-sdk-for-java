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

import java.io.IOException;
import java.util.List;
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
            .onConstructorInvocation(propertiesType.getDeclaredConstructor())).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(propertiesType.getMethod(methodName, parameterTypes))).accepts(hints);
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
            .onMethodInvocation(AzureKeyVaultSecretProperties.class, "getPropertySources")).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructorInvocation(AzureKeyVaultPropertySourceProperties.class.getDeclaredConstructor()))
            .accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzureKeyVaultPropertySourceProperties.class, "setEndpoint")).accepts(hints);
    }

    @Test
    void registersGlobalAndPasswordlessHintsWithoutKeyVaultClient() throws IOException, NoSuchMethodException {
        try (FilteredClassLoader classLoader = new FilteredClassLoader("com.azure.security.keyvault.secrets")) {
            RuntimeHints hints = loadHints(classLoader);

            assertGlobalBindingHints(hints);
            assertPasswordlessBindingHints(hints);
            assertThat(hints.reflection().getTypeHint(AzureKeyVaultSecretProperties.class)).isNull();
            assertThat(hints.reflection().getTypeHint(AzureKeyVaultPropertySourceProperties.class)).isNull();
            assertThat(hints.reflection().getTypeHint(AzureIdentityEnvVars.class)).isNull();
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
            .onConstructorInvocation(AzureIdentityEnvVars.class.getDeclaredConstructor())).accepts(hints);
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
            .onMethodInvocation(type.getDeclaredMethod("fromJson", JsonReader.class))).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(type.getDeclaredMethod("toJson", JsonWriter.class))).accepts(hints);
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
            .onConstructorInvocation(AzureGlobalProperties.class.getDeclaredConstructor())).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzureGlobalProperties.class, "getCredential")).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(TokenCredentialConfigurationProperties.class, "setClientId")).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzureProfileConfigurationProperties.class, "setTenantId")).accepts(hints);
    }

    private void assertPasswordlessBindingHints(RuntimeHints hints) throws NoSuchMethodException {
        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructorInvocation(AzurePasswordlessProperties.class.getDeclaredConstructor())).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzurePasswordlessProperties.class, "setPasswordlessEnabled")).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzurePasswordlessProperties.class, "setScopes")).accepts(hints);
    }

    private void assertJdbcPasswordlessBindingHints(RuntimeHints hints) throws NoSuchMethodException {
        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructorInvocation(AzureJdbcPasswordlessProperties.class.getDeclaredConstructor())).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzureJdbcPasswordlessProperties.class, "setPasswordlessEnabled")).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzureJdbcPasswordlessProperties.class, "setScopes")).accepts(hints);
    }
}
