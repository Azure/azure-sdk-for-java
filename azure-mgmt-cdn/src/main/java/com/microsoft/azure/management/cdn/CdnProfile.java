/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.cdn.implementation.CdnManager;
import com.microsoft.azure.management.cdn.implementation.ProfileInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;
import java.util.Map;

/**
 * An immutable client-side representation of an Azure CDN profile.
 */
@Fluent
public interface CdnProfile extends
        GroupableResource<CdnManager>,
        Refreshable<CdnProfile>,
        Wrapper<ProfileInner>,
        Updatable<CdnProfile.Update> {

    /**
     * @return Sku.
     */
    Sku sku();

    /**
     * @return CDN profile state.
     */
    String resourceState();

    /**
     * @return endpoints in the CDN manager profile, indexed by the name
     */
    Map<String, CdnEndpoint> endpoints();

    /**
     * Generates a dynamic SSO URI used to sign in to the CDN supplemental portal used for advanced management tasks.
     *
     * @return URI used to login to third party web portal.
     */
    @Method
    String generateSsoUri();

    /**
     * Starts stopped CDN endpoint in current profile.
     *
     * @param endpointName name of the endpoint under the profile which is unique globally.
     */
    void startEndpoint(String endpointName);

    /**
     * Stops running CDN endpoint in the current profile.
     *
     * @param endpointName name of the endpoint under the profile which is unique globally.
     */
    void stopEndpoint(String endpointName);

    /**
     * Forcibly purges CDN endpoint content in current profile.
     *
     * @param endpointName name of the endpoint under the profile which is unique globally.
     * @param contentPaths the path to the content to be purged. Can describe a file path or a wild card directory.
     */
    void purgeEndpointContent(String endpointName, List<String> contentPaths);

    /**
     * Forcibly pre-loads CDN endpoint content in current profile. Available for Verizon Profiles.
     *
     * @param endpointName name of the endpoint under the profile which is unique globally.
     * @param contentPaths the path to the content to be loaded. Should describe a file path.
     */
    void loadEndpointContent(String endpointName, List<String> contentPaths);

    /**
     * Validates a custom domain mapping to ensure it maps to the correct CNAME in DNS in current profile.
     *
     * @param endpointName name of the endpoint under the profile which is unique globally.
     * @param hostName the host name of the custom domain. Must be a domain name.
     * @return CustomDomainValidationResult object if successful.
     */
    CustomDomainValidationResult validateEndpointCustomDomain(String endpointName, String hostName);

    /**
     * Checks the availability of a endpoint name without creating the CDN endpoint.
     *
     * @param name the endpoint resource name to validate.
     * @return the CheckNameAvailabilityResult object if successful.
     */
    CheckNameAvailabilityResult checkEndpointNameAvailability(String name);

    /**
     * Checks if current instance of CDN profile Sku is Premium Verizon.
     *
     * @return true if current instance of CDN Profile Sku is of Premium Verizon, false otherwise.
     */
    boolean isPremiumVerizon();

    /**************************************************************
     * Fluent interfaces to provision a CDN.
     **************************************************************/
    /**
     * The entirety of the CDN profile.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithSku,
            DefinitionStages.WithStandardCreate,
            DefinitionStages.WithPremiumVerizonCreate,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of CDN profile definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a CDN profile definition.
         */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /**
         * A Redis Cache definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }

        /**
         * A CDN profile definition allowing the sku to be set.
         */
        interface WithSku {
            /**
             * Specifies the Standard Akamai sku of the CDN profile.
             *
             * @return the next stage of CDN profile definition.
             */
            WithStandardCreate withStandardAkamaiSku();

            /**
             * Specifies the Standard Verizon sku of the CDN profile.
             *
             * @return the next stage of CDN profile definition.
             */
            WithStandardCreate withStandardVerizonSku();

            /**
             * Specifies the Premium Verizon sku of the CDN profile.
             *
             * @return the next stage of CDN profile definition.
             */
            WithPremiumVerizonCreate withPremiumVerizonSku();
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithStandardCreate extends WithCreate {
            /**
             * Adds new endpoint to current CDN profile.
             *
             * @param endpointOriginHostname the endpoint origin hostname.
             * @return the next stage of CDN profile definition.
             */
            WithStandardCreate withNewEndpoint(String endpointOriginHostname);

            /**
             * Specifies definition of an endpoint to be attached to the CDN profile.
             *
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.DefinitionStages.Blank.StandardEndpoint<WithStandardCreate> defineNewEndpoint();

            /**
             * Specifies definition of an endpoint to be attached to the CDN profile.
             *
             * @param name the name for the endpoint
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.DefinitionStages.Blank.StandardEndpoint<WithStandardCreate> defineNewEndpoint(String name);

            /**
             * Specifies definition of an endpoint to be attached to the CDN profile.
             *
             * @param name the name for the endpoint
             * @param endpointOriginHostname the endpoint origin hostname.
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.DefinitionStages.WithStandardAttach<WithStandardCreate> defineNewEndpoint(String name, String endpointOriginHostname);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithPremiumVerizonCreate extends WithCreate {
            /**
             * Adds new endpoint to current CDN profile.
             *
             * @param endpointOriginHostname the endpoint origin hostname.
             * @return the next stage of CDN profile definition.
             */
            WithPremiumVerizonCreate withNewPremiumEndpoint(String endpointOriginHostname);

            /**
             * Specifies definition of an endpoint to be attached to the CDN profile.
             *
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.DefinitionStages.Blank.PremiumEndpoint<WithPremiumVerizonCreate> defineNewPremiumEndpoint();

            /**
             * Specifies definition of an endpoint to be attached to the CDN profile.
             *
             * @param name the name for the endpoint
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.DefinitionStages.Blank.PremiumEndpoint<WithPremiumVerizonCreate> defineNewPremiumEndpoint(String name);

            /**
             * Specifies definition of an endpoint to be attached to the CDN profile.
             *
             * @param name the name for the endpoint
             * @param endpointOriginHostname the endpoint origin hostname.
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.DefinitionStages.WithPremiumAttach<WithPremiumVerizonCreate> defineNewPremiumEndpoint(String name, String endpointOriginHostname);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<CdnProfile>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of CDN manager update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the CDN profile update allowing to specify the endpoints
         * for the profile.
         */
        interface WithEndpoint {
            /**
             * Adds new endpoint to current CDN profile.
             *
             * @param endpointOriginHostname the endpoint origin hostname.
             * @return the next stage of CDN profile update.
             */
            Update withNewEndpoint(String endpointOriginHostname);


            /**
             * Specifies definition of an endpoint to be attached to the CDN profile.
             *
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.UpdateDefinitionStages.Blank.StandardEndpoint<Update> defineNewEndpoint();

            /**
             * Specifies definition of an endpoint to be attached to the CDN profile.
             *
             * @param name the name for the endpoint
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.UpdateDefinitionStages.Blank.StandardEndpoint<Update> defineNewEndpoint(String name);

            /**
             * Specifies definition of an endpoint to be attached to the CDN profile.
             *
             * @param name the name for the endpoint
             * @param endpointOriginHostname the endpoint origin hostname.
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.UpdateDefinitionStages.WithStandardAttach<Update> defineNewEndpoint(String name, String endpointOriginHostname);

            /**
             * Adds new endpoint to current Premium Verizon CDN profile.
             *
             * @param endpointOriginHostname the endpoint origin hostname.
             * @return the next stage of CDN profile update.
             */
            Update withNewPremiumEndpoint(String endpointOriginHostname);

            /**
             * Specifies definition of an endpoint to be attached to the current Premium Verizon CDN profile.
             *
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.UpdateDefinitionStages.Blank.PremiumEndpoint<Update> defineNewPremiumEndpoint();

            /**
             * Specifies definition of an endpoint to be attached to the current Premium Verizon CDN profile.
             *
             * @param name the name for the endpoint
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.UpdateDefinitionStages.Blank.PremiumEndpoint<Update> defineNewPremiumEndpoint(String name);

            /**
             * Specifies definition of an endpoint to be attached to the current Premium Verizon CDN profile.
             *
             * @param name the name for the endpoint
             * @param endpointOriginHostname the endpoint origin hostname.
             * @return the stage representing configuration for the endpoint
             */
            CdnEndpoint.UpdateDefinitionStages.WithPremiumAttach<Update> defineNewPremiumEndpoint(String name, String endpointOriginHostname);

            /**
             * Begins the description of an update of an existing endpoint in current profile.
             *
             * @param name the name of the endpoint
             * @return the stage representing updating configuration for the endpoint
             */
            CdnEndpoint.UpdateStandardEndpoint updateEndpoint(String name);

            /**
             * Begins the description of an update of an existing endpoint in current Premium Verizon profile.
             *
             * @param name the name of the endpoint
             * @return the stage representing updating configuration for the endpoint
             */
            CdnEndpoint.UpdatePremiumEndpoint updatePremiumEndpoint(String name);

            /**
             * Removes an endpoint in the profile.
             *
             * @param name the name of the endpoint
             * @return the next stage of the CDN profile update
             */
            Update withoutEndpoint(String name);
        }
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<CdnProfile>,
            UpdateStages.WithEndpoint,
            Resource.UpdateWithTags<Update> {
    }
}