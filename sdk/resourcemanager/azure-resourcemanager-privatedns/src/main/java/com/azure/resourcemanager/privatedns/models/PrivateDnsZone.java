// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.privatedns.PrivateDnsZoneManager;
import com.azure.resourcemanager.privatedns.fluent.inner.PrivateZoneInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/** An immutable client-side representation of an Azure Private DNS Zone. */
@Fluent
public interface PrivateDnsZone
    extends GroupableResource<PrivateDnsZoneManager, PrivateZoneInner>,
        Refreshable<PrivateDnsZone>,
        Updatable<PrivateDnsZone.Update> {

    /**
     * @return the ETag of the zone.
     */
    String etag();

    /**
     * @return the maximum number of record sets that can be created in this Private DNS zone.
     */
    long maxNumberOfRecordSets();

    /**
     * @return the numberOfRecordSets property: The current number of record sets in this Private DNS zone.
     */
    long numberOfRecordSets();

    /**
     * @return the maximum number of virtual networks that can be linked to this Private DNS zone.
     */
    long maxNumberOfVirtualNetworkLinks();

    /**
     * @return the current number of virtual networks that are linked to this Private DNS zone.
     */
    long numberOfVirtualNetworkLinks();

    /**
     * @return the maximum number of virtual networks that can be linked to this Private DNS zone
     * with registration enabled.
     */
    long maxNumberOfVirtualNetworkLinksWithRegistration();

    /**
     * @return the current number of virtual networks that are linked to this Private DNS zone
     * with registration enabled.
     */
    long numberOfVirtualNetworkLinksWithRegistration();

    /**
     * @return the provisioning state of the resource.
     */
    ProvisioningState provisioningState();

    /** @return the record sets in this zone. */
    PagedIterable<PrivateDnsRecordSet> listRecordSets();

    /** @return the record sets in this zone asynchronously. */
    PagedFlux<PrivateDnsRecordSet> listRecordSetsAsync();

    /**
     * Lists all the record sets in this zone with the given suffix.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @return the record sets
     */
    PagedIterable<PrivateDnsRecordSet> listRecordSets(String recordSetNameSuffix);

    /**
     * Lists all the record sets in this zone with the given suffix asynchronously.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @return the record sets
     */
    PagedFlux<PrivateDnsRecordSet> listRecordSetsAsync(String recordSetNameSuffix);

    /**
     * Lists all the record sets in this zone with each entries in each page limited to the given size.
     *
     * @param pageSize the maximum number of record sets in a page
     * @return the record sets
     */
    PagedIterable<PrivateDnsRecordSet> listRecordSets(int pageSize);

    /**
     * Lists all the record sets in this zone with each entries in each page limited to the given size asynchronously.
     *
     * @param pageSize the maximum number of record sets in a page
     * @return the record sets
     */
    PagedFlux<PrivateDnsRecordSet> listRecordSetsAsync(int pageSize);

    /**
     * Lists all the record sets in this zone with the given suffix, also limits the number of entries per page to the
     * given page size.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @param pageSize the maximum number of record sets in a page
     * @return the record sets
     */
    PagedIterable<PrivateDnsRecordSet> listRecordSets(String recordSetNameSuffix, int pageSize);

    /**
     * Lists all the record sets in this zone with the given suffix, also limits the number of entries per page to the
     * given page size asynchronously.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @param pageSize the maximum number of record sets in a page
     * @return the record sets
     */
    PagedFlux<PrivateDnsRecordSet> listRecordSetsAsync(String recordSetNameSuffix, int pageSize);

    /** @return entry point to manage record sets in this zone containing AAAA (IPv6 address) records */
    AaaaRecordSets aaaaRecordSets();

    /** @return entry point to manage record sets in this zone containing A (IPv4 address) records */
    ARecordSets aRecordSets();

    /** @return the CNAME (canonical name) record set */
    CnameRecordSets cnameRecordSets();

    /** @return entry point to manage record sets in this zone containing MX (mail exchange) records */
    MxRecordSets mxRecordSets();

    /** @return entry point to manage record sets in this zone containing PTR (pointer) records */
    PtrRecordSets ptrRecordSets();

    /** @return the record set containing SOA (start of authority) record associated with this DNS zone */
    SoaRecordSet getSoaRecordSet();

    /** @return entry point to manage record sets in this zone containing SRV (service) records */
    SrvRecordSets srvRecordSets();

    /** @return entry point to manage record sets in this zone containing TXT (text) records */
    TxtRecordSets txtRecordSets();

    /** @return entry point to manage virtual network links in this zone */
    VirtualNetworkLinks virtualNetworkLinks();

    /** The entirety of the private DNS zone definition. */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithCreate {
    }

    /** Grouping of private DNS zone definition stages. */
    interface DefinitionStages {
        /** The stage of the private DNS zone definition allowing to specify the resource group. */
        interface Blank extends GroupableResource.DefinitionStages.WithGroupAndRegion<WithCreate> {
        }

        /** The stage of the private DNS zone definition allowing to specify record set. */
        interface WithRecordSet {
            /**
             * Specifies definition of an AAAA record set.
             *
             * @param name name of the AAAA record set
             * @return the stage representing configuration for the AAAA record set
             */
            PrivateDnsRecordSet.DefinitionStages.AaaaRecordSetBlank<WithCreate> defineAaaaRecordSet(String name);

            /**
             * Specifies definition of an A record set.
             *
             * @param name name of the A record set
             * @return the stage representing configuration for the A record set
             */
            PrivateDnsRecordSet.DefinitionStages.ARecordSetBlank<WithCreate> defineARecordSet(String name);

            /**
             * Specifies definition of a CNAME record set.
             *
             * @param name name of the CNAME record set
             * @param alias the CNAME record alias
             * @return the next stage of DNS zone definition
             */
            WithCreate withCnameRecordSet(String name, String alias);

            /**
             * Specifies definition of a CNAME record set.
             *
             * @param name name of the CNAME record set
             * @return the next stage of DNS zone definition
             */
            PrivateDnsRecordSet.DefinitionStages.CNameRecordSetBlank<WithCreate> defineCnameRecordSet(String name);

            /**
             * Specifies definition of a MX record set.
             *
             * @param name name of the MX record set
             * @return the stage representing configuration for the MX record set
             */
            PrivateDnsRecordSet.DefinitionStages.MXRecordSetBlank<WithCreate> defineMxRecordSet(String name);

            /**
             * Specifies definition of a PTR record set.
             *
             * @param name name of the PTR record set
             * @return the stage representing configuration for the PTR record set
             */
            PrivateDnsRecordSet.DefinitionStages.PtrRecordSetBlank<WithCreate> definePtrRecordSet(String name);

            /**
             * Specifies definition of a SOA record set.
             *
             * @return the stage representing configuration for the SOA record set
             */
            PrivateDnsRecordSet.DefinitionStages.SoaRecordSetBlank<WithCreate> defineSoaRecordSet();

            /**
             * Specifies definition of a SRV record set.
             *
             * @param name the name of the SRV record set
             * @return the stage representing configuration for the SRV record set
             */
            PrivateDnsRecordSet.DefinitionStages.SrvRecordSetBlank<WithCreate> defineSrvRecordSet(String name);

            /**
             * Specifies definition of a TXT record set.
             *
             * @param name the name of the TXT record set
             * @return the stage representing configuration for the TXT record set
             */
            PrivateDnsRecordSet.DefinitionStages.TxtRecordSetBlank<WithCreate> defineTxtRecordSet(String name);
        }

        /** The stage of the private DNS zone definition allowing to specify virtual network link. */
        interface WithVirtualNetworkLink {
            /**
             * Specifies definition of a virtual network link.
             *
             * @param name the name of the virtual network link
             * @return the stage representing configuration for the virtual network link
             */
            VirtualNetworkLink.DefinitionStages.Blank<WithCreate> defineVirtualNetworkLink(String name);
        }

        /** The stage of the private DNS zone definition allowing to enable ETag validation. */
        interface WithETagCheck {
            /**
             * Set the If-None-Match header with * to prevent updating an existing private DNS zone.
             *
             * @return the next stage of the definition
             */
            WithCreate withETagCheck();
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<PrivateDnsZone>,
                DefinitionStages.WithRecordSet,
                DefinitionStages.WithVirtualNetworkLink,
                DefinitionStages.WithETagCheck,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /** Grouping of private DNS zone update stages. */
    interface UpdateStages {
        /** The stage of the private DNS zone update allowing to specify record set. */
        interface WithRecordSet {
            /**
             * Specifies definition of an AAAA record set to be attached to the private DNS zone.
             *
             * @param name name of the AAAA record set
             * @return the stage representing configuration for the AAAA record set
             */
            PrivateDnsRecordSet.UpdateDefinitionStages.AaaaRecordSetBlank<Update> defineAaaaRecordSet(String name);

            /**
             * Specifies definition of an A record set to be attached to the private DNS zone.
             *
             * @param name name of the A record set
             * @return the stage representing configuration for the A record set
             */
            PrivateDnsRecordSet.UpdateDefinitionStages.ARecordSetBlank<Update> defineARecordSet(String name);

            /**
             * Specifies definition of a CNAME record set to be attached to the private DNS zone.
             *
             * @param name name of the CNAME record set
             * @param alias the CNAME record alias
             * @return the next stage of DNS zone definition
             */
            Update withCnameRecordSet(String name, String alias);

            /**
             * Specifies definition of a CNAME record set.
             *
             * @param name name of the CNAME record set
             * @return the next stage of DNS zone definition
             */
            PrivateDnsRecordSet.UpdateDefinitionStages.CNameRecordSetBlank<Update> defineCnameRecordSet(String name);

            /**
             * Specifies definition of a MX record set to be attached to the private DNS zone.
             *
             * @param name name of the MX record set
             * @return the stage representing configuration for the MX record set
             */
            PrivateDnsRecordSet.UpdateDefinitionStages.MXRecordSetBlank<Update> defineMxRecordSet(String name);

            /**
             * Specifies definition of a PTR record set to be attached to the private DNS zone.
             *
             * @param name name of the PTR record set
             * @return the stage representing configuration for the PTR record set
             */
            PrivateDnsRecordSet.UpdateDefinitionStages.PtrRecordSetBlank<Update> definePtrRecordSet(String name);

            /**
             * Specifies definition of a SOA record set to be attached to the private DNS zone.
             *
             * @return the stage representing configuration for the SOA record set
             */
            PrivateDnsRecordSet.UpdateDefinitionStages.SoaRecordSetBlank<Update> defineSoaRecordSet();

            /**
             * Specifies definition of a SRV record set to be attached to the private DNS zone.
             *
             * @param name the name of the SRV record set
             * @return the stage representing configuration for the SRV record set
             */
            PrivateDnsRecordSet.UpdateDefinitionStages.SrvRecordSetBlank<Update> defineSrvRecordSet(String name);

            /**
             * Specifies definition of a TXT record set to be attached to the private DNS zone.
             *
             * @param name the name of the TXT record set
             * @return the stage representing configuration for the TXT record set
             */
            PrivateDnsRecordSet.UpdateDefinitionStages.TxtRecordSetBlank<Update> defineTxtRecordSet(String name);

            /**
             * Begins the description of an update of an existing AAAA record set in this DNS zone.
             *
             * @param name name of the AAAA record set
             * @return the stage representing configuration for the AAAA record set
             */
            PrivateDnsRecordSet.UpdateAaaaRecordSet updateAaaaRecordSet(String name);

            /**
             * Begins the description of an update of an existing A record set in this DNS zone.
             *
             * @param name name of the A record set
             * @return the stage representing configuration for the A record set
             */
            PrivateDnsRecordSet.UpdateARecordSet updateARecordSet(String name);

            /**
             * Specifies definition of a CNAME record set.
             *
             * @param name name of the CNAME record set
             * @return the stage representing configuration for the CNAME record set
             */
            PrivateDnsRecordSet.UpdateCNameRecordSet updateCnameRecordSet(String name);

            /**
             * Begins the description of an update of an existing MX record set in this DNS zone.
             *
             * @param name name of the MX record set
             * @return the stage representing configuration for the MX record set
             */
            PrivateDnsRecordSet.UpdateMXRecordSet updateMxRecordSet(String name);

            /**
             * Begins the description of an update of an existing PTR record set in this DNS zone.
             *
             * @param name name of the PTR record set
             * @return the stage representing configuration for the PTR record set
             */
            PrivateDnsRecordSet.UpdatePtrRecordSet updatePtrRecordSet(String name);

            /**
             * Begins the description of an update of the SOA record in this DNS zone.
             *
             * @return the stage representing configuration for the SOA record set
             */
            PrivateDnsRecordSet.UpdateSoaRecord updateSoaRecord();

            /**
             * Begins the description of an update of an existing SRV record set in this DNS zone.
             *
             * @param name the name of the SRV record set
             * @return the stage representing configuration for the SRV record set
             */
            PrivateDnsRecordSet.UpdateSrvRecordSet updateSrvRecordSet(String name);

            /**
             * Begins the description of an update of an existing TXT record set in this DNS zone.
             *
             * @param name the name of the TXT record set
             * @return the stage representing configuration for the TXT record set
             */
            PrivateDnsRecordSet.UpdateTxtRecordSet updateTxtRecordSet(String name);

            /**
             * Removes a AAAA record set in the private DNS zone.
             *
             * @param name name of the AAAA record set
             * @return the next stage of DNS zone update
             */
            Update withoutAaaaRecordSet(String name);

            /**
             * Removes a AAAA record set in the private DNS zone.
             *
             * @param name name of the AAAA record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutAaaaRecordSet(String name, String etagValue);

            /**
             * Removes a A record set in the private DNS zone.
             *
             * @param name name of the A record set
             * @return the next stage of DNS zone update
             */
            Update withoutARecordSet(String name);

            /**
             * Removes a A record set in the private DNS zone.
             *
             * @param name name of the A record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutARecordSet(String name, String etagValue);

            /**
             * Removes a CNAME record set in the private DNS zone.
             *
             * @param name name of the CNAME record set
             * @return the next stage of DNS zone update
             */
            Update withoutCNameRecordSet(String name);

            /**
             * Removes a CNAME record set in the private DNS zone.
             *
             * @param name name of the CNAME record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutCNameRecordSet(String name, String etagValue);

            /**
             * Removes a MX record set in the private DNS zone.
             *
             * @param name name of the MX record set
             * @return the next stage of DNS zone update
             */
            Update withoutMXRecordSet(String name);

            /**
             * Removes a MX record set in the private DNS zone.
             *
             * @param name name of the MX record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutMXRecordSet(String name, String etagValue);

            /**
             * Removes a PTR record set in the private DNS zone.
             *
             * @param name name of the PTR record set
             * @return the next stage of DNS zone update
             */
            Update withoutPtrRecordSet(String name);

            /**
             * Removes a PTR record set in the private DNS zone.
             *
             * @param name name of the PTR record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutPtrRecordSet(String name, String etagValue);

            /**
             * Removes a SRV record set in the private DNS zone.
             *
             * @param name name of the SRV record set
             * @return the next stage of DNS zone update
             */
            Update withoutSrvRecordSet(String name);

            /**
             * Removes a SRV record set in the private DNS zone.
             *
             * @param name name of the SRV record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutSrvRecordSet(String name, String etagValue);

            /**
             * Removes a TXT record set in the private DNS zone.
             *
             * @param name name of the TXT record set
             * @return the next stage of DNS zone update
             */
            Update withoutTxtRecordSet(String name);

            /**
             * Removes a TXT record set in the private DNS zone.
             *
             * @param name name of the TXT record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutTxtRecordSet(String name, String etagValue);
        }

        /** The stage of the private DNS zone update allowing to specify virtual network link. */
        interface WithVirtualNetworkLink {
            /**
             * Specifies definition of a virtual network link to be attached to the private DNS zone.
             *
             * @param name the name of the virtual network link
             * @return the stage representing configuration for the virtual network link
             */
            VirtualNetworkLink.UpdateDefinitionStages.Blank<Update> defineVirtualNetworkLink(String name);

            /**
             * Begins the description of an update of an existing virtual network link in this DNS zone.
             *
             * @param name the name of the virtual network link
             * @return the stage representing configuration for the virtual network link
             */
            VirtualNetworkLink.Update updateVirtualNetworkLink(String name);

            /**
             * Removes a virtual network link in the private DNS zone.
             *
             * @param name name of the virtual network link
             * @return the next stage of DNS zone update
             */
            Update withoutVirtualNetworkLink(String name);

            /**
             * Removes a virtual network link in the private DNS zone.
             *
             * @param name name of the virtual network link
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutVirtualNetworkLink(String name, String etagValue);
        }

        /** The stage of the private DNS zone update allowing to enable ETag validation. */
        interface WithETagCheck {
            /**
             * Set the If-Match header with the current etag value of the private DNS Zone.
             *
             * @return the next stage of the update
             */
            Update withETagCheck();

            /**
             * Set the If-Match header with the given etag value.
             *
             * @param etagValue the etag value
             * @return the next stage of the update
             */
            Update withETagCheck(String etagValue);
        }
    }

    /**
     * The template for an update operation, containing all the settings that can be modified.
     *
     * <p>Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update
        extends Appliable<PrivateDnsZone>,
            UpdateStages.WithRecordSet,
            UpdateStages.WithVirtualNetworkLink,
            UpdateStages.WithETagCheck,
            Resource.UpdateWithTags<Update> {
    }
}
