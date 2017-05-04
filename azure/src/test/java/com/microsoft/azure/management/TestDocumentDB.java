/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.documentdb.DatabaseAccount;
import com.microsoft.azure.management.documentdb.DatabaseAccountKind;
import com.microsoft.azure.management.documentdb.DatabaseAccounts;
import com.microsoft.azure.management.documentdb.DefaultConsistencyLevel;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

public class TestDocumentDB extends TestTemplate<DatabaseAccount, DatabaseAccounts> {

    @Override
    public DatabaseAccount createResource(DatabaseAccounts registries) throws Exception {
        final String newName = "docDB" + this.testId;
        return registries.define(newName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
                .defineLocation(Region.US_WEST)
                    .withFailoverPriority(0)
                    .attach()
                .create();
    }

    @Override
    public DatabaseAccount updateResource(DatabaseAccount resource) throws Exception {
        // Modify existing container service
        resource =  resource.update()
                .withConsistencyPolicy(DefaultConsistencyLevel.SESSION, 1, 1)
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
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