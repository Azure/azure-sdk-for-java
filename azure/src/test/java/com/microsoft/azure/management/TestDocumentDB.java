/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.documentdb.*;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

public class TestDocumentDB extends TestTemplate<DatabaseAccount, DatabaseAccounts> {

    @Override
    public DatabaseAccount createResource(DatabaseAccounts resources) throws Exception {
        final String newName = "docDB" + this.testId;
        DatabaseAccount databaseAccount = resources.define(newName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
                .withReadableFailover(Region.ASIA_EAST)
                .withWritableFailover(Region.US_EAST)
                .withStrongConsistencyPolicy()
                .withIpRangeFilter("")
                .create();
        Assert.assertEquals(databaseAccount.name(), newName.toLowerCase());
        Assert.assertEquals(databaseAccount.kind(), DatabaseAccountKind.GLOBAL_DOCUMENT_DB);
        Assert.assertEquals(databaseAccount.writableReplications().size(), 1);
        Assert.assertEquals(databaseAccount.readableReplications().size(), 2);
        Assert.assertEquals(databaseAccount.defaultConsistencyLevel(), DefaultConsistencyLevel.STRONG);
        return databaseAccount;
    }

    @Override
    public DatabaseAccount updateResource(DatabaseAccount resource) throws Exception {
        // Modify existing container service
        resource =  resource.update()
                .withSessionConsistencyPolicy()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
        Assert.assertEquals(resource.defaultConsistencyLevel(), DefaultConsistencyLevel.SESSION);
        Assert.assertTrue(resource.tags().containsKey("tag2"));
        Assert.assertTrue(!resource.tags().containsKey("tag1"));

        return resource;
    }

    @Override
    public void print(DatabaseAccount resource) {
        System.out.println(new StringBuilder().append("Regsitry: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .toString());
    }
}