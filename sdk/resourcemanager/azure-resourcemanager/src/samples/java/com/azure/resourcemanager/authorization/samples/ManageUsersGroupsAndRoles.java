// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.RoleDefinition;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.SampleUtils;

import java.time.Duration;

/**
 * Azure Users, Groups and Roles sample.
 *  - Create a user
 *  - Assign a role to the user, then revoke it
 *  - Get a role definition by scope and role name
 *  - Create a service principal and assign a role to it
 *  - Create two Active Directory groups
 *  - Add the user, the service principal and the first group as members of the second group
 */
public final class ManageUsersGroupsAndRoles {
    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of AzureResourceManager
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String subscriptionId = azureResourceManager.subscriptionId();
        final String userEmail = SampleUtils.randomResourceName(azureResourceManager, "test", 15);
        final String userName = userEmail.replace("test", "Test ");
        final String spName = SampleUtils.randomResourceName(azureResourceManager, "sp", 15);
        final String raName1 = SampleUtils.randomUuid(azureResourceManager);
        final String raName2 = SampleUtils.randomUuid(azureResourceManager);
        final String groupEmail1 = SampleUtils.randomResourceName(azureResourceManager, "group1", 15);
        final String groupEmail2 = SampleUtils.randomResourceName(azureResourceManager, "group2", 15);
        final String groupName1 = groupEmail1.replace("group1", "Group ");
        final String groupName2 = groupEmail2.replace("group2", "Group ");
        ActiveDirectoryUser user = null;
        ServicePrincipal sp = null;
        RoleAssignment spRoleAssignment = null;
        ActiveDirectoryGroup group1 = null;
        ActiveDirectoryGroup group2 = null;
        try {
            // Create an AD user.
            user = azureResourceManager.accessManagement()
                .activeDirectoryUsers()
                .define(userName)
                .withEmailAlias(userEmail)
                .withPassword(SampleUtils.password())
                .create();

            // Assign the Reader role to the user, then revoke it.
            RoleAssignment roleAssignment1 = azureResourceManager.accessManagement()
                .roleAssignments()
                .define(raName1)
                .forUser(user)
                .withBuiltInRole(BuiltInRole.READER)
                .withSubscriptionScope(subscriptionId)
                .create();
            azureResourceManager.accessManagement().roleAssignments().deleteById(roleAssignment1.id());

            // Get a role definition by scope and role name.
            RoleDefinition roleDefinition = azureResourceManager.accessManagement()
                .roleDefinitions()
                .getByScopeAndRoleName("subscriptions/" + subscriptionId, "Contributor");
            System.out.println("Contributor role definition id: " + roleDefinition.id());

            // Create a service principal and assign the Contributor role to it.
            sp = azureResourceManager.accessManagement()
                .servicePrincipals()
                .define(spName)
                .withNewApplication()
                .create();
            // Wait for the service principal to propagate before assigning a role.
            ResourceManagerUtils.sleep(Duration.ofSeconds(15));

            spRoleAssignment = azureResourceManager.accessManagement()
                .roleAssignments()
                .define(raName2)
                .forServicePrincipal(sp)
                .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                .withSubscriptionScope(subscriptionId)
                .create();

            // Create two Active Directory groups.
            group1 = azureResourceManager.accessManagement()
                .activeDirectoryGroups()
                .define(groupName1)
                .withEmailAlias(groupEmail1)
                .create();
            group2 = azureResourceManager.accessManagement()
                .activeDirectoryGroups()
                .define(groupName2)
                .withEmailAlias(groupEmail2)
                .create();

            // Add the user, the service principal and the first group as members of the second group.
            group2.update().withMember(user).withMember(sp).withMember(group1).apply();

            System.out.println("Added user, service principal and group " + groupName1 + " to group " + groupName2);
            return true;
        } finally {
            if (group2 != null) {
                azureResourceManager.accessManagement().activeDirectoryGroups().deleteById(group2.id());
            }
            if (group1 != null) {
                azureResourceManager.accessManagement().activeDirectoryGroups().deleteById(group1.id());
            }
            if (spRoleAssignment != null) {
                azureResourceManager.accessManagement().roleAssignments().deleteById(spRoleAssignment.id());
            }
            if (sp != null) {
                // withNewApplication() created an AD application named after the service principal; delete that
                // application (which also deletes the service principal) so nothing is left orphaned. Guard the
                // lookup because getByName(...) can return null under eventual consistency or a name collision.
                ActiveDirectoryApplication application = azureResourceManager.accessManagement()
                    .activeDirectoryApplications()
                    .getByName(spName);
                if (application != null) {
                    azureResourceManager.accessManagement().activeDirectoryApplications().deleteById(application.id());
                }
            }
            if (user != null) {
                azureResourceManager.accessManagement().activeDirectoryUsers().deleteById(user.id());
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
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ManageUsersGroupsAndRoles() {
    }
}
