// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.sql.fluent.models.DatabaseSecurityAlertPolicyInner;

import java.util.List;

/** A representation of the Azure SQL Database threat detection policy. */
@Fluent
public interface SqlDatabaseThreatDetectionPolicy
    extends ExternalChildResource<SqlDatabaseThreatDetectionPolicy, SqlDatabase>, HasParent<SqlDatabase>,
    HasInnerModel<DatabaseSecurityAlertPolicyInner>, HasResourceGroup, Refreshable<SqlDatabaseThreatDetectionPolicy>,
    Updatable<SqlDatabaseThreatDetectionPolicy.Update> {

    /**
     * Gets the geo-location where the resource lives.
     *
     * @return the geo-location where the resource lives
     */
    Region region();

    /**
     * Gets the resource kind.
     *
     * @return the resource kind
     */
    String kind();

    /**
     * Gets the state of the policy.
     *
     * @return the state of the policy.
     */
    SecurityAlertPolicyState currentState();

    /**
     * Gets the semicolon-separated list of alerts that are disabled.
     *
     * @return the semicolon-separated list of alerts that are disabled
     * @deprecated use {@link SqlDatabaseThreatDetectionPolicy#disabledAlertList()}
     */
    @Deprecated
    String disabledAlerts();

    /**
     * Gets a list of disabled alerts.
     *
     * @return a list of disabled alerts
     */
    List<String> disabledAlertList();

    /**
     * Gets the semicolon-separated list of e-mail addresses to which the alert is sent.
     *
     * @return the semicolon-separated list of e-mail addresses to which the alert is sent
     * @deprecated use {@link SqlDatabaseThreatDetectionPolicy#emailAddressList()}
     */
    @Deprecated
    String emailAddresses();

    /**
     * Gets a list of e-mail addresses to which the alert is sent.
     *
     * @return a list of e-mail addresses to which the alert is sent
     */
    List<String> emailAddressList();

    /**
     * Checks whether the alert is sent to the account administrators.
     *
     * @return true if the alert is sent to the account administrators
     */
    boolean emailAccountAdmins();

    /**
     * Gets the blob storage endpoint.
     *
     * @return the blob storage endpoint
     */
    String storageEndpoint();

    /**
     * Gets the identifier key of the Threat Detection audit storage account.
     *
     * @return the identifier key of the Threat Detection audit storage account
     */
    String storageAccountAccessKey();

    /**
     * Gets the number of days to keep in the Threat Detection audit logs.
     *
     * @return the number of days to keep in the Threat Detection audit logs
     */
    int retentionDays();

    /**
     * Checks whether using default server policy.
     *
     * @return true if using default server policy
     */
    boolean isDefaultSecurityAlertPolicy();

    /** Container interface for SQL database threat detection policy operations. */
    interface SqlDatabaseThreatDetectionPolicyOperations
        extends SupportsCreating<SqlDatabaseThreatDetectionPolicy.DefinitionStages.Blank> {
        /**
         * Begins a definition for a security alert policy.
         *
         * @param policyName the name of the security alert policy
         * @return the first stage of the SqlDatabaseThreatDetectionPolicy definition
         */
        SqlDatabaseThreatDetectionPolicy.DefinitionStages.Blank defineThreatDetectionPolicy(String policyName);

        /**
         * Gets a SQL database threat detection policy.
         *
         * @return the SQL database threat detection policy for the current database
         */
        SqlDatabaseThreatDetectionPolicy getThreatDetectionPolicy();
    }

    /** Container interface for all the definitions that need to be implemented. */
    interface SqlDatabaseThreatDetectionPolicyDefinition
        extends SqlDatabaseThreatDetectionPolicy.DefinitionStages.Blank,
        SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithSecurityAlertPolicyState,
        SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithStorageEndpoint,
        SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithStorageAccountAccessKey,
        SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithAlertsFilter,
        SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithEmailAddresses,
        SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithRetentionDays,
        SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithEmailToAccountAdmins,
        SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate {
    }

    /** Grouping of all the SQL database threat detection policy definition stages. */
    interface DefinitionStages {
        /** The first stage of the SQL database threat detection policy definition. */
        interface Blank extends SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithSecurityAlertPolicyState {
        }

        /** The SQL database threat detection policy definition to set the state. */
        interface WithSecurityAlertPolicyState {
            /**
             * Sets the security alert policy state to "Enabled".
             *
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithStorageEndpoint withPolicyEnabled();

            /**
             * Sets the security alert policy state to "Disabled".
             *
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate withPolicyDisabled();

            /**
             * Sets the security alert policy state to "New".
             *
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate withDefaultSecurityAlertPolicy();
        }

        /** The SQL database threat detection policy definition to set the storage endpoint. */
        interface WithStorageEndpoint {
            /**
             * Sets the security alert policy storage endpoint.
             *
             * @param storageEndpoint the blob storage endpoint (e.g. https://MyAccount.blob.core.windows.net); this
             *     blob storage will hold all Threat Detection audit logs.
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithStorageAccountAccessKey
                withStorageEndpoint(String storageEndpoint);
        }

        /** The SQL database threat detection policy definition to set the storage access key. */
        interface WithStorageAccountAccessKey {
            /**
             * Sets the security alert policy storage access key.
             *
             * @param storageAccountAccessKey the storage access key
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate
                withStorageAccountAccessKey(String storageAccountAccessKey);
        }

        /**
         * The SQL database threat detection policy definition to set the security alert policy alerts to be disabled.
         */
        interface WithAlertsFilter {
            /**
             * Sets the security alert policy alerts to be disabled.
             *
             * @param alertsFilter the semicolon-separated list of alerts that are disabled, or empty string to disable
             *     no alerts. Possible values: Sql_Injection; Sql_Injection_Vulnerability; Access_Anomaly;
             *     Usage_Anomaly.
             * @return the next stage of the definition
             * @deprecated use {@link SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate#withAlertsFilter(List)}
             */
            @Deprecated
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate withAlertsFilter(String alertsFilter);

            /**
             * Sets the security alert policy alerts to be disabled.
             *
             * @param alertsFilter the list of alerts that are disabled, or empty string to disable
             *     no alerts. Possible values: Sql_Injection; Sql_Injection_Vulnerability; Access_Anomaly;
             *     Usage_Anomaly.
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate withAlertsFilter(List<String> alertsFilter);
        }

        /** The SQL database threat detection policy definition to set the security alert policy email addresses. */
        interface WithEmailAddresses {
            /**
             * Sets the security alert policy email addresses.
             *
             * @param addresses the semicolon-separated list of e-mail addresses to which the alert is sent to
             * @return the next stage of the definition
             * @deprecated use {@link WithEmailAddresses#withEmailAddresses(List)}
             */
            @Deprecated
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate withEmailAddresses(String addresses);

            /**
             * Sets the security alert policy email addresses.
             *
             * @param addresses the list of e-mail addresses to which the alert is sent to
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate withEmailAddresses(List<String> addresses);
        }

        /**
         * The SQL database threat detection policy definition to set the number of days to keep in the Threat Detection
         * audit logs.
         */
        interface WithRetentionDays {
            /**
             * Sets the security alert policy email addresses.
             *
             * <p>Specifies the number of days to keep in the Threat Detection audit logs.
             *
             * @param retentionDays the number of days to keep in the Threat Detection audit logs
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate withRetentionDays(int retentionDays);
        }

        /**
         * The SQL database threat detection policy definition to set that the alert is sent to the account
         * administrators.
         */
        interface WithEmailToAccountAdmins {
            /**
             * Enables the alert to be sent to the account administrators.
             *
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate withEmailToAccountAdmins();

            /**
             * Disables the alert will be sent to the account administrators.
             *
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithCreate withoutEmailToAccountAdmins();
        }

        /** The final stage of the SQL database threat detection policy definition. */
        interface WithCreate extends SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithStorageEndpoint,
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithStorageAccountAccessKey,
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithAlertsFilter,
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithEmailAddresses,
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithRetentionDays,
            SqlDatabaseThreatDetectionPolicy.DefinitionStages.WithEmailToAccountAdmins,
            Creatable<SqlDatabaseThreatDetectionPolicy> {
        }
    }

    /**
     * The template for a SQL database threat detection policy update operation, containing all the settings that can be
     * modified.
     */
    interface Update extends UpdateStages.WithSecurityAlertPolicyState, UpdateStages.WithStorageEndpoint,
        UpdateStages.WithStorageAccountAccessKey, UpdateStages.WithAlertsFilter, UpdateStages.WithEmailAddresses,
        UpdateStages.WithRetentionDays, UpdateStages.WithEmailToAccountAdmins,
        Appliable<SqlDatabaseThreatDetectionPolicy> {
    }

    /** Grouping of all the SQL database threat detection policy update stages. */
    interface UpdateStages {
        /** The SQL database threat detection policy update definition to set the state. */
        interface WithSecurityAlertPolicyState {
            /**
             * Updates the security alert policy state to "Enabled".
             *
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.Update withPolicyEnabled();

            /**
             * Update the security alert policy state to "Disabled".
             *
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.Update withPolicyDisabled();

            /**
             * Updates the security alert policy state to "New".
             *
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.Update withDefaultSecurityAlertPolicy();
        }

        /** The SQL database threat detection policy update definition to set the storage endpoint. */
        interface WithStorageEndpoint {
            /**
             * Updates the security alert policy storage endpoint.
             *
             * @param storageEndpoint the blob storage endpoint (e.g. https://MyAccount.blob.core.windows.net); this
             *     blob storage will hold all Threat Detection audit logs.
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.Update withStorageEndpoint(String storageEndpoint);
        }

        /** The SQL database threat detection policy update definition to set the storage access key. */
        interface WithStorageAccountAccessKey {
            /**
             * Updates the security alert policy storage access key.
             *
             * @param storageAccountAccessKey the storage access key
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.Update withStorageAccountAccessKey(String storageAccountAccessKey);
        }

        /**
         * The SQL database threat detection policy update definition to set the security alert policy alerts to be
         * disabled.
         */
        interface WithAlertsFilter {
            /**
             * Updates the security alert policy alerts to be disabled.
             *
             * @param alertsFilter the semicolon-separated list of alerts that are disabled, or empty string to disable
             *     no alerts. Possible values: Sql_Injection; Sql_Injection_Vulnerability; Access_Anomaly;
             *     Usage_Anomaly.
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.Update withAlertsFilter(String alertsFilter);
        }

        /**
         * The SQL database threat detection policy update definition to set the security alert policy email addresses.
         */
        interface WithEmailAddresses {
            /**
             * Updates the security alert policy email addresses.
             *
             * @param addresses the semicolon-separated list of e-mail addresses to which the alert is sent to
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.Update withEmailAddresses(String addresses);
        }

        /**
         * The SQL database threat detection policy update definition to set the number of days to keep in the Threat
         * Detection audit logs.
         */
        interface WithRetentionDays {
            /**
             * Updates the security alert policy email addresses.
             *
             * <p>Specifies the number of days to keep in the Threat Detection audit logs.
             *
             * @param retentionDays the number of days to keep in the Threat Detection audit logs
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.Update withRetentionDays(int retentionDays);
        }

        /**
         * The SQL database threat detection policy update definition to set that the alert is sent to the account
         * administrators.
         */
        interface WithEmailToAccountAdmins {
            /**
             * Enables the alert to be sent to the account administrators.
             *
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.Update withEmailToAccountAdmins();

            /**
             * Disables the alert will be sent to the account administrators.
             *
             * @return the next stage of the definition
             */
            SqlDatabaseThreatDetectionPolicy.Update withoutEmailToAccountAdmins();
        }
    }
}
