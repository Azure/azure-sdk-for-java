// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.inner.AvailableProvidersListInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.Map;

/** An immutable client-side representation of available Internet service providers. */
@Fluent
public interface AvailableProviders
    extends Executable<AvailableProviders>, HasInner<AvailableProvidersListInner>, HasParent<NetworkWatcher> {
    /** @return parameters used to query available internet providers */
    AvailableProvidersListParameters availableProvidersParameters();

    /** @return read-only map of available internet providers, indexed by country */
    Map<String, AvailableProvidersListCountry> providersByCountry();

    /** The entirety of available providers parameters definition. */
    interface Definition
        extends DefinitionStages.WithExecuteAndCountry,
            DefinitionStages.WithExecuteAndState,
            DefinitionStages.WithExecuteAndCity {
    }

    /** Grouping of available providers parameters definition stages. */
    interface DefinitionStages {
        /** The first stage of available providers parameters definition. */
        interface WithAzureLocations {
            /**
             * Set the list of Azure regions. Note: this will overwrite locations if already set.
             *
             * @param azureLocations locations list
             * @return the AvailableProviders object itself.
             */
            WithExecute withAzureLocations(String... azureLocations);

            /**
             * Sets Azure region name. Note: this method has additive effect.
             *
             * @param azureLocation region name
             * @return the AvailableProviders object itself.
             */
            WithExecuteAndCountry withAzureLocation(String azureLocation);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for execution, but also allows for
         * any other optional settings to be specified.
         */
        interface WithExecute extends Executable<AvailableProviders>, DefinitionStages.WithAzureLocations {
        }

        /** The stage of the definition which allows to specify country or execute the query. */
        interface WithExecuteAndCountry extends WithExecute {
            /**
             * @param country the country for available providers list
             * @return the next stage of the definition
             */
            WithExecuteAndState withCountry(String country);
        }

        /** The stage of the definition which allows to specify state or execute the query. */
        interface WithExecuteAndState extends WithExecute {
            /**
             * @param state the state for available providers list
             * @return the next stage of the definition
             */
            WithExecuteAndCity withState(String state);
        }

        /** The stage of the definition which allows to specify city or execute the query. */
        interface WithExecuteAndCity extends WithExecute {
            /**
             * @param city the city or town for available providers list
             * @return the next stage of the definition
             */
            WithExecute withCity(String city);
        }
    }
}
