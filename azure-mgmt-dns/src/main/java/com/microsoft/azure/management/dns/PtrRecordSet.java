package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.resources.fluentcore.arm.models.Taggable;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import java.util.List;

/**
 * An immutable client-side representation of a Ptr (pointer) record set in Azure Dns Zone.
 */
public interface PtrRecordSet extends DnsRecordSet<PtrRecordSet, DnsZone> {
    /**
     * @return the target domain names of Ptr records in this record set
     */
    List<String> targetDomainNames();

    /**
     * The entirety of the Ns record set definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithTargetDomain,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Ptr record set definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a Ptr record set definition.
         */
        interface Blank extends WithTargetDomain {
        }

        /**
         * The stage of the Ptr record set definition allowing to add a record.
         */
        interface WithTargetDomain {
            /**
             * Creates a Ptr record with the provided target domain in this record set.
             *
             * @param targetDomainName the name of the target domain
             * @return the next stage of the record set definition
             */
            WithCreate withTargetDomain(String targetDomainName);
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
                Creatable<PtrRecordSet>,
                Taggable.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithTargetDomain,
                DefinitionStages.WithTtl {
        }
    }

    /**
     * Grouping of Ptr record set update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the Ptr record set definition allowing to add or remove a record.
         */
        interface WithTargetDomain {
            /**
             * Creates a Ptr record with the provided target domain in this record set.
             *
             * @param targetDomainName the name of the target domain
             * @return the next stage of the record set update
             */
            Update withTargetDomain(String targetDomainName);

            /**
             * Removes a Ptr record with the provided target domain from this record set.
             *
             * @param targetDomainName the name of the target domain
             * @return the next stage of the record set update
             */
            Update withoutTargetDomain(String targetDomainName);
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
            Appliable<PtrRecordSet>,
            Taggable.UpdateWithTags<Update>,
            UpdateStages.WithTargetDomain,
            UpdateStages.WithTtl {
    }
}
