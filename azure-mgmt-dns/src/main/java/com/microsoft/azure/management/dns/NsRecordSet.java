package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.resources.fluentcore.arm.models.HasTags;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import java.util.List;

public interface NsRecordSet extends DnsRecordSet<NsRecordSet, DnsZone> {
    /**
     * @return the name server names of Ns (name server) records in this record set
     */
    List<String> nameServers();

    /**
     * The entirety of the Ns record set definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithNameServer,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Ns record set definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a Ns record set definition.
         */
        interface Blank extends WithNameServer {
        }

        /**
         * The stage of the Ns record set definition allowing to add a record.
         */
        interface WithNameServer {
            /**
             * Creates a Ns record with the provided name server in this record set.
             *
             * @param nameServerHostName the name server host name
             * @return the next stage of the record set definition
             */
            WithCreate withNameServer(String nameServerHostName);
        }

        /**
         * The stage of the record set definition allowing to specify Ttl for the records in this record set.
         */
        interface WithTtl {
            /**
             * Specifies the Ttl for the records in the record set.
             *
             * @param ttlInSeconds ttl in seconds
             * @return the next stage of the record set definition
             */
            WithCreate withTimeToLive(long ttlInSeconds);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<NsRecordSet>,
                HasTags.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithNameServer,
                DefinitionStages.WithTtl {
        }
    }

    /**
     * Grouping of Ns record set update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the Ns record set definition allowing to add or remove a record.
         */
        interface WithNameServer {
            /**
             * Creates a Ns record with the provided name server in this record set.
             *
             * @param nameServerHostName the name server host name
             * @return the next stage of the record set definition
             */
            Update withNameServer(String nameServerHostName);

            /**
             * Removes a Ns record with the provided name server from this record set.
             *
             * @param nameServerHostName the name server host name
             * @return the next stage of the record set definition
             */
            Update withoutNameServer(String nameServerHostName);
        }

        /**
         * The stage of the record set update allowing to specify Ttl for the records in this record set.
         */
        interface WithTtl {
            /**
             * Specifies the Ttl for the records in the record set.
             *
             * @param ttlInSeconds ttl in seconds
             * @return the next stage of the record set update
             */
            Update withTimeToLive(long ttlInSeconds);
        }
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<NsRecordSet>,
            HasTags.UpdateWithTags<Update>,
            UpdateStages.WithNameServer,
            UpdateStages.WithTtl {
    }
}
