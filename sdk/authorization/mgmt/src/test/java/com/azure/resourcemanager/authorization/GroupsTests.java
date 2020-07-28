// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization;

import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryObject;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

public class GroupsTests extends GraphRbacManagementTest {

    @Test
    public void canCRUDGroup() throws Exception {
        String userName = sdkContext.randomResourceName("user", 16);
        String spName = sdkContext.randomResourceName("sp", 16);
        String group1Name = sdkContext.randomResourceName("group", 16);
        String group2Name = sdkContext.randomResourceName("group", 16);
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
                    .withPassword("StrongPass!123")
                    .create();
            servicePrincipal =
                authorizationManager.servicePrincipals().define(spName).withNewApplication("https://" + spName).create();
            group1 = authorizationManager.groups().define(group1Name).withEmailAlias(group1Name).create();
            SdkContext.sleep(15000);
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
            Assertions.assertEquals(3, members.size());
            Iterator<ActiveDirectoryObject> iterator = members.iterator();
            Assertions.assertNotNull(iterator.next().id());
            Assertions.assertNotNull(iterator.next().id());
            Assertions.assertNotNull(iterator.next().id());
        } finally {
            if (servicePrincipal != null) {
                authorizationManager.servicePrincipals().deleteById(servicePrincipal.id());
            }
            // cannot delete users or groups from service principal
            //            if (user != null) {
            //                graphRbacManager.users().deleteById(user.id());
            //            }
            //            if (group != null) {
            //                graphRbacManager.groups().deleteById(group.id());
            //            }
        }
    }
}
