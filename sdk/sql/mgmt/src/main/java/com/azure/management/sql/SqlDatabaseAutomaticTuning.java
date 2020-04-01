/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.model.Appliable;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.model.Refreshable;
import com.azure.management.resources.fluentcore.model.Updatable;
import com.azure.management.sql.models.DatabaseAutomaticTuningInner;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure SQL database automatic tuning object.
 */
@Fluent
public interface SqlDatabaseAutomaticTuning extends
    HasInner<DatabaseAutomaticTuningInner>,
    Refreshable<SqlDatabaseAutomaticTuning>,
    Updatable<SqlDatabaseAutomaticTuning.Update> {

    /**
     * @return the database automatic tuning desired state
     */
    AutomaticTuningMode desiredState();

    /**
     * @return the database automatic tuning actual state
     */
    AutomaticTuningMode actualState();

    /**
     * @return the database automatic tuning individual options
     */
    Map<String, AutomaticTuningOptions> tuningOptions();



    /**************************************************************
     * Fluent interfaces to update a SqlDatabaseAutomaticTuning
     **************************************************************/

    /**
     * The template for a SqlDatabaseAutomaticTuning update operation, containing all the settings that can be modified.
     */
    interface Update extends
        SqlDatabaseAutomaticTuning.UpdateStages.WithAutomaticTuningMode,
        SqlDatabaseAutomaticTuning.UpdateStages.WithAutomaticTuningOptions,
        Appliable<SqlDatabaseAutomaticTuning> {
    }

    /**
     * Grouping of all the SqlDatabaseAutomaticTuning update stages.
     */
    interface UpdateStages {
        /**
         * The update stage setting the database automatic tuning desired state.
         */
        interface WithAutomaticTuningMode {
            /**
             * Sets the SQL database automatic tuning desired state.
             *
             * @param desiredState the server automatic tuning desired state
             * @return Next stage of the update.
             */
            Update withAutomaticTuningMode(AutomaticTuningMode desiredState);
        }

        /**
         * The update stage setting the database automatic tuning options.
         */
        interface WithAutomaticTuningOptions {
            /**
             * Sets the various SQL database automatic tuning options desired state.
             *
             * @param tuningOptionName tuning option name (
             *
             * @return Next stage of the update.
             */
            Update withAutomaticTuningOption(String tuningOptionName, AutomaticTuningOptionModeDesired desiredState);

            /**
             * Sets the various SQL database automatic tuning options desired state.
             *
             * @return Next stage of the update.
             */
            Update withAutomaticTuningOptions(Map<String, AutomaticTuningOptionModeDesired> tuningOptions);
        }
    }
}
