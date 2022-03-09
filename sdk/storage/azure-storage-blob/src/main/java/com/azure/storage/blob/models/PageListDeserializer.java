// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Custom Jackson JsonDeserializer that handles deserializing PageList responses.
 * <p>
 * PageList responses intersperse PageRange and ClearRange elements, without this deserializer if we received the
 * following response the resulting PageList would only contain one PageRange element and one ClearRange element.
 *
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="utf-8"?>
 * <PageList>
 *    <PageRange>
 *       <Start>Start Byte</Start>
 *       <End>End Byte</End>
 *    </PageRange>
 *    <ClearRange>
 *       <Start>Start Byte</Start>
 *       <End>End Byte</End>
 *    </ClearRange>
 *    <PageRange>
 *       <Start>Start Byte</Start>
 *       <End>End Byte</End>
 *    </PageRange>
 * </PageList>
 * }
 * </pre>
 *
 * With the custom deserializer the response correctly returns two PageRange elements and one ClearRange element.
 */
final class PageListDeserializer extends JsonDeserializer<PageList> {
    /*
     * Added as of Jackson 2.12 as empty/missing XML was no longer triggering deserialize.
     */
    @Override
    public PageList getNullValue(DeserializationContext ctxt) {
        return new PageList();
    }

    @Override
    public PageList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ArrayList<PageRange> pageRanges = new ArrayList<>();
        ArrayList<ClearRange> clearRanges = new ArrayList<>();

        // Get the deserializer that handles PageRange.
        JsonDeserializer<Object> pageRangeDeserializer =
            ctxt.findRootValueDeserializer(ctxt.constructType(PageRange.class));

        // Get the deserializer that handles ClearRange.
        JsonDeserializer<Object> clearRangeDeserializer =
            ctxt.findRootValueDeserializer(ctxt.constructType(ClearRange.class));

        for (JsonToken currentToken = p.nextToken(); currentToken.id() != JsonTokenId.ID_END_OBJECT;
             currentToken = p.nextToken()) {
            // Get to the root element of the next item.
            p.nextToken();

            if (p.getCurrentName().equals("PageRange")) {
                // Current token is the node that begins a PageRange object.
                pageRanges.add((PageRange) pageRangeDeserializer.deserialize(p, ctxt));
            } else if (p.getCurrentName().equals("ClearRange")) {
                // Current token is the node that begins a ClearRange object.
                clearRanges.add((ClearRange) clearRangeDeserializer.deserialize(p, ctxt));
            }
        }

        return new PageList().setPageRange(pageRanges).setClearRange(clearRanges);
    }
}
