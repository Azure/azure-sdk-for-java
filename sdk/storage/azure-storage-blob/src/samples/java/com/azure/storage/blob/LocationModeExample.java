package com.azure.storage.blob;

import com.azure.storage.common.policy.RequestRetryOptions;

import java.time.Duration;

public class LocationModeExample {
    // LocationMode does not exist, but its behavior can be approximated.
    // Requires two clients: primaryFirst client and a secondaryFirst client
    // Can I show the basic and the circuit breaker example in one?
    // will have to swap out the client. Example does not demonstrate concurrency control, which should always be considered
    // when introducing mutable state into a distributed application, but that is unique to many scenarios and beyond
    // the scope of this example

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

        // This could be refactored into a method, but it is written out explicitly here for clarity and ease of comparison
        primaryOnlyClient = builder
            .endpoint(primaryEndpoint)
            .retryOptions(buildRetryOptions(null)) // technically this can be omitted as it produces purely default options, but it is included for clarity
            .buildClient();

        secondaryOnlyClient = builder
            .endpoint(secondaryEndpoint)
            .retryOptions(buildRetryOptions(null))
            .buildClient();

        primaryThenSecondaryClient = builder
            .endpoint(primaryEndpoint)
            .retryOptions(buildRetryOptions(secondaryEndpoint))
            .buildClient();

        secondaryThenPrimaryClient = builder
            .endpoint(secondaryEndpoint)
            .retryOptions(buildRetryOptions(primaryEndpoint))
            .buildClient();

        // Demonstrate building each of these clients
        // In a sense, the track 2 sdk is always primary or primary-then-secondary. However, by passing the secondary endpoint as the primary and vice versa
        // it can be made to be effectively secondary or secondary-then-primary

        // To do the circuit breaker thing, will have to have some BlobClient variable that gets switched out after so many retries
        // Have to switch out the client instead of the location mode as the location mode is immutable for a given client
    }

    private static RequestRetryOptions buildRetryOptions(String secondaryEndpoint) {
        // null will accept the default; these options may be further configured as appropriate.
        // A distinct set of options must be created for each client or else modification would overwrite them (reword this)
        return new RequestRetryOptions(null, null, (Duration)null, null, null, secondaryEndpoint);
    }
}
