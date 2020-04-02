/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac;

import com.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Set;

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
            user = graphRbacManager.users().define(userName)
                    .withEmailAlias(userName)
                    .withPassword("StrongPass!123")
                    .create();
            servicePrincipal = graphRbacManager.servicePrincipals().define(spName)
                    .withNewApplication("https://" + spName)
                    .create();
            group1 = graphRbacManager.groups().define(group1Name)
                    .withEmailAlias(group1Name)
                    .create();
            SdkContext.sleep(15000);
            group2 = graphRbacManager.groups().define(group2Name)
                    .withEmailAlias(group2Name)
                    .withMember(user.id())
                    .withMember(servicePrincipal.id())
                    .withMember(group1.id())
                    .create();

            Assertions.assertNotNull(group2);
            Assertions.assertNotNull(group2.id());
            Set<ActiveDirectoryObject> members = group2.listMembers();
            Assertions.assertEquals(3, members.size());
            Iterator<ActiveDirectoryObject> iterator = members.iterator();
            Assertions.assertNotNull(iterator.next().id());
            Assertions.assertNotNull(iterator.next().id());
            Assertions.assertNotNull(iterator.next().id());
        } finally {
            if (servicePrincipal != null) {
                graphRbacManager.servicePrincipals().deleteById(servicePrincipal.id());
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
