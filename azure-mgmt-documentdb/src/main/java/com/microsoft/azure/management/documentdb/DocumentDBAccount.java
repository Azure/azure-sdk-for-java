/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.documentdb;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.documentdb.implementation.DatabaseAccountListConnectionStringsResult;
import com.microsoft.azure.management.documentdb.implementation.DatabaseAccountListKeysResult;
import com.microsoft.azure.management.documentdb.implementation.DocumentDBManager;
import com.microsoft.azure.management.documentdb.implementation.DatabaseAccountInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import rx.Observable;

import java.util.List;

/**
 * An immutable client-side representation of an Azure document db.
 */
@Fluent
@Beta(SinceVersion.V1_1_0)
public interface DocumentDBAccount extends
    GroupableResource<DocumentDBManager, DatabaseAccountInner>,
    Refreshable<DocumentDBAccount>,
    Updatable<DocumentDBAccount.Update> {

    /**
     * @return indicates the type of database account
     */
    DatabaseAccountKind kind();

    /**
     * @return the connection endpoint for the DocumentDB database account
     */
    String documentEndpoint();

    /**
     * @return the offer type for the DocumentDB database account
     */
    DatabaseAccountOfferType databaseAccountOfferType();

    /**
     * @return specifies the set of IP addresses or IP address ranges in CIDR form.
     */
    String ipRangeFilter();

    /**
     * @return the consistency policy for the DocumentDB database account
     */
    ConsistencyPolicy consistencyPolicy();

    /**
     * @return the default consistency level for the DocumentDB database account
     */
    DefaultConsistencyLevel defaultConsistencyLevel();

    /**
     * @return an array that contains the writable georeplication locations enabled for the DocumentDB account
     */
    List<Location> writableReplications();

    /**
     * @return an array that contains the readable georeplication locations enabled for the DocumentDB account
     */
    List<Location> readableReplications();

    /**
     * @return the access keys for the specified Azure DocumentDB database account
     */
    DatabaseAccountListKeysResult listKeys();

    /**
     * @return the access keys for the specified Azure DocumentDB database account
     */
    Observable<DatabaseAccountListKeysResult> listKeysAsync();

    /**
     * @return the connection strings for the specified Azure DocumentDB database account
     */
    DatabaseAccountListConnectionStringsResult listConnectionStrings();

    /**
     * @return the connection strings for the specified Azure DocumentDB database account
     */
    Observable<DatabaseAccountListConnectionStringsResult> listConnectionStringsAsync();

    /**
     * @param keyKind the key kind
     */
    void regenerateKey(KeyKind keyKind);

    /**
     * @param keyKind the key kind
     * @return the ServiceResponse object if successful.
     */
    Observable<Void> regenerateKeyAsync(KeyKind keyKind);

    /**
     * Grouping of document db definition stages.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithKind,
            DefinitionStages.WithWriteReplication,
            DefinitionStages.WithReadReplication,
            DefinitionStages.WithCreate {

    }

    /**
     * Grouping of document db definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a document db definition.
         */
        interface Blank extends
                DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the document db definition allowing to specify the resource group.
         */
        interface WithGroup extends
                GroupableResource.DefinitionStages.WithGroup<WithKind> {
        }

        /**
         * The stage of the document db definition allowing to set the database account kind.
         */
        interface WithKind {
            /**
             * The database account kind for the DocumentDB account.
             * @return the next stage of the definition
             */
            WithConsistencyPolicy withKind(DatabaseAccountKind kind);
        }

        /**
         * The stage of the document db definition allowing to set the consistency policy.
         */
        interface WithConsistencyPolicy {
            /**
             * The eventual consistency policy for the DocumentDB account.
             * @return the next stage of the definition
             */
            WithWriteReplication withEventualConsistency();

            /**
             * The session consistency policy for the DocumentDB account.
             * @return the next stage of the definition
             */
            WithWriteReplication withSessionConsistency();

            /**
             * The bounded staleness consistency policy for the DocumentDB account.
             * @param maxStalenessPrefix the max staleness prefix
             * @param maxIntervalInSeconds the max interval in seconds
             * @return the next stage of the definition
             */
            WithWriteReplication withBoundedStalenessConsistency(int maxStalenessPrefix, int maxIntervalInSeconds);

            /**
             * The strong consistency policy for the DocumentDB account.
             * @return the next stage of the definition
             */
            WithCreate withStrongConsistency();
        }

        /**
         * The stage of the document db definition allowing to set the IP range filter.
         */
        interface WithIpRangeFilter {
            /**
             * DocumentDB Firewall Support: This value specifies the set of IP addresses or IP address ranges in CIDR
             * form to be included as the allowed list of client IPs for a given database account. IP addresses/ranges
             * must be comma separated and must not contain any spaces.
             * @param ipRangeFilter specifies the set of IP addresses or IP address ranges
             * @return the next stage of the definition
             */
            WithCreate withIpRangeFilter(String ipRangeFilter);
        }

        /**
         * The stage of the document db definition allowing the definition of a read location.
         */
        interface WithWriteReplication {
            /**
             * A georeplication location for the DocumentDB account.
             * @param region the region for the location
             * @return the next stage
             */
            WithCreate withWriteReplication(Region region);
        }

        /**
         * The stage of the document db definition allowing the definition of a write location.
         */
        interface WithReadReplication {
            /**
             * A georeplication location for the DocumentDB account.
             * @param region the region for the location
             * @return the next stage
             */
            WithCreate withReadReplication(Region region);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created, but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<DocumentDBAccount>,
                WithConsistencyPolicy,
                WithReadReplication,
                WithIpRangeFilter {
        }
    }

    /**
     * Grouping of document db update stages.
     */
    interface Update extends
            UpdateStages.WithReadLocations,
            UpdateStages.WithOptionals {
    }

    /**
     * Grouping of document db update stages.
     */
    interface UpdateStages {
        /**
         * Grouping of document db update stages.
         */
        interface WithOptionals extends
            Resource.UpdateWithTags<WithOptionals>,
            Appliable<DocumentDBAccount>,
            UpdateStages.WithConsistencyPolicy,
            UpdateStages.WithIpRangeFilter {
        }

        /**
         * The stage of the document db definition allowing the definition of a write location.
         */
        interface WithReadLocations
                extends Appliable<DocumentDBAccount> {
            /**
             * A georeplication location for the DocumentDB account.
             * @param region the region for the location
             * @return the next stage
             */
            WithReadLocations withReadReplication(Region region);

            /**
             * A georeplication location for the DocumentDB account.
             * @param region the region for the location
             * @return the next stage
             */
            WithReadLocations withoutReadReplication(Region region);
        }

        /**
         * The stage of the document db update allowing to set the consistency policy.
         */
        interface WithConsistencyPolicy {
            /**
             * The consistency policy for the DocumentDB account.
             * @return the next stage of the definition
             */
            WithOptionals withEventualConsistency();

            /**
             * The consistency policy for the DocumentDB account.
             * @return the next stage of the definition
             */
            WithOptionals withSessionConsistency();

            /**
             * The consistency policy for the DocumentDB account.
             * @param maxStalenessPrefix the max staleness prefix
             * @param maxIntervalInSeconds the max interval in seconds
             * @return the next stage of the definition
             */
            WithOptionals withBoundedStalenessConsistency(int maxStalenessPrefix, int maxIntervalInSeconds);

            /**
             * The consistency policy for the DocumentDB account.
             * @return the next stage of the definition
             */
            WithOptionals withStrongConsistency();
        }

        /**
         * The stage of the document db definition allowing to set the IP range filter.
         */
        interface WithIpRangeFilter {
            /**
             * DocumentDB Firewall Support: This value specifies the set of IP addresses or IP address ranges in CIDR
             * form to be included as the allowed list of client IPs for a given database account. IP addresses/ranges
             * must be comma separated and must not contain any spaces.
             * @param ipRangeFilter specifies the set of IP addresses or IP address ranges
             * @return the next stage of the definition
             */
            WithOptionals withIpRangeFilter(String ipRangeFilter);
        }
    }
}
