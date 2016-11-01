package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.resources.fluentcore.arm.models.HasTags;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;

/**
 * An immutable client-side representation of a Soa (start of authority) record set in Azure Dns Zone.
 */
public interface SoaRecordSet extends DnsRecordSet<SoaRecordSet, DnsZone> {
    /**
     * @return the Soa record in this record set
     */
    SoaRecord record();

    /**
     * Grouping of Soa record set update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the Soa record set definition allowing specify the email server.
         */
        interface WithEmailServer {
            /**
             * Specifies the email server associated with the Soa record.
             *
             * @param emailServerHostName the email server
             * @return the next stage of the record set update
             */
            Update withEmailServer(String emailServerHostName);
        }

        /**
         * The stage of the Soa record set definition allowing to specify the refresh time.
         */
        interface WithRefreshTime {
            /**
             * Specifies time in seconds that a secondary name server should wait before trying to contact the
             * the primary name server for a zone file update.
             *
             * @param refreshTimeInSeconds the refresh time in seconds
             * @return the next stage of the record set update
             */
            Update withRefreshTimeInSeconds(int refreshTimeInSeconds);
        }

        /**
         * The stage of the Soa record set definition allowing to specify the retry time.
         */
        interface WithRetryTime {
            /**
             * Specifies the time in seconds that a secondary name server should wait before trying to contact
             * the primary name server again after a failed attempt to check for a zone file update.
             *
             * @param refreshTimeInSeconds the retry time in seconds
             * @return the next stage of the record set update
             */
            Update withRetryTimeInSeconds(int refreshTimeInSeconds);
        }

        /**
         * The stage of the Soa record set definition allowing to specify the expire time.
         */
        interface WithExpireTime {
            /**
             * Specifies the time in seconds that a secondary name server will treat its cached zone file as valid
             * when the primary name server cannot be contacted.
             *
             * @param expireTimeInSeconds the expire time in seconds
             * @return the next stage of the record set update
             */
            Update withExpireTimeInSeconds(int expireTimeInSeconds);
        }

        /**
         * The stage of the Soa record set definition allowing to specify the Ttl for cached negative response.
         */
        interface WithNegativeCachingTtl {
            /**
             * Specifies the time in seconds that any name server or resolver should cache a negative response.
             *
             * @param negativeCachingTimeToLive the Ttl for cached negative response
             * @return the next stage of the record set update
             */
            Update withNegativeCachingTimeToLiveInSeconds(int negativeCachingTimeToLive);
        }

        /**
         * The stage of the Soa record set definition allowing to specify the serial number.
         */
        interface WithSerialNumber {
            /**
             * Specifies the serial number for the zone file.
             *
             * @param serialNumber the serial number
             * @return the next stage of the record set update
             */
            Update withSerialNumber(int serialNumber);
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
            Appliable<SoaRecordSet>,
            HasTags.UpdateWithTags<Update>,
            UpdateStages.WithEmailServer,
            UpdateStages.WithRefreshTime,
            UpdateStages.WithRetryTime,
            UpdateStages.WithExpireTime,
            UpdateStages.WithNegativeCachingTtl,
            UpdateStages.WithSerialNumber,
            UpdateStages.WithTtl {
    }
}
