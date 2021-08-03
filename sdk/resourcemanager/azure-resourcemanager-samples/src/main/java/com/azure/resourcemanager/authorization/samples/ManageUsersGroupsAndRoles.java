// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.RoleDefinition;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;

import java.time.Duration;

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
     * @param azureResourceManager instance of AzureResourceManager
     * @param profile the profile works with sample
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, AzureProfile profile) {
        final String userEmail = Utils.randomResourceName(azureResourceManager, "test", 15);
        final String userName = userEmail.replace("test", "Test ");
        final String spName = Utils.randomResourceName(azureResourceManager, "sp", 15);
        final String raName1 = Utils.randomUuid(azureResourceManager);
        final String raName2 = Utils.randomUuid(azureResourceManager);
        final String groupEmail1 = Utils.randomResourceName(azureResourceManager, "group1", 15);
        final String groupEmail2 = Utils.randomResourceName(azureResourceManager, "group2", 15);
        final String groupName1 = groupEmail1.replace("group1", "Group ");
        final String groupName2 = groupEmail2.replace("group2", "Group ");
        String spId = "";
        try {
            // ============================================================
            // Create a user

            System.out.println("Creating an AD user " + userName + "...");

            ActiveDirectoryUser user = azureResourceManager.accessManagement().activeDirectoryUsers()
                    .define(userName)
                    .withEmailAlias(userEmail)
                    .withPassword(Utils.password())
                    .create();

            System.out.println("Created AD user " + userName);
            Utils.print(user);

            // ============================================================
            // Assign role to AD user

            RoleAssignment roleAssignment1 = azureResourceManager.accessManagement().roleAssignments()
                    .define(raName1)
                    .forUser(user)
                    .withBuiltInRole(BuiltInRole.READER)
                    .withSubscriptionScope(profile.getSubscriptionId())
                    .create();
            System.out.println("Created Role Assignment:");
            Utils.print(roleAssignment1);

            // ============================================================
            // Revoke role from AD user

            azureResourceManager.accessManagement().roleAssignments().deleteById(roleAssignment1.id());
            System.out.println("Revoked Role Assignment: " + roleAssignment1.id());

            // ============================================================
            // Get role by scope and role name

            RoleDefinition roleDefinition = azureResourceManager.accessManagement().roleDefinitions()
                    .getByScopeAndRoleName("subscriptions/" + profile.getSubscriptionId(), "Contributor");
            Utils.print(roleDefinition);

            // ============================================================
            // Create Service Principal

            ServicePrincipal sp = azureResourceManager.accessManagement().servicePrincipals().define(spName)
                    .withNewApplication()
                    .create();
            // wait till service principal created and propagated
            ResourceManagerUtils.sleep(Duration.ofSeconds(15));
            System.out.println("Created Service Principal:");
            Utils.print(sp);
            spId = sp.id();

            // ============================================================
            // Assign role to Service Principal

            RoleAssignment roleAssignment2 = azureResourceManager.accessManagement().roleAssignments()
                    .define(raName2)
                    .forServicePrincipal(sp)
                    .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                    .withSubscriptionScope(profile.getSubscriptionId())
                    .create();
            System.out.println("Created Role Assignment:");
            Utils.print(roleAssignment2);

            // ============================================================
            // Create Active Directory groups

            System.out.println("Creating Active Directory group " + groupName1 + "...");
            ActiveDirectoryGroup group1 = azureResourceManager.accessManagement().activeDirectoryGroups()
                    .define(groupName1)
                    .withEmailAlias(groupEmail1)
                    .create();

            System.out.println("Created Active Directory group " + groupName1);
            Utils.print(group1);

            System.out.println("Creating Active Directory group " + groupName2 + "...");
            ActiveDirectoryGroup group2 = azureResourceManager.accessManagement().activeDirectoryGroups()
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
        } finally {
            try {
                System.out.println("Deleting Service Principal: " + spName);
                azureResourceManager.accessManagement().servicePrincipals().deleteById(spId);
                System.out.println("Deleted Service Principal: " + spName);
            } catch (Exception e) {
                System.out.println("Did not create Service Principal in Azure. No clean up is necessary");
            }
        }
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
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager, profile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageUsersGroupsAndRoles() {
    }
}
