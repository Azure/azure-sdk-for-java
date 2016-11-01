package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.resources.fluentcore.arm.models.HasTags;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import java.util.List;

/**
 * An immutable client-side representation of a Txt (text) record set in Azure Dns Zone.
 */
public interface TxtRecordSet extends DnsRecordSet<TxtRecordSet, DnsZone> {
    /**
     * @return the Txt records in this record set
     */
    List<TxtRecord> records();

    /**
     * The entirety of the Txt record set definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithText,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Txt record set definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a Txt record set definition.
         */
        interface Blank extends WithText {
        }

        /**
         * The stage of the Txt record set definition allowing to add a record.
         */
        interface WithText {
            /**
             * Creates a Txt record with the given text in this record set.
             *
             * @param text the text value
             * @return the next stage of the record set definition
             */
            WithCreate withText(String text);
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
                Creatable<MxRecordSet>,
                HasTags.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithText,
                DefinitionStages.WithTtl {
        }
    }

    /**
     * Grouping of Txt record set update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the Txt record set definition allowing to add or remove a record.
         */
        interface WithText {
            /**
             * Creates a Txt record with the given text in this record set.
             *
             * @param text the text value
             * @return the next stage of the record set update
             */
            Update withText(String text);

            /**
             * Removes Txt record with the given text from this record set.
             *
             * @param text the text value
             * @return the next stage of the record set update
             */
            Update withoutText(String text);
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
            Appliable<MxRecordSet>,
            HasTags.UpdateWithTags<Update>,
            UpdateStages.WithText,
            UpdateStages.WithTtl {
    }
}
