// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import org.junit.Assert;
import org.junit.Test;

public class IndexingActionTest {
    @Test
    public void deleteIndexAction() {
        Document document = new Document();
        document.put("Id", "1");
        IndexAction indexAction = IndexingAction.delete(document);
        Assert.assertEquals(IndexActionType.DELETE, indexAction.actionType());
        Assert.assertEquals(document, indexAction.additionalProperties());
    }

    @Test
    public void deleteWithKeyName() {
        Document document = new Document();
        document.put("Id", "1");
        IndexAction indexAction = IndexingAction.delete("Id", "1");
        Assert.assertEquals(IndexActionType.DELETE, indexAction.actionType());
        Assert.assertEquals("1", indexAction.additionalProperties().get("Id"));
    }

    @Test
    public void merge() {
        Document document = new Document();
        document.put("Id", "1");
        IndexAction indexAction = IndexingAction.merge(document);
        Assert.assertEquals(IndexActionType.MERGE, indexAction.actionType());
        Assert.assertEquals(document, indexAction.additionalProperties());
    }

    @Test
    public void mergeOrUpload() {
        Document document = new Document();
        document.put("Id", "1");
        IndexAction indexAction = IndexingAction.mergeOrUpload(document);
        Assert.assertEquals(IndexActionType.MERGE_OR_UPLOAD, indexAction.actionType());
        Assert.assertEquals(document, indexAction.additionalProperties());
    }

    @Test
    public void upload() {
        Document document = new Document();
        document.put("Id", "1");
        IndexAction indexAction = IndexingAction.upload(document);
        Assert.assertEquals(IndexActionType.UPLOAD, indexAction.actionType());
        Assert.assertEquals(document, indexAction.additionalProperties());
    }
}
