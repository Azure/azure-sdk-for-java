/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.network.implementation.ApplicationGatewayInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * Entry point for application gateway management API in Azure.
 */
public interface ApplicationGateway extends
        GroupableResource,
        Refreshable<ApplicationGateway>,
        Wrapper<ApplicationGatewayInner>,
        Updatable<ApplicationGateway.Update> {

    // Getters

    /**
     * The entirety of the application gateway definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithSku,
            DefinitionStages.WithIpConfiguration,
            DefinitionStages.WithPublicIpAddresses,
            DefinitionStages.WithFrontendPort,
            DefinitionStages.WithBackendHttpSettings,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of application gateway definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a application gateway definition.
         */
        interface Blank
                extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the application gateway definition allowing to specify the resource group.
         */
        interface WithGroup
                extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithSku> {
        }

        /**
         * An application gateway definition allowing the sku to be set.
         */
        interface WithSku {
            /**
             * Specifies the sku of the application gateway.
             *
             * @param skuName the sku
             * @return the next stage of application gateway definition
             */
            WithSku withSku(ApplicationGatewaySkuName skuName);

            WithIpConfiguration withCapacity(Integer capacity);
        }

        /**
         * The stage of application gateway definition allowing to specify virtual network.
         */
        interface WithIpConfiguration {
            WithPublicIpAddresses withExistingNetwork(Network network);
        }

        /**
         * The stage of the application gateway definition allowing to add a public ip address to the application
         * gateway front end.
         */
        interface WithPublicIpAddresses {
            /**
             * Sets the provided set of public IP addresses as the front end for the application gateway, making it an Internet-facing application gateway.
             *
             * @param publicIpAddresses existing public IP addresses
             * @return the next stage of the resource definition
             */
            WithFrontendPort withExistingPublicIpAddresses(PublicIpAddress... publicIpAddresses);

            /**
             * Adds a new public IP address to the front end of the application gateway, using an automatically generated name and leaf DNS label
             * derived from the application gateway name, in the same resource group and region.
             *
             * @return the next stage of the definition
             */
            WithFrontendPort withNewPublicIpAddress();

            /**
             * Adds a new public IP address to the front end of the application gateway, using the specified DNS leaft label,
             * an automatically generated name derived from the DNS label, in the same resource group and region.
             *
             * @return the next stage of the definition
             */
            WithFrontendPort withNewPublicIpAddress(String dnsLeafLabel);

            /**
             * Adds a new public IP address to the front end of the application gateway, creating the public IP based on the provided {@link Creatable}
             * stage of a public IP endpoint's definition.
             *
             * @return the next stage of the definition
             */
            WithFrontendPort withNewPublicIpAddress(Creatable<PublicIpAddress> creatablePublicIpAddress);
        }

        /**
         * The stage of application gateway definition allowing to specify front end port.
         */
        interface WithFrontendPort {
            WithBackendHttpSettings withFrontendPort(Integer port);
        }

        /**
         * The stage of application gateway definition allowing to specify htp settings.
         */
        interface WithBackendHttpSettings {
            WithCreate withBackendHttpSettings(Integer port);
        }

        /**
         * The stage of the application gateway definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<ApplicationGateway>,
                DefinitionStages.WithSku,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of application gateway update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for an application gateway update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<ApplicationGateway>,
            Resource.UpdateWithTags<Update> {
    }
}


