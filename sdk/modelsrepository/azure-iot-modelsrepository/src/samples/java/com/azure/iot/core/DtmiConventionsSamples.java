// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.core;

import com.azure.iot.modelsrepository.DtmiConventions;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Samples demonstrating how to use {@link DtmiConventions}.
 */
public class DtmiConventionsSamples {

    /**
     * Demonstrate how to use {@link DtmiConventions#isValidDtmi(String)}
     */
    public static void isValidDtmi() {
        // This snippet shows how to validate a given DTMI string is well-formed.

        // Returns True
        String validDtmi = "dtmi:com:example:Thermostat;1";
        System.out.println(String.format("Dtmi %s is a valid dtmi: %s", validDtmi, DtmiConventions.isValidDtmi(validDtmi)));

        // Returns False
        String invalidDtmi = "dtmi:com:example:Thermostat";
        System.out.println(String.format("Dtmi %s is a valid dtmi: %s", invalidDtmi, DtmiConventions.isValidDtmi(invalidDtmi)));
    }

    /**
     * Demonstrates how to use {@link DtmiConventions#getModelUri(String, URI, boolean)}
     */
    public static void getModelUri() {
        // This snippet shows obtaining a fully qualified path to a model file.

        // Local repository example:
        try {
            URI localRepositoryUri = new URI("file:///path/to/repository");
            String fullyQualifiedModelUri = DtmiConventions.getModelUri("dtmi:com:example:Thermostat;1", localRepositoryUri, false).toString();

            // Prints: file:///path/to/repository/dtmi/com/example/thermostat-1.json
            System.out.println(fullyQualifiedModelUri);

            // Remote repository example with expanded enabled.
            URI remoteRepositoryUri = new URI("https://contoso.com/models/");
            fullyQualifiedModelUri = DtmiConventions.getModelUri("dtmi:com:example:Thermostat;1", remoteRepositoryUri, true).toString();

            // Prints: https://constoso.com/models/dtmi/com/example/thermostat-1.expanded.json
            System.out.println(fullyQualifiedModelUri);
        } catch (URISyntaxException ex) {
            System.out.println("Invalid URI path has been used to instantiate the URI object. Exiting...");
            return;
        }
    }
}
