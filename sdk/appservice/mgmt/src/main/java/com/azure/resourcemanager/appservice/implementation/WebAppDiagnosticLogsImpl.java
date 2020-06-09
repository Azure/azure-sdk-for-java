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
import com.azure.resourcemanager.appservice.fluent.inner.SiteLogsConfigInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;

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
        if (inner().applicationLogs() == null
            || inner().applicationLogs().fileSystem() == null
            || inner().applicationLogs().fileSystem().level() == null) {
            return LogLevel.OFF;
        } else {
            return inner().applicationLogs().fileSystem().level();
        }
    }

    @Override
    public String applicationLoggingStorageBlobContainer() {
        if (inner().applicationLogs() == null || inner().applicationLogs().azureBlobStorage() == null) {
            return null;
        } else {
            return inner().applicationLogs().azureBlobStorage().sasUrl();
        }
    }

    @Override
    public LogLevel applicationLoggingStorageBlobLogLevel() {
        if (inner().applicationLogs() == null
            || inner().applicationLogs().azureBlobStorage() == null
            || inner().applicationLogs().azureBlobStorage().level() == null) {
            return LogLevel.OFF;
        } else {
            return inner().applicationLogs().azureBlobStorage().level();
        }
    }

    @Override
    public int applicationLoggingStorageBlobRetentionDays() {
        if (inner().applicationLogs() == null || inner().applicationLogs().azureBlobStorage() == null) {
            return 0;
        } else {
            return Utils.toPrimitiveInt(inner().applicationLogs().azureBlobStorage().retentionInDays());
        }
    }

    @Override
    public int webServerLoggingFileSystemQuotaInMB() {
        if (inner().httpLogs() == null || inner().httpLogs().fileSystem() == null) {
            return 0;
        } else {
            return Utils.toPrimitiveInt(inner().httpLogs().fileSystem().retentionInMb());
        }
    }

    @Override
    public int webServerLoggingFileSystemRetentionDays() {
        if (inner().httpLogs() == null || inner().httpLogs().fileSystem() == null) {
            return 0;
        } else {
            return Utils.toPrimitiveInt(inner().httpLogs().fileSystem().retentionInDays());
        }
    }

    @Override
    public int webServerLoggingStorageBlobRetentionDays() {
        if (inner().httpLogs() == null || inner().httpLogs().azureBlobStorage() == null) {
            return 0;
        } else {
            return Utils.toPrimitiveInt(inner().httpLogs().azureBlobStorage().retentionInDays());
        }
    }

    @Override
    public String webServerLoggingStorageBlobContainer() {
        if (inner().httpLogs() == null || inner().httpLogs().azureBlobStorage() == null) {
            return null;
        } else {
            return inner().httpLogs().azureBlobStorage().sasUrl();
        }
    }

    @Override
    public boolean failedRequestsTracing() {
        return inner().failedRequestsTracing() != null
            && Utils.toPrimitiveBoolean(inner().failedRequestsTracing().enabled());
    }

    @Override
    public boolean detailedErrorMessages() {
        return inner().detailedErrorMessages() != null
            && Utils.toPrimitiveBoolean(inner().detailedErrorMessages().enabled());
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
        inner().withApplicationLogs(new ApplicationLogsConfig());
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
        inner().withHttpLogs(new HttpLogsConfig());
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
        inner().withDetailedErrorMessages(new EnabledConfig().withEnabled(enabled));
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withFailedRequestTracing(boolean enabled) {
        inner().withFailedRequestsTracing(new EnabledConfig().withEnabled(enabled));
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withApplicationLogsStoredOnFileSystem() {
        if (inner().applicationLogs() != null) {
            inner()
                .applicationLogs()
                .withFileSystem(new FileSystemApplicationLogsConfig().withLevel(applicationLogLevel));
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withApplicationLogsStoredOnStorageBlob(
        String containerSasUrl) {
        if (inner().applicationLogs() != null) {
            inner()
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
        if (inner().httpLogs() != null) {
            inner().httpLogs().withFileSystem(new FileSystemHttpLogsConfig().withEnabled(true));
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withWebServerLogsStoredOnStorageBlob(String containerSasUrl) {
        if (inner().httpLogs() != null) {
            inner()
                .httpLogs()
                .withAzureBlobStorage(
                    new AzureBlobStorageHttpLogsConfig().withEnabled(true).withSasUrl(containerSasUrl));
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withoutWebServerLogsStoredOnFileSystem() {
        if (inner().httpLogs() != null && inner().httpLogs().fileSystem() != null) {
            inner().httpLogs().fileSystem().withEnabled(false);
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withoutWebServerLogsStoredOnStorageBlob() {
        if (inner().httpLogs() != null && inner().httpLogs().azureBlobStorage() != null) {
            inner().httpLogs().azureBlobStorage().withEnabled(false);
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
        if (inner().httpLogs() != null
            && inner().httpLogs().fileSystem() != null
            && inner().httpLogs().fileSystem().enabled()) {
            inner().httpLogs().fileSystem().withRetentionInMb(quotaInMB);
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withLogRetentionDays(int retentionDays) {
        if (inner().httpLogs() != null
            && inner().httpLogs().fileSystem() != null
            && inner().httpLogs().fileSystem().enabled()) {
            inner().httpLogs().fileSystem().withRetentionInDays(retentionDays);
        }
        if (inner().httpLogs() != null
            && inner().httpLogs().azureBlobStorage() != null
            && inner().httpLogs().azureBlobStorage().enabled()) {
            inner().httpLogs().azureBlobStorage().withRetentionInDays(retentionDays);
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withUnlimitedLogRetentionDays() {
        if (inner().httpLogs() != null
            && inner().httpLogs().fileSystem() != null
            && inner().httpLogs().fileSystem().enabled()) {
            inner().httpLogs().fileSystem().withRetentionInDays(0);
        }
        if (inner().httpLogs() != null
            && inner().httpLogs().azureBlobStorage() != null
            && inner().httpLogs().fileSystem().enabled()) {
            inner().httpLogs().azureBlobStorage().withRetentionInDays(0);
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withoutApplicationLogsStoredOnFileSystem() {
        if (inner().applicationLogs() != null && inner().applicationLogs().fileSystem() != null) {
            inner().applicationLogs().fileSystem().withLevel(LogLevel.OFF);
        }
        return this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> withoutApplicationLogsStoredOnStorageBlob() {
        if (inner().applicationLogs() != null && inner().applicationLogs().azureBlobStorage() != null) {
            inner().applicationLogs().azureBlobStorage().withLevel(LogLevel.OFF);
        }
        return this;
    }
}
