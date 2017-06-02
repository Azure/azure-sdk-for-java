/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.graphrbac.samples;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUser;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.graphrbac.RoleAssignment;
import com.microsoft.azure.management.graphrbac.RoleDefinition;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.List;

/**
 * Azure Users, Groups and Roles sample
 */
public final class ManageUsersGroupsAndRoles {
    /**
     * Main function which runs the actual sample.
     *
     * @param authenticated instance of Authenticated
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure.Authenticated authenticated, String defaultSubscription) {
        final String spName = Utils.createRandomName("sp");
        final String raName = SdkContext.randomUuid();
        String spId = "";
        try {
            // ============================================================
            // Get user by email

            ActiveDirectoryUser user = authenticated.activeDirectoryUsers().getByName("admin@azuresdkteam.onmicrosoft.com");
            Utils.print(user);

            // ============================================================
            // Get role by scope and role name
            RoleDefinition roleDefinition = authenticated.roleDefinitions()
                    .getByScopeAndRoleName("subscriptions/" + defaultSubscription, "Contributor");
            Utils.print(roleDefinition);

            // ============================================================
            // Create Service Principal

            ServicePrincipal sp = authenticated.servicePrincipals().define(spName)
                    .withNewApplication("http://" + spName)
                    .create();
            // wait till service principal created
            SdkContext.sleep(10000);
            System.out.println("Created Service Principal:");
//            Utils.print(sp);
            spId = sp.id();

            // ============================================================
            // Assign role to Service Principal
            RoleAssignment roleAssignment = authenticated.roleAssignments()
                    .define(raName)
                    .forServicePrincipal(sp)
                    .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                    .withSubscriptionScope(defaultSubscription)
                    .create();
            System.out.println("Created Role Assignment:");
            Utils.print(roleAssignment);

            // ============================================================
            // List Active Directory groups
            List<ActiveDirectoryGroup> groups = authenticated.activeDirectoryGroups().list();
            for (ActiveDirectoryGroup group : groups) {
                Utils.print(group);
            }

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Service Principal: " + spName);
                authenticated.servicePrincipals().deleteById(spId);
                System.out.println("Deleted Service Principal: " + spName);
            }
            catch (Exception e) {
                System.out.println("Did not create Service Principal in Azure. No clean up is necessary");
            }
        }
        return false;
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
            ApplicationTokenCredentials credentials = ApplicationTokenCredentials.fromFile(credFile);
            Azure.Authenticated authenticated = Azure.configure()
                    .withLogLevel(LogLevel.BODY)
                    .authenticate(credentials);

            runSample(authenticated, credentials.defaultSubscriptionId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageUsersGroupsAndRoles() {
    }
}
