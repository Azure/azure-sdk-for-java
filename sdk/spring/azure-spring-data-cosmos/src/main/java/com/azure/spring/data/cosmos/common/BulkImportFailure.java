// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import java.util.ArrayList;
import java.util.List;

public class BulkImportFailure {
    private Exception bulkImportFailureException;
    private List<String> documentsFailedToImport = new ArrayList<>();

    public List<String> getDocumentIdsFailedToImport() {
        return documentIdsFailedToImport;
    }

    public void setDocumentIdsFailedToImport(List<String> documentIdsFailedToImport) {
        this.documentIdsFailedToImport = documentIdsFailedToImport;
    }

    private List<String> documentIdsFailedToImport = new ArrayList<>();

    public BulkImportFailure() {
    }

    public Exception getBulkImportFailureException() {
        return this.bulkImportFailureException;
    }

    public void setBulkImportFailureException(Exception bulkImportFailureException) {
        this.bulkImportFailureException = bulkImportFailureException;
    }

    public List<String> getDocumentsFailedToImport() {
        return this.documentsFailedToImport;
    }

    public void setDocumentsFailedToImport(List<String> documentsFailedToImport) {
        this.documentsFailedToImport = documentsFailedToImport;
    }
}
