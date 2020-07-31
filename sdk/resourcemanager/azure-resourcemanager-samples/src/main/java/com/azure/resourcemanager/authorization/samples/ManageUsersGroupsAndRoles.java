// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.RoleDefinition;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.samples.Utils;

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
     * @param profile the profile works with sample
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure.Authenticated authenticated, AzureProfile profile) {
        final String userEmail = authenticated.sdkContext().randomResourceName("test", 15);
        final String userName = userEmail.replace("test", "Test ");
        final String spName = authenticated.sdkContext().randomResourceName("sp", 15);
        final String raName1 = authenticated.sdkContext().randomUuid();
        final String raName2 = authenticated.sdkContext().randomUuid();
        final String groupEmail1 = authenticated.sdkContext().randomResourceName("group1", 15);
        final String groupEmail2 = authenticated.sdkContext().randomResourceName("group2", 15);
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
                    .withSubscriptionScope(profile.subscriptionId())
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
                    .getByScopeAndRoleName("subscriptions/" + profile.subscriptionId(), "Contributor");
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
                    .withSubscriptionScope(profile.subscriptionId())
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
            } catch (Exception e) {
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
            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .build();

            Azure.Authenticated authenticated = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile);
            runSample(authenticated, profile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageUsersGroupsAndRoles() {
    }
}
