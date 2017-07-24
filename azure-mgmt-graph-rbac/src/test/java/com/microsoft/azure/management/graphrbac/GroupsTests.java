/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class GroupsTests extends GraphRbacManagementTest {

    @Test
    public void canCRUDGroup() throws Exception {
        String userName = SdkContext.randomResourceName("user", 16);
        String spName = SdkContext.randomResourceName("sp", 16);
        String group1Name = SdkContext.randomResourceName("group", 16);
        String group2Name = SdkContext.randomResourceName("group", 16);
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

            Assert.assertNotNull(group2);
            Assert.assertNotNull(group2.id());
            Set<ActiveDirectoryObject> members = group2.listMembers();
            Assert.assertEquals(3, members.size());
            Assert.assertNotNull(members.iterator().next().id());
            Assert.assertNotNull(members.iterator().next().id());
            Assert.assertNotNull(members.iterator().next().id());
            Assert.assertNotNull(members.iterator().next().id());
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
