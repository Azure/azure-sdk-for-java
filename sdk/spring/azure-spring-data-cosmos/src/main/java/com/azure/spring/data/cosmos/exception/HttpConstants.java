// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;


/**
 * Http Constants.
 */
final class HttpConstants {

    /**
     * Cosmos Exception Status Codes.
     */
    static final class CosmosExceptionStatusCodes {
        /**
         * Default constructor.
         */
        private CosmosExceptionStatusCodes() {
        }
        /**
         * Retry With Status Code.
         */
        static final int RETRY_WITH = 449;
    }

    /**
     * Cosmos Exception Sub Status Codes.
     */
    static final class CosmosExceptionSubStatusCodes {
        /**
         * Default constructor.
         */
        private CosmosExceptionSubStatusCodes() {
        }
        // For 410 GONE
        /**
         * Name Cache Is Stale Sub Status Code.
         */
        static final int NAME_CACHE_IS_STALE = 1000;
        /**
         * Partition Key Range Gone Sub Status Code.
         */
        static final int PARTITION_KEY_RANGE_GONE = 1002;
        /**
         * Completing Split or Merge Sub Status Code.
         */
        static final int COMPLETING_SPLIT_OR_MERGE = 1007;
        /**
         * Completing Partition Migration Sub Status Code.
         */
        static final int COMPLETING_PARTITION_MIGRATION = 1008;

        // For 408 REQUEST_TIMEOUT
        /**
         * Client Operation Timeout Sub Status Code.
         */
        static final int CLIENT_OPERATION_TIMEOUT = 20008;
    }

    /**
     * Default constructor.
     */
    private HttpConstants() {
    }

}
