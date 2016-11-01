package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.dns.implementation.ZoneInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;


/**
 * An immutable client-side representation of an Azure DNS Zone.
 */
@Fluent
public interface DnsZone extends
        GroupableResource,
        Refreshable<DnsZone>,
        Wrapper<ZoneInner>,
        Updatable<DnsZone.Update> {
    /**
     * @return the maximum number of record sets that can be created in this zone.
     */
    long maxNumberOfRecordSets();

    /**
     * @return the current number of record sets in this zone.
     */
    long numberOfRecordSets();

    /**
     * @return name servers assigned for this zone.
     */
    List<String> nameServers();

    /**
     * @return entry point to manage record sets in this zone containing A (Ipv4 address) records
     */
    ARecordSets aRecordSets();

    /**
     * @return entry point to manage record sets in this zone containing AAAA (IPv6 address) records
     */
    AaaaRecordSets aaaaRecordSets();

    /**
     * @return entry point to manage record sets in this zone containing CName (canonical name) records
     */
    CnameRecordSets cnameRecordSets();

    /**
     * @return entry point to manage record sets in this zone containing Mx (mail exchange) records
     */
    MxRecordSets mxRecordSets();

    /**
     * @return entry point to manage record sets in this zone containing Ns (name server) records
     */
    NsRecordSets nsRecordSets();

    /**
     * @return entry point to manage record sets in this zone containing Ptr (pointer) records
     */
    PtrRecordSets ptrRecordSets();

    /**
     * @return entry point to manage record sets in this zone containing Srv (service) records
     */
    SrvRecordSets srvRecordSets();

    /**
     * @return entry point to manage record sets in this zone containing Txt (text) records
     */
    TxtRecordSets txtRecordSets();

    /**
     * @return entry point to manage record sets in this zone containing Soa (start of authority) records
     */
    SoaRecordSets soaRecordSets();

    /**
     * The entirety of the Dns zone definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Dns zone definition stages.
     */
    interface DefinitionStages {
        /**
         * The stage of the Dns zone definition allowing to specify the resource group.
         */
        interface Blank extends GroupableResource.DefinitionStages.WithGroupAndRegion<WithCreate> {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<DnsZone>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of Dns zone update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for an update operation, containing all the settings that can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<DnsZone>,
            Resource.UpdateWithTags<Update> {
    }
}
