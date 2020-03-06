// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.IndexAction;
import com.azure.search.models.IndexActionType;
import com.azure.search.models.IndexBatch;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexBatchTests {

    @Test
    public void uploadDocument() {
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("Id", "1");

        IndexAction<SearchDocument> indexAction = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.UPLOAD)
            .setDocument(searchDocument);

        IndexBatch<SearchDocument> expected = new IndexBatch<SearchDocument>()
            .actions(Collections.singletonList(indexAction));

        IndexBatch<SearchDocument> actual = new IndexBatch<SearchDocument>()
            .addUploadAction(searchDocument);

        validate(expected, actual);
    }

    @Test
    public void uploadDocuments() {
        SearchDocument doc1 = new SearchDocument();
        doc1.put("Id", "1");

        SearchDocument doc2 = new SearchDocument();
        doc2.put("Id", "2");

        SearchDocument doc3 = new SearchDocument();
        doc3.put("Id", "3");

        List<SearchDocument> docs = Arrays.asList(doc1, doc2, doc3);
        List<IndexAction<SearchDocument>> indexActions = docs.stream()
            .map(doc -> new IndexAction<SearchDocument>().setActionType(IndexActionType.UPLOAD).setDocument(doc))
            .collect(Collectors.toList());

        IndexBatch<SearchDocument> expectedBatch = new IndexBatch<SearchDocument>()
            .actions(indexActions);

        IndexBatch<SearchDocument> actualBatch = new IndexBatch<SearchDocument>()
            .addUploadAction(docs);

        validate(expectedBatch, actualBatch);
    }

    @Test
    public void mergeDocument() {
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("Id", "1");

        IndexAction<SearchDocument> indexAction = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.MERGE)
            .setDocument(searchDocument);

        IndexBatch<SearchDocument> expected = new IndexBatch<SearchDocument>()
            .actions(Collections.singletonList(indexAction));

        IndexBatch<SearchDocument> actual = new IndexBatch<SearchDocument>()
            .addMergeAction(searchDocument);

        validate(expected, actual);
    }

    @Test
    public void mergeDocuments() {
        SearchDocument doc1 = new SearchDocument();
        doc1.put("Id", "1");

        SearchDocument doc2 = new SearchDocument();
        doc2.put("Id", "2");

        SearchDocument doc3 = new SearchDocument();
        doc3.put("Id", "3");

        List<SearchDocument> docs = Arrays.asList(doc1, doc2, doc3);
        List<IndexAction<SearchDocument>> indexActions = docs.stream()
            .map(doc -> new IndexAction<SearchDocument>().setActionType(IndexActionType.MERGE).setDocument(doc))
            .collect(Collectors.toList());

        IndexBatch<SearchDocument> expectedBatch = new IndexBatch<SearchDocument>()
            .actions(indexActions);

        IndexBatch<SearchDocument> actualBatch = new IndexBatch<SearchDocument>()
            .addMergeAction(docs);

        validate(expectedBatch, actualBatch);
    }

    @Test
    public void mergeOrUploadDocument() {
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("Id", "1");

        IndexAction<SearchDocument> indexAction = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.MERGE_OR_UPLOAD)
            .setDocument(searchDocument);

        IndexBatch<SearchDocument> expected = new IndexBatch<SearchDocument>()
            .actions(Collections.singletonList(indexAction));

        IndexBatch<SearchDocument> actual = new IndexBatch<SearchDocument>()
            .addMergeOrUploadAction(searchDocument);

        validate(expected, actual);
    }

    @Test
    public void mergeOrUploadDocuments() {
        SearchDocument doc1 = new SearchDocument();
        doc1.put("Id", "1");

        SearchDocument doc2 = new SearchDocument();
        doc2.put("Id", "2");

        SearchDocument doc3 = new SearchDocument();
        doc3.put("Id", "3");

        List<SearchDocument> docs = Arrays.asList(doc1, doc2, doc3);
        List<IndexAction<SearchDocument>> indexActions = docs.stream()
            .map(doc -> new IndexAction<SearchDocument>().setActionType(IndexActionType.MERGE_OR_UPLOAD).setDocument(doc))
            .collect(Collectors.toList());

        IndexBatch<SearchDocument> expectedBatch = new IndexBatch<SearchDocument>()
            .actions(indexActions);

        IndexBatch<SearchDocument> actualBatch = new IndexBatch<SearchDocument>()
            .addMergeOrUploadAction(docs);

        validate(expectedBatch, actualBatch);
    }

    @Test
    public void deleteDocument() {
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("Id", "1");

        IndexAction<SearchDocument> indexAction = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.DELETE)
            .setDocument(searchDocument);

        IndexBatch<SearchDocument> expected = new IndexBatch<SearchDocument>()
            .actions(Collections.singletonList(indexAction));

        IndexBatch<SearchDocument> actual = new IndexBatch<SearchDocument>()
            .addDeleteAction(searchDocument);

        validate(expected, actual);
    }

    @Test
    public void deleteDocuments() {
        SearchDocument doc1 = new SearchDocument();
        doc1.put("Id", "1");

        SearchDocument doc2 = new SearchDocument();
        doc2.put("Id", "2");

        SearchDocument doc3 = new SearchDocument();
        doc3.put("Id", "3");

        List<SearchDocument> docs = Arrays.asList(doc1, doc2, doc3);
        List<IndexAction<SearchDocument>> indexActions = docs.stream()
            .map(doc -> new IndexAction<SearchDocument>().setActionType(IndexActionType.DELETE).setDocument(doc))
            .collect(Collectors.toList());

        IndexBatch<SearchDocument> expectedBatch = new IndexBatch<SearchDocument>()
            .actions(indexActions);

        IndexBatch<SearchDocument> actualBatch = new IndexBatch<SearchDocument>()
            .addDeleteAction(docs);

        validate(expectedBatch, actualBatch);
    }

    @Test
    public void canBuildIndexBatchWithMultipleActionsAndSingleDocument() {
        SearchDocument documentToMerge = new SearchDocument();
        documentToMerge.put("Id", "merge");

        SearchDocument documentToMergeOrUpload = new SearchDocument();
        documentToMergeOrUpload.put("Id", "mergeOrUpload");

        SearchDocument documentToUpload = new SearchDocument();
        documentToUpload.put("Id", "upload");

        SearchDocument documentToDelete = new SearchDocument();
        documentToDelete.put("Id", "delete");

        IndexAction<SearchDocument> mergeAction = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.MERGE)
            .setDocument(documentToMerge);

        IndexAction<SearchDocument> mergeOrUploadAction = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.MERGE_OR_UPLOAD)
            .setDocument(documentToMergeOrUpload);

        IndexAction<SearchDocument> deleteAction = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.DELETE)
            .setDocument(documentToDelete);

        IndexAction<SearchDocument> uploadAction = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.UPLOAD)
            .setDocument(documentToUpload);


        IndexBatch<SearchDocument> expected = new IndexBatch<SearchDocument>()
            .actions(Arrays.asList(mergeAction, mergeOrUploadAction, deleteAction, uploadAction));

        IndexBatch<SearchDocument> actual = new IndexBatch<SearchDocument>()
            .addMergeAction(documentToMerge)
            .addMergeOrUploadAction(documentToMergeOrUpload)
            .addDeleteAction(documentToDelete)
            .addUploadAction(documentToUpload);

        validate(expected, actual);
    }

    @Test
    public void canBuildIndexBatchWithMultipleActionsAndMultipleDocuments() {
        List<SearchDocument> documentsToMerge = new ArrayList<>();

        SearchDocument merge1 = new SearchDocument();
        merge1.put("Id", "merge1");
        documentsToMerge.add(merge1);

        SearchDocument merge2 = new SearchDocument();
        merge2.put("Id", "merge2");
        documentsToMerge.add(merge2);

        List<SearchDocument> documentsToDelete = new ArrayList<>();

        SearchDocument delete1 = new SearchDocument();
        delete1.put("Id", "delete1");
        documentsToDelete.add(delete1);

        SearchDocument delete2 = new SearchDocument();
        delete2.put("Id", "delete2");
        documentsToDelete.add(delete2);

        List<SearchDocument> documentsToMergeOrUpload = new ArrayList<>();

        SearchDocument mergeOrUpload1 = new SearchDocument();
        mergeOrUpload1.put("Id", "mergeOrUpload1");
        documentsToMergeOrUpload.add(mergeOrUpload1);

        SearchDocument mergeOrUpload2 = new SearchDocument();
        mergeOrUpload2.put("Id", "mergeOrUpload2");
        documentsToMergeOrUpload.add(mergeOrUpload2);

        List<SearchDocument> documentsToUpload = new ArrayList<>();

        SearchDocument upload1 = new SearchDocument();
        upload1.put("Id", "upload1");
        documentsToUpload.add(upload1);

        SearchDocument upload2 = new SearchDocument();
        upload2.put("Id", "upload2");
        documentsToUpload.add(upload2);

        IndexAction<SearchDocument> mergeAction1 = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.MERGE)
            .setDocument(documentsToMerge.get(0));

        IndexAction<SearchDocument> mergeAction2 = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.MERGE)
            .setDocument(documentsToMerge.get(1));

        IndexAction<SearchDocument> mergeOrUploadAction1 = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.MERGE_OR_UPLOAD)
            .setDocument(documentsToMergeOrUpload.get(0));

        IndexAction<SearchDocument> mergeOrUploadAction2 = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.MERGE_OR_UPLOAD)
            .setDocument(documentsToMergeOrUpload.get(1));

        IndexAction<SearchDocument> deleteAction1 = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.DELETE)
            .setDocument(documentsToDelete.get(0));

        IndexAction<SearchDocument> deleteAction2 = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.DELETE)
            .setDocument(documentsToDelete.get(1));

        IndexAction<SearchDocument> uploadAction1 = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.UPLOAD)
            .setDocument(documentsToUpload.get(0));

        IndexAction<SearchDocument> uploadAction2 = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.UPLOAD)
            .setDocument(documentsToUpload.get(1));

        IndexBatch<SearchDocument> expected = new IndexBatch<SearchDocument>()
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

        IndexBatch<SearchDocument> actual = new IndexBatch<SearchDocument>()
            .addMergeAction(documentsToMerge)
            .addMergeOrUploadAction(documentsToMergeOrUpload)
            .addDeleteAction(documentsToDelete)
            .addUploadAction(documentsToUpload);

        validate(expected, actual);
    }

    private void validate(IndexBatch<SearchDocument> expected, IndexBatch<SearchDocument> actual) {
        assertEquals(expected.getActions().size(), actual.getActions().size());

        for (int i = 0; i < actual.getActions().size(); i++) {
            IndexAction<SearchDocument> expectedIndexAction = expected.getActions().get(i);
            IndexAction<SearchDocument> actualIndexAction = actual.getActions().get(i);

            assertEquals(expectedIndexAction.getActionType(), actualIndexAction.getActionType());
            assertEquals(expectedIndexAction.getDocument(), actualIndexAction.getDocument());
        }
    }
}
