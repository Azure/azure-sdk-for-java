// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.models;

import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.trafficmanager.TrafficManager;
import com.azure.resourcemanager.trafficmanager.fluent.models.ProfileInner;
import java.util.Map;

/** An immutable client-side representation of an Azure traffic manager profile. */
public interface TrafficManagerProfile
    extends GroupableResource<TrafficManager, ProfileInner>,
        Refreshable<TrafficManagerProfile>,
        Updatable<TrafficManagerProfile.Update> {
    /** @return the relative DNS name of the traffic manager profile */
    String dnsLabel();

    /** @return fully qualified domain name (FQDN) of the traffic manager profile. */
    String fqdn();

    /** @return the DNS Time-To-Live (TTL), in seconds */
    long timeToLive();

    /** @return true if the traffic manager profile is enabled, false if enabled */
    boolean isEnabled();

    /** @return the routing method used to route traffic to traffic manager profile endpoints */
    TrafficRoutingMethod trafficRoutingMethod();

    /**
     * @return profile monitor status which is combination of the endpoint monitor status values for all endpoints in
     *     the profile, and the configured profile status
     */
    ProfileMonitorStatus monitorStatus();

    /** @return the port that is monitored to check the health of traffic manager profile endpoints */
    long monitoringPort();

    /** @return the path that is monitored to check the health of traffic manager profile endpoints */
    String monitoringPath();

    /** @return external endpoints in the traffic manager profile, indexed by the name */
    Map<String, TrafficManagerExternalEndpoint> externalEndpoints();

    /** @return Azure endpoints in the traffic manager profile, indexed by the name */
    Map<String, TrafficManagerAzureEndpoint> azureEndpoints();

    /** @return nested traffic manager profile endpoints in this traffic manager profile, indexed by the name */
    Map<String, TrafficManagerNestedProfileEndpoint> nestedProfileEndpoints();

    /** The entirety of the traffic manager profile definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithLeafDomainLabel,
            DefinitionStages.WithTrafficRoutingMethod,
            DefinitionStages.WithCreate {
    }

    /** Grouping of traffic manager profile definition stages. */
    interface DefinitionStages {
        /** The stage of the traffic manager profile definition allowing to specify the resource group. */
        interface Blank extends GroupableResource.DefinitionStages.WithGroupAndRegion<WithLeafDomainLabel> {
        }

        /** The stage of the traffic manager profile definition allowing to specify the relative DNS name. */
        interface WithLeafDomainLabel {
            /**
             * Specify the relative DNS name of the profile.
             *
             * <p>The fully qualified domain name (FQDN) will be constructed automatically by appending the rest of the
             * domain to this label.
             *
             * @param dnsLabel the relative DNS name of the profile
             * @return the next stage of the definition
             */
            WithTrafficRoutingMethod withLeafDomainLabel(String dnsLabel);
        }

        /**
         * The stage of the traffic manager profile definition allowing to specify the traffic routing method for the
         * profile.
         */
        interface WithTrafficRoutingMethod {
            /**
             * Specifies that end user traffic should be routed to the endpoint based on its priority i.e. use the
             * endpoint with highest priority and if it is not available fallback to next highest priority endpoint.
             *
             * @return the next stage of the definition
             */
            WithEndpoint withPriorityBasedRouting();

            /**
             * Specifies that end user traffic should be distributed to the endpoints based on the weight assigned to
             * the endpoint.
             *
             * @return the next stage of the definition
             */
            WithEndpoint withWeightBasedRouting();

            /**
             * Specifies that end user traffic should be routed based on the closest available endpoint in terms of the
             * lowest network latency.
             *
             * @return the next stage of the definition
             */
            WithEndpoint withPerformanceBasedRouting();

            /**
             * Specifies that end user traffic should be routed to the endpoint that is designated to serve users
             * geographic region.
             *
             * @return the next stage of the definition
             */
            WithEndpoint withGeographicBasedRouting();

            /**
             * Specifies that end user traffic should be routed to the endpoint that return multiple healthy endpoints.
             *
             * @param maxReturn the maximum number of result to return
             * @return the next stage of the definition
             */
            WithEndpoint withMultiValueBasedRouting(long maxReturn);

            /**
             * Specifies that end user traffic should be routed to the endpoint which is decided based on the end-user
             * ip address.
             *
             * @return the next stage of the definition
             */
            WithEndpoint withSubnetBasedRouting();

            /**
             * Specify the traffic routing method for the profile.
             *
             * @param routingMethod the traffic routing method for the profile
             * @return the next stage of the definition
             */
            WithEndpoint withTrafficRoutingMethod(TrafficRoutingMethod routingMethod);
        }

        /** The stage of the traffic manager profile definition allowing to specify endpoint. */
        interface WithEndpoint {
            /**
             * Specifies definition of an Azure endpoint to be attached to the traffic manager profile.
             *
             * @param name the name for the endpoint
             * @return the stage representing configuration for the endpoint
             */
            TrafficManagerEndpoint.DefinitionStages.AzureTargetEndpointBlank<WithCreate> defineAzureTargetEndpoint(
                String name);

            /**
             * Specifies definition of an external endpoint to be attached to the traffic manager profile.
             *
             * @param name the name for the endpoint
             * @return the stage representing configuration for the endpoint
             */
            TrafficManagerEndpoint.DefinitionStages.ExternalTargetEndpointBlank<WithCreate>
                defineExternalTargetEndpoint(String name);

            /**
             * Specifies definition of an nested profile endpoint to be attached to the traffic manager profile.
             *
             * @param name the name for the endpoint
             * @return the stage representing configuration for the endpoint
             */
            TrafficManagerEndpoint.DefinitionStages.NestedProfileTargetEndpointBlank<WithCreate>
                defineNestedTargetEndpoint(String name);
        }

        /**
         * The stage of the traffic manager profile definition allowing to specify the endpoint monitoring
         * configuration.
         */
        interface WithMonitoringConfiguration {
            /**
             * Specify to use HTTP monitoring for the endpoints that checks for HTTP 200 response from the path '/' at
             * regular intervals, using port 80.
             *
             * @return the next stage of the definition
             */
            WithCreate withHttpMonitoring();

            /**
             * Specify to use HTTPS monitoring for the endpoints that checks for HTTPS 200 response from the path '/' at
             * regular intervals, using port 443.
             *
             * @return the next stage of the definition
             */
            WithCreate withHttpsMonitoring();

            /**
             * Specify the HTTP monitoring for the endpoints that checks for HTTP 200 response from the specified path
             * at regular intervals, using the specified port.
             *
             * @param port the monitoring port
             * @param path the monitoring path
             * @return the next stage of the definition
             */
            WithCreate withHttpMonitoring(int port, String path);

            /**
             * Specify the HTTPS monitoring for the endpoints that checks for HTTPS 200 response from the specified path
             * at regular intervals, using the specified port.
             *
             * @param port the monitoring port
             * @param path the monitoring path
             * @return the next stage of the definition
             */
            WithCreate withHttpsMonitoring(int port, String path);
        }

        /** The stage of the traffic manager profile definition allowing to specify the DNS TTL. */
        interface WithTtl {
            /**
             * Specify the DNS TTL in seconds.
             *
             * @param ttlInSeconds DNS TTL in seconds
             * @return the next stage of the definition
             */
            WithCreate withTimeToLive(int ttlInSeconds);
        }

        /** The stage of the traffic manager profile definition allowing to disable the profile. */
        interface WithProfileStatus {
            /**
             * Specify that the profile needs to be disabled.
             *
             * <p>Disabling the profile will disables traffic to all endpoints in the profile
             *
             * @return the next stage of the definition
             */
            WithCreate withProfileStatusDisabled();
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<TrafficManagerProfile>,
                Resource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithMonitoringConfiguration,
                DefinitionStages.WithTtl,
                DefinitionStages.WithProfileStatus,
                DefinitionStages.WithEndpoint {
        }
    }

    /** Grouping of traffic manager update stages. */
    interface UpdateStages {
        /**
         * The stage of the traffic manager profile update allowing to specify the traffic routing method for the
         * profile.
         */
        interface WithTrafficRoutingMethod {
            /**
             * Specifies that end user traffic should be routed to the endpoint based on its priority i.e. use the
             * endpoint with highest priority and if it is not available fallback to next highest priority endpoint.
             *
             * @return the next stage of the traffic manager profile update
             */
            Update withPriorityBasedRouting();

            /**
             * Specifies that end user traffic should be distributed to the endpoints based on the weight assigned to
             * the endpoint.
             *
             * @return the next stage of the traffic manager profile update
             */
            Update withWeightBasedRouting();

            /**
             * Specifies that end user traffic should be routed based on the closest available endpoint in terms of the
             * lowest network latency.
             *
             * @return the next stage of the update
             */
            Update withPerformanceBasedRouting();

            /**
             * Specifies that end user traffic should be routed to the endpoint that is designated to serve users
             * geographic region.
             *
             * @return the next stage of the update
             */
            Update withGeographicBasedRouting();

            /**
             * Specifies that end user traffic should be routed to the endpoint that return multiple healthy endpoints.
             *
             * @param maxReturn the maximum number of result to return
             * @return the next stage of the Update
             */
            Update withMultiValueBasedRouting(long maxReturn);

            /**
             * Specifies that end user traffic should be routed to the endpoint which is decided based on the end-user
             * ip address.
             *
             * @return the next stage of the update
             */
            Update withSubnetBasedRouting();

            /**
             * Specifies the traffic routing method for the profile.
             *
             * @param routingMethod the traffic routing method for the profile
             * @return the next stage of the traffic manager profile update
             */
            Update withTrafficRoutingMethod(TrafficRoutingMethod routingMethod);
        }

        /**
         * The stage of the traffic manager profile update allowing to specify the endpoint monitoring configuration.
         */
        interface WithMonitoringConfiguration {
            /**
             * Specify to use HTTP monitoring for the endpoints that checks for HTTP 200 response from the path '/' at
             * regular intervals, using port 80.
             *
             * @return the next stage of the traffic manager profile update
             */
            Update withHttpMonitoring();

            /**
             * Specify to use HTTPS monitoring for the endpoints that checks for HTTPS 200 response from the path '/' at
             * regular intervals, using port 443.
             *
             * @return the next stage of the traffic manager profile update
             */
            Update withHttpsMonitoring();

            /**
             * Specify the HTTP monitoring for the endpoints that checks for HTTP 200 response from the specified path
             * at regular intervals, using the specified port.
             *
             * @param port the monitoring port
             * @param path the monitoring path
             * @return the next stage of the traffic manager profile update
             */
            Update withHttpMonitoring(int port, String path);

            /**
             * Specify the HTTPS monitoring for the endpoints that checks for HTTPS 200 response from the specified path
             * at regular intervals, using the specified port.
             *
             * @param port the monitoring port
             * @param path the monitoring path
             * @return the next stage of the traffic manager profile update
             */
            Update withHttpsMonitoring(int port, String path);
        }

        /** The stage of the traffic manager profile update allowing to specify endpoints. */
        interface WithEndpoint {
            /**
             * Begins the definition of an Azure endpoint to be attached to the traffic manager profile.
             *
             * @param name the name for the endpoint
             * @return the stage representing configuration for the endpoint
             */
            TrafficManagerEndpoint.UpdateDefinitionStages.AzureTargetEndpointBlank<Update> defineAzureTargetEndpoint(
                String name);

            /**
             * Begins the definition of an external endpoint to be attached to the traffic manager profile.
             *
             * @param name the name for the endpoint
             * @return the stage representing configuration for the endpoint
             */
            TrafficManagerEndpoint.UpdateDefinitionStages.ExternalTargetEndpointBlank<Update>
                defineExternalTargetEndpoint(String name);

            /**
             * Begins the definition of a nested profile endpoint to be attached to the traffic manager profile.
             *
             * @param name the name for the endpoint
             * @return the stage representing configuration for the endpoint
             */
            TrafficManagerEndpoint.UpdateDefinitionStages.NestedProfileTargetEndpointBlank<Update>
                defineNestedTargetEndpoint(String name);

            /**
             * Begins the description of an update of an existing Azure endpoint in this profile.
             *
             * @param name the name of the Azure endpoint
             * @return the stage representing updating configuration for the Azure endpoint
             */
            TrafficManagerEndpoint.UpdateAzureEndpoint updateAzureTargetEndpoint(String name);

            /**
             * Begins the description of an update of an existing external endpoint in this profile.
             *
             * @param name the name of the external endpoint
             * @return the stage representing updating configuration for the external endpoint
             */
            TrafficManagerEndpoint.UpdateExternalEndpoint updateExternalTargetEndpoint(String name);

            /**
             * Begins the description of an update of an existing nested traffic manager profile endpoint in this
             * profile.
             *
             * @param name the name of the nested profile endpoint
             * @return the stage representing updating configuration for the nested traffic manager profile endpoint
             */
            TrafficManagerEndpoint.UpdateNestedProfileEndpoint updateNestedProfileTargetEndpoint(String name);

            /**
             * Removes an endpoint in the profile.
             *
             * @param name the name of the endpoint
             * @return the next stage of the traffic manager profile update
             */
            Update withoutEndpoint(String name);
        }

        /** The stage of the traffic manager profile update allowing to specify the DNS TTL. */
        interface WithTtl {
            /**
             * Specify the DNS TTL in seconds.
             *
             * @param ttlInSeconds DNS TTL in seconds
             * @return the next stage of the traffic manager profile update
             */
            Update withTimeToLive(int ttlInSeconds);
        }

        /** The stage of the traffic manager profile update allowing to disable or enable the profile. */
        interface WithProfileStatus {
            /**
             * Specify that the profile needs to be disabled.
             *
             * <p>Disabling the profile will disables traffic to all endpoints in the profile
             *
             * @return the next stage of the traffic manager profile update
             */
            Update withProfileStatusDisabled();

            /**
             * Specify that the profile needs to be enabled.
             *
             * <p>Enabling the profile will enables traffic to all endpoints in the profile
             *
             * @return the next stage of the traffic manager profile update
             */
            Update withProfileStatusEnabled();
        }
    }

    /**
     * The template for an update operation, containing all the settings that can be modified.
     *
     * <p>Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update
        extends Appliable<TrafficManagerProfile>,
            UpdateStages.WithTrafficRoutingMethod,
            UpdateStages.WithMonitoringConfiguration,
            UpdateStages.WithEndpoint,
            UpdateStages.WithTtl,
            UpdateStages.WithProfileStatus,
            Resource.UpdateWithTags<Update> {
    }
}
