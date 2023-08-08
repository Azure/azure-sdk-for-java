// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.common.policy.RequestRetryOptions;

import java.time.Duration;


/**
 * This example shows how to approximate the LocationMode behavior from the track 1 SDK. It is a general translation to
 * achieve roughly the same results, but it is not an identical implementation. It may be modified to suit the use case.
 * For more information on redundant storage, see here:
 * https://docs.microsoft.com/en-us/azure/storage/common/storage-redundancy
 *
 * In a sense, the track 2 SDK is always primary-only or primary-then-secondary. However, by passing the secondary
 * endpoint as the primary and the primary as the secondary, the behavior of secondary-only or secondary-then-primary
 * can be achieved. To avoid confusion of terms in this example, 'preferred' and 'fallback' will refer to the location
 * that is tried by the client first and then second respectively, whereas 'primary' and 'secondary' will refer to the
 * Storage concept of primary read-write storage and back-up/redundant/read-only storage respectively.
 *
 * The general pattern is to create a BlobClient and pass the preferred location to the builder as the endpoint. To
 * configure a fallback location, set it as the `secondaryEndpoint` on {@link RequestRetryOptions} and pass the
 * configured options to {@link BlobClientBuilder#retryOptions(RequestRetryOptions)}. Switching LocationMode requires
 * using a different client that is configured for the new request behavior. In this case, concurrency control should
 * be carefully considered to prevent race conditions.
 *
 * Requests will always go first to the preferred location passed as the endpoint. If a request must be retried, it will
 * and the error indicates it may be helped by checking the fallback, a request will immediately be reissued to the
 * fallback. If that also fails and still a retry may be helpful, the client will wait for a backoff period specified by
 * the retry options before retrying the initial location again.
 *
 * The client does not internally track the LocationMode or read it from an object that is passed because of how that
 * might cause race conditions if it is shared between clients.
 *
 * Each of the clients constructed in this sample will have behavior according to the variable name. This sample does
 * not demonstrate meaningful independent behavior, so running it will do nothing, but these clients can be copied and
 * used as a component in other code.
 *
 * This example can be combined with the StorageEventExample to approximate the Circuit Breaker RAGRS sample here:
 * https://github.com/Azure-Samples/storage-dotnet-circuit-breaker-ha-ra-grs/blob/master/storage-dotnet-circuit-breaker-ha-ra-grs/Program.cs
 * In this case, in the StorageEvent callback, rather than switching the LocationMode on the DefaultRequestOptions, the
 * client should be swapped out to the client with the appropriate LocationMode and the request reissued, alternating
 * between a primary only and secondary only client.
 *
 * The main areas of divergence from the original LocationMode behavior are:
 * - There is no LocationMode type
 * - The v12 analogue of LocationMode is configured at client build time and is static for a given client; a new client
 * must be used if different location behavior is desired.
 * - Changing LocationMode entails changing the client being used to issue requests
 */
public class LocationModeExample {

    public static void main(String[] args) {
        String primaryEndpoint = "<primary-endpoint>";
        String secondaryEndpoint = "<secondary-endpoint>";

        BlobClient primaryOnlyClient;
        BlobClient secondaryOnlyClient;
        BlobClient primaryThenSecondaryClient;
        BlobClient secondaryThenPrimaryClient;

        BlobClientBuilder builder = new BlobClientBuilder()
            .containerName("<container-name>")
            .blobName("<blob-name>");

        /*
         This could be refactored into a helper methods, but it is written out explicitly here for clarity and ease of
         comparison.
         Null in all cases indicates accepting the default value.
         A distinct set of options must be created for each client to prevent overwriting the options held by another
         client.
         */
        // Create a primary only client by passing the primary endpoint as the preferred and passing no fallback.
        RequestRetryOptions primaryOnlyRetryOptions = new RequestRetryOptions(null, null, (Duration) null, null, null,
            null);
        primaryOnlyClient = builder
            .endpoint(primaryEndpoint)
            .retryOptions(primaryOnlyRetryOptions)
            .buildClient();

        // Create a secondary only client by passing the secondary as the preferred and passing no fallback.
        RequestRetryOptions secondaryOnlyRetryOptions = new RequestRetryOptions(null, null, (Duration) null, null, null,
            null);
        secondaryOnlyClient = builder
            .endpoint(secondaryEndpoint)
            .retryOptions(secondaryOnlyRetryOptions)
            .buildClient();

        // Create a primary then secondary by passing a primary as the preferred and secondary as a fallback.
        RequestRetryOptions primaryThenSecondaryRetryOptions = new RequestRetryOptions(null, null, (Duration) null,
            null, null, secondaryEndpoint);
        primaryThenSecondaryClient = builder
            .endpoint(primaryEndpoint)
            .retryOptions(primaryThenSecondaryRetryOptions)
            .buildClient();

        // Create a secondary then primary by passing a secondary as the preferred and a primary as a fallback.
        RequestRetryOptions secondaryThenPrimaryRetryOptions = new RequestRetryOptions(null, null, (Duration) null,
            null, null, primaryEndpoint);
        secondaryThenPrimaryClient = builder
            .endpoint(secondaryEndpoint)
            .retryOptions(secondaryThenPrimaryRetryOptions)
            .buildClient();
    }
}
