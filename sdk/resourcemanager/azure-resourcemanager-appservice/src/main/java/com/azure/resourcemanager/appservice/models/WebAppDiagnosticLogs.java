// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.models.WebAppDiagnosticLogs.UpdateStages.Update;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/** A web app diagnostic log configuration in a web app. */
@Fluent
public interface WebAppDiagnosticLogs extends HasInnerModel<SiteLogsConfigInner>, Indexable, HasParent<WebAppBase> {
    /** @return application log level on file system */
    LogLevel applicationLoggingFileSystemLogLevel();

    /** @return Azure Storage Blob container URL for storing application logs */
    String applicationLoggingStorageBlobContainer();

    /** @return application log level on Azure Storage Blob */
    LogLevel applicationLoggingStorageBlobLogLevel();

    /** @return application log retention days on Azure Storage Blob */
    int applicationLoggingStorageBlobRetentionDays();

    /** @return web server quota in MB on file system */
    int webServerLoggingFileSystemQuotaInMB();

    /** @return web server log retention days on file system */
    int webServerLoggingFileSystemRetentionDays();

    /** @return web server log retention days on Azure Storage Blob */
    int webServerLoggingStorageBlobRetentionDays();

    /** @return Azure Storage Blob container URL for storing web server logs */
    String webServerLoggingStorageBlobContainer();

    /** @return if diagnostic information on failed requests should be gathered */
    boolean failedRequestsTracing();

    /** @return if detailed error messages should be gathered */
    boolean detailedErrorMessages();

    /**
     * The entirety of a web app diagnostic log definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithDiagnosticLogging<ParentT>,
            DefinitionStages.WithApplicationLogLevel<ParentT>,
            DefinitionStages.WithStorageLocationForApplication<ParentT>,
            DefinitionStages.WithStorageLocationForWebServer<ParentT>,
            DefinitionStages.WithAttachForWebServerStorage<ParentT>,
            DefinitionStages.WithAttachForWebServerFileSystem<ParentT>,
            DefinitionStages.WithAttachForApplicationStorage<ParentT> {
    }

    /** Grouping of web app diagnostic log definition stages applicable as part of a web app creation. */
    interface DefinitionStages {
        /**
         * The first stage of a web app diagnostic log definition as part of a definition of a web app.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithDiagnosticLogging<ParentT> {
        }

        /**
         * A web app diagnostic log definition allowing the log source to be set.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithDiagnosticLogging<ParentT> {
            /**
             * Enable logging from the web application.
             *
             * @return the next stage of the definition
             */
            WithApplicationLogLevel<ParentT> withApplicationLogging();

            /**
             * Enable logging from the web server.
             *
             * @return the next stage of the definition
             */
            WithStorageLocationForWebServer<ParentT> withWebServerLogging();
        }

