/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.network.samples;

/**
 * Azure Network sample for managing user defined routes -
 *  - Create an user defined route for a front end subnet
 *  - Create an user defined route for a back end subnet
 *  - Enable IP forwarding
 *  See https://azure.microsoft.com/en-us/documentation/articles/virtual-networks-udr-overview/
 *  See https://azure.microsoft.com/en-us/documentation/articles/virtual-network-create-udr-arm-ps/.
 */

public final class ManageUserDefinedRoute {

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {

        try {

            // Create an user defined route for the front end subnet

            // Create an user defined route for the back end subnet

            // Enable IP forwarding

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    private ManageUserDefinedRoute() {

    }
}
