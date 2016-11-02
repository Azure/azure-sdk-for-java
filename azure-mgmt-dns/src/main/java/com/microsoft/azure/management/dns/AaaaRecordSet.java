package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.resources.fluentcore.arm.models.HasTags;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import java.util.List;

/**
 * An immutable client-side representation of a Aaaa (Ipv6) record set in Azure Dns Zone.
 */
public interface AaaaRecordSet extends DnsRecordSet<AaaaRecordSet, DnsZone> {
    /**
     * @return the IPv6 addresses of Aaaa records in this record set
     */
    List<String> ipv6Addresses();

    /**
     * The entirety of the Aaaa record set definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithIpv6Address,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Aaaa record set definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a Aaaa record set definition.
         */
        interface Blank extends WithIpv6Address {
        }

        /**
         * The stage of the Aaaa record set definition allowing to add a record.
         */
        interface WithIpv6Address {
            /**
             * Creates an Aaaa record with the provided Ipv6 address in this record set.
             *
             * @param ipv6Address the Ipv6 address
             * @return the next stage of the record set definition
             */
            WithCreate withIpv6Address(String ipv6Address);
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
                Creatable<AaaaRecordSet>,
                HasTags.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithIpv6Address,
                DefinitionStages.WithTtl {
        }
    }

    /**
     * Grouping of Aaaa record set update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the Aaaa record set update allowing to add or remove a record.
         */
        interface WithIpv6Address {
            /**
             * Creates an Aaaa record with the provided Ipv6 address in this record set.
             *
             * @param ipv6Address the Ipv6 address
             * @return the next stage of the record set update
             */
            Update withIpv6Address(String ipv6Address);

            /**
             * Removes an Aaaa record with the provided Ipv6 address from this record set.
             *
             * @param ipv6Address the Ipv6 address
             * @return the next stage of the record set update
             */
            Update withoutIpv6Address(String ipv6Address);
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
            Appliable<AaaaRecordSet>,
            HasTags.UpdateWithTags<Update>,
            UpdateStages.WithIpv6Address,
            UpdateStages.WithTtl {
    }
}
