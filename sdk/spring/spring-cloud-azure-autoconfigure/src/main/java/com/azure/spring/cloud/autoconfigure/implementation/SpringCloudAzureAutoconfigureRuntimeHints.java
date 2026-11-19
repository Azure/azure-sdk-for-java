// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.implementation.passwordless.properties.AzureJdbcPasswordlessProperties;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.boot.context.properties.bind.BindableRuntimeHintsRegistrar;
import org.springframework.util.ClassUtils;

import java.util.Arrays;

class SpringCloudAzureAutoconfigureRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    @SuppressWarnings("removal")
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Environment and JDBC post-processors bind these types outside bean registration.
        BindableRuntimeHintsRegistrar.forTypes(AzureGlobalProperties.class, AzurePasswordlessProperties.class,
            AzureJdbcPasswordlessProperties.class)
            .registerHints(hints);
        hints.reflection().registerTypeIfPresent(classLoader,
            "com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin",
            MemberCategory.DECLARED_CLASSES,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS);
        hints.reflection().registerTypeIfPresent(classLoader,
            "com.azure.identity.extensions.jdbc.mysql.AzureMysqlAuthenticationPlugin",
            MemberCategory.DECLARED_CLASSES,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS);
        hints.reflection().registerTypeIfPresent(classLoader,
            "com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider",
            MemberCategory.DECLARED_CLASSES,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS);

        hints.reflection().registerTypeIfPresent(classLoader, "com.azure.identity.AzureIdentityEnvVars",
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // ConnectionDetails factories bind properties without @ConfigurationProperties.
        if (ClassUtils.isPresent("com.azure.cosmos.CosmosClientBuilder", classLoader)) {
            BindableRuntimeHintsRegistrar.forTypes(AzureCosmosProperties.class).registerHints(hints);
        }
        // The Event Hubs binding graph includes its Blob checkpoint-store properties.
        if (ClassUtils.isPresent("com.azure.messaging.eventhubs.EventHubClientBuilder", classLoader)
            && ClassUtils.isPresent("com.azure.storage.blob.BlobServiceVersion", classLoader)) {
            BindableRuntimeHintsRegistrar.forTypes(AzureEventHubsProperties.class).registerHints(hints);
        }
        if (ClassUtils.isPresent("com.azure.messaging.servicebus.ServiceBusClientBuilder", classLoader)) {
            BindableRuntimeHintsRegistrar.forTypes(AzureServiceBusProperties.class).registerHints(hints);
        }
        if (ClassUtils.isPresent("com.azure.storage.blob.BlobServiceClientBuilder", classLoader)) {
            BindableRuntimeHintsRegistrar.forTypes(AzureStorageBlobProperties.class).registerHints(hints);
        }
        if (ClassUtils.isPresent("com.azure.storage.queue.QueueServiceClientBuilder", classLoader)) {
            BindableRuntimeHintsRegistrar.forTypes(AzureStorageQueueProperties.class).registerHints(hints);
        }

        if (ClassUtils.isPresent("com.azure.servicebus.jms.ServiceBusJmsConnectionFactory", classLoader)) {
            // Qpid applies URI options through JavaBeans introspection and reflective setter calls.
            hints.reflection().registerTypeIfPresent(classLoader, "org.apache.qpid.jms.JmsConnectionFactory",
                typeHint -> typeHint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS).withField("extensionMap"));
            hints.reflection().registerTypeIfPresent(classLoader,
                "org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy", MemberCategory.INVOKE_PUBLIC_METHODS);
            // The Standard-tier customizer accesses the wrapped factory and its AMQP extensions.
            hints.reflection().registerTypeIfPresent(classLoader,
                "com.azure.servicebus.jms.ServiceBusJmsConnectionFactory", typeHint -> typeHint.withField("factory"));
            // These are Qpid FactoryFinder descriptors, not standard Java ServiceLoader files.
            registerQpidFactoryHints(hints, classLoader, "provider/amqps", "provider.amqp.AmqpProviderFactory");
            registerQpidFactoryHints(hints, classLoader, "provider/failover", "provider.failover.FailoverProviderFactory");
            registerQpidFactoryHints(hints, classLoader, "transports/ssl", "transports.netty.NettySslTransportFactory");
            registerQpidFactoryHints(hints, classLoader, "sasl/PLAIN", "sasl.PlainMechanismFactory");
            registerQpidFactoryHints(hints, classLoader, "sasl/ANONYMOUS", "sasl.AnonymousMechanismFactory");
            registerQpidFactoryHints(hints, classLoader, "tracing/noop", "tracing.JmsNoOpTracerFactory");
            if (ClassUtils.isPresent("org.springframework.jms.connection.CachingConnectionFactory", classLoader)) {
                // Spring builds these interface lists from the concrete connection and session capabilities.
                registerJmsProxyHints(hints, "jakarta.jms.Connection");
                registerJmsProxyHints(hints, "jakarta.jms.Connection", "jakarta.jms.QueueConnection");
                registerJmsProxyHints(hints, "jakarta.jms.Connection", "jakarta.jms.TopicConnection");
                registerJmsProxyHints(hints, "jakarta.jms.Connection", "jakarta.jms.QueueConnection",
                    "jakarta.jms.TopicConnection");
                registerJmsProxyHints(hints, "org.springframework.jms.connection.SessionProxy");
                registerJmsProxyHints(hints, "org.springframework.jms.connection.SessionProxy", "jakarta.jms.QueueSession");
                registerJmsProxyHints(hints, "org.springframework.jms.connection.SessionProxy", "jakarta.jms.TopicSession");
                registerJmsProxyHints(hints, "org.springframework.jms.connection.SessionProxy", "jakarta.jms.QueueSession",
                    "jakarta.jms.TopicSession");
            }
        }

        if (ClassUtils.isPresent("com.azure.security.keyvault.secrets.SecretClient", classLoader)) {
            // The environment post-processor binds this type directly, outside bean registration.
            BindableRuntimeHintsRegistrar.forTypes(AzureKeyVaultSecretProperties.class).registerHints(hints);
            // Azure Core detects and invokes these models' JSON streaming methods reflectively.
            hints.reflection().registerTypeIfPresent(classLoader,
                "com.azure.security.keyvault.secrets.implementation.models.SecretItem",
                MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerTypeIfPresent(classLoader,
                "com.azure.security.keyvault.secrets.implementation.models.SecretBundle",
                MemberCategory.INVOKE_PUBLIC_METHODS);
        }
    }

    private void registerQpidFactoryHints(RuntimeHints hints, ClassLoader classLoader, String resource, String type) {
        hints.resources().registerPattern("META-INF/services/org/apache/qpid/jms/" + resource);
        hints.reflection().registerTypeIfPresent(classLoader, "org.apache.qpid.jms." + type,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
    }

    private void registerJmsProxyHints(RuntimeHints hints, String... interfaces) {
        hints.proxies().registerJdkProxy(Arrays.stream(interfaces).map(TypeReference::of).toArray(TypeReference[]::new));
    }
}
