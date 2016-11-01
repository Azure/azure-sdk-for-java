package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.resources.fluentcore.arm.models.HasTags;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import java.util.List;

/**
 * An immutable client-side representation of a A (Ipv4) record set in Azure Dns Zone.
 */
public interface ARecordSet extends DnsRecordSet<ARecordSet, DnsZone> {
    /**
     * @return the Ipv4 addresses of A records in this record set
     */
    List<String> ipv4Addresses();

    /**
     * The entirety of the A record set definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of A record set definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an A record set definition.
         */
        interface Blank extends WithIpv4Address {
        }

        /**
         * The stage of the A record set definition allowing to add a record.
         */
        interface WithIpv4Address {
            /**
             * Creates an A record with the provided Ipv4 address in this record set.
             *
             * @param ipv4Address the Ipv4 address
             * @return the next stage of the record set definition
             */
            WithCreate withIpv4Address(String ipv4Address);
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
                Creatable<ARecordSet>,
                HasTags.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithTtl {
        }
    }

    /**
     * Grouping of A record set update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the Aaaa record set update allowing to add or remove a record.
         */
        interface WithIpv4Address {
            /**
             * Creates an A record with the provided Ipv4 address in this record set.
             *
             * @param ipv4Address the Ipv4 address
             * @return the next stage of the record set update
             */
            Update withIpv4Address(String ipv4Address);

            /**
             * Removes an A record with the provided Ipv4 address from this record set.
             *
             * @param ipv4Address the Ipv4 address
             * @return the next stage of the record set update
             */
            Update withoutIpv4Address(String ipv4Address);
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
            Appliable<ARecordSet>,
            HasTags.UpdateWithTags<Update>,
            UpdateStages.WithIpv4Address,
            UpdateStages.WithTtl {
    }
}
