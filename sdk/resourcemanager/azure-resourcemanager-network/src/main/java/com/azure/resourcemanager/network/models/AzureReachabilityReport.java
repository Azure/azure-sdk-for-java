// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.AzureReachabilityReportInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.time.OffsetDateTime;
import java.util.List;

/** An immutable client-side representation of Azure reachability report details. */
@Fluent
public interface AzureReachabilityReport
    extends Executable<AzureReachabilityReport>,
        HasInnerModel<AzureReachabilityReportInner>,
        HasParent<NetworkWatcher> {
    /** @return the aggregation level of Azure reachability report. Can be Country, State or City. */
    String aggregationLevel();

    /** @return the providerLocation property */
    AzureReachabilityReportLocation providerLocation();

    /** @return list of Azure reachability report items. */
    List<AzureReachabilityReportItem> reachabilityReport();
    /** @return parameters used to query available internet providers */
    AzureReachabilityReportParameters azureReachabilityReportParameters();

    /** The entirety of Azure reachability report parameters definition. */
    interface Definition
        extends DefinitionStages.WithProviderLocation,
            DefinitionStages.WithStartTime,
            DefinitionStages.WithEndTime,
            DefinitionStages.WithExecute {
    }

    /** Grouping of Azure reachability report definition stages. */
    interface DefinitionStages {
        /** The first stage of Azure reachability report parameters definition. */
        interface WithProviderLocation {
            /**
             * @param country the name of the country
             * @return the AzureReachabilityReport object itself
             */
            WithStartTime withProviderLocation(String country);
            /**
             * @param country the name of the country
             * @param state the name of the state
             * @return the AzureReachabilityReport object itself
             */
            WithStartTime withProviderLocation(String country, String state);
            /**
             * @param country the name of the country
             * @param state the name of the state
             * @param city the name of the city
             * @return the AzureReachabilityReport object itself
             */
            WithStartTime withProviderLocation(String country, String state, String city);
        }

        /** Sets the start time for the Azure reachability report. */
        interface WithStartTime {
            /**
             * @param startTime the start time for the Azure reachability report
             * @return the next stage of the definition
             */
            WithEndTime withStartTime(OffsetDateTime startTime);
        }

        /** Sets the end time for the Azure reachability report. */
        interface WithEndTime {
            /**
             * @param endTime the start time for the Azure reachability report
             * @return the next stage of the definition
             */
            WithExecute withEndTime(OffsetDateTime endTime);
        }

        /**
         * Sets Azure regions to scope the query to. Note: if none or multiple Azure regions specified, only one
         * provider should be set. If none or multiple providers specified, only one Azure region should be set.
         */
        interface WithAzureLocations {
            WithExecute withAzureLocations(String... azureLocations);
        }

        /**
         * Sets the list of Internet service providers. Note: if none or multiple Azure regions specified, only one
         * provider should be set. If none or multiple providers specified, only one Azure region should be set.
         */
        interface WithProviders {
            /**
             * @param providers the list of Internet service providers
             * @return the next stage of the definition
             */
            WithExecute withProviders(String... providers);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for execution, but also allows for
         * any other optional settings to be specified.
         */
        interface WithExecute
            extends Executable<AzureReachabilityReport>,
                DefinitionStages.WithAzureLocations,
                DefinitionStages.WithProviders {
        }
    }
}
