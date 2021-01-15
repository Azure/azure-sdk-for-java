// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.models.ApplicationLogsConfig;
import com.azure.resourcemanager.appservice.models.AzureBlobStorageApplicationLogsConfig;
import com.azure.resourcemanager.appservice.models.AzureBlobStorageHttpLogsConfig;
import com.azure.resourcemanager.appservice.models.EnabledConfig;
import com.azure.resourcemanager.appservice.models.FileSystemApplicationLogsConfig;
import com.azure.resourcemanager.appservice.models.FileSystemHttpLogsConfig;
import com.azure.resourcemanager.appservice.models.HttpLogsConfig;
import com.azure.resourcemanager.appservice.models.LogLevel;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.appservice.models.WebAppDiagnosticLogs;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

/**
 * Implementation for WebAppDiagnosticLogs and its create and update interfaces.
 *
 * @param <FluentT> the fluent interface of the parent web app
 * @param <FluentImplT> the fluent implementation of the parent web app
 */
class WebAppDiagnosticLogsImpl<FluentT extends WebAppBase, FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends IndexableWrapperImpl<SiteLogsConfigInner>
    implements WebAppDiagnosticLogs,
        WebAppDiagnosticLogs.Definition<WebAppBase.DefinitionStages.WithCreate<FluentT>>,
        WebAppDiagnosticLogs.UpdateDefinition<WebAppBase.Update<FluentT>> {

    private final WebAppBaseImpl<FluentT, FluentImplT> parent;

    private LogLevel applicationLogLevel = null;

    WebAppDiagnosticLogsImpl(SiteLogsConfigInner inner, WebAppBaseImpl<FluentT, FluentImplT> parent) {
        super(inner);
        if (inner.applicationLogs() != null) {
            inner.applicationLogs().withAzureTableStorage(null);
        }
        this.parent = parent;
    }

    @Override
    public LogLevel applicationLoggingFileSystemLogLevel() {
        if (innerModel().applicationLogs() == null
            || innerModel().applicationLogs().fileSystem() == null
            || innerModel().applicationLogs().fileSystem().level() == null) {
            return LogLevel.OFF;
        } else {
            return innerModel().applicationLogs().fileSystem().level();
        }
    }

    @Override
    public String applicationLoggingStorageBlobContainer() {
        if (innerModel().applicationLogs() == null || innerModel().applicationLogs().azureBlobStorage() == null) {
            return null;
        } else {
            return innerModel().applicationLogs().azureBlobStorage().sasUrl();
        }
    }

    @Override
    public LogLevel applicationLoggingStorageBlobLogLevel() {
        if (innerModel().applicationLogs() == null
            || innerModel().applicationLogs().azureBlobStorage() == null
            || innerModel().applicationLogs().azureBlobStorage().level() == null) {
            return LogLevel.OFF;
        } else {
            return innerModel().applicationLogs().azureBlobStorage().level();
        }
    }

    @Override
    public int applicationLoggingStorageBlobRetentionDays() {
        if (innerModel().applicationLogs() == null || innerModel().applicationLogs().azureBlobStorage() == null) {
            return 0;
        } else {
            return ResourceManagerUtils.toPrimitiveInt(
                innerModel().applicationLogs().azureBlobStorage().retentionInDays());
        }
    }

    @Override
    public int webServerLoggingFileSystemQuotaInMB() {
        if (innerModel().httpLogs() == null || innerModel().httpLogs().fileSystem() == null) {
            return 0;
        } else {
            return ResourceManagerUtils.toPrimitiveInt(innerModel().httpLogs().fileSystem().retentionInMb());
        }
    }

    @Override
    public int webServerLoggingFileSystemRetentionDays() {
        if (innerModel().httpLogs() == null || innerModel().httpLogs().fileSystem() == null) {
            return 0;
        } else {
            return ResourceManagerUtils.toPrimitiveInt(innerModel().httpLogs().fileSystem().retentionInDays());
        }
    }

    @Override
    public int webServerLoggingStorageBlobRetentionDays() {
        if (innerModel().httpLogs() == null || innerModel().httpLogs().azureBlobStorage() == null) {
            return 0;
        } else {
            return ResourceManagerUtils.toPrimitiveInt(innerModel().httpLogs().azureBlobStorage().retentionInDays());
        }
    }

    @Override
    public String webServerLoggingStorageBlobContainer() {
        if (innerModel().httpLogs() == null || innerModel().httpLogs().azureBlobStorage() == null) {
            return null;
        } else {
            return innerModel().httpLogs().azureBlobStorage().sasUrl();
        }
    }

    @Override
    public boolean failedRequestsTracing() {
        return innerModel().failedRequestsTracing() != null
            && ResourceManagerUtils.toPrimitiveBoolean(innerModel().failedRequestsTracing().enabled());
    }

    @Override
    public boolean detailedErrorMessages() {
        return innerModel().detailedErrorMessages() != null
            && ResourceManagerUtils.toPrimitiveBoolean(innerModel().detailedErrorMessages().enabled());
    }

    @Override
    public FluentImplT attach() {
        parent.withDiagnosticLogs(this);
        return parent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT parent() {
        parent.withDiagnosticLogs(this);
        return (FluentImplT) this.parent;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withApplicationLogging() {
        innerModel().withApplicationLogs(new ApplicationLogsConfig());
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withoutApplicationLogging() {
        withoutApplicationLogsStoredOnFileSystem();
        withoutApplicationLogsStoredOnStorageBlob();
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withWebServerLogging() {
        innerModel().withHttpLogs(new HttpLogsConfig());
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withoutWebServerLogging() {
        withoutWebServerLogsStoredOnFileSystem();
        withoutWebServerLogsStoredOnStorageBlob();
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withDetailedErrorMessages(boolean enabled) {
        innerModel().withDetailedErrorMessages(new EnabledConfig().withEnabled(enabled));
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withFailedRequestTracing(boolean enabled) {
        innerModel().withFailedRequestsTracing(new EnabledConfig().withEnabled(enabled));
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withApplicationLogsStoredOnFileSystem() {
        if (innerModel().applicationLogs() != null) {
            innerModel()
                .applicationLogs()
                .withFileSystem(new FileSystemApplicationLogsConfig().withLevel(applicationLogLevel));
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withApplicationLogsStoredOnStorageBlob(
        String containerSasUrl) {
        if (innerModel().applicationLogs() != null) {
            innerModel()
                .applicationLogs()
                .withAzureBlobStorage(
                    new AzureBlobStorageApplicationLogsConfig()
                        .withLevel(applicationLogLevel)
                        .withSasUrl(containerSasUrl));
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withWebServerLogsStoredOnFileSystem() {
        if (innerModel().httpLogs() != null) {
            innerModel().httpLogs().withFileSystem(new FileSystemHttpLogsConfig().withEnabled(true));
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withWebServerLogsStoredOnStorageBlob(String containerSasUrl) {
        if (innerModel().httpLogs() != null) {
            innerModel()
                .httpLogs()
                .withAzureBlobStorage(
                    new AzureBlobStorageHttpLogsConfig().withEnabled(true).withSasUrl(containerSasUrl));
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withoutWebServerLogsStoredOnFileSystem() {
        if (innerModel().httpLogs() != null && innerModel().httpLogs().fileSystem() != null) {
            innerModel().httpLogs().fileSystem().withEnabled(false);
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withoutWebServerLogsStoredOnStorageBlob() {
        if (innerModel().httpLogs() != null && innerModel().httpLogs().azureBlobStorage() != null) {
            innerModel().httpLogs().azureBlobStorage().withEnabled(false);
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withLogLevel(LogLevel logLevel) {
        this.applicationLogLevel = logLevel;
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withWebServerFileSystemQuotaInMB(int quotaInMB) {
        if (innerModel().httpLogs() != null
            && innerModel().httpLogs().fileSystem() != null
            && innerModel().httpLogs().fileSystem().enabled()) {
            innerModel().httpLogs().fileSystem().withRetentionInMb(quotaInMB);
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withLogRetentionDays(int retentionDays) {
        if (innerModel().httpLogs() != null
            && innerModel().httpLogs().fileSystem() != null
            && innerModel().httpLogs().fileSystem().enabled()) {
            innerModel().httpLogs().fileSystem().withRetentionInDays(retentionDays);
        }
        if (innerModel().httpLogs() != null
            && innerModel().httpLogs().azureBlobStorage() != null
            && innerModel().httpLogs().azureBlobStorage().enabled()) {
            innerModel().httpLogs().azureBlobStorage().withRetentionInDays(retentionDays);
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withUnlimitedLogRetentionDays() {
        if (innerModel().httpLogs() != null
            && innerModel().httpLogs().fileSystem() != null
            && innerModel().httpLogs().fileSystem().enabled()) {
            innerModel().httpLogs().fileSystem().withRetentionInDays(0);
        }
        if (innerModel().httpLogs() != null
            && innerModel().httpLogs().azureBlobStorage() != null
            && innerModel().httpLogs().fileSystem().enabled()) {
            innerModel().httpLogs().azureBlobStorage().withRetentionInDays(0);
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withoutApplicationLogsStoredOnFileSystem() {
        if (innerModel().applicationLogs() != null && innerModel().applicationLogs().fileSystem() != null) {
            innerModel().applicationLogs().fileSystem().withLevel(LogLevel.OFF);
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withoutApplicationLogsStoredOnStorageBlob() {
        if (innerModel().applicationLogs() != null && innerModel().applicationLogs().azureBlobStorage() != null) {
            innerModel().applicationLogs().azureBlobStorage().withLevel(LogLevel.OFF);
        }
        return this;
    }
}
