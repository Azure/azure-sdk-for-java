// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class IndexBatchBuilderTests {

    @Test
    public void uploadDocument() {
        Document document = new Document();
        document.put("Id", "1");

        IndexAction<Document> indexAction = new IndexAction<Document>()
            .actionType(IndexActionType.UPLOAD)
            .document(document);

        IndexBatch<Document> expected = new IndexBatch<Document>()
            .actions(Arrays.asList(indexAction));

        IndexBatch<Document> actual = new IndexBatchBuilder<Document>()
            .upload(document).build();

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

        for (int i = 0; i < docs.size(); i++) {
            indexActions.add(
                new IndexAction<Document>()
                    .actionType(IndexActionType.UPLOAD)
                    .document(docs.get(i))
            );
        }
        IndexBatch<Document> expectedBatch = new IndexBatch<Document>().actions(indexActions);

        IndexBatch<Document> actualBatch = new IndexBatchBuilder<Document>()
            .upload(docs)
            .build();

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
            .actions(Arrays.asList(indexAction));

        IndexBatch<Document> actual = new IndexBatchBuilder<Document>()
            .merge(document).build();

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

        for (int i = 0; i < docs.size(); i++) {
            indexActions.add(
                new IndexAction<Document>()
                    .actionType(IndexActionType.MERGE)
                    .document(docs.get(i))
            );
        }
        IndexBatch<Document> expectedBatch = new IndexBatch<Document>().actions(indexActions);

        IndexBatch<Document> actualBatch = new IndexBatchBuilder<Document>()
            .merge(docs)
            .build();

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
            .actions(Arrays.asList(indexAction));

        IndexBatch<Document> actual = new IndexBatchBuilder<Document>()
            .mergeOrUpload(document).build();

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

        for (int i = 0; i < docs.size(); i++) {
            indexActions.add(
                new IndexAction<Document>()
                    .actionType(IndexActionType.MERGE_OR_UPLOAD)
                    .document(docs.get(i))
            );
        }
        IndexBatch<Document> expectedBatch = new IndexBatch<Document>().actions(indexActions);

        IndexBatch<Document> actualBatch = new IndexBatchBuilder<Document>()
            .mergeOrUpload(docs)
            .build();

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
            .actions(Arrays.asList(indexAction));

        IndexBatch<Document> actual = new IndexBatchBuilder<Document>()
            .delete(document).build();

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

        for (int i = 0; i < docs.size(); i++) {
            indexActions.add(
                new IndexAction<Document>()
                    .actionType(IndexActionType.DELETE)
                    .document(docs.get(i))
            );
        }
        IndexBatch<Document> expectedBatch = new IndexBatch<Document>().actions(indexActions);

        IndexBatch<Document> actualBatch = new IndexBatchBuilder<Document>()
            .delete(docs)
            .build();

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

        IndexBatch<Document> actual = new IndexBatchBuilder<Document>()
            .merge(documentToMerge)
            .mergeOrUpload(documentToMergeOrUpload)
            .delete(documentToDelete)
            .upload(documentToUpload)
            .build();

        validate(expected, actual);
    }

    @Test
    public void canBuildIndexBatchWithMultipleActionsAndMultipleDocuments() {
        List<Document> documentsToMerge = Arrays.asList(
            new Document() {
                {
                    put("Id", "merge1");
                }
            },
            new Document() {
                {
                    put("Id", "merge2");
                }
            }
        );

        List<Document> documentsToDelete = Arrays.asList(
            new Document() {
                {
                    put("Id", "delete1");
                }
            },
            new Document() {
                {
                    put("Id", "delete2");
                }
            }
        );

        List<Document> documentsToMergeOrUpload = Arrays.asList(
            new Document() {
                {
                    put("Id", "mergeOrUpload1");
                }
            },
            new Document() {
                {
                    put("Id", "mergeOrUpload2");
                }
            }
        );

        List<Document> documentsToUpload = Arrays.asList(
            new Document() {
                {
                    put("Id", "upload1");
                }
            },
            new Document() {
                {
                    put("Id", "upload2");
                }
            }
        );

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

        IndexBatch<Document> actual = new IndexBatchBuilder<Document>()
            .merge(documentsToMerge)
            .mergeOrUpload(documentsToMergeOrUpload)
            .delete(documentsToDelete)
            .upload(documentsToUpload)
            .build();

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
