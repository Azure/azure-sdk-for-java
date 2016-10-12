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
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.trafficmanager.implementation.EndpointInner;

/**
 * An immutable client-side representation of an Azure traffic manager profile endpoint.
 */
@Fluent
public interface TrafficManagerEndpoint extends
        ExternalChildResource<TrafficManagerEndpoint, TrafficManagerProfile>,
        Wrapper<EndpointInner> {
    /**
     * @return the endpoint type
     */
    EndpointType endpointType();

    /**
     * @return the monitor status of the endpoint
     */
    EndpointMonitorStatus monitorStatus();

    /**
     * @return true if the endpoint is disabled, false otherwise
     */
    boolean isDisabled();

    /**
     * @return the weight of the endpoint which is used when traffic manager profile is configured with
     * Weighted traffic-routing method
     */
    int routingWeight();

    /**
     * @return the priority of the endpoint which is used when traffic manager profile is configured with
     * Priority traffic-routing method
     */
    int routingPriority();

    /**
     * The entirety of a traffic manager profile endpoint definition as a part of parent definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithEndpointType<ParentT>,
            DefinitionStages.WithEndpointThreshold<ParentT>,
            DefinitionStages.WithSourceTrafficLocation<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of traffic manager profile endpoint definition stages as a part of parent traffic manager
     * profile definition.
     */
    interface DefinitionStages {
        /**
         * The first stage of a traffic manager profile endpoint definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithEndpointType<ParentT> {
        }

        /**
         * The stage of the traffic manager profile endpoint definition allowing to specify the type.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithEndpointType<ParentT> {
            /**
             * Specify that the endpoint is an Azure cloud service, web app or public ip.
             *
             * @param resourceId the Azure resource id
             * @return the next stage of the endpoint definition
             */
            WithAttach<ParentT> withTargetAzureResourceId(String resourceId);

            /**
             * Specify that the endpoint is an external endpoint that is not hosted in Azure.
             *
             * @param externalFqdn the external FQDN
             * @return the next stage of the endpoint definition
             */
            WithSourceTrafficLocation<ParentT> withExternalFqdn(String externalFqdn);

            /**
             * Specify that the endpoint is a nested traffic manager profile.
             *
             * @param nestedProfile the nested traffic manager profile
             * @return the next stage of the endpoint definition
             */
            WithEndpointThreshold<ParentT> withNestedProfile(TrafficManagerProfile nestedProfile);
        }

        /**
         * The stage of the nested traffic manager profile endpoint definition allowing to specify the minimum
         * endpoints to be online in the nested profile to consider it as not degraded.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithEndpointThreshold<ParentT> {
            /**
             * Specifies the child endpoint threshold.
             *
             * @param count the number of child endpoints to be online to consider nested profile as healthy
             * @return the next stage of the endpoint definition
             */
            WithSourceTrafficLocation<ParentT> withMinimumChildEndpoints(int count);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the location of the external
         * or nested profile endpoints.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSourceTrafficLocation<ParentT> {
            /**
             * Specifies the location of the endpoint that will be used when the parent profile is configured with
             * Performance routing method {@link TrafficRoutingMethod#PERFORMANCE}.
             *
             * @param location the location
             * @return the next stage of the endpoint definition
             */
            WithAttach<ParentT> withSourceTrafficLocation(Region location);
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
             * @return the next stage of the endpoint definition
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
             * @return the next stage of the endpoint definition
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
             * Specify that this endpoint should be excluded from receiving traffic.
             *
             * @return the next stage of the endpoint definition
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
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithEndpointType<ParentT>,
            UpdateDefinitionStages.WithEndpointThreshold<ParentT>,
            UpdateDefinitionStages.WithSourceTrafficLocation<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of traffic manager profile endpoint definition stages as a part of parent traffic manager
     * profile update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a traffic manager profile endpoint definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithEndpointType<ParentT> {
        }

        /**
         * The stage of the traffic manager profile endpoint definition allowing to specify the type.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithEndpointType<ParentT> {
            /**
             * Specify that the endpoint is an Azure cloud service, web app or public ip.
             *
             * @param resourceId the Azure resource id
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTargetAzureResourceId(String resourceId);

            /**
             * Specify that the endpoint is an external endpoint that is not hosted in Azure.
             *
             * @param externalFqdn the external FQDN
             * @return the next stage of the definition
             */
            WithSourceTrafficLocation<ParentT> withExternalFqdn(String externalFqdn);

            /**
             * Specify that the endpoint is a nested traffic manager profile.
             *
             * @param nestedProfile the nested traffic manager profile
             * @return the next stage of the definition
             */
            WithEndpointThreshold<ParentT> withNestedProfile(TrafficManagerProfile nestedProfile);
        }

        /**
         * The stage of the nested traffic manager profile endpoint definition allowing to specify the minimum
         * endpoints to be online in the nested profile to consider it as not degraded.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithEndpointThreshold<ParentT> {
            /**
             * Specifies the child endpoint threshold.
             *
             * @param count the number of child endpoints to be online to consider nested profile as healthy
             * @return the next stage of the definition
             */
            WithSourceTrafficLocation<ParentT> withMinimumChildEndpoints(int count);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the location of the external
         * or nested profile endpoints.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSourceTrafficLocation<ParentT> {
            /**
             * Specifies the location of the endpoint that will be used when the parent profile is configured with
             * Performance routing method {@link TrafficRoutingMethod#PERFORMANCE}.
             *
             * @param location the location
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSourceTrafficLocation(Region location);
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
             * Specifies the weight for the endpoint that will be used when the parent profile is configured with
             * Priority routing method {@link TrafficRoutingMethod#PRIORITY}.
             *
             * @param priority
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
             * Specify that this endpoint should be excluded from receiving traffic.
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
            UpdateStages.WithTargetAzureResourceId,
            Update {
    }

    /**
     * The entirety of an external endpoint update as a part of parent traffic manager profile profile update.
     */
    interface UpdateExternalEndpoint extends
            UpdateStages.WithExternalFqdn,
            UpdateStages.WithSourceTrafficLocation,
            Update {
    }

    /**
     * The entirety of a nested profile endpoint update as a part of parent traffic manager profile profile update.
     */
    interface UpdateNestedProfileEndpoint extends
            UpdateStages.WithNestedProfileConfig,
            UpdateStages.WithSourceTrafficLocation,
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
        interface WithTargetAzureResourceId {
            /**
             * Specify the resource id of target Azure cloud service, web app or public ip.
             *
             * @param resourceId the Azure resource id
             * @return the next stage of the endpoint update
             */
            Update withTargetAzureResourceId(String resourceId);
        }

        /**
         * The stage of an external endpoint update allowing to specify the FQDN.
         */
        interface WithExternalFqdn {
            /**
             * Specify the FQDN of an external endpoint that is not hosted in Azure.
             *
             * @param externalFqdn the external FQDN
             * @return the next stage of the endpoint update
             */
            UpdateExternalEndpoint withExternalFqdn(String externalFqdn);
        }

        /**
         * The stage of an nested profile endpoint update allowing to specify profile and
         * minimum child endpoint.
         */
        interface WithNestedProfileConfig {
            /**
             * Specify the traffic manager profile to be used as nested profile.
             *
             * @param nestedProfile the nested traffic manager profile
             * @return the next stage of the update
             */
            UpdateNestedProfileEndpoint withNestedProfile(TrafficManagerProfile nestedProfile);

            /**
             * Specifies the child endpoint threshold.
             *
             * @param count the number of child endpoints to be online to consider nested profile as healthy
             * @return the next stage of the endpoint update
             */
            UpdateNestedProfileEndpoint withMinimumChildEndpoints(int count);
        }

        /**
         * The stage of the traffic manager endpoint update allowing to specify the location of the external
         * or nested profile endpoints.
         */
        interface WithSourceTrafficLocation {
            /**
             * Specifies the location of the endpoint that will be used when the parent profile is configured with
             * Performance routing method {@link TrafficRoutingMethod#PERFORMANCE}.
             *
             * @param location the location
             * @return the next stage of the endpoint update
             */
            Update withSourceTrafficLocation(Region location);
        }

        /**
         * The stage of the traffic manager profile endpoint update allowing to specify the endpoint weight.
         */
        interface WithRoutingWeight {
            /**
             * Specifies the weight for the endpoint that will be used when the parent profile is configured with
             * Weighted routing method {@link TrafficRoutingMethod#WEIGHTED}.
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
             * Specifies the weight for the endpoint that will be used when the parent profile is configured with
             * Priority routing method {@link TrafficRoutingMethod#PRIORITY}.
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
             * Specify that the endpoint should be excluded from receiving traffic.
             *
             * @return the next stage of the update
             */
            Update withTrafficDisabled();

            /**
             * Specify that the endpoint should receive the traffic.
             *
             * @return the next stage of the update
             */
            Update withTrafficEnabled();
        }
    }
}
