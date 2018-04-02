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

package com.microsoft.azure.cosmosdb.rx.internal;

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
    }

    static class StreamApi {
        static final int STREAM_LENGTH_EOF = -1;
    }
}
