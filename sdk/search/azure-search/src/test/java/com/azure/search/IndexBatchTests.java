// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.IndexAction;
import com.azure.search.models.IndexActionType;
import com.azure.search.models.IndexBatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class IndexBatchTests {

    @Test
    public void uploadDocument() {
        Document document = new Document();
        document.put("Id", "1");

        IndexAction<Document> indexAction = new IndexAction<Document>()
            .actionType(IndexActionType.UPLOAD)
            .document(document);

        IndexBatch<Document> expected = new IndexBatch<Document>()
            .actions(Collections.singletonList(indexAction));

        IndexBatch<Document> actual = new IndexBatch<Document>()
            .addUploadAction(document);

        validate(expected, actual);
    }

    @Test
    public void uploadDocuments() {
        Document doc1 = new Document();
        doc1.put("Id", "1");

        Document doc2 = new Document();
        doc2.put("Id", "2");

        Document doc3 = new Document();
        doc3.put("Id", "3");

        List<Document> docs = Arrays.asList(doc1, doc2, doc3);
        List<IndexAction<Document>> indexActions = new LinkedList<>();

        for (Document doc : docs) {
            indexActions.add(
                new IndexAction<Document>()
                    .actionType(IndexActionType.UPLOAD)
                    .document(doc)
            );
        }
        IndexBatch<Document> expectedBatch = new IndexBatch<Document>()
            .actions(indexActions);

        IndexBatch<Document> actualBatch = new IndexBatch<Document>()
            .addUploadAction(docs);

        validate(expectedBatch, actualBatch);
    }

    @Test
    public void mergeDocument() {
        Document document = new Document();
        document.put("Id", "1");

        IndexAction<Document> indexAction = new IndexAction<Document>()
            .actionType(IndexActionType.MERGE)
            .document(document);

        IndexBatch<Document> expected = new IndexBatch<Document>()
            .actions(Collections.singletonList(indexAction));

        IndexBatch<Document> actual = new IndexBatch<Document>()
            .addMergeAction(document);

        validate(expected, actual);
    }

    @Test
    public void mergeDocuments() {
        Document doc1 = new Document();
        doc1.put("Id", "1");

        Document doc2 = new Document();
        doc2.put("Id", "2");

        Document doc3 = new Document();
        doc3.put("Id", "3");

        List<Document> docs = Arrays.asList(doc1, doc2, doc3);
        List<IndexAction<Document>> indexActions = new LinkedList<>();

        for (Document doc : docs) {
            indexActions.add(
                new IndexAction<Document>()
                    .actionType(IndexActionType.MERGE)
                    .document(doc)
            );
        }
        IndexBatch<Document> expectedBatch = new IndexBatch<Document>()
            .actions(indexActions);

        IndexBatch<Document> actualBatch = new IndexBatch<Document>()
            .addMergeAction(docs);

        validate(expectedBatch, actualBatch);
    }

    @Test
    public void mergeOrUploadDocument() {
        Document document = new Document();
        document.put("Id", "1");

        IndexAction<Document> indexAction = new IndexAction<Document>()
            .actionType(IndexActionType.MERGE_OR_UPLOAD)
            .document(document);

        IndexBatch<Document> expected = new IndexBatch<Document>()
            .actions(Collections.singletonList(indexAction));

        IndexBatch<Document> actual = new IndexBatch<Document>()
            .addMergeOrUploadAction(document);

        validate(expected, actual);
    }

    @Test
    public void mergeOrUploadDocuments() {
        Document doc1 = new Document();
        doc1.put("Id", "1");

        Document doc2 = new Document();
        doc2.put("Id", "2");

        Document doc3 = new Document();
        doc3.put("Id", "3");

        List<Document> docs = Arrays.asList(doc1, doc2, doc3);
        List<IndexAction<Document>> indexActions = new LinkedList<>();

        for (Document doc : docs) {
            indexActions.add(
                new IndexAction<Document>()
                    .actionType(IndexActionType.MERGE_OR_UPLOAD)
                    .document(doc)
            );
        }
        IndexBatch<Document> expectedBatch = new IndexBatch<Document>()
            .actions(indexActions);

        IndexBatch<Document> actualBatch = new IndexBatch<Document>()
            .addMergeOrUploadAction(docs);

        validate(expectedBatch, actualBatch);
    }

    @Test
    public void deleteDocument() {
        Document document = new Document();
        document.put("Id", "1");

        IndexAction<Document> indexAction = new IndexAction<Document>()
            .actionType(IndexActionType.DELETE)
            .document(document);

        IndexBatch<Document> expected = new IndexBatch<Document>()
            .actions(Collections.singletonList(indexAction));

        IndexBatch<Document> actual = new IndexBatch<Document>()
            .addDeleteAction(document);

        validate(expected, actual);
    }

    @Test
    public void deleteDocuments() {
        Document doc1 = new Document();
        doc1.put("Id", "1");

        Document doc2 = new Document();
        doc2.put("Id", "2");

        Document doc3 = new Document();
        doc3.put("Id", "3");

        List<Document> docs = Arrays.asList(doc1, doc2, doc3);
        List<IndexAction<Document>> indexActions = new LinkedList<>();

        for (Document doc : docs) {
            indexActions.add(
                new IndexAction<Document>()
                    .actionType(IndexActionType.DELETE)
                    .document(doc)
            );
        }
        IndexBatch<Document> expectedBatch = new IndexBatch<Document>()
            .actions(indexActions);

        IndexBatch<Document> actualBatch = new IndexBatch<Document>()
            .addDeleteAction(docs);

        validate(expectedBatch, actualBatch);
    }

    @Test
    public void canBuildIndexBatchWithMultipleActionsAndSingleDocument() {
        Document documentToMerge = new Document();
        documentToMerge.put("Id", "merge");

        Document documentToMergeOrUpload =  new Document();
        documentToMergeOrUpload.put("Id", "mergeOrUpload");

        Document documentToUpload = new Document();
        documentToUpload.put("Id", "upload");

        Document documentToDelete = new Document();
        documentToDelete.put("Id", "delete");

        IndexAction<Document> mergeAction = new IndexAction<Document>()
            .actionType(IndexActionType.MERGE)
            .document(documentToMerge);

        IndexAction<Document> mergeOrUploadAction = new IndexAction<Document>()
            .actionType(IndexActionType.MERGE_OR_UPLOAD)
            .document(documentToMergeOrUpload);

        IndexAction<Document> deleteAction = new IndexAction<Document>()
            .actionType(IndexActionType.DELETE)
            .document(documentToDelete);

        IndexAction<Document> uploadAction = new IndexAction<Document>()
            .actionType(IndexActionType.UPLOAD)
            .document(documentToUpload);


        IndexBatch<Document> expected = new IndexBatch<Document>()
            .actions(Arrays.asList(mergeAction, mergeOrUploadAction, deleteAction, uploadAction));

        IndexBatch<Document> actual = new IndexBatch<Document>()
            .addMergeAction(documentToMerge)
            .addMergeOrUploadAction(documentToMergeOrUpload)
            .addDeleteAction(documentToDelete)
            .addUploadAction(documentToUpload);

        validate(expected, actual);
    }

    @Test
    public void canBuildIndexBatchWithMultipleActionsAndMultipleDocuments() {
        List<Document> documentsToMerge = new ArrayList<>();

        Document merge1 = new Document();
        merge1.put("Id", "merge1");
        documentsToMerge.add(merge1);

        Document merge2 = new Document();
        merge2.put("Id", "merge2");
        documentsToMerge.add(merge2);

        List<Document> documentsToDelete = new ArrayList<>();

        Document delete1 = new Document();
        delete1.put("Id", "delete1");
        documentsToDelete.add(delete1);

        Document delete2 = new Document();
        delete2.put("Id", "delete2");
        documentsToDelete.add(delete2);

        List<Document> documentsToMergeOrUpload = new ArrayList<>();

        Document mergeOrUpload1 = new Document();
        mergeOrUpload1.put("Id", "mergeOrUpload1");
        documentsToMergeOrUpload.add(mergeOrUpload1);

        Document mergeOrUpload2 = new Document();
        mergeOrUpload2.put("Id", "mergeOrUpload2");
        documentsToMergeOrUpload.add(mergeOrUpload2);

        List<Document> documentsToUpload = new ArrayList<>();

        Document upload1 = new Document();
        upload1.put("Id", "upload1");
        documentsToUpload.add(upload1);

        Document upload2 = new Document();
        upload2.put("Id", "upload2");
        documentsToUpload.add(upload2);

        IndexAction<Document> mergeAction1 = new IndexAction<Document>()
            .actionType(IndexActionType.MERGE)
            .document(documentsToMerge.get(0));

        IndexAction<Document> mergeAction2 = new IndexAction<Document>()
            .actionType(IndexActionType.MERGE)
            .document(documentsToMerge.get(1));

        IndexAction<Document> mergeOrUploadAction1 = new IndexAction<Document>()
            .actionType(IndexActionType.MERGE_OR_UPLOAD)
            .document(documentsToMergeOrUpload.get(0));

        IndexAction<Document> mergeOrUploadAction2 = new IndexAction<Document>()
            .actionType(IndexActionType.MERGE_OR_UPLOAD)
            .document(documentsToMergeOrUpload.get(1));

        IndexAction<Document> deleteAction1 = new IndexAction<Document>()
            .actionType(IndexActionType.DELETE)
            .document(documentsToDelete.get(0));

        IndexAction<Document> deleteAction2 = new IndexAction<Document>()
            .actionType(IndexActionType.DELETE)
            .document(documentsToDelete.get(1));

        IndexAction<Document> uploadAction1 = new IndexAction<Document>()
            .actionType(IndexActionType.UPLOAD)
            .document(documentsToUpload.get(0));

        IndexAction<Document> uploadAction2 = new IndexAction<Document>()
            .actionType(IndexActionType.UPLOAD)
            .document(documentsToUpload.get(1));

        IndexBatch<Document> expected = new IndexBatch<Document>()
            .actions(
                Arrays.asList(
                    mergeAction1,
                    mergeAction2,
                    mergeOrUploadAction1,
                    mergeOrUploadAction2,
                    deleteAction1,
                    deleteAction2,
                    uploadAction1,
                    uploadAction2
                )
            );

        IndexBatch<Document> actual = new IndexBatch<Document>()
            .addMergeAction(documentsToMerge)
            .addMergeOrUploadAction(documentsToMergeOrUpload)
            .addDeleteAction(documentsToDelete)
            .addUploadAction(documentsToUpload);

        validate(expected, actual);
    }

    private void validate(IndexBatch<Document> expected, IndexBatch<Document> actual) {
        Assert.assertEquals(expected.actions().size(), actual.actions().size());

        for (int i = 0; i < actual.actions().size(); i++) {
            IndexAction<Document> expectedIndexAction = expected.actions().get(i);
            IndexAction<Document> actualIndexAction = actual.actions().get(i);

            Assert.assertEquals(expectedIndexAction.actionType(), actualIndexAction.actionType());
            Assert.assertEquals(expectedIndexAction.getDocument(), actualIndexAction.getDocument());
        }
    }
}
