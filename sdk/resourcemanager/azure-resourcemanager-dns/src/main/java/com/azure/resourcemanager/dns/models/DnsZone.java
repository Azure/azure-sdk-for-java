// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.dns.DnsZoneManager;
import com.azure.resourcemanager.dns.fluent.inner.ZoneInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.List;

/** An immutable client-side representation of an Azure DNS Zone. */
@Fluent
public interface DnsZone
    extends GroupableResource<DnsZoneManager, ZoneInner>, Refreshable<DnsZone>, Updatable<DnsZone.Update> {
    /** @return the maximum number of record sets that can be created in this zone. */
    long maxNumberOfRecordSets();

    /** @return the current number of record sets in this zone. */
    long numberOfRecordSets();

    /** @return the etag associated with this zone. */
    String etag();

    /** @return the access type of this zone (Private or Public). */
    ZoneType accessType();

    /**
     * @return a list of references to virtual networks that register hostnames in this DNS zone for Private DNS zone.
     */
    List<String> registrationVirtualNetworkIds();

    /** @return a list of references to virtual networks that resolve records in this DNS zone for Private DNS zone. */
    List<String> resolutionVirtualNetworkIds();

    /** @return the record sets in this zone. */
    PagedIterable<DnsRecordSet> listRecordSets();

    /**
     * Lists all the record sets in this zone with the given suffix.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @return the record sets
     */
    PagedIterable<DnsRecordSet> listRecordSets(String recordSetNameSuffix);

    /**
     * Lists all the record sets in this zone with each entries in each page limited to the given size.
     *
     * @param pageSize the maximum number of record sets in a page
     * @return the record sets
     */
    PagedIterable<DnsRecordSet> listRecordSets(int pageSize);

    /**
     * Lists all the record sets in this zone with the given suffix, also limits the number of entries per page to the
     * given page size.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @param pageSize the maximum number of record sets in a page
     * @return the record sets
     */
    PagedIterable<DnsRecordSet> listRecordSets(String recordSetNameSuffix, int pageSize);

    /** @return name servers assigned for this zone. */
    List<String> nameServers();

    /** @return entry point to manage record sets in this zone containing A (IPv4 address) records */
    ARecordSets aRecordSets();

    /** @return entry point to manage record sets in this zone containing AAAA (IPv6 address) records */
    AaaaRecordSets aaaaRecordSets();

    /** @return entry point to manage record sets in this zone containing Caa (canonical name) records */
    CaaRecordSets caaRecordSets();

    /** @return entry point to manage record sets in this zone containing CNAME (canonical name) records */
    CNameRecordSets cNameRecordSets();

    /** @return entry point to manage record sets in this zone containing MX (mail exchange) records */
    MXRecordSets mxRecordSets();

    /** @return entry point to manage record sets in this zone containing NS (name server) records */
    NSRecordSets nsRecordSets();

    /** @return entry point to manage record sets in this zone containing PTR (pointer) records */
    PtrRecordSets ptrRecordSets();

    /** @return entry point to manage record sets in this zone containing SRV (service) records */
    SrvRecordSets srvRecordSets();

    /** @return entry point to manage record sets in this zone containing TXT (text) records */
    TxtRecordSets txtRecordSets();

    /** @return the record set containing SOA (start of authority) record associated with this DNS zone */
    SoaRecordSet getSoaRecordSet();

    /** The entirety of the DNS zone definition. */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithCreate {
    }

    /** Grouping of DNS zone definition stages. */
    interface DefinitionStages {
        /** The stage of the DNS zone definition allowing to specify the resource group. */
        interface Blank extends GroupableResource.DefinitionStages.WithGroupAndRegion<WithCreate> {
        }

        /** The stage of the DNS zone definition allowing to specify record set. */
        interface WithRecordSet {
            /**
             * Specifies definition of an A record set.
             *
             * @param name name of the A record set
             * @return the stage representing configuration for the A record set
             */
            DnsRecordSet.DefinitionStages.ARecordSetBlank<WithCreate> defineARecordSet(String name);

            /**
             * Specifies definition of an AAAA record set.
             *
             * @param name name of the AAAA record set
             * @return the stage representing configuration for the AAAA record set
             */
            DnsRecordSet.DefinitionStages.AaaaRecordSetBlank<WithCreate> defineAaaaRecordSet(String name);

            /**
             * Specifies definition of a Caa record set.
             *
             * @param name the name of the Caa record set
             * @return the stage representing configuration for the Caa record set
             */
            DnsRecordSet.DefinitionStages.CaaRecordSetBlank<WithCreate> defineCaaRecordSet(String name);

            /**
             * Specifies definition of a CNAME record set.
             *
             * @param name name of the CNAME record set
             * @param alias the CNAME record alias
             * @return the next stage of DNS zone definition
             */
            WithCreate withCNameRecordSet(String name, String alias);

            /**
             * Specifies definition of a CNAME record set.
             *
             * @param name name of the CNAME record set
             * @return the next stage of DNS zone definition
             */
            DnsRecordSet.DefinitionStages.CNameRecordSetBlank<WithCreate> defineCNameRecordSet(String name);

            /**
             * Specifies definition of a MX record set.
             *
             * @param name name of the MX record set
             * @return the stage representing configuration for the MX record set
             */
            DnsRecordSet.DefinitionStages.MXRecordSetBlank<WithCreate> defineMXRecordSet(String name);

            /**
             * Specifies definition of an NS record set.
             *
             * @param name name of the NS record set
             * @return the stage representing configuration for the NS record set
             */
            DnsRecordSet.DefinitionStages.NSRecordSetBlank<WithCreate> defineNSRecordSet(String name);

            /**
             * Specifies definition of a PTR record set.
             *
             * @param name name of the PTR record set
             * @return the stage representing configuration for the PTR record set
             */
            DnsRecordSet.DefinitionStages.PtrRecordSetBlank<WithCreate> definePtrRecordSet(String name);

            /**
             * Specifies definition of a SRV record set.
             *
             * @param name the name of the SRV record set
             * @return the stage representing configuration for the SRV record set
             */
            DnsRecordSet.DefinitionStages.SrvRecordSetBlank<WithCreate> defineSrvRecordSet(String name);

            /**
             * Specifies definition of a TXT record set.
             *
             * @param name the name of the TXT record set
             * @return the stage representing configuration for the TXT record set
             */
            DnsRecordSet.DefinitionStages.TxtRecordSetBlank<WithCreate> defineTxtRecordSet(String name);
        }

        /** The stage of the DNS zone definition allowing to enable ETag validation. */
        interface WithETagCheck {
            /**
             * Specifies that If-None-Match header needs to set to * to prevent updating an existing DNS zone.
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
            extends Creatable<DnsZone>,
                DefinitionStages.WithRecordSet,
                DefinitionStages.WithETagCheck,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /** Grouping of DNS zone update stages. */
    interface UpdateStages {
        /** The stage of the DNS zone update allowing to specify record set. */
        interface WithRecordSet {
            /**
             * Specifies definition of an A record set to be attached to the DNS zone.
             *
             * @param name name of the A record set
             * @return the stage representing configuration for the A record set
             */
            DnsRecordSet.UpdateDefinitionStages.ARecordSetBlank<Update> defineARecordSet(String name);

            /**
             * Specifies definition of an AAAA record set to be attached to the DNS zone.
             *
             * @param name name of the AAAA record set
             * @return the stage representing configuration for the AAAA record set
             */
            DnsRecordSet.UpdateDefinitionStages.AaaaRecordSetBlank<Update> defineAaaaRecordSet(String name);

            /**
             * Specifies definition of a Caa record set to be attached to the DNS zone.
             *
             * @param name the name of the Caa record set
             * @return the stage representing configuration for the Caa record set
             */
            DnsRecordSet.UpdateDefinitionStages.CaaRecordSetBlank<Update> defineCaaRecordSet(String name);

            /**
             * Specifies definition of a CNAME record set to be attached to the DNS zone.
             *
             * @param name name of the CNAME record set
             * @param alias the CNAME record alias
             * @return the next stage of DNS zone definition
             */
            Update withCNameRecordSet(String name, String alias);

            /**
             * Specifies definition of a CNAME record set.
             *
             * @param name name of the CNAME record set
             * @return the next stage of DNS zone definition
             */
            DnsRecordSet.UpdateDefinitionStages.CNameRecordSetBlank<Update> defineCNameRecordSet(String name);

            /**
             * Specifies definition of a MX record set to be attached to the DNS zone.
             *
             * @param name name of the MX record set
             * @return the stage representing configuration for the MX record set
             */
            DnsRecordSet.UpdateDefinitionStages.MXRecordSetBlank<Update> defineMXRecordSet(String name);

            /**
             * Specifies definition of an NS record set to be attached to the DNS zone.
             *
             * @param name name of the NS record set
             * @return the stage representing configuration for the NS record set
             */
            DnsRecordSet.UpdateDefinitionStages.NSRecordSetBlank<Update> defineNSRecordSet(String name);

            /**
             * Specifies definition of a PTR record set to be attached to the DNS zone.
             *
             * @param name name of the PTR record set
             * @return the stage representing configuration for the PTR record set
             */
            DnsRecordSet.UpdateDefinitionStages.PtrRecordSetBlank<Update> definePtrRecordSet(String name);

            /**
             * Specifies definition of a SRV record set to be attached to the DNS zone.
             *
             * @param name the name of the SRV record set
             * @return the stage representing configuration for the SRV record set
             */
            DnsRecordSet.UpdateDefinitionStages.SrvRecordSetBlank<Update> defineSrvRecordSet(String name);

            /**
             * Specifies definition of a TXT record set to be attached to the DNS zone.
             *
             * @param name the name of the TXT record set
             * @return the stage representing configuration for the TXT record set
             */
            DnsRecordSet.UpdateDefinitionStages.TxtRecordSetBlank<Update> defineTxtRecordSet(String name);

            /**
             * Begins the description of an update of an existing A record set in this DNS zone.
             *
             * @param name name of the A record set
             * @return the stage representing configuration for the A record set
             */
            DnsRecordSet.UpdateARecordSet updateARecordSet(String name);

            /**
             * Begins the description of an update of an existing AAAA record set in this DNS zone.
             *
             * @param name name of the AAAA record set
             * @return the stage representing configuration for the AAAA record set
             */
            DnsRecordSet.UpdateAaaaRecordSet updateAaaaRecordSet(String name);

            /**
             * Begins the description of an update of an existing Caa record set in this DNS zone.
             *
             * @param name the name of the Caa record set
             * @return the stage representing configuration for the Caa record set
             */
            DnsRecordSet.UpdateCaaRecordSet updateCaaRecordSet(String name);

            /**
             * Specifies definition of a CNAME record set.
             *
             * @param name name of the CNAME record set
             * @return the stage representing configuration for the CNAME record set
             */
            DnsRecordSet.UpdateCNameRecordSet updateCNameRecordSet(String name);

            /**
             * Begins the description of an update of an existing MX record set in this DNS zone.
             *
             * @param name name of the MX record set
             * @return the stage representing configuration for the MX record set
             */
            DnsRecordSet.UpdateMXRecordSet updateMXRecordSet(String name);

            /**
             * Begins the description of an update of an existing NS record set in this DNS zone.
             *
             * @param name name of the NS record set
             * @return the stage representing configuration for the NS record set
             */
            DnsRecordSet.UpdateNSRecordSet updateNSRecordSet(String name);

            /**
             * Begins the description of an update of an existing PTR record set in this DNS zone.
             *
             * @param name name of the PTR record set
             * @return the stage representing configuration for the PTR record set
             */
            DnsRecordSet.UpdatePtrRecordSet updatePtrRecordSet(String name);

            /**
             * Begins the description of an update of an existing SRV record set in this DNS zone.
             *
             * @param name the name of the SRV record set
             * @return the stage representing configuration for the SRV record set
             */
            DnsRecordSet.UpdateSrvRecordSet updateSrvRecordSet(String name);

            /**
             * Begins the description of an update of an existing TXT record set in this DNS zone.
             *
             * @param name the name of the TXT record set
             * @return the stage representing configuration for the TXT record set
             */
            DnsRecordSet.UpdateTxtRecordSet updateTxtRecordSet(String name);

            /**
             * Begins the description of an update of the SOA record in this DNS zone.
             *
             * @return the stage representing configuration for the TXT record set
             */
            DnsRecordSet.UpdateSoaRecord updateSoaRecord();

            /**
             * Removes a A record set in the DNS zone.
             *
             * @param name name of the A record set
             * @return the next stage of DNS zone update
             */
            Update withoutARecordSet(String name);

            /**
             * Removes a A record set in the DNS zone.
             *
             * @param name name of the A record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutARecordSet(String name, String etagValue);

            /**
             * Removes a AAAA record set in the DNS zone.
             *
             * @param name name of the AAAA record set
             * @return the next stage of DNS zone update
             */
            Update withoutAaaaRecordSet(String name);

            /**
             * Removes a AAAA record set in the DNS zone.
             *
             * @param name name of the AAAA record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutAaaaRecordSet(String name, String etagValue);

            /**
             * Removes a Caa record set in the DNS zone.
             *
             * @param name name of the Caa record set
             * @return the next stage of DNS zone update
             */
            Update withoutCaaRecordSet(String name);

            /**
             * Removes a Caa record set in the DNS zone.
             *
             * @param name name of the Caa record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutCaaRecordSet(String name, String etagValue);

            /**
             * Removes a CNAME record set in the DNS zone.
             *
             * @param name name of the CNAME record set
             * @return the next stage of DNS zone update
             */
            Update withoutCNameRecordSet(String name);

            /**
             * Removes a CNAME record set in the DNS zone.
             *
             * @param name name of the CNAME record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutCNameRecordSet(String name, String etagValue);

            /**
             * Removes a MX record set in the DNS zone.
             *
             * @param name name of the MX record set
             * @return the next stage of DNS zone update
             */
            Update withoutMXRecordSet(String name);

            /**
             * Removes a MX record set in the DNS zone.
             *
             * @param name name of the MX record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutMXRecordSet(String name, String etagValue);

            /**
             * Removes a NS record set in the DNS zone.
             *
             * @param name name of the NS record set
             * @return the next stage of DNS zone update
             */
            Update withoutNSRecordSet(String name);

            /**
             * Removes a NS record set in the DNS zone.
             *
             * @param name name of the NS record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutNSRecordSet(String name, String etagValue);

            /**
             * Removes a PTR record set in the DNS zone.
             *
             * @param name name of the PTR record set
             * @return the next stage of DNS zone update
             */
            Update withoutPtrRecordSet(String name);

            /**
             * Removes a PTR record set in the DNS zone.
             *
             * @param name name of the PTR record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutPtrRecordSet(String name, String etagValue);

            /**
             * Removes a SRV record set in the DNS zone.
             *
             * @param name name of the SRV record set
             * @return the next stage of DNS zone update
             */
            Update withoutSrvRecordSet(String name);

            /**
             * Removes a SRV record set in the DNS zone.
             *
             * @param name name of the SRV record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutSrvRecordSet(String name, String etagValue);

            /**
             * Removes a TXT record set in the DNS zone.
             *
             * @param name name of the TXT record set
             * @return the next stage of DNS zone update
             */
            Update withoutTxtRecordSet(String name);

            /**
             * Removes a TXT record set in the DNS zone.
             *
             * @param name name of the TXT record set
             * @param etagValue the etag to use for concurrent protection
             * @return the next stage of DNS zone update
             */
            Update withoutTxtRecordSet(String name, String etagValue);
        }

        /** The stage of the DNS zone update allowing to enable ETag validation. */
        interface WithETagCheck {
            /**
             * Specifies that If-Match header needs to set to the current etag value associated with the DNS Zone.
             *
             * @return the next stage of the update
             */
            Update withETagCheck();

            /**
             * Specifies that if-Match header needs to set to the given etag value.
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
        extends Appliable<DnsZone>,
            UpdateStages.WithRecordSet,
            UpdateStages.WithETagCheck,
            Resource.UpdateWithTags<Update> {
    }
}
