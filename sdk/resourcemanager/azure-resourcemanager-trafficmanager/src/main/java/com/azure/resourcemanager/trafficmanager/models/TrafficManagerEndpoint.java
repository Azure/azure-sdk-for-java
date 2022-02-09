// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.models;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import com.azure.resourcemanager.trafficmanager.fluent.models.EndpointInner;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** An immutable client-side representation of an Azure traffic manager profile endpoint. */
public interface TrafficManagerEndpoint
    extends ExternalChildResource<TrafficManagerEndpoint, TrafficManagerProfile>, HasInnerModel<EndpointInner> {
    /** @return the endpoint type */
    EndpointType endpointType();

    /** @return the monitor status of the endpoint */
    EndpointMonitorStatus monitorStatus();

    /** @return true if the endpoint is enabled, false otherwise */
    boolean isEnabled();

    /**
     * @return the weight of the endpoint which is used when traffic manager profile is configured with Weighted
     *     traffic-routing method
     */
    long routingWeight();

    /**
     * @return the priority of the endpoint which is used when traffic manager profile is configured with Priority
     *     traffic-routing method
     */
    long routingPriority();

    /** @return the geographic location codes indicating the locations to which traffic will be distributed. */
    Set<String> geographicLocationCodes();

    /** @return the list of subnets, IP addresses, and/or address ranges mapped to this endpoint. */
    Collection<EndpointPropertiesSubnetsItem> subnets();

    /** @return custom headers associated with the endpoint as key-value pair. */
    Map<String, String> customHeaders();

    /**
     * The entirety of a traffic manager profile endpoint definition as a part of parent definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.AzureTargetEndpointBlank<ParentT>,
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
     * Grouping of traffic manager profile endpoint definition stages as a part of parent traffic manager profile
     * definition.
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
         * The stage of the traffic manager profile Azure endpoint definition allowing to specify the ID of the target
         * Azure resource.
         *
         * @param <ParentT> the return type of {@link UpdateDefinitionStages.WithAttach#attach()}
         */
        interface WithAzureResource<ParentT> {
            /**
             * Specifies the resource ID of an Azure resource.
             *
             * <p>supported Azure resources are cloud service, web app or public ip
             *
             * @param resourceId the Azure resource id
             * @return the next stage of the definition
             */
            WithAttach<ParentT> toResourceId(String resourceId);
        }

        /**
         * The stage of the traffic manager profile external endpoint definition allowing to specify the FQDN.
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
         * The stage of the traffic manager endpoint definition allowing to specify the location of the nested profile
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
         * The stage of the nested traffic manager profile endpoint definition allowing to specify the minimum endpoints
         * to be online in the nested profile to consider it as not degraded.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithEndpointThreshold<ParentT> extends WithAttach<ParentT> {
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
            WithAttach<ParentT> withRoutingPriority(int priority);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the geographic region.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithGeographicLocation<ParentT> {
            /**
             * Specifies the geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method {@link TrafficRoutingMethod#GEOGRAPHIC}.
             *
             * @param geographicLocation the geographic location
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGeographicLocation(GeographicLocation geographicLocation);

            /**
             * Specifies the list of geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method {@link TrafficRoutingMethod#GEOGRAPHIC}.
             *
             * @param geographicLocations the geographic locations
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGeographicLocations(List<GeographicLocation> geographicLocations);
            /**
             * Specifies the geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method {@link TrafficRoutingMethod#GEOGRAPHIC}.
             *
             * @param geographicLocationCode the geographic location code
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGeographicLocation(String geographicLocationCode);
            /**
             * Specifies the list of geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method {@link TrafficRoutingMethod#GEOGRAPHIC}.
             *
             * @param geographicLocationCodes the geographic location codes
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGeographicLocations(Collection<String> geographicLocationCodes);
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

        /**
         * The stage of the traffic manager endpoint definition allowing to specify subnets.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSubnet<ParentT> {
            /**
             * Specifies the subnets for the endpoint in CIDR format (start ip, mask).
             *
             * @param subnetStartIp the first ip in the subnet
             * @param mask the subnet mask
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSubnet(String subnetStartIp, int mask);

            /**
             * Specifies the subnets for the endpoint as ip range.
             *
             * @param subnetStartIp the first ip in the subnet
             * @param subnetEndIp the last ip in the subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSubnet(String subnetStartIp, String subnetEndIp);

            /**
             * Specifies the subnets for this endpoint. This method replaces current subnets with the provided subnets.
             *
             * @param subnets the array of subnet descriptions
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSubnets(List<EndpointPropertiesSubnetsItem> subnets);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify custom headers.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithCustomHeader<ParentT> {
            /**
             * Add a custom header.
             *
             * @param name the header name
             * @param value the header value
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withCustomHeader(String name, String value);

            /**
             * Add a custom header. This method replaces the current headers with the provided headers.
             *
             * @param headerValues the map containing header name and value pair
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withCustomHeaders(Map<String, String> headerValues);
        }

        /**
         * The final stage of the traffic manager profile endpoint definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the traffic manager profile endpoint
         * definition can be attached to the parent traffic manager profile definition using {@link
         * TrafficManagerEndpoint.DefinitionStages.WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link TrafficManagerEndpoint.DefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
                DefinitionStages.WithRoutingWeight<ParentT>,
                DefinitionStages.WithRoutingPriority<ParentT>,
                DefinitionStages.WithGeographicLocation<ParentT>,
                DefinitionStages.WithTrafficDisabled<ParentT>,
                DefinitionStages.WithSubnet<ParentT>,
                DefinitionStages.WithCustomHeader<ParentT> {
        }
    }

    /**
     * The entirety of a traffic manager profile endpoint definition as a part of parent update.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.AzureTargetEndpointBlank<ParentT>,
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
     * Grouping of traffic manager profile endpoint definition stages as a part of parent traffic manager profile
     * update.
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
         * The stage of the traffic manager profile Azure endpoint definition allowing to specify the ID of the target
         * Azure resource.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAzureResource<ParentT> {
            /**
             * Specifies the resource ID of an Azure resource.
             *
             * <p>supported Azure resources are cloud service, web app or public ip
             *
             * @param resourceId the Azure resource id
             * @return the next stage of the definition
             */
            WithAttach<ParentT> toResourceId(String resourceId);
        }

        /**
         * The stage of the traffic manager profile external endpoint definition allowing to specify the FQDN.
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
         * The stage of the traffic manager endpoint definition allowing to specify the location of the nested endpoint.
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
         * The stage of the nested traffic manager profile endpoint definition allowing to specify the minimum endpoints
         * to be online in the nested profile to consider it as not degraded.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithEndpointThreshold<ParentT> extends WithAttach<ParentT> {
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
             * Specifies the weight for the endpoint that will be used when the weight-based routing method {@link
             * TrafficRoutingMethod#WEIGHTED} is enabled on the profile.
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
             * Specifies the weight for the endpoint that will be used when priority-based routing method is {@link
             * TrafficRoutingMethod#PRIORITY} enabled on the profile.
             *
             * @param priority priority of this endpoint. Possible values are from 1 to 1000, lower values represent
             *     higher priority.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRoutingPriority(int priority);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify the geographic region.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithGeographicLocation<ParentT> {
            /**
             * Specifies the geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method.
             *
             * @param geographicLocation the geographic location
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGeographicLocation(GeographicLocation geographicLocation);
            /**
             * Specifies the list of geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method.
             *
             * @param geographicLocations the geographic locations
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGeographicLocations(List<GeographicLocation> geographicLocations);
            /**
             * Specifies the geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method.
             *
             * @param geographicLocationCode the geographic location code
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGeographicLocation(String geographicLocationCode);
            /**
             * Specifies the list of geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method.
             *
             * @param geographicLocationCodes the geographic location codes
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGeographicLocations(Collection<String> geographicLocationCodes);
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

        /**
         * The stage of the traffic manager endpoint definition allowing to specify subnets.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSubnet<ParentT> {
            /**
             * Specifies the subnet for the endpoint in CIDR format (start ip, mask).
             *
             * @param subnetStartIp the first ip in the subnet
             * @param mask the subnet scope
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSubnet(String subnetStartIp, int mask);

            /**
             * Specifies the subnet for the endpoint as an ip range.
             *
             * @param subnetStartIp the first ip in the subnet
             * @param subnetEndIp the last ip in the subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSubnet(String subnetStartIp, String subnetEndIp);

            /**
             * Specifies the usbnets for this endpoint. This method replaces the current subnets with the provided
             * subnets.
             *
             * @param subnets the array of subnets description
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSubnets(List<EndpointPropertiesSubnetsItem> subnets);
        }

        /**
         * The stage of the traffic manager endpoint definition allowing to specify custom headers.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithCustomHeader<ParentT> {
            /**
             * Add a custom header.
             *
             * @param name the header name
             * @param value the header value
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withCustomHeader(String name, String value);

            /**
             * Add custom headers. This method replaces the current headers with the provided headers.
             *
             * @param headerValues the map containing header name and value pair
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withCustomHeaders(Map<String, String> headerValues);
        }

        /**
         * The final stage of the traffic manager profile endpoint definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the traffic manager profile endpoint
         * definition can be attached to the parent traffic manager profile update using {@link
         * TrafficManagerEndpoint.DefinitionStages.WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link TrafficManagerEndpoint.DefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT>
            extends Attachable.InUpdate<ParentT>,
                UpdateDefinitionStages.WithRoutingWeight<ParentT>,
                UpdateDefinitionStages.WithRoutingPriority<ParentT>,
                UpdateDefinitionStages.WithGeographicLocation<ParentT>,
                UpdateDefinitionStages.WithTrafficDisabled<ParentT>,
                UpdateDefinitionStages.WithSubnet<ParentT>,
                UpdateDefinitionStages.WithCustomHeader<ParentT> {
        }
    }

    /** The entirety of an Azure endpoint update as a part of parent traffic manager profile profile update. */
    interface UpdateAzureEndpoint extends UpdateStages.WithAzureResource, Update {
    }

    /** The entirety of an external endpoint update as a part of parent traffic manager profile profile update. */
    interface UpdateExternalEndpoint extends UpdateStages.WithFqdn, UpdateStages.WithSourceTrafficRegion, Update {
    }

    /** The entirety of a nested profile endpoint update as a part of parent traffic manager profile profile update. */
    interface UpdateNestedProfileEndpoint
        extends UpdateStages.WithNestedProfileConfig, UpdateStages.WithSourceTrafficRegion, Update {
    }

    /**
     * the set of configurations that can be updated for all endpoint irrespective of their type (Azure, external,
     * nested profile).
     */
    interface Update
        extends Settable<TrafficManagerProfile.Update>,
            UpdateStages.WithRoutingWeight,
            UpdateStages.WithRoutingPriority,
            UpdateStages.WithGeographicLocation,
            UpdateStages.WithTrafficDisabledOrEnabled,
            UpdateStages.WithSubnet,
            UpdateStages.WithCustomHeader {
    }

    /** Grouping of traffic manager profile endpoint update stages. */
    interface UpdateStages {
        /** The stage of an Azure endpoint update allowing to specify the target Azure resource. */
        interface WithAzureResource {
            /**
             * Specifies the resource ID of an Azure resource.
             *
             * <p>supported Azure resources are cloud service, web app or public ip
             *
             * @param resourceId the Azure resource id
             * @return the next stage of the update
             */
            Update toResourceId(String resourceId);
        }

        /** The stage of an external endpoint update allowing to specify the FQDN. */
        interface WithFqdn {
            /**
             * Specifies the FQDN of an external endpoint that is not hosted in Azure.
             *
             * @param externalFqdn the external FQDN
             * @return the next stage of the endpoint update
             */
            UpdateExternalEndpoint toFqdn(String externalFqdn);
        }

        /** The stage of an nested profile endpoint update allowing to specify profile and minimum child endpoint. */
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
             * @param count number of endpoints
             * @return the next stage of the endpoint update
             */
            UpdateNestedProfileEndpoint withMinimumEndpointsToEnableTraffic(int count);
        }

        /**
         * The stage of the traffic manager endpoint update allowing to specify the location of the external or nested
         * profile endpoints.
         */
        interface WithSourceTrafficRegion {
            /**
             * Specifies the region of the endpoint that will be used when the performance-based routing method {@link
             * TrafficRoutingMethod#PERFORMANCE} is enabled on the profile.
             *
             * @param location the location
             * @return the next stage of the endpoint update
             */
            Update fromRegion(Region location);
        }

        /** The stage of the traffic manager profile endpoint update allowing to specify the endpoint weight. */
        interface WithRoutingWeight {
            /**
             * Specifies the weight for the endpoint that will be used when the weight-based routing method {@link
             * TrafficRoutingMethod#WEIGHTED} is enabled on the profile.
             *
             * @param weight the endpoint weight
             * @return the next stage of the update
             */
            Update withRoutingWeight(int weight);
        }

        /** The stage of the traffic manager profile endpoint update allowing to specify the endpoint priority. */
        interface WithRoutingPriority {
            /**
             * Specifies the weight for the endpoint that will be used when priority-based routing method is {@link
             * TrafficRoutingMethod#PRIORITY} enabled on the profile.
             *
             * @param priority the endpoint priority
             * @return the next stage of the update
             */
            Update withRoutingPriority(int priority);
        }

        /** The stage of the traffic manager update definition allowing to specify the geographic region. */
        interface WithGeographicLocation {
            /**
             * Specifies the geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method.
             *
             * @param geographicLocation the geographic location
             * @return the next stage of the update
             */
            Update withGeographicLocation(GeographicLocation geographicLocation);
            /**
             * Specifies the list of geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method.
             *
             * @param geographicLocations the geographic locations
             * @return the next stage of the update
             */
            Update withGeographicLocations(List<GeographicLocation> geographicLocations);
            /**
             * Specifies the geographic location to be removed from the endpoint's geographic location entries.
             *
             * @param geographicLocation the geographic location
             * @return the next stage of the update
             */
            Update withoutGeographicLocation(GeographicLocation geographicLocation);
            /**
             * Specifies the geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method.
             *
             * @param geographicLocationCode the geographic location code
             * @return the next stage of the update
             */
            Update withGeographicLocation(String geographicLocationCode);

            /**
             * Specifies the list of geographic location for the endpoint that will be used when the parent profile is
             * configured with geographic based routing method.
             *
             * @param geographicLocationCodes the geographic location codes
             * @return the next stage of the update
             */
            Update withGeographicLocations(Collection<String> geographicLocationCodes);
            /**
             * Specifies the geographic location to be removed from the endpoint's geographic location entries.
             *
             * @param geographicLocationCode the geographic location code
             * @return the next stage of the update
             */
            Update withoutGeographicLocation(String geographicLocationCode);
        }

        /** The stage of the traffic manager profile endpoint update allowing to enable or disable it. */
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

        /** The stage of the traffic manager endpoint update allowing to specify subnets. */
        interface WithSubnet {
            /**
             * Specifies the subnets for the endpoint in CIDR format (start ip, mask).
             *
             * @param subnetStartIp the first ip in the subnet
             * @param mask the subnet mask
             * @return the next stage of the update
             */
            Update withSubnet(String subnetStartIp, int mask);

            /**
             * Specifies the subnets for the endpoint as an ip range.
             *
             * @param subnetStartIp the first ip in the subnet
             * @param subnetEndIp the last ip in the subnet
             * @return the next stage of the update
             */
            Update withSubnet(String subnetStartIp, String subnetEndIp);

            /**
             * Specifies the subnets for this endpoint.
             *
             * @param subnets the array of subnet descriptions
             * @return the next stage of the update
             */
            Update withSubnets(List<EndpointPropertiesSubnetsItem> subnets);

            /**
             * Specifies that the given subnet CIDR (start ip, mask) should be removed.
             *
             * @param subnetStartIp the first ip in the subnet
             * @param scope the subnet scope
             * @return the next stage of the update
             */
            Update withoutSubnet(String subnetStartIp, int scope);

            /**
             * Specifies that subnet with the given range should be removed.
             *
             * @param subnetStartIp the first ip in the subnet
             * @param subnetEndIp the last ip in the subnet
             * @return the next stage of the update
             */
            Update withoutSubnet(String subnetStartIp, String subnetEndIp);
        }

        /** The stage of the traffic manager endpoint update allowing to specify custom headers. */
        interface WithCustomHeader {
            /**
             * Add a custom header.
             *
             * @param name the header name
             * @param value the header value
             * @return the next stage of the update
             */
            Update withCustomHeader(String name, String value);

            /**
             * Add custom headers. This method replaces the current headers with the provided headers.
             *
             * @param headers the map containing header name and value pair
             * @return the next stage of the update
             */
            Update withCustomHeaders(Map<String, String> headers);

            /**
             * Removes a custom header.
             *
             * @param name the name of the header to remove
             * @return the next stage of the update
             */
            Update withoutCustomHeader(String name);
        }
    }
}
