// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import com.azure.storage.file.share.implementation.models.DirectoryItem;
import com.azure.storage.file.share.implementation.models.FileItem;
import com.azure.storage.file.share.implementation.models.FilesAndDirectoriesListSegment;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Custom Jackson JsonDeserializer that handles deserializing FilesAndDirectoriesListSegment responses.
 * <p>
 * FilesAndDirectoriesListSegment responses intersperse DirectoryItem and FileItem elements, without this deserializer
 * if we received the following response the resulting FilesAndDirectoriesListSegment would only contain one
 * DirectoryItem element and one FileItem element.
 *
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="utf-8"?>
 * <Entries>
 *    <File>
 *       <Name>a</Name>
 *       <Properties>
 *           <Content-Length>2</Content-Length>
 *       </Properties>
 *    </File>
 *    <Directory>
 *       <Name>b</Name>
 *       <Properties />
 *    </Directory>
 *    <File>
 *       <Name>c</Name>
 *       <Properties>
 *           <Content-Length>2</Content-Length>
 *       </Properties>
 *    </File>
 *    <Directory>
 *       <Name>d</Name>
 *       <Properties />
 *    </Directory>
 *    <File>
 *       <Name>e</Name>
 *       <Properties>
 *           <Content-Length>2</Content-Length>
 *       </Properties>
 *    </File>
 * </Entries>
 * }
 * </pre>
 *
 * With the custom deserializer the response correctly returns two DirectoryItem elements and three FileItem elements.
 */

public final class FilesAndDirectoriesListSegmentDeserializer extends JsonDeserializer<FilesAndDirectoriesListSegment> {
    /*
     * Added as of Jackson 2.12 as empty/missing XML was no longer triggering deserialize.
     */
    @Override
    public FilesAndDirectoriesListSegment getNullValue(DeserializationContext ctxt) {
        return new FilesAndDirectoriesListSegment();
    }

    @Override
    public FilesAndDirectoriesListSegment deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ArrayList<DirectoryItem> directoryItems = new ArrayList<>();
        ArrayList<FileItem> fileItems = new ArrayList<>();

        // Get the deserializer that handles DirectoryItem.
        JsonDeserializer<Object> directoryItemDeserializer =
            ctxt.findRootValueDeserializer(ctxt.constructType(DirectoryItem.class));

        // Get the deserializer that handles FileItem.
        JsonDeserializer<Object> fileItemDeserializer =
            ctxt.findRootValueDeserializer(ctxt.constructType(FileItem.class));

        for (JsonToken currentToken = p.nextToken(); currentToken.id() != JsonTokenId.ID_END_OBJECT;
             currentToken = p.nextToken()) {
            // Get to the root element of the next item.
            p.nextToken();

            if (p.getCurrentName().equals("Directory")) {
                // Current token is the node that begins a DirectoryItem object.
                directoryItems.add((DirectoryItem) directoryItemDeserializer.deserialize(p, ctxt));
            } else if (p.getCurrentName().equals("File")) {
                // Current token is the node that begins a FileItem object.
                fileItems.add((FileItem) fileItemDeserializer.deserialize(p, ctxt));
            }
        }

        return new FilesAndDirectoriesListSegment().setDirectoryItems(directoryItems).setFileItems(fileItems);
    }
}
