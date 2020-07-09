// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
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

        IndexDocumentsBatch<SearchDocument> expected = new IndexDocumentsBatch<SearchDocument>()
            .addActions(Collections.singletonList(indexAction));

        IndexDocumentsBatch<SearchDocument> actual = new IndexDocumentsBatch<SearchDocument>()
            .addUploadActions(Collections.singletonList(searchDocument));

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

        IndexDocumentsBatch<SearchDocument> expectedBatch = new IndexDocumentsBatch<SearchDocument>()
            .addActions(indexActions);

        IndexDocumentsBatch<SearchDocument> actualBatch = new IndexDocumentsBatch<SearchDocument>()
            .addUploadActions(docs);

        validate(expectedBatch, actualBatch);
    }

    @Test
    public void mergeDocument() {
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("Id", "1");

        IndexAction<SearchDocument> indexAction = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.MERGE)
            .setDocument(searchDocument);

        IndexDocumentsBatch<SearchDocument> expected = new IndexDocumentsBatch<SearchDocument>()
            .addActions(Collections.singletonList(indexAction));

        IndexDocumentsBatch<SearchDocument> actual = new IndexDocumentsBatch<SearchDocument>()
            .addMergeActions(Collections.singletonList(searchDocument));

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

        IndexDocumentsBatch<SearchDocument> expectedBatch = new IndexDocumentsBatch<SearchDocument>()
            .addActions(indexActions);

        IndexDocumentsBatch<SearchDocument> actualBatch = new IndexDocumentsBatch<SearchDocument>()
            .addMergeActions(docs);

        validate(expectedBatch, actualBatch);
    }

    @Test
    public void mergeOrUploadDocument() {
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("Id", "1");

        IndexAction<SearchDocument> indexAction = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.MERGE_OR_UPLOAD)
            .setDocument(searchDocument);

        IndexDocumentsBatch<SearchDocument> expected = new IndexDocumentsBatch<SearchDocument>()
            .addActions(Collections.singletonList(indexAction));

        IndexDocumentsBatch<SearchDocument> actual = new IndexDocumentsBatch<SearchDocument>()
            .addMergeOrUploadActions(Collections.singletonList(searchDocument));

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

        IndexDocumentsBatch<SearchDocument> expectedBatch = new IndexDocumentsBatch<SearchDocument>()
            .addActions(indexActions);

        IndexDocumentsBatch<SearchDocument> actualBatch = new IndexDocumentsBatch<SearchDocument>()
            .addMergeOrUploadActions(docs);

        validate(expectedBatch, actualBatch);
    }

    @Test
    public void deleteDocument() {
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("Id", "1");

        IndexAction<SearchDocument> indexAction = new IndexAction<SearchDocument>()
            .setActionType(IndexActionType.DELETE)
            .setDocument(searchDocument);

        IndexDocumentsBatch<SearchDocument> expected = new IndexDocumentsBatch<SearchDocument>()
            .addActions(Collections.singletonList(indexAction));

        IndexDocumentsBatch<SearchDocument> actual = new IndexDocumentsBatch<SearchDocument>()
            .addDeleteActions(Collections.singletonList(searchDocument));

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

        IndexDocumentsBatch<SearchDocument> expectedBatch = new IndexDocumentsBatch<SearchDocument>()
            .addActions(indexActions);

        IndexDocumentsBatch<SearchDocument> actualBatch = new IndexDocumentsBatch<SearchDocument>()
            .addDeleteActions(docs);

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

        IndexDocumentsBatch<SearchDocument> expected = new IndexDocumentsBatch<SearchDocument>()
            .addActions(Arrays.asList(mergeAction, mergeOrUploadAction, deleteAction, uploadAction));

        IndexDocumentsBatch<SearchDocument> actual = new IndexDocumentsBatch<SearchDocument>()
            .addMergeActions(Collections.singletonList(documentToMerge))
            .addMergeOrUploadActions(Collections.singletonList(documentToMergeOrUpload))
            .addDeleteActions(Collections.singletonList(documentToDelete))
            .addUploadActions(Collections.singletonList(documentToUpload));

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

        IndexDocumentsBatch<SearchDocument> expected = new IndexDocumentsBatch<SearchDocument>()
            .addActions(Arrays.asList(
                mergeAction1,
                mergeAction2,
                mergeOrUploadAction1,
                mergeOrUploadAction2,
                deleteAction1,
                deleteAction2,
                uploadAction1,
                uploadAction2));

        IndexDocumentsBatch<SearchDocument> actual = new IndexDocumentsBatch<SearchDocument>()
            .addMergeActions(documentsToMerge)
            .addMergeOrUploadActions(documentsToMergeOrUpload)
            .addDeleteActions(documentsToDelete)
            .addUploadActions(documentsToUpload);

        validate(expected, actual);
    }

    private void validate(IndexDocumentsBatch<SearchDocument> expected, IndexDocumentsBatch<SearchDocument> actual) {
        assertEquals(expected.getActions().size(), actual.getActions().size());

        for (int i = 0; i < actual.getActions().size(); i++) {
            IndexAction<SearchDocument> expectedIndexAction = expected.getActions().get(i);
            IndexAction<SearchDocument> actualIndexAction = actual.getActions().get(i);

            assertEquals(expectedIndexAction.getActionType(), actualIndexAction.getActionType());
            assertEquals(expectedIndexAction.getDocument(), actualIndexAction.getDocument());
        }
    }
}
