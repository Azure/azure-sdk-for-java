// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.sql.fluent.inner.ServerAutomaticTuningInner;
import java.util.Map;

/** An immutable client-side representation of an Azure SQL Server automatic tuning object. */
@Fluent
public interface SqlServerAutomaticTuning
    extends HasInner<ServerAutomaticTuningInner>,
        Refreshable<SqlServerAutomaticTuning>,
        Updatable<SqlServerAutomaticTuning.Update> {

    /** @return the server automatic tuning desired state */
    AutomaticTuningServerMode desiredState();

    /** @return the server automatic tuning actual state */
    AutomaticTuningServerMode actualState();

    /** @return the server automatic tuning individual options */
    Map<String, AutomaticTuningServerOptions> tuningOptions();

    /**************************************************************
     * Fluent interfaces to update a SqlServerAutomaticTuning
     **************************************************************/

    /**
     * The template for a SqlServerAutomaticTuning update operation, containing all the settings that can be modified.
     */
    interface Update
        extends SqlServerAutomaticTuning.UpdateStages.WithAutomaticTuningMode,
            SqlServerAutomaticTuning.UpdateStages.WithAutomaticTuningOptions,
            Appliable<SqlServerAutomaticTuning> {
    }

    /** Grouping of all the SqlServerAutomaticTuning update stages. */
    interface UpdateStages {
        /** The update stage setting the SQL server automatic tuning desired state. */
        interface WithAutomaticTuningMode {
            /**
             * Sets the SQL server automatic tuning desired state.
             *
             * @param desiredState the server automatic tuning desired state
             * @return Next stage of the update.
             */
            Update withAutomaticTuningMode(AutomaticTuningServerMode desiredState);
        }

        /** The update stage setting the server automatic tuning options. */
        interface WithAutomaticTuningOptions {
            /**
             * Sets the various SQL server automatic tuning options desired state.
             *
             * @param tuningOptionName the tuning option name
             * @param desiredState the desired state of tuning option
             * @return Next stage of the update.
             */
            Update withAutomaticTuningOption(String tuningOptionName, AutomaticTuningOptionModeDesired desiredState);

            /**
             * Sets the various SQL server automatic tuning options desired state.
             *
             * @param tuningOptions the tuning option names and desired states
             * @return Next stage of the update.
             */
            Update withAutomaticTuningOptions(Map<String, AutomaticTuningOptionModeDesired> tuningOptions);
        }
    }
}
