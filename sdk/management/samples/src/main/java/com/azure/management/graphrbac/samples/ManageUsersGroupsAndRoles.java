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

/**
 * Azure Users, Groups and Roles sample.
 * - Create a user
 * - Assign role to AD user
 * - Revoke role from AD user
 * - Get role by scope and role name
 * - Create Service Principal
 * - Assign role to Service Principal
 * - Create 2 Active Directory groups
 * - Add the user, the service principal and the 1st group as members of the 2nd group
 */
public final class ManageUsersGroupsAndRoles {
    /**
     * Main function which runs the actual sample.
     *
     * @param authenticated instance of Authenticated
     * @param defaultSubscription default subscription id
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure.Authenticated authenticated, String defaultSubscription) {
        final String userEmail = Utils.createRandomName("test");
        final String userName = userEmail.replace("test", "Test ");
        final String spName = Utils.createRandomName("sp");
        final String raName1 = SdkContext.randomUuid();
        final String raName2 = SdkContext.randomUuid();
        final String groupEmail1 = Utils.createRandomName("group1");
        final String groupEmail2 = Utils.createRandomName("group2");
        final String groupName1 = groupEmail1.replace("group1", "Group ");
        final String groupName2 = groupEmail2.replace("group2", "Group ");
        String spId = "";
        try {
            // ============================================================
            // Create a user

            System.out.println("Creating an AD user " + userName + "...");

            ActiveDirectoryUser user = authenticated.activeDirectoryUsers()
                    .define(userName)
                    .withEmailAlias(userEmail)
                    .withPassword("StrongPass!12")
                    .create();

            System.out.println("Created AD user " + userName);
            Utils.print(user);

            // ============================================================
            // Assign role to AD user

            RoleAssignment roleAssignment1 = authenticated.roleAssignments()
                    .define(raName1)
                    .forUser(user)
                    .withBuiltInRole(BuiltInRole.READER)
                    .withSubscriptionScope(defaultSubscription)
                    .create();
            System.out.println("Created Role Assignment:");
            Utils.print(roleAssignment1);

            // ============================================================
            // Revoke role from AD user

            authenticated.roleAssignments().deleteById(roleAssignment1.id());
            System.out.println("Revoked Role Assignment: " + roleAssignment1.id());

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
            // wait till service principal created and propagated
            SdkContext.sleep(15000);
            System.out.println("Created Service Principal:");
            Utils.print(sp);
            spId = sp.id();

            // ============================================================
            // Assign role to Service Principal

            RoleAssignment roleAssignment2 = authenticated.roleAssignments()
                    .define(raName2)
                    .forServicePrincipal(sp)
                    .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                    .withSubscriptionScope(defaultSubscription)
                    .create();
            System.out.println("Created Role Assignment:");
            Utils.print(roleAssignment2);

            // ============================================================
            // Create Active Directory groups

            System.out.println("Creating Active Directory group " + groupName1 + "...");
            ActiveDirectoryGroup group1 = authenticated.activeDirectoryGroups()
                    .define(groupName1)
                    .withEmailAlias(groupEmail1)
                    .create();

            System.out.println("Created Active Directory group " + groupName1);
            Utils.print(group1);

            System.out.println("Creating Active Directory group " + groupName2 + "...");
            ActiveDirectoryGroup group2 = authenticated.activeDirectoryGroups()
                    .define(groupName2)
                    .withEmailAlias(groupEmail2)
                    .create();

            System.out.println("Created Active Directory group " + groupName2);
            Utils.print(group2);

            System.out.println("Adding group members to group " + groupName2 + "...");
            group2.update()
                    .withMember(user)
                    .withMember(sp)
                    .withMember(group1)
                    .apply();
            System.out.println("Group members added to group " + groupName2);
            Utils.print(group2);

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
