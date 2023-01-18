// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.implementation.cloud.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.azure.spring.cloud.core.implementation.util.RuntimeHintsUtils.findTypes;

class AzureSdkRuntimeHints implements RuntimeHintsRegistrar {

    private static final Log LOGGER = LogFactory.getLog(AzureSdkRuntimeHints.class);

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        registerReflectionByPackage(hints, classLoader,
            List.of("com.azure.data.appconfiguration.models",
                "com.azure.core.models",
                "com.azure.core.http",
                "com.azure.core.amqp.models",
                "com.azure.cosmos.models",
                "com.azure.cosmos.implementation.directconnectivity",
                "com.azure.messaging.eventhubs.models",
                "com.azure.messaging.servicebus.models",
                "com.azure.security.keyvault.certificates.models",
                "com.azure.security.keyvault.secrets.models",
                "com.azure.storage.blob.implementation.models",
                "com.azure.storage.blob.options",
                "com.azure.storage.blob.models",
                "com.azure.storage.file.share.implementation.models",
                "com.azure.storage.file.share.models",
                "com.azure.storage.file.share.options",
                "com.azure.storage.queue.implementation.models",
                "com.azure.storage.queue.models"),
            MemberCategory.DECLARED_CLASSES,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS
        );

        List.of(
            "com.azure.core.http.rest.ResponseBase",
            "com.azure.core.http.rest.StreamResponse",
            "com.azure.core.util.DateTimeRfc1123",
            "com.azure.core.util.ExpandableStringEnum",
            "com.azure.cosmos.implementation.routing.PartitionKeyInternal$PartitionKeyInternalJsonSerializer",
            "com.azure.cosmos.implementation.ClientSideRequestStatistics$ClientSideRequestStatisticsSerializer",
            "com.azure.data.appconfiguration.implementation.ConfigurationSettingPage",
            "com.azure.identity.implementation.IntelliJAuthMethodDetails",
            "com.azure.security.keyvault.secrets.implementation.SecretPropertiesPage",
            "com.azure.security.keyvault.secrets.implementation.SecretRequestAttributes",
            "com.azure.security.keyvault.secrets.implementation.SecretRequestParameters"
        ).forEach(c -> hints.reflection().registerTypeIfPresent(classLoader, c,
            MemberCategory.DECLARED_CLASSES,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS));

        hints.reflection().registerTypeIfPresent(classLoader, "com.azure.cosmos.implementation.DatabaseAccount",
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerTypeIfPresent(classLoader, "com.azure.cosmos.implementation.DocumentCollection",
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // Register resources
        hints.resources().registerPattern("azure-core.properties");
        hints.resources().registerPattern("azure-cosmos.properties");
        hints.resources().registerPattern("azure-data-appconfiguration.properties");
        hints.resources().registerPattern("azure-key-vault-secrets.properties");
        hints.resources().registerPattern("azure-messaging-eventhubs-checkpointstore-blob.properties");
        hints.resources().registerPattern("azure-messaging-eventhubs.properties");
        hints.resources().registerPattern("azure-messaging-servicebus.properties");
        hints.resources().registerPattern("azure-spring-identifier.properties");
        hints.resources().registerPattern("azure-storage-blob.properties");
        hints.resources().registerPattern("azure-storage-file-share.properties");
        hints.resources().registerPattern("azure-storage-queue.properties");
        hints.resources().registerPattern("eventhubs-checkpointstore-blob-messages.properties");
        hints.resources().registerPattern("kvErrorStrings.properties");

        // Register proxy
        List.of(
            "com.azure.data.appconfiguration.implementation.ConfigurationClientImpl$ConfigurationService",
            "com.azure.security.keyvault.secrets.implementation.SecretClientImpl$SecretService",
            "com.azure.storage.blob.implementation.AppendBlobsImpl$AppendBlobsService",
            "com.azure.storage.blob.implementation.BlobsImpl$BlobsService",
            "com.azure.storage.blob.implementation.BlockBlobsImpl$BlockBlobsService",
            "com.azure.storage.blob.implementation.ContainersImpl$ContainersService",
            "com.azure.storage.blob.implementation.PageBlobsImpl$PageBlobsService",
            "com.azure.storage.blob.implementation.ServicesImpl$ServicesService",
            "com.azure.storage.file.share.implementation.DirectoriesImpl$DirectoriesService",
            "com.azure.storage.file.share.implementation.FilesImpl$FilesService",
            "com.azure.storage.file.share.implementation.ServicesImpl$ServicesService",
            "com.azure.storage.file.share.implementation.SharesImpl$SharesService",
            "com.azure.storage.queue.implementation.MessageIdsImpl$MessageIdsService",
            "com.azure.storage.queue.implementation.MessagesImpl$MessagesService",
            "com.azure.storage.queue.implementation.QueuesImpl$QueuesService",
            "com.azure.storage.queue.implementation.ServicesImpl$ServicesService"
        ).forEach(s -> hints.proxies().registerJdkProxy(TypeReference.of(s)));
    }

    private void registerReflectionByPackage(RuntimeHints hints, ClassLoader classLoader, List<String> basePackages,
                                             MemberCategory... memberCategories) {
        @SuppressWarnings("rawtypes")
        List<Class> classes = new ArrayList<>();
        for (String p : basePackages) {
            try {
                classes.addAll(findTypes(p, classLoader));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        classes.forEach(c -> hints.reflection().registerTypeIfPresent(classLoader, c.getName(), memberCategories));
    }

}
