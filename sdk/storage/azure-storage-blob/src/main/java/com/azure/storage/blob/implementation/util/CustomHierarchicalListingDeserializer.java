// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.models.BlobHierarchyListSegment;
import com.azure.storage.blob.implementation.models.BlobItemInternal;
import com.azure.storage.blob.models.BlobPrefix;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;

public final class CustomHierarchicalListingDeserializer extends JsonDeserializer<BlobHierarchyListSegment> {
    /*
     * Added as of Jackson 2.12 as empty/missing XML was no longer triggering deserialize.
     */
    @Override
    public BlobHierarchyListSegment getNullValue(DeserializationContext ctxt) {
        return new BlobHierarchyListSegment();
    }

    @Override
    public BlobHierarchyListSegment deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ArrayList<BlobItemInternal> blobItems = new ArrayList<>();
        ArrayList<BlobPrefix> blobPrefixes = new ArrayList<>();

        JsonDeserializer<Object> blobItemDeserializer =
            ctxt.findRootValueDeserializer(ctxt.constructType(BlobItemInternal.class));
        JsonDeserializer<Object> blobPrefixDeserializer =
            ctxt.findRootValueDeserializer(ctxt.constructType(BlobPrefix.class));

        for (JsonToken currentToken = p.nextToken(); !currentToken.name().equals("END_OBJECT");
             currentToken = p.nextToken()) {
            // Get to the root element of the next item.
            p.nextToken();

            if (p.getCurrentName().equals("Blob")) {
                blobItems.add((BlobItemInternal) blobItemDeserializer.deserialize(p, ctxt));
            } else if (p.getCurrentName().equals("BlobPrefix")) {
                blobPrefixes.add((BlobPrefix) blobPrefixDeserializer.deserialize(p, ctxt));
            }
        }

        return new BlobHierarchyListSegment().setBlobItems(blobItems).setBlobPrefixes(blobPrefixes);
    }
}
