// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.sql.fluent.inner.ServerSecurityAlertPolicyInner;
import java.util.List;

/** An immutable client-side representation of an Azure SQL Server Security Alert Policy. */
@Fluent
public interface SqlServerSecurityAlertPolicy
    extends HasId,
        HasInner<ServerSecurityAlertPolicyInner>,
        HasName,
        HasResourceGroup,
        Indexable,
        Refreshable<SqlServerSecurityAlertPolicy>,
        Updatable<SqlServerSecurityAlertPolicy.Update> {

    /** @return name of the SQL Server to which this DNS alias belongs */
    String sqlServerName();

    /** @return the parent SQL server ID */
    String parentId();

    /** @return the state of the policy, whether it is enabled or disabled */
    SecurityAlertPolicyState state();

    /** @return a list of alerts that are disabled */
    List<String> disabledAlerts();

    /** @return a list of e-mail addresses to which the alert is sent */
    List<String> emailAddresses();

    /** @return true if an alert will be sent to the account administrators */
    boolean emailAccountAdmins();

    /**
     * @return the blob storage endpoint (e.g. https://MyAccount.blob.core.windows.net); this blob storage will hold all
     *     Threat Detection audit logs
     */
    String storageEndpoint();

    /** @return the identifier key of the Threat Detection audit storage account */
    String storageAccountAccessKey();

    /** @return the number of days to keep in the Threat Detection audit logs */
    int retentionDays();

    /**
     * The template for a SQL Server Security Alert Policy update operation, containing all the settings that can be
     * modified.
     */
    interface Update
        extends SqlServerSecurityAlertPolicy.UpdateStages.WithState,
            SqlServerSecurityAlertPolicy.UpdateStages.WithEmailAccountAdmins,
            SqlServerSecurityAlertPolicy.UpdateStages.WithStorageAccount,
            SqlServerSecurityAlertPolicy.UpdateStages.WithEmailAddresses,
            SqlServerSecurityAlertPolicy.UpdateStages.WithDisabledAlerts,
            SqlServerSecurityAlertPolicy.UpdateStages.WithRetentionDays,
            Appliable<SqlServerSecurityAlertPolicy> {
    }

    /** Grouping of all the SQL Server Security Alert Policy update stages. */
    interface UpdateStages {
        /** The SQL Server Security Alert Policy update definition to set the state. */
        interface WithState {
            /**
             * Specifies the state of the policy, whether it is enabled or disabled.
             *
             * @param state the state of the policy, whether it is enabled or disabled
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicy.Update withState(SecurityAlertPolicyState state);
        }

        /**
         * The SQL Server Security Alert Policy update definition to set if an alert will be sent to the account
         * administrators.
         */
        interface WithEmailAccountAdmins {
            /**
             * Specifies that an alert will be sent to the account administrators.
             *
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicy.Update withEmailAccountAdmins();

            /**
             * Specifies that an alert will not be sent to the account administrators.
             *
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicy.Update withoutEmailAccountAdmins();
        }

        /**
         * The SQL Server Security Alert Policy update definition to specify the storage account blob endpoint and
         * access key.
         */
        interface WithStorageAccount {
            /**
             * Specifies the storage account blob endpoint and access key.
             *
             * @param storageEndpointUri the blob storage endpoint (e.g. https://MyAccount.blob.core.windows.net); this
             *     blob storage will hold all Threat Detection audit logs
             * @param storageAccessKey the identifier key of the Threat Detection audit storage account
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicy.Update withStorageEndpoint(String storageEndpointUri, String storageAccessKey);
        }

        /**
         * The SQL Server Security Alert Policy update definition to set an array of e-mail addresses to which the alert
         * is sent.
         */
        interface WithEmailAddresses {
            /**
             * Specifies an array of e-mail addresses to which the alert is sent.
             *
             * @param emailAddresses an array of e-mail addresses to which the alert is sent to
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicy.Update withEmailAddresses(String... emailAddresses);
        }

        /** The SQL Server Security Alert Policy update definition to set an array of alerts that are disabled. */
        interface WithDisabledAlerts {
            /**
             * Specifies an array of alerts that are disabled.
             *
             * @param disabledAlerts an array of alerts that are disabled
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicy.Update withDisabledAlerts(String... disabledAlerts);
        }

        /**
         * The SQL Server Security Alert Policy update definition to set the number of days to keep in the Threat
         * Detection audit logs.
         */
        interface WithRetentionDays {
            /**
             * Specifies the number of days to keep in the Threat Detection audit logs.
             *
             * @param days the number of days to keep in the Threat Detection audit logs
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicy.Update withRetentionDays(int days);
        }
    }
}
