// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;

final class CustomFileAndDirectoryListingDeserializer extends JsonDeserializer<FilesAndDirectoriesListSegment> {
    @Override
    public FilesAndDirectoriesListSegment deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ArrayList<DirectoryItem> directoryItems = new ArrayList<>();
        ArrayList<FileItem> fileItems = new ArrayList<>();

        JsonDeserializer<Object> directoryItemDeserializer =
            ctxt.findRootValueDeserializer(ctxt.constructType(DirectoryItem.class));
        JsonDeserializer<Object> fileItemDeserializer =
            ctxt.findRootValueDeserializer(ctxt.constructType(FileItem.class));

        for (JsonToken currentToken = p.nextToken(); !currentToken.name().equals("END_OBJECT");
             currentToken = p.nextToken()) {
            // Get to the root element of the next item.
            p.nextToken();

            if (p.getCurrentName().equals("Directory")) {
                directoryItems.add((DirectoryItem) directoryItemDeserializer.deserialize(p, ctxt));
            } else if (p.getCurrentName().equals("File")) {
                fileItems.add((FileItem) fileItemDeserializer.deserialize(p, ctxt));
            }
        }

        return new FilesAndDirectoriesListSegment().setDirectoryItems(directoryItems).setFileItems(fileItems);
    }
}
