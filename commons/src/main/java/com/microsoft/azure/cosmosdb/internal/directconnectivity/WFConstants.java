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

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

class WFConstants {
    static final int DefaultFabricNameResolutionTimeoutInSeconds = 10;

    static class AzureNames {
        public static final String WorkerRoleName = "WAFab";
        public static final String NodeAddressEndpoint = "NodeAddress";
    }

    static class WireNames {
        public static final String NamedEndpoint = "App=";
        public static final String Namespace = "http://docs.core.windows.net";
        public static final String Request = "Request";
        public static final String Response = "Response";
        public static final String RequestAction = "http://docs.core.windows.net/DocDb/Invoke";
        public static final String ResponseAction = "http://docs.core.windows.net/DocDb/InvokeResponse";

        public static final String AddressingHeaderNamespace = "http://www.w3.org/2005/08/addressing";
        public static final String MessageIdHeaderName = "MessageID";
        public static final String RelatesToHeaderName = "RelatesTo";
    }

    static class BackendHeaders {
        public static final String ResourceId = "x-docdb-resource-id";
        public static final String OwnerId = "x-docdb-owner-id";
        public static final String ENTITY_ID = "x-docdb-entity-id";
        public static final String DatabaseEntityMaxCount = "x-ms-database-entity-max-count";
        public static final String DatabaseEntityCurrentCount = "x-ms-database-entity-current-count";
        public static final String CollectionEntityMaxCount = "x-ms-collection-entity-max-count";
        public static final String CollectionEntityCurrentCount = "x-ms-collection-entity-current-count";
        public static final String UserEntityMaxCount = "x-ms-user-entity-max-count";
        public static final String UserEntityCurrentCount = "x-ms-user-entity-current-count";
        public static final String PermissionEntityMaxCount = "x-ms-permission-entity-max-count";
        public static final String PermissionEntityCurrentCount = "x-ms-permission-entity-current-count";
        public static final String RootEntityMaxCount = "x-ms-root-entity-max-count";
        public static final String RootEntityCurrentCount = "x-ms-root-entity-current-count";
        public static final String ResourceSchemaName = "x-ms-resource-schema-name";
        public static final String LSN = "lsn";
        public static final String QuorumAckedLSN = "x-ms-quorum-acked-lsn";
        public static final String CurrentWriteQuorum = "x-ms-current-write-quorum";
        public static final String CurrentReplicaSetSize = "x-ms-current-replica-set-size";
        public static final String COLLECTION_PARTITION_INDEX = "collection-partition-index";
        public static final String COLLECTION_SERVICE_INDEX = "collection-service-index";
        public static final String Status = "Status";
        public static final String ActivityId = "ActivityId";
        public static final String IS_FANOUT_REQUEST = "x-ms-is-fanout-request";
        public static final String PRIMARY_MASTER_KEY = "x-ms-primary-master-key";
        public static final String SECONDARY_MASTER_KEY = "x-ms-secondary-master-key";
        public static final String PRIMARY_READONLY_KEY = "x-ms-primary-readonly-key";
        public static final String SECONDARY_READONLY_KEY = "x-ms-secondary-readonly-key";
        public static final String BIND_REPLICA_DIRECTIVE = "x-ms-bind-replica";
        public static final String DatabaseAccountId = "x-ms-database-account-id";
        public static final String RequestValidationFailure = "x-ms-request-validation-failure";
        public static final String SubStatus = "x-ms-substatus";
        public static final String PARTITION_KEY_RANGE_ID = "x-ms-documentdb-partitionkeyrangeid";
        public static final String BIND_MIN_EFFECTIVE_PARTITION_KEY = "x-ms-documentdb-bindmineffectivepartitionkey";
        public static final String BIND_MAX_EFFECTIVE_PARTITION_KEY = "x-ms-documentdb-bindmaxeffectivepartitionkey";
        public static final String BIND_PARTITION_KEY_RANGE_ID = "x-ms-documentdb-bindpartitionkeyrangeid";
        public static final String BIND_PARTITION_KEY_RANGE_RID_PREFIX = "x-ms-documentdb-bindpartitionkeyrangeridprefix";
        public static final String MINIMUM_ALLOWED_CLIENT_VERSION = "x-ms-documentdb-minimumallowedclientversion";
        public static final String PARTITION_COUNT = "x-ms-documentdb-partitioncount";
        public static final String COLLECTION_RID = "x-ms-documentdb-collection-rid";
        public static final String GlobalCommittedLSN = "x-ms-global-committed-lsn";
        public static final String NumberOfReadRegions = "x-ms-number-of-read-regions";
        public static final String ItemLSN = "x-ms-item-lsn";
    }
}
