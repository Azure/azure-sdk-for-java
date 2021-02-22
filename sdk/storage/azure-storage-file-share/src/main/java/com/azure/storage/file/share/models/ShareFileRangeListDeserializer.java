// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Custom Jackson JsonDeserializer that handles deserializing ShareFileRangeList responses.
 * <p>
 * ShareFileRangeList responses intersperse FileRange and ClearRange elements, without this deserializer if we received
 * the following response the resulting ShareFileRangeList would only contain one FileRange element and one ClearRange
 * element.
 *
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="utf-8"?>
 * <Ranges>
 *    <Range>
 *       <Start>Start Byte</Start>
 *       <End>End Byte</End>
 *    </Range>
 *    <ClearRange>
 *       <Start>Start Byte</Start>
 *       <End>End Byte</End>
 *    </ClearRange>
 *    <Range>
 *       <Start>Start Byte</Start>
 *       <End>End Byte</End>
 *    </Range>
 * </Ranges>
 * }
 * </pre>
 *
 * With the custom deserializer the response correctly returns two FileRange elements and one ClearRange element.
 */
final class ShareFileRangeListDeserializer extends JsonDeserializer<ShareFileRangeList> {
    /*
     * Added as of Jackson 2.12 as empty/missing XML was no longer triggering deserialize.
     */
    @Override
    public ShareFileRangeList getNullValue(DeserializationContext ctxt) {
        return new ShareFileRangeList();
    }

    @Override
    public ShareFileRangeList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ArrayList<FileRange> pageRanges = new ArrayList<>();
        ArrayList<ClearRange> clearRanges = new ArrayList<>();

        // Get the deserializer that handles PageRange.
        JsonDeserializer<Object> pageRangeDeserializer =
            ctxt.findRootValueDeserializer(ctxt.constructType(FileRange.class));

        // Get the deserializer that handles ClearRange.
        JsonDeserializer<Object> clearRangeDeserializer =
            ctxt.findRootValueDeserializer(ctxt.constructType(ClearRange.class));

        for (JsonToken currentToken = p.nextToken(); currentToken.id() != JsonTokenId.ID_END_OBJECT;
             currentToken = p.nextToken()) {
            // Get to the root element of the next item.
            p.nextToken();

            if (p.getCurrentName().equals("Range")) {
                // Current token is the node that begins a FileRange object.
                pageRanges.add((FileRange) pageRangeDeserializer.deserialize(p, ctxt));
            } else if (p.getCurrentName().equals("ClearRange")) {
                // Current token is the node that begins a ClearRange object.
                clearRanges.add((ClearRange) clearRangeDeserializer.deserialize(p, ctxt));
            }
        }

        return new ShareFileRangeList().setRanges(pageRanges).setClearRanges(clearRanges);
    }
}
