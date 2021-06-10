// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableRow;

import java.time.OffsetDateTime;

/**
 * Sample to demonstrate using a custom model to read the results of a logs query.
 */
public class LogsQueryWithModels {

    /**
     * The main method to run the sample.
     * @param args ignored args
     */
    public static void main(String[] args) {
        ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
                .clientId(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_ID"))
                .clientSecret(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_SECRET"))
                .tenantId(Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID"))
                .build();

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(tokenCredential)
                .buildClient();

        LogsQueryResult queryResults = logsQueryClient
                .queryLogs("{workspace-id}", "AppRequests", null);

        // Sample to use a model type to read the results
        for (LogsTable table : queryResults.getLogsTables()) {
            for (LogsTableRow row : table.getTableRows()) {
                CustomModel model = row.getRowAsObject(CustomModel.class);
                System.out.println("Time generated " + model.getTimeGenerated() + "; success = " + model.getSuccess()
                        + "; operation name = " + model.getOperationName());
            }
        }
    }

    /**
     * A custom model to read the logs query result.
     */
    private static class CustomModel {
        private OffsetDateTime timeGenerated;
        private String tenantId;
        private String id;
        private String source;
        private Boolean success;
        private Double durationMs;
        private Object properties;
        private String operationName;
        private String operationId;


        /**
         * Returns the time the log event was generated.
         * @return the time the log event was generated.
         */
        public OffsetDateTime getTimeGenerated() {
            return timeGenerated;
        }

        /**
         * Returns the tenant id of the resource for which this log was recorded.
         * @return the tenant id of the resource for which this log was recorded.
         */
        public String getTenantId() {
            return tenantId;
        }

        /**
         * Returns the unique identifier of this log.
         * @return the unique identifier of this log.
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the source of this log.
         * @return the source of this log.
         */
        public String getSource() {
            return source;
        }

        /**
         * Returns {@code true} if the logged request returned a successful response.
         * @return {@code true} if the logged request returned a successful response.
         */
        public Boolean getSuccess() {
            return success;
        }

        /**
         * Returns the time duration the service took to process the request.
         * @return the time duration the service took to process the request.
         */
        public Double getDurationMs() {
            return durationMs;
        }

        /**
         * Returns additional properties of the request.
         * @return additional properties of the request.
         */
        public Object getProperties() {
            return properties;
        }

        /**
         * Returns the name of the operation.
         * @return the name of the operation.
         */
        public String getOperationName() {
            return operationName;
        }
    }

}
