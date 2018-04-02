/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal;

/**
 * Used internally. Contains string constants to work with the paths in the Azure Cosmos DB database service.
 */
public class Paths {
    static final String ROOT = "/";

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
    public static final String PARTITION_KEY_RANGE_PATH_SEGMENT = "pkranges";
}
