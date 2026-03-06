// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.cdn.fluent.models.AfdOriginGroupInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure Front Door (AFD) origin group that lives under a
 * {@link CdnProfile}.
 */
@Fluent
public interface OriginGroup
    extends ExternalChildResource<OriginGroup, CdnProfile>, HasInnerModel<AfdOriginGroupInner> {

    /**
     * Gets the name of the profile which holds the origin group.
     *
     * @return the profile name
     */
    String profileName();

    /**
     * Gets the load balancing settings for the origin group.
     *
     * @return the load balancing settings
     */
    LoadBalancingSettingsParameters loadBalancingSettings();

    /**
     * Gets the health probe settings for the origin group.
     *
     * @return the health probe settings
     */
    HealthProbeParameters healthProbeSettings();

    /**
     * Gets the time in minutes to shift traffic to the endpoint gradually when an unhealthy endpoint
     * comes healthy or a new endpoint is added.
     *
     * @return the traffic restoration time in minutes
     */
    Integer trafficRestorationTimeToHealedOrNewEndpointsInMinutes();

    /**
     * Gets the session affinity state for this origin group.
     *
     * @return the session affinity state
     */
    EnabledState sessionAffinityState();

    /**
     * Gets the authentication settings for origins in this origin group.
     *
     * @return the origin authentication properties
     */
    OriginAuthenticationProperties authentication();

    /**
     * Gets the provisioning state reported by the service.
     *
     * @return the provisioning state
     */
    AfdProvisioningState provisioningState();

    /**
     * Gets the deployment status for the origin group.
     *
     * @return the deployment status
     */
    DeploymentStatus deploymentStatus();

    /**
     * Gets the origins in this origin group, indexed by name.
     *
     * @return origins in this origin group, indexed by name
     */
    Map<String, Origin> origins();

    /**
     * Grouping of origin group definition stages as part of a parent {@link CdnProfile} definition.
     */
    interface DefinitionStages {
        /**
         * The first stage of an origin group definition.
         * Load balancing settings must be specified before any other configuration.
         *
         * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
         */
        interface Blank<ParentT> {
            /**
             * Specifies the load balancing settings for this origin group (required).
             * <p>
             * Property {@link LoadBalancingSettingsParameters#withSampleSize(Integer)} and
             * {@link LoadBalancingSettingsParameters#withSuccessfulSamplesRequired(Integer)} is required to be set.
             *
             * @param loadBalancingSettings the load balancing settings
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLoadBalancingSettings(LoadBalancingSettingsParameters loadBalancingSettings);
        }

        /**
         * The stage of the definition containing all optional settings prior to attachment.
         *
         * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable<ParentT> {
            /**
             * Specifies the health probe settings for this origin group.
             *
             * @param healthProbeSettings the health probe settings
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHealthProbeSettings(HealthProbeParameters healthProbeSettings);

            /**
             * Specifies the time in minutes to shift traffic gradually to a healed or new endpoint.
             *
             * @param minutes the traffic restoration time in minutes
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTrafficRestorationTimeToHealedOrNewEndpointsInMinutes(Integer minutes);

            /**
             * Specifies the session affinity state for this origin group.
             *
             * @param sessionAffinityState the session affinity state
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSessionAffinityState(EnabledState sessionAffinityState);

            /**
             * Starts the definition of a new origin to be attached to this origin group.
             *
             * @param name a new origin name
             * @return the first stage of a new origin definition
             */
            Origin.DefinitionStages.Blank<WithAttach<ParentT>> defineOrigin(String name);
        }

        /**
         * The final stage of an origin group definition.
         *
         * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
         */
        interface Attachable<ParentT> {
            /**
             * Attaches the defined origin group to the parent CDN profile.
             *
             * @return the next stage of the parent definition
             */
            ParentT attach();
        }
    }

    /**
     * The entirety of an origin group definition.
     *
     * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends DefinitionStages.Blank<ParentT>, DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of origin group definition stages that run as part of a {@link CdnProfile#update()} flow.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an origin group definition inside a profile update.
         * Load balancing settings must be specified before any other configuration.
         *
         * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
         */
        interface Blank<ParentT> {
            /**
             * Specifies the load balancing settings for this origin group (required).
             * <p>
             * Property {@link LoadBalancingSettingsParameters#withSampleSize(Integer)} and
             * {@link LoadBalancingSettingsParameters#withSuccessfulSamplesRequired(Integer)} is required to be set.
             *
             * @param loadBalancingSettings the load balancing settings
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLoadBalancingSettings(LoadBalancingSettingsParameters loadBalancingSettings);
        }

        /**
         * The stage of the definition containing all optional settings prior to attachment.
         *
         * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable<ParentT> {
            /**
             * Specifies the health probe settings for this origin group.
             *
             * @param healthProbeSettings the health probe settings
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHealthProbeSettings(HealthProbeParameters healthProbeSettings);

            /**
             * Specifies the time in minutes to shift traffic gradually to a healed or new endpoint.
             *
             * @param minutes the traffic restoration time in minutes
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTrafficRestorationTimeToHealedOrNewEndpointsInMinutes(Integer minutes);

            /**
             * Specifies the session affinity state for this origin group.
             *
             * @param sessionAffinityState the session affinity state
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSessionAffinityState(EnabledState sessionAffinityState);
        }

        /**
         * The final stage of an origin group definition inside a profile update.
         *
         * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
         */
        interface Attachable<ParentT> {
            /**
             * Attaches the defined origin group to the parent CDN profile update.
             *
             * @return the next stage of the parent update
             */
            ParentT attach();
        }
    }

    /**
     * The entirety of an origin group update inside a {@link CdnProfile#update()} flow.
     */
    interface Update extends Settable<CdnProfile.Update> {
        /**
         * Specifies the load balancing settings for this origin group.
         *
         * @param loadBalancingSettings the load balancing settings
         * @return the next stage of the update
         */
        Update withLoadBalancingSettings(LoadBalancingSettingsParameters loadBalancingSettings);

        /**
         * Specifies the health probe settings for this origin group.
         *
         * @param healthProbeSettings the health probe settings
         * @return the next stage of the update
         */
        Update withHealthProbeSettings(HealthProbeParameters healthProbeSettings);

        /**
         * Specifies the time in minutes to shift traffic gradually to a healed or new endpoint.
         *
         * @param minutes the traffic restoration time in minutes
         * @return the next stage of the update
         */
        Update withTrafficRestorationTimeToHealedOrNewEndpointsInMinutes(Integer minutes);

        /**
         * Specifies the session affinity state for this origin group.
         *
         * @param sessionAffinityState the session affinity state
         * @return the next stage of the update
         */
        Update withSessionAffinityState(EnabledState sessionAffinityState);

        /**
         * Starts the definition of a new origin to be attached to this origin group.
         *
         * @param name a new origin name
         * @return the first stage of a new origin definition
         */
        Origin.UpdateDefinitionStages.Blank<Update> defineOrigin(String name);

        /**
         * Begins the update of an existing origin in this origin group.
         *
         * @param name the name of an existing origin
         * @return the first stage of the origin update
         */
        Origin.Update updateOrigin(String name);

        /**
         * Removes an origin from this origin group.
         *
         * @param name the name of an existing origin
         * @return the next stage of the origin group update
         */
        Update withoutOrigin(String name);
    }
}
