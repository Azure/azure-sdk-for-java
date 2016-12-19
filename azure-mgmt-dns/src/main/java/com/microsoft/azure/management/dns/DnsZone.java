/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
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
     * @return the record set containing Soa (start of authority) record associated with this Dns zone
     */
    SoaRecordSet getSoaRecordSet();

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
         * The stage of the Dns zone definition allowing to specify record set.
         */
        interface WithRecordSet {
            /**
             * Specifies definition of an A record set.
             *
             * @param name name of the A record set
             * @return the stage representing configuration for the A record set
             */
            DnsRecordSet.DefinitionStages.ARecordSetBlank<WithCreate> defineARecordSet(String name);

            /**
             * Specifies definition of an Aaaa record set.
             *
             * @param name name of the Aaaa record set
             * @return the stage representing configuration for the Aaaa record set
             */
            DnsRecordSet.DefinitionStages.AaaaRecordSetBlank<WithCreate> defineAaaaRecordSet(String name);

            /**
             * Specifies definition of a Cname record set.
             *
             * @param name name of the Cname record set
             * @param alias the Cname record alias
             * @return the next stage of Dns zone definition
             */
            WithCreate withCnameRecordSet(String name, String alias);

            /**
             * Specifies definition of a Mx record set.
             *
             * @param name name of the Mx record set
             * @return the stage representing configuration for the Mx record set
             */
            DnsRecordSet.DefinitionStages.MxRecordSetBlank<WithCreate> defineMxRecordSet(String name);

            /**
             * Specifies definition of an Ns record set.
             *
             * @param name name of the Ns record set
             * @return the stage representing configuration for the Ns record set
             */
            DnsRecordSet.DefinitionStages.NsRecordSetBlank<WithCreate> defineNsRecordSet(String name);

            /**
             * Specifies definition of a Ptr record set.
             *
             * @param name name of the Ptr record set
             * @return the stage representing configuration for the Ptr record set
             */
            DnsRecordSet.DefinitionStages.PtrRecordSetBlank<WithCreate> definePtrRecordSet(String name);

            /**
             * Specifies definition of a Srv record set.
             *
             * @param name the name of the Srv record set
             * @return the stage representing configuration for the Srv record set
             */
            DnsRecordSet.DefinitionStages.SrvRecordSetBlank<WithCreate> defineSrvRecordSet(String name);

            /**
             * Specifies definition of a Txt record set.
             *
             * @param name the name of the Txt record set
             * @return the stage representing configuration for the Txt record set
             */
            DnsRecordSet.DefinitionStages.TxtRecordSetBlank<WithCreate> defineTxtRecordSet(String name);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<DnsZone>,
                DefinitionStages.WithRecordSet,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of Dns zone update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the Dns zone update allowing to specify record set.
         */
        interface WithRecordSet {
            /**
             * Specifies definition of an A record set to be attached to the Dns zone.
             *
             * @param name name of the A record set
             * @return the stage representing configuration for the A record set
             */
            DnsRecordSet.UpdateDefinitionStages.ARecordSetBlank<Update> defineARecordSet(String name);

            /**
             * Specifies definition of an Aaaa record set to be attached to the Dns zone.
             *
             * @param name name of the Aaaa record set
             * @return the stage representing configuration for the Aaaa record set
             */
            DnsRecordSet.UpdateDefinitionStages.AaaaRecordSetBlank<Update> defineAaaaRecordSet(String name);

            /**
             * Specifies definition of a Cname record set to be attached to the Dns zone.
             *
             * @param name name of the Cname record set
             * @param alias the Cname record alias
             * @return the next stage of Dns zone definition
             */
            Update withCnameRecordSet(String name, String alias);

            /**
             * Specifies definition of a Mx record set to be attached to the Dns zone.
             *
             * @param name name of the Mx record set
             * @return the stage representing configuration for the Mx record set
             */
            DnsRecordSet.UpdateDefinitionStages.MxRecordSetBlank<Update> defineMxRecordSet(String name);

            /**
             * Specifies definition of an Ns record set to be attached to the Dns zone.
             *
             * @param name name of the Ns record set
             * @return the stage representing configuration for the Ns record set
             */
            DnsRecordSet.UpdateDefinitionStages.NsRecordSetBlank<Update> defineNsRecordSet(String name);

            /**
             * Specifies definition of a Ptr record set to be attached to the Dns zone.
             *
             * @param name name of the Ptr record set
             * @return the stage representing configuration for the Ptr record set
             */
            DnsRecordSet.UpdateDefinitionStages.PtrRecordSetBlank<Update> definePtrRecordSet(String name);

            /**
             * Specifies definition of a Srv record set to be attached to the Dns zone.
             *
             * @param name the name of the Srv record set
             * @return the stage representing configuration for the Srv record set
             */
            DnsRecordSet.UpdateDefinitionStages.SrvRecordSetBlank<Update> defineSrvRecordSet(String name);

            /**
             * Specifies definition of a Txt record set to be attached to the Dns zone.
             *
             * @param name the name of the Txt record set
             * @return the stage representing configuration for the Txt record set
             */
            DnsRecordSet.UpdateDefinitionStages.TxtRecordSetBlank<Update> defineTxtRecordSet(String name);

            /**
             * Begins the description of an update of an existing A record set in this Dns zone.
             *
             * @param name name of the A record set
             * @return the stage representing configuration for the A record set
             */
            DnsRecordSet.UpdateARecordSet updateARecordSet(String name);

            /**
             * Begins the description of an update of an existing Aaaa record set in this Dns zone.
             *
             * @param name name of the Aaaa record set
             * @return the stage representing configuration for the Aaaa record set
             */
            DnsRecordSet.UpdateAaaaRecordSet updateAaaaRecordSet(String name);

            /**
             * Begins the description of an update of an existing Mx record set in this Dns zone.
             *
             * @param name name of the Mx record set
             * @return the stage representing configuration for the Mx record set
             */
            DnsRecordSet.UpdateMxRecordSet updateMxRecordSet(String name);

            /**
             * Begins the description of an update of an existing Ns record set in this Dns zone.
             *
             * @param name name of the Ns record set
             * @return the stage representing configuration for the Ns record set
             */
            DnsRecordSet.UpdateNsRecordSet updateNsRecordSet(String name);

            /**
             * Begins the description of an update of an existing Ptr record set in this Dns zone.
             *
             * @param name name of the Ptr record set
             * @return the stage representing configuration for the Ptr record set
             */
            DnsRecordSet.UpdatePtrRecordSet updatePtrRecordSet(String name);

            /**
             * Begins the description of an update of an existing Srv record set in this Dns zone.
             *
             * @param name the name of the Srv record set
             * @return the stage representing configuration for the Srv record set
             */
            DnsRecordSet.UpdateSrvRecordSet updateSrvRecordSet(String name);

            /**
             * Begins the description of an update of an existing Txt record set in this Dns zone.
             *
             * @param name the name of the Txt record set
             * @return the stage representing configuration for the Txt record set
             */
            DnsRecordSet.UpdateTxtRecordSet updateTxtRecordSet(String name);

            /**
             * Begins the description of an update of the Soa record in this Dns zone.
             *
             * @return the stage representing configuration for the Txt record set
             */
            DnsRecordSet.UpdateSoaRecord updateSoaRecord();

            /**
             * Removes a A record set in the Dns zone.
             *
             * @param name name of the A record set
             * @return the next stage of Dns zone update
             */
            Update withoutARecordSet(String name);

            /**
             * Removes a Aaaa record set in the Dns zone.
             *
             * @param name name of the Aaaa record set
             * @return the next stage of Dns zone update
             */
            Update withoutAaaaRecordSet(String name);

            /**
             * Removes a Cname record set in the Dns zone.
             *
             * @param name name of the Cname record set
             * @return the next stage of Dns zone update
             */
            Update withoutCnameRecordSet(String name);

            /**
             * Removes a Mx record set in the Dns zone.
             *
             * @param name name of the Mx record set
             * @return the next stage of Dns zone update
             */
            Update withoutMxRecordSet(String name);

            /**
             * Removes a Ns record set in the Dns zone.
             *
             * @param name name of the Ns record set
             * @return the next stage of Dns zone update
             */
            Update withoutNsRecordSet(String name);

            /**
             * Removes a Ptr record set in the Dns zone.
             *
             * @param name name of the Ptr record set
             * @return the next stage of Dns zone update
             */
            Update withoutPtrRecordSet(String name);

            /**
             * Removes a Srv record set in the Dns zone.
             *
             * @param name name of the Srv record set
             * @return the next stage of Dns zone update
             */
            Update withoutSrvRecordSet(String name);

            /**
             * Removes a Txt record set in the Dns zone.
             *
             * @param name name of the Txt record set
             * @return the next stage of Dns zone update
             */
            Update withoutTxtRecordSet(String name);
        }
    }

    /**
     * The template for an update operation, containing all the settings that can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<DnsZone>,
            UpdateStages.WithRecordSet,
            Resource.UpdateWithTags<Update> {
    }
}
