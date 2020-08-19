// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.inner.PublicIpPrefixInner;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

import java.util.List;
import java.util.Set;

/**
 * Type representing PublicIpPrefix.
 */
public interface PublicIpPrefix extends
    GroupableResource<NetworkManager, PublicIpPrefixInner>,
    Refreshable<PublicIpPrefix>,
    Updatable<PublicIpPrefix.Update>,
    UpdatableWithTags<PublicIpPrefix> {
    /**
     * @return the ipPrefix value.
     */
    String ipPrefix();

    /**
     * @return the ipTags value.
     */
    List<IpTag> ipTags();

    /**
     * @return the loadBalancerFrontendIpConfiguration value.
     */
    SubResource loadBalancerFrontendIpConfiguration();

    /**
     * @return the prefixLength value.
     */
    Integer prefixLength();

    /**
     * @return the provisioningState value.
     */
    ProvisioningState provisioningState();

    /**
     * @return the publicIpAddresses value.
     */
    List<ReferencedPublicIpAddress> publicIpAddresses();

    /**
     * @return the publicIpAddressVersion value.
     */
    IpVersion publicIpAddressVersion();

    /**
     * @return the resourceGuid value.
     */
    String resourceGuid();

    /**
     * @return the sku value.
     */
    PublicIpPrefixSku sku();

    /**
     * @return the availability zones assigned to the public Ip prefix
     */
    Set<AvailabilityZoneId> availabilityZones();

    /**
     * The entirety of the PublicIpPrefix definition.
     */
    interface Definition extends DefinitionStages.Blank,
        DefinitionStages.WithGroup, DefinitionStages.WithCreate {
    }

    /**
     * Grouping of PublicIpPrefix definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a PublicIpPrefix definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the PublicIpPrefix definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage of the publicipprefix definition allowing to specify IpTags.
         */
        interface WithIpTags {
            /**
             * Specifies ipTags.
             *
             * @param ipTags The list of tags associated with the public Ip prefix
             * @return the next definition stage
             */
            WithCreate withIpTags(List<IpTag> ipTags);
        }

        /**
         * The stage of the publicipprefix definition allowing to specify PrefixLength.
         */
        interface WithPrefixLength {
            /**
             * Specifies prefixLength.
             *
             * @param prefixLength The Length of the Public Ip Prefix
             * @return the next definition stage
             */
            WithCreate withPrefixLength(Integer prefixLength);
        }

        /**
         * The stage of the publicipprefix definition allowing to specify PublicIpAddressVersion.
         */
        interface WithPublicIpAddressVersion {
            /**
             * Specifies publicIpAddressVersion.
             *
             * @param publicIpAddressVersion The public Ip address version. Possible values include: 'Ipv4', 'Ipv6'
             * @return the next definition stage
             */
            WithCreate withPublicIpAddressVersion(IpVersion publicIpAddressVersion);
        }

        /**
         * The stage of the publicipprefix definition allowing to specify Sku.
         */
        interface WithSku {
            /**
             * Specifies sku.
             *
             * @param sku The public Ip prefix SKU
             * @return the next definition stage
             */
            WithCreate withSku(PublicIpPrefixSku sku);
        }

        /**
         * The stage of the Ip public prefix definition allowing to specify availability zone.
         */
        interface WithAvailabilityZone {
            /**
             * Specifies the availability zone for the Ip address.
             *
             * @param zoneId the zone identifier.
             * @return the next stage of the definition
             */
            WithCreate withAvailabilityZone(AvailabilityZoneId zoneId);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<PublicIpPrefix>,
            Resource.DefinitionWithTags<WithCreate>,
            DefinitionStages.WithIpTags,
            DefinitionStages.WithPrefixLength,
            DefinitionStages.WithPublicIpAddressVersion,
            DefinitionStages.WithSku,
            DefinitionStages.WithAvailabilityZone {
        }
    }

    /**
     * The template for a PublicIpPrefix update operation, containing all the settings that can be modified.
     */
    interface Update extends Appliable<PublicIpPrefix>,
        Resource.UpdateWithTags<Update>,
        UpdateStages.WithIpTags {
    }

    /**
     * Grouping of PublicIpPrefix update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the publicipprefix update allowing to specify IpTags.
         */
        interface WithIpTags {
            /**
             * Specifies ipTags.
             *
             * @param ipTags The list of tags associated with the public Ip prefix
             * @return the next update stage
             */
            Update withIpTags(List<IpTag> ipTags);
        }


    }
}
