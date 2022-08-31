// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.documentanalysis.models.TypedDocumentField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Model class representing a US Receipt document
 */
public class ReceiptDocument extends AnalyzedDocument {

    @JsonProperty("MerchantName")
    public TypedDocumentField<String> merchantName;

    @JsonProperty("MerchantAddress")
    public TypedDocumentField<String> merchantAddress;


    @JsonProperty("Items")
    public TypedDocumentField<List<TypedDocumentField<ItemField>>> items;

    public TypedDocumentField<String> getMerchantName() {
        return merchantName;
    }

    public TypedDocumentField<String> getMerchantAddress() {
        return merchantAddress;
    }

    public TypedDocumentField<List<TypedDocumentField<ItemField>>> getItems() {
        return items;
    }

    static class ItemField extends TypedDocumentField<Object> {

        public ItemField() {
        }

        @JsonProperty("Description")
        TypedDocumentField<String> description;

        //getters
    }
}
