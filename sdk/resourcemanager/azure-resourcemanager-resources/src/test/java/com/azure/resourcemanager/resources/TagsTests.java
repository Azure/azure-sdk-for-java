// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.resourcemanager.resources.implementation.TypeSerializationTests;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.TagResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TagsTests extends ResourceManagementTest {

    @Test
    public void canUpdateTag() throws Exception {
        // assume there is a resource
        GenericResource resource = resourceClient.genericResources().list().iterator().next();

        Map<String, String> originalTags = resource.tags();
        if (originalTags == null) {
            originalTags = new HashMap<>();
        }

        TagResource updatedTags = resourceClient.tagOperations().updateTags(resource, new TypeSerializationTests.Map1<>("tag.1", "value.1"));
        Assertions.assertNotNull(updatedTags.tags());
        Assertions.assertEquals(1, updatedTags.tags().size());
        Assertions.assertTrue(updatedTags.tags().containsKey("tag.1"));
        Assertions.assertEquals("value.1", updatedTags.tags().get("tag.1"));

        updatedTags = resourceClient.tagOperations().updateTags(resource, new HashMap<>());
        Assertions.assertEquals(0, updatedTags.tags().size());

        updatedTags = resourceClient.tagOperations().updateTags(resource, originalTags);
        Assertions.assertEquals(originalTags, updatedTags.tags());
    }
}
