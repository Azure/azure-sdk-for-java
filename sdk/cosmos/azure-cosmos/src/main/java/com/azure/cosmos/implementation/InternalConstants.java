// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

class InternalConstants {

    static class ResourceKeys {
        static final String ATTACHMENTS = "Attachments";
        static final String CONFLICTS = "Conflicts";
        static final String DATABASES = "Databases";
        static final String DOCUMENTS = "Documents";
        static final String DOCUMENT_COLLECTIONS = "DocumentCollections";
        static final String OFFERS = "Offers";
        static final String PERMISSIONS = "Permissions";
        static final String PARTITION_KEY_RANGES = "PartitionKeyRanges";
        static final String TRIGGERS = "Triggers";
        static final String STOREDPROCEDURES = "StoredProcedures";
        static final String USERS = "Users";
        static final String USER_DEFINED_FUNCTIONS = "UserDefinedFunctions";
        static final String ADDRESSES = "Addresss";
        static final String CLIENT_ENCRYPTION_KEYS = "ClientEncryptionKeys";
    }

    static class StreamApi {
        static final int STREAM_LENGTH_EOF = -1;
    }
}
