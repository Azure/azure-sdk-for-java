/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.graphrbac.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUser;
import com.microsoft.rest.LogLevel;

import java.io.File;

/**
 * Azure Service Principal sample
 */
public final class ManageUsers {
    /**
     * Main function which runs the actual sample.
     * @param authenticated instance of Authenticated
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure.Authenticated authenticated) {
        try {
            ActiveDirectoryUser user = authenticated.activeDirectoryUsers().getByName("admin@azuresdkteam.onmicrosoft.com");

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
        }
        return false;
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure.Authenticated authenticated = Azure.configure()
                    .withLogLevel(LogLevel.BODY)
                    .authenticate(credFile);

            runSample(authenticated);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageUsers() {
    }
}
