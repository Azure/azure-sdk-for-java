// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization;

import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryObject;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

public class GroupsTests extends GraphRbacManagementTest {

    @Test
    public void canCRUDGroup() throws Exception {
        String userName = generateRandomResourceName("user", 16);
        String spName = generateRandomResourceName("sp", 16);
        String group1Name = generateRandomResourceName("group", 16);
        String group2Name = generateRandomResourceName("group", 16);
        ActiveDirectoryUser user = null;
        ServicePrincipal servicePrincipal = null;
        ActiveDirectoryGroup group1 = null;
        ActiveDirectoryGroup group2 = null;
        try {
            user =
                authorizationManager
                    .users()
                    .define(userName)
                    .withEmailAlias(userName)
                    .withPassword(password())
                    .create();
            servicePrincipal =
                authorizationManager.servicePrincipals().define(spName).withNewApplication().create();
            group1 = authorizationManager.groups().define(group1Name).withEmailAlias(group1Name).create();
            ResourceManagerUtils.sleep(Duration.ofSeconds(15));
            group2 =
                authorizationManager
                    .groups()
                    .define(group2Name)
                    .withEmailAlias(group2Name)
                    .withMember(user.id())
                    .withMember(servicePrincipal.id())
                    .withMember(group1.id())
                    .create();
            Assertions.assertNotNull(group2);
            Assertions.assertNotNull(group2.id());

            List<ActiveDirectoryObject> members = group2.listMembers();
            Assertions.assertEquals(2, members.size()); // Group list will not show service principal anymore.
            Iterator<ActiveDirectoryObject> iterator = members.iterator();
            Assertions.assertNotNull(iterator.next().id());
            Assertions.assertNotNull(iterator.next().id());
        } finally {
            try {
                if (servicePrincipal != null) {
                    try {
                        authorizationManager.servicePrincipals().deleteById(servicePrincipal.id());
                    } finally {
                        authorizationManager.applications().deleteById(
                            authorizationManager.applications().getByName(servicePrincipal.applicationId()).id()
                        );
                    }
                }
            } finally {
                try {
                    if (user != null) {
                        authorizationManager.users().deleteById(user.id());
                    }
                } finally {
                    try {
                        if (group1 != null) {
                            authorizationManager.groups().deleteById(group1.id());
                        }
                    } finally {
                        if (group2 != null) {
                            authorizationManager.groups().deleteById(group2.id());
                        }
                    }
                }
            }
        }
    }
}
