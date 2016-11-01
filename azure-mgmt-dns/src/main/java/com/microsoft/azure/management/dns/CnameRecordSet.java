package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.resources.fluentcore.arm.models.HasTags;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import java.util.List;

/**
 * An immutable client-side representation of a CName record set in Azure Dns Zone.
 */
public interface CnameRecordSet extends DnsRecordSet<CnameRecordSet, DnsZone> {
    /**
     * @return the canonical names (without a terminating dot) of CNAME records in this record set
     */
    List<String> canonicalNames();

    /**
     * The entirety of the CName record set definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of CName record set definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a CName record set definition.
         */
        interface Blank {
            // TODO
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
                DefinitionStages.WithTtl {
        }
    }

    /**
     * Grouping of CName record set update stages.
     */
    interface UpdateStages {
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
            UpdateStages.WithTtl {
    }
}
