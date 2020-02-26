// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery.models;

public class BlobQuickQuerySerialization <T extends BlobQuickQuerySerialization<T>> {

    private char recordSeparator;

    public char getRecordSeparator() {
        return recordSeparator;
    }

    public T setRecordSeparator(char recordSeparator) {
        this.recordSeparator = recordSeparator;
        return (T) this;
    }

}