        /**
         * A web app diagnostic log definition allowing detailed error messages to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDetailedErrorMessages<ParentT> {
            /**
             * Specifies if detailed error messages should be gathered from the web app.
             *
             * @param enabled true if detailed error messages should be gathered
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDetailedErrorMessages(boolean enabled);
        }

        /**
         * A web app diagnostic log definition allowing failed request tracing to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithFailedRequestTracing<ParentT> {
            /**
             * Specifies if diagnostic information on failed requests should be gathered.
             *
             * @param enabled true if diagnostic information on failed requests should be gathered
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFailedRequestTracing(boolean enabled);
        }

        /**
         * A web app diagnostic log definition allowing application log storage location to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithStorageLocationForApplication<ParentT> {
            /**
             * Specifies the storage location of application logs to be on the file system.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withApplicationLogsStoredOnFileSystem();

            /**
             * Specifies the storage location of application logs to be on in a Storage blob.
             *
             * @param containerSasUrl the URL to the container including the SAS token
             * @return the next stage of the definition
             */
            WithAttachForApplicationStorage<ParentT> withApplicationLogsStoredOnStorageBlob(String containerSasUrl);
        }

        /**
         * A web app diagnostic log definition allowing web server log storage location to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithStorageLocationForWebServer<ParentT> {
            /**
             * Specifies the storage location of web server logs to be on the file system.
             *
             * @return the next stage of the definition
             */
            WithAttachForWebServerFileSystem<ParentT> withWebServerLogsStoredOnFileSystem();

            /**
             * Specifies the storage location of web server logs to be on in a Storage blob.
             *
             * @param containerSasUrl the URL to the container including the SAS token
             * @return the next stage of the definition
             */
            WithAttachForWebServerStorage<ParentT> withWebServerLogsStoredOnStorageBlob(String containerSasUrl);
        }

        /**
         * A web app diagnostic log definition allowing application log level to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithApplicationLogLevel<ParentT> {
            /**
             * Specifies the application log level.
             *
             * @param logLevel the application log level
             * @return the next stage of the definition
             */
            WithStorageLocationForApplication<ParentT> withLogLevel(LogLevel logLevel);
        }

        /**
         * A web app diagnostic log definition allowing web server file system logging quota to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithQuota<ParentT> {
            /**
             * Specifies the maximum size of logs allowed on the file system (in MB).
             *
             * @param quotaInMB the maximum size of logs allowed (in MB). Must be between 25 and 100.
             * @return the next stage of the definition
             */
            WithAttachForWebServerFileSystem<ParentT> withWebServerFileSystemQuotaInMB(int quotaInMB);
        }

        /**
         * A web app diagnostic log definition allowing retention days to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRetentionDays<ParentT> {
            /**
             * Specifies the maximum days of logs to keep. Logs older than this will be deleted.
             *
             * @param retentionDays the maximum days of logs to keep
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLogRetentionDays(int retentionDays);

            /**
             * Specifies the logs will not be deleted beyond a certain time.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withUnlimitedLogRetentionDays();
        }

        /**
         * The final stage of the web app diagnostic log definition, plus extra settings for application logs stored in
         * a Storage blob.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app diagnostic log definition
         * can be attached to the parent web app definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttachForApplicationStorage<ParentT> extends WithAttach<ParentT>, WithRetentionDays<ParentT> {
        }

        /**
         * The final stage of the web app diagnostic log definition, plus extra settings for web server logs stored in a
         * Storage blob.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app diagnostic log definition
         * can be attached to the parent web app definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttachForWebServerStorage<ParentT> extends WithAttach<ParentT>, WithRetentionDays<ParentT> {
        }

        /**
         * The final stage of the web app diagnostic log definition, plus extra settings for web server logs stored in
         * the file system.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app diagnostic log definition
         * can be attached to the parent web app definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttachForWebServerFileSystem<ParentT>
            extends WithAttach<ParentT>, WithQuota<ParentT>, WithRetentionDays<ParentT> {
        }

        /**
         * The final stage of the web app diagnostic log definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app diagnostic log definition
         * can be attached to the parent web app definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
                DefinitionStages.WithDetailedErrorMessages<ParentT>,
                DefinitionStages.WithFailedRequestTracing<ParentT> {
        }
    }

    /**
     * The entirety of a web app diagnostic log definition as part of a web app update.
     *
     * @param <ParentT> the return type of the final {@link Update#parent()} ()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateStages.Blank<ParentT>,
            UpdateStages.Update<ParentT>,
            UpdateStages.WithDiagnosticLogging<ParentT>,
            UpdateStages.WithApplicationLogLevel<ParentT>,
            UpdateStages.WithStorageLocationForApplication<ParentT>,
            UpdateStages.WithStorageLocationForWebServer<ParentT>,
            UpdateStages.WithAttachForWebServerStorage<ParentT>,
            UpdateStages.WithAttachForWebServerFileSystem<ParentT>,
            UpdateStages.WithAttachForApplicationStorage<ParentT> {
    }

    /** Grouping of web app diagnostic log update stages applicable as part of a web app update. */
    interface UpdateStages {
        /**
         * The first stage of a web app diagnostic log update as part of a update of a web app.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithDiagnosticLogging<ParentT> {
        }

        /**
         * A web app diagnostic log update allowing the log source to be set.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithDiagnosticLogging<ParentT> {
            /**
             * Enable logging from the web application.
             *
             * @return the next stage of the update
             */
            WithApplicationLogLevel<ParentT> withApplicationLogging();

            /**
             * Disable logging from the web application.
             *
             * @return the next stage of the update
             */
            Update<ParentT> withoutApplicationLogging();

            /**
             * Enable logging from the web server.
             *
             * @return the next stage of the update
             */
            WithStorageLocationForWebServer<ParentT> withWebServerLogging();

            /**
             * Disable logging from the web server.
             *
             * @return the next stage of the update
             */
            Update<ParentT> withoutWebServerLogging();
        }

        /**
         * A web app diagnostic log update allowing detailed error messages to be specified.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this update
         */
        interface WithDetailedErrorMessages<ParentT> {
            /**
             * Specifies if detailed error messages should be gathered from the web app.
             *
             * @param enabled true if detailed error messages should be gathered
             * @return the next stage of the update
             */
            Update<ParentT> withDetailedErrorMessages(boolean enabled);
        }

        /**
         * A web app diagnostic log update allowing failed request tracing to be specified.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this update
         */
        interface WithFailedRequestTracing<ParentT> {
            /**
             * Specifies if diagnostic information on failed requests should be gathered.
             *
             * @param enabled true if diagnostic information on failed requests should be gathered
             * @return the next stage of the update
             */
            Update<ParentT> withFailedRequestTracing(boolean enabled);
        }

        /**
         * A web app diagnostic log update allowing application log storage location to be specified.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this update
         */
        interface WithStorageLocationForApplication<ParentT> {
            /**
             * Specifies the storage location of application logs to be on the file system.
             *
             * @return the next stage of the update
             */
            Update<ParentT> withApplicationLogsStoredOnFileSystem();

            /**
             * Specifies the storage location of application logs to be on in a Storage blob.
             *
             * @param containerSasUrl the URL to the container including the SAS token
             * @return the next stage of the update
             */
            WithAttachForApplicationStorage<ParentT> withApplicationLogsStoredOnStorageBlob(String containerSasUrl);
        }

        /**
         * A web app diagnostic log update allowing application log storage location to be specified.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this update
         */
        interface WithoutStorageLocationForApplication<ParentT> {
            /**
             * Stops application logs to be stored on the file system.
             *
             * @return the next stage of the update
             */
            Update<ParentT> withoutApplicationLogsStoredOnFileSystem();

            /**
             * Stops application logs to be stored on in a Storage blob.
             *
             * @return the next stage of the update
             */
            Update<ParentT> withoutApplicationLogsStoredOnStorageBlob();
        }

        /**
         * A web app diagnostic log update allowing web server log storage location to be specified.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this update
         */
        interface WithStorageLocationForWebServer<ParentT> {
            /**
             * Specifies the storage location of web server logs to be on the file system.
             *
             * @return the next stage of the definition
             */
            WithAttachForWebServerFileSystem<ParentT> withWebServerLogsStoredOnFileSystem();

            /**
             * Specifies the storage location of web server logs to be on in a Storage blob.
             *
             * @param containerSasUrl the URL to the container including the SAS token
             * @return the next stage of the update
             */
            WithAttachForWebServerStorage<ParentT> withWebServerLogsStoredOnStorageBlob(String containerSasUrl);

            /**
             * Stops web server logs to be stored on the file system.
             *
             * @return the next stage of the update
             */
            Update<ParentT> withoutWebServerLogsStoredOnFileSystem();

            /**
             * Stops web server logs to be stored on in a Storage blob.
             *
             * @return the next stage of the update
             */
            Update<ParentT> withoutWebServerLogsStoredOnStorageBlob();
        }

        /**
         * A web app diagnostic log update allowing application log level to be specified.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this update
         */
        interface WithApplicationLogLevel<ParentT> extends WithoutStorageLocationForApplication<ParentT> {
            /**
             * Specifies the application log level.
             *
             * @param logLevel the application log level
             * @return the next stage of the update
             */
            WithStorageLocationForApplication<ParentT> withLogLevel(LogLevel logLevel);
        }

        /**
         * A web app diagnostic log update allowing web server file system logging quota to be specified.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this update
         */
        interface WithQuota<ParentT> {
            /**
             * Specifies the maximum size of logs allowed on the file system (in MB).
             *
             * @param quotaInMB the maximum size of logs allowed (in MB). Must be between 25 and 100.
             * @return the next stage of the update
             */
            WithAttachForWebServerFileSystem<ParentT> withWebServerFileSystemQuotaInMB(int quotaInMB);
        }

        /**
         * A web app diagnostic log update allowing retention days to be specified.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this update
         */
        interface WithRetentionDays<ParentT> {
            /**
             * Specifies the maximum days of logs to keep. Logs older than this will be deleted.
             *
             * @param retentionDays the maximum days of logs to keep
             * @return the next stage of the update
             */
            Update<ParentT> withLogRetentionDays(int retentionDays);

            /**
             * Specifies the logs will not be deleted beyond a certain time.
             *
             * @return the next stage of the update
             */
            Update<ParentT> withUnlimitedLogRetentionDays();
        }

        /**
         * The final stage of the web app diagnostic log update, plus extra settings for application logs stored in a
         * Storage blob.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app diagnostic log update can
         * be attached to the parent web app update using {@link Update#parent()}.
         *
         * @param <ParentT> the return type of {@link Update#parent()}
         */
        interface WithAttachForApplicationStorage<ParentT> extends Update<ParentT>, WithRetentionDays<ParentT> {
        }

        /**
         * The final stage of the web app diagnostic log update, plus extra settings for web server logs stored in a
         * Storage blob.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app diagnostic log update can
         * be attached to the parent web app update using {@link Update#parent()}.
         *
         * @param <ParentT> the return type of {@link Update#parent()}
         */
        interface WithAttachForWebServerStorage<ParentT> extends Update<ParentT>, WithRetentionDays<ParentT> {
        }

        /**
         * The final stage of the web app diagnostic log update, plus extra settings for web server logs stored in the
         * file system.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app diagnostic log update can
         * be attached to the parent web app update using {@link Update#parent()}.
         *
         * @param <ParentT> the return type of {@link Update#parent()}
         */
        interface WithAttachForWebServerFileSystem<ParentT>
            extends Update<ParentT>, WithQuota<ParentT>, WithRetentionDays<ParentT> {
        }

        /**
         * The final stage of the web app diagnostic log update.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app diagnostic log update can
         * be attached to the parent web app update using {@link Update#parent()}.
         *
         * @param <ParentT> the return type of {@link Update#parent()}
         */
        interface Update<ParentT>
            extends Settable<ParentT>, WithDetailedErrorMessages<ParentT>, WithFailedRequestTracing<ParentT> {
        }
    }
}
