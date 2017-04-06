/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.trafficmanager.implementation.EndpointInner;

/**
 * An immutable client-side representation of an Azure traffic manager profile endpoint.
 */
@Fluent
public interface TrafficManagerEndpoint extends
        ExternalChildResource<TrafficManagerEndpoint, TrafficManagerProfile>,
        HasInner<EndpointInner> {
    /**
     * @return the endpoint type
     */
    EndpointType endpointType();

    /**
     * @return the monitor status of the endpoint
     */
    EndpointMonitorStatus monitorStatus();

    /**
     * @return true if the endpoint is enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * @return the weight of the endpoint which is used when traffic manager profile is configured with
     * Weighted traffic-routing method
     */
    long routingWeight();

    /**
     * @return the priority of the endpoint which is used when traffic manager profile is configured with
     * Priority traffic-routing method
     */
    long routingPriority();

    /**
     * The entirety of a traffic manager profile endpoint definition as a part of parent definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
            DefinitionStages.AzureTargetEndpointBlank<ParentT>,
            DefinitionStages.ExternalTargetEndpointBlank<ParentT>,
            DefinitionStages.NestedProfileTargetEndpointBlank<ParentT>,
            DefinitionStages.WithAzureResource<ParentT>,
            DefinitionStages.WithFqdn<ParentT>,
            DefinitionStages.WithSourceTrafficRegion<ParentT>,
            DefinitionStages.WithSourceTrafficRegionThenThreshold<ParentT>,
            DefinitionStages.WithEndpointThreshold<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of traffic manager profile endpoint definition stages as a part of parent traffic manager
     * profile definition.
     */
    interface DefinitionStages {
        /**
         * The first stage of a traffic manager profile Azure endpoint definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface AzureTargetEndpointBlank<ParentT> extends WithAzureResource<ParentT> {
        }

        /**
         * The first stage of a traffic manager profile external endpoint definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface ExternalTargetEndpointBlank<ParentT> extends WithFqdn<ParentT> {
        }

        /**
         * The first stage of a traffic manager profile nested profile endpoint definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface NestedProfileTargetEndpointBlank<ParentT> extends WithNestedProfile<ParentT> {
        }

        /**
         * The stage of the traffic manager profile Azure endpoint definition allowing to specify the ID
         * of the target Azure resource.
         *
         * @param <ParentT> the return type of {@link UpdateDefinitionStages.WithAttach#attach()}
         */
        interface WithAzureResource<ParentT> {
            /**
             * Specifies the resource ID of an Azure resource.
             * <p>
             * supported Azure resources are cloud service, web app or public ip
             *
             * @param resourceId the Azure resource id
             * @return the next stage of the definition
             */
            WithAttach<ParentT> toResourceId(String resourceId);
        }

        /**
         * The stage of the traffic manager profile external endpoint definition allowing to specify
         * the FQDN.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithFqdn<ParentT> {
            /**
             * Specifies the FQDN of an external endpoint.
             *
             * @param externalFqdn the external FQDN
             * @return the next stage of the definition
             */
            WithSourceTrafficRegion<ParentT> toFqdn(String externalFqdn);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the location of the external
         * endpoint.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSourceTrafficRegion<ParentT> {
            /**
             * Specifies the location of the endpoint that will be used when the parent profile is configured with
             * Performance routing method {@link TrafficRoutingMethod#PERFORMANCE}.
             *
             * @param region the location
             * @return the next stage of the definition
             */
            WithAttach<ParentT> fromRegion(Region region);
        }

        /**
         * The stage of the traffic manager nested profile endpoint definition allowing to specify the profile.
         *
         * @param <ParentT> the return type of {@link UpdateDefinitionStages.WithAttach#attach()}
         */
        interface WithNestedProfile<ParentT> {
            /**
             * Specifies a nested traffic manager profile for the endpoint.
             *
             * @param profile the nested traffic manager profile
             * @return the next stage of the definition
             */
            WithSourceTrafficRegionThenThreshold<ParentT> toProfile(TrafficManagerProfile profile);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the location of the nested
         * profile endpoint.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSourceTrafficRegionThenThreshold<ParentT> {
            /**
             * Specifies the location of the endpoint that will be used when the parent profile is configured with
             * Performance routing method {@link TrafficRoutingMethod#PERFORMANCE}.
             *
             * @param region the location
             * @return the next stage of the definition
             */
            WithEndpointThreshold<ParentT> fromRegion(Region region);
        }

        /**
         * The stage of the nested traffic manager profile endpoint definition allowing to specify the minimum
         * endpoints to be online in the nested profile to consider it as not degraded.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithEndpointThreshold<ParentT>  extends WithAttach<ParentT> {
            /**
             * Specifies the minimum number of endpoints to be online for the nested profile to be considered healthy.
             *
             * @param count the number of endpoints
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMinimumEndpointsToEnableTraffic(int count);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the endpoint weight.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithRoutingWeight<ParentT> {
            /**
             * Specifies the weight for the endpoint that will be used when the parent profile is configured with
             * Weighted routing method {@link TrafficRoutingMethod#WEIGHTED}.
             *
             * @param weight the endpoint weight
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRoutingWeight(int weight);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the endpoint priority.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithRoutingPriority<ParentT> {
            /**
             * Specifies the priority for the endpoint that will be used when the parent profile is configured with
             * Priority routing method {@link TrafficRoutingMethod#PRIORITY}.
             *
             * @param priority the endpoint priority
             * @return the next stage of the definition
             */
            WithAttach<ParentT>  withRoutingPriority(int priority);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to disable the endpoint.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithTrafficDisabled<ParentT> {
            /**
             * Specifies that this endpoint should be excluded from receiving traffic.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTrafficDisabled();
        }

        /** The final stage of the traffic manager profile endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the traffic manager profile endpoint
         * definition can be attached to the parent traffic manager profile definition using {@link TrafficManagerEndpoint.DefinitionStages.WithAttach#attach()}.
         * @param <ParentT> the return type of {@link TrafficManagerEndpoint.DefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT>,
                DefinitionStages.WithRoutingWeight<ParentT>,
                DefinitionStages.WithRoutingPriority<ParentT>,
                DefinitionStages.WithTrafficDisabled<ParentT> {
        }
    }

    /**
     * The entirety of a traffic manager profile endpoint definition as a part of parent update.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.AzureTargetEndpointBlank<ParentT>,
            UpdateDefinitionStages.ExternalTargetEndpointBlank<ParentT>,
            UpdateDefinitionStages.NestedProfileTargetEndpointBlank<ParentT>,
            UpdateDefinitionStages.WithAzureResource<ParentT>,
            UpdateDefinitionStages.WithFqdn<ParentT>,
            UpdateDefinitionStages.WithSourceTrafficRegion<ParentT>,
            UpdateDefinitionStages.WithSourceTrafficRegionThenThreshold<ParentT>,
            UpdateDefinitionStages.WithEndpointThreshold<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of traffic manager profile endpoint definition stages as a part of parent traffic manager
     * profile update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a traffic manager profile Azure endpoint definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface AzureTargetEndpointBlank<ParentT> extends WithAzureResource<ParentT> {
        }

        /**
         * The first stage of a traffic manager profile external endpoint definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface ExternalTargetEndpointBlank<ParentT> extends WithFqdn<ParentT> {
        }

        /**
         * The first stage of a traffic manager profile nested profile endpoint definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface NestedProfileTargetEndpointBlank<ParentT> extends WithNestedProfile<ParentT> {
        }

        /**
         * The stage of the traffic manager profile Azure endpoint definition allowing to specify the ID
         * of the target Azure resource.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAzureResource<ParentT> {
            /**
             * Specifies the resource ID of an Azure resource.
             * <p>
             * supported Azure resources are cloud service, web app or public ip
             *
             * @param resourceId the Azure resource id
             * @return the next stage of the definition
             */
            WithAttach<ParentT> toResourceId(String resourceId);
        }

        /**
         * The stage of the traffic manager profile external endpoint definition allowing to specify
         * the FQDN.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithFqdn<ParentT> {
            /**
             * Specifies the FQDN of an external endpoint.
             *
             * @param externalFqdn the external FQDN
             * @return the next stage of the definition
             */
            WithSourceTrafficRegion<ParentT> toFqdn(String externalFqdn);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the location of the external
         * endpoint.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSourceTrafficRegion<ParentT> {
            /**
             * Specifies the location of the endpoint that will be used when the parent profile is configured with
             * Performance routing method {@link TrafficRoutingMethod#PERFORMANCE}.
             *
             * @param region the location
             * @return the next stage of the definition
             */
            WithAttach<ParentT> fromRegion(Region region);
        }

        /**
         * The stage of the traffic manager nested profile endpoint definition allowing to specify the profile.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithNestedProfile<ParentT> {
            /**
             * Specifies a nested traffic manager profile for the endpoint.
             *
             * @param profile the nested traffic manager profile
             * @return the next stage of the definition
             */
            WithSourceTrafficRegionThenThreshold<ParentT> toProfile(TrafficManagerProfile profile);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the location of the nested
         * endpoint.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSourceTrafficRegionThenThreshold<ParentT> {
            /**
             * Specifies the location of the endpoint that will be used when the parent profile is configured with
             * Performance routing method {@link TrafficRoutingMethod#PERFORMANCE}.
             *
             * @param region the location
             * @return the next stage of the definition
             */
            WithEndpointThreshold<ParentT> fromRegion(Region region);
        }

        /**
         * The stage of the nested traffic manager profile endpoint definition allowing to specify the minimum
         * endpoints to be online in the nested profile to consider it as not degraded.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithEndpointThreshold<ParentT>  extends WithAttach<ParentT> {
            /**
             * Specifies the minimum number of endpoints to be online for the nested profile to be considered healthy.
             *
             * @param count the number of endpoints
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMinimumEndpointsToEnableTraffic(int count);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the endpoint weight.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithRoutingWeight<ParentT> {
            /**
             * Specifies the weight for the endpoint that will be used when the weight-based routing method
             * {@link TrafficRoutingMethod#WEIGHTED} is enabled on the profile.
             *
             * @param weight the endpoint weight
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRoutingWeight(int weight);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the endpoint priority.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithRoutingPriority<ParentT> {
            /**
             * Specifies the weight for the endpoint that will be used when priority-based routing method
             * is {@link TrafficRoutingMethod#PRIORITY} enabled on the profile.
             *
             * @param priority priority of this endpoint. Possible values are from 1 to 1000, lower
             *                 values represent higher priority.
             * @return the next stage of the definition
             */
            WithAttach<ParentT>  withRoutingPriority(int priority);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to disable the endpoint.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithTrafficDisabled<ParentT> {
            /**
             * Specifies that this endpoint should be excluded from receiving traffic.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTrafficDisabled();
        }

        /** The final stage of the traffic manager profile endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the traffic manager profile endpoint
         * definition can be attached to the parent traffic manager profile update using {@link TrafficManagerEndpoint.DefinitionStages.WithAttach#attach()}.
         * @param <ParentT> the return type of {@link TrafficManagerEndpoint.DefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InUpdate<ParentT>,
                UpdateDefinitionStages.WithRoutingWeight<ParentT>,
                UpdateDefinitionStages.WithRoutingPriority<ParentT>,
                UpdateDefinitionStages.WithTrafficDisabled<ParentT> {
        }
    }

    /**
     * The entirety of an Azure endpoint update as a part of parent traffic manager profile profile update.
     */
    interface UpdateAzureEndpoint extends
            UpdateStages.WithAzureResource,
            Update {
    }

    /**
     * The entirety of an external endpoint update as a part of parent traffic manager profile profile update.
     */
    interface UpdateExternalEndpoint extends
            UpdateStages.WithFqdn,
            UpdateStages.WithSourceTrafficRegion,
            Update {
    }

    /**
     * The entirety of a nested profile endpoint update as a part of parent traffic manager profile profile update.
     */
    interface UpdateNestedProfileEndpoint extends
            UpdateStages.WithNestedProfileConfig,
            UpdateStages.WithSourceTrafficRegion,
            Update {
    }

    /**
     * the set of configurations that can be updated for all endpoint irrespective of their type (Azure, external, nested profile).
     */
    interface Update extends
            Settable<TrafficManagerProfile.Update>,
            UpdateStages.WithRoutingWeight,
            UpdateStages.WithRoutingPriority,
            UpdateStages.WithTrafficDisabledOrEnabled {
    }

    /**
     * Grouping of traffic manager profile endpoint update stages.
     */
    interface UpdateStages {
        /**
         * The stage of an Azure endpoint update allowing to specify the target Azure resource.
         */
        interface WithAzureResource {
            /**
             * Specifies the resource ID of an Azure resource.
             * <p>
             * supported Azure resources are cloud service, web app or public ip
             *
             * @param resourceId the Azure resource id
             * @return the next stage of the update
             */
            Update toResourceId(String resourceId);
        }

        /**
         * The stage of an external endpoint update allowing to specify the FQDN.
         */
        interface WithFqdn {
            /**
             * Specifies the FQDN of an external endpoint that is not hosted in Azure.
             *
             * @param externalFqdn the external FQDN
             * @return the next stage of the endpoint update
             */
            UpdateExternalEndpoint toFqdn(String externalFqdn);
        }

        /**
         * The stage of an nested profile endpoint update allowing to specify profile and
         * minimum child endpoint.
         */
        interface WithNestedProfileConfig {
            /**
             * Specifies a nested traffic manager profile for the endpoint.
             *
             * @param nestedProfile the nested traffic manager profile
             * @return the next stage of the update
             */
            UpdateNestedProfileEndpoint toProfile(TrafficManagerProfile nestedProfile);

            /**
             * Specifies the minimum number of endpoints to be online for the nested profile to be considered healthy.
             *
             * @param count  number of endpoints
             * @return the next stage of the endpoint update
             */
            UpdateNestedProfileEndpoint withMinimumEndpointsToEnableTraffic(int count);
        }

        /**
         * The stage of the traffic manager endpoint update allowing to specify the location of the external
         * or nested profile endpoints.
         */
        interface WithSourceTrafficRegion {
            /**
             * Specifies the region of the endpoint that will be used when the performance-based routing method
             * {@link TrafficRoutingMethod#PERFORMANCE} is enabled on the profile.
             *
             * @param location the location
             * @return the next stage of the endpoint update
             */
            Update fromRegion(Region location);
        }

        /**
         * The stage of the traffic manager profile endpoint update allowing to specify the endpoint weight.
         */
        interface WithRoutingWeight {
            /**
             * Specifies the weight for the endpoint that will be used when the weight-based routing method
             * {@link TrafficRoutingMethod#WEIGHTED} is enabled on the profile.
             *
             * @param weight the endpoint weight
             * @return the next stage of the update
             */
            Update withRoutingWeight(int weight);
        }

        /**
         * The stage of the traffic manager profile endpoint update allowing to specify the endpoint priority.
         */
        interface WithRoutingPriority {
            /**
             * Specifies the weight for the endpoint that will be used when priority-based routing method
             * is {@link TrafficRoutingMethod#PRIORITY} enabled on the profile.
             *
             * @param priority the endpoint priority
             * @return the next stage of the update
             */
            Update  withRoutingPriority(int priority);
        }

        /**
         * The stage of the traffic manager profile endpoint update allowing to enable or disable it.
         */
        interface WithTrafficDisabledOrEnabled {
            /**
             * Specifies that the endpoint should be excluded from receiving traffic.
             *
             * @return the next stage of the update
             */
            Update withTrafficDisabled();

            /**
             * Specifies that the endpoint should receive the traffic.
             *
             * @return the next stage of the update
             */
            Update withTrafficEnabled();
        }
    }
}
