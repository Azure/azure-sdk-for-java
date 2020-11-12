// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

/**
 * Used internally. Contains string constants to work with the paths in the Azure Cosmos DB database service.
 */
public class Paths {
    static final String ROOT = "/";
    static final char ROOT_CHAR = '/';
    static final char ESCAPE_CHAR = '\\';

    public static final String DATABASES_PATH_SEGMENT = "dbs";
    public static final String DATABASES_ROOT = ROOT + DATABASES_PATH_SEGMENT;

    public static final String USERS_PATH_SEGMENT = "users";
    public static final String PERMISSIONS_PATH_SEGMENT = "permissions";
    public static final String COLLECTIONS_PATH_SEGMENT = "colls";
    public static final String STORED_PROCEDURES_PATH_SEGMENT = "sprocs";
    public static final String TRIGGERS_PATH_SEGMENT = "triggers";
    public static final String USER_DEFINED_FUNCTIONS_PATH_SEGMENT = "udfs";
    public static final String CONFLICTS_PATH_SEGMENT = "conflicts";
    public static final String DOCUMENTS_PATH_SEGMENT = "docs";
    public static final String ATTACHMENTS_PATH_SEGMENT = "attachments";

    // /offers
    public static final String OFFERS_PATH_SEGMENT = "offers";
    public static final String OFFERS_ROOT = ROOT + OFFERS_PATH_SEGMENT + "/";

    public static final String ADDRESS_PATH_SEGMENT = "addresses";
    public static final String PARTITIONS_PATH_SEGMENT = "partitions";
    public static final String DATABASE_ACCOUNT_PATH_SEGMENT = "databaseaccount";
    public static final String TOPOLOGY_PATH_SEGMENT = "topology";
    public static final String MEDIA_PATH_SEGMENT = "media";
    public static final String MEDIA_ROOT = ROOT + MEDIA_PATH_SEGMENT;
    public static final String SCHEMAS_PATH_SEGMENT = "schemas";
    public static final String PARTITION_KEY_RANGES_PATH_SEGMENT = "pkranges";

    public static final String USER_DEFINED_TYPES_PATH_SEGMENT = "udts";

    public static final String RID_RANGE_PATH_SEGMENT = "ridranges";
}
