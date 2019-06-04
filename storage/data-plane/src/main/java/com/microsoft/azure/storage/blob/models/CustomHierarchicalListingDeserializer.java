// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.storage.blob.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;

// implement ContextualDeserializer or ResolvableDeserializer?
final class CustomHierarchicalListingDeserializer extends JsonDeserializer<BlobHierarchyListSegment> {

    @Override
    public BlobHierarchyListSegment deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ArrayList<BlobItem> blobItems = new ArrayList<>();
        ArrayList<BlobPrefix> blobPrefixes = new ArrayList<>();

        JsonDeserializer<Object> blobItemDeserializer =
                ctxt.findRootValueDeserializer(ctxt.constructType(BlobItem.class));
        JsonDeserializer<Object> blobPrefixDeserializer =
                ctxt.findRootValueDeserializer(ctxt.constructType(BlobPrefix.class));

        for (JsonToken currentToken = p.nextToken(); !currentToken.name().equals("END_OBJECT");
             currentToken = p.nextToken()) {
            // Get to the root element of the next item.
            p.nextToken();

            if (p.getCurrentName().equals("Blob")) {
                blobItems.add((BlobItem)blobItemDeserializer.deserialize(p, ctxt));
            }
            else if (p.getCurrentName().equals("BlobPrefix")) {
                blobPrefixes.add((BlobPrefix)blobPrefixDeserializer.deserialize(p, ctxt));
            }
        }

        return new BlobHierarchyListSegment().withBlobItems(blobItems).withBlobPrefixes(blobPrefixes);
    }
}
