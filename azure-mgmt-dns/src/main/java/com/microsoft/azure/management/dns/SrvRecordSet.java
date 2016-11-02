package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.resources.fluentcore.arm.models.HasTags;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import java.util.List;

/**
 * An immutable client-side representation of a Srv (service) record set in Azure Dns Zone.
 */
public interface SrvRecordSet extends DnsRecordSet<SrvRecordSet, DnsZone> {
    /**
     * @return the Srv records in this record set
     */
    List<SrvRecord> records();

    /**
     * The entirety of the Srv record set definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithRecord,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Srv record set definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a Srv record set definition.
         */
        interface Blank extends WithRecord {
        }

        /**
         * The stage of the srv record set definition allowing to add a Srv record.
         */
        interface WithRecord {
            /**
             * Specifies a service record for a service.
             *
             * @param target the canonical name of the target host running the service
             * @param port the port on which the service is bounded
             * @param priority the priority of the target host, lower the value higher the priority
             * @param weight the relative weight (preference) of the records with the same priority, higher the value more the preference
             * @return the next stage of the record set definition
             */
            WithCreate withRecord(String target, int port, int priority, int weight);
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
                Creatable<SrvRecordSet>,
                HasTags.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithRecord,
                DefinitionStages.WithTtl {
        }
    }

    /**
     * Grouping of Srv record set update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the srv record set update allowing to add or remove a Srv record.
         */
        interface WithRecord {
            /**
             * Specifies a service record for a service.
             *
             * @param target the canonical name of the target host running the service
             * @param port the port on which the service is bounded
             * @param priority the priority of the target host, lower the value higher the priority
             * @param weight the relative weight (preference) of the records with the same priority, higher the value more the preference
             * @return the next stage of the record set update
             */
            Update withRecord(String target, int port, int priority, int weight);

            /**
             * Removes a service record.
             *
             * @param target the canonical name of the target host running the service
             * @param port the port on which the service is bounded
             * @param priority the priority of the record
             * @param weight the weight of the record
             * @return the next stage of the record set update
             */
            Update withoutRecord(String target, int port, int priority, int weight);
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
            Appliable<SrvRecordSet>,
            HasTags.UpdateWithTags<Update>,
            UpdateStages.WithRecord,
            UpdateStages.WithTtl {
    }
}
