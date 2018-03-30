/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

import static com.microsoft.azure.cosmosdb.internal.Constants.QueryExecutionContext.INCREMENTAL_FEED_HEADER_VALUE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceResponse;


/**
 * This is meant to be used only internally as a bridge access to classes in
 * com.microsoft.azure.cosmosdb
 **/
public class BridgeInternal {

    public static Document documentFromObject(Object document, ObjectMapper mapper) {
        return Document.FromObject(document, mapper);
    }

    public static <T extends Resource> ResourceResponse<T> toResourceResponse(RxDocumentServiceResponse response,
            Class<T> cls) {
        return new ResourceResponse<T>(response, cls);
    }

    public static <T extends Resource> MediaResponse toMediaResponse(RxDocumentServiceResponse response, boolean willBuffer) {
        return new MediaResponse(response, willBuffer);
    }

    public static <T extends Resource> FeedResponse<T> toFeedResponsePage(RxDocumentServiceResponse response,
            Class<T> cls) {
        return new FeedResponse<T>(response.getQueryResponse(cls), response.getResponseHeaders());
    }

    public static <T extends Resource> FeedResponse<T> toChaneFeedResponsePage(RxDocumentServiceResponse response,
            Class<T> cls) {
        return new FeedResponse<T>(noChanges(response) ? Collections.emptyList(): response.getQueryResponse(cls), response.getResponseHeaders(), noChanges(response));
    }

    public static StoredProcedureResponse toStoredProcedureResponse(RxDocumentServiceResponse response) {
        return new StoredProcedureResponse(response);
    }

    public static DatabaseAccount toDatabaseAccount(RxDocumentServiceResponse response) {
        DatabaseAccount account = response.getResource(DatabaseAccount.class);

        // read the headers and set to the account
        Map<String, String> responseHeader = response.getResponseHeaders();

        account.setMaxMediaStorageUsageInMB(
                Long.valueOf(responseHeader.get(HttpConstants.HttpHeaders.MAX_MEDIA_STORAGE_USAGE_IN_MB)));
        account.setMediaStorageUsageInMB(
                Long.valueOf(responseHeader.get(HttpConstants.HttpHeaders.CURRENT_MEDIA_STORAGE_USAGE_IN_MB)));

        return account;
    }

    public static Map<String, String> getFeedHeaders(FeedOptionsBase options) {

        if (options == null)
            return new HashMap<>();

        Map<String, String> headers = new HashMap<>();

        if (options.getMaxItemCount() != null) {
            headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, options.getMaxItemCount().toString());
        }

        if (options instanceof ChangeFeedOptions) {
            ChangeFeedOptions changeFeedOptions = (ChangeFeedOptions) options;

            String ifNoneMatchValue = null;
            if (changeFeedOptions.getRequestContinuation() != null) {
                ifNoneMatchValue = changeFeedOptions.getRequestContinuation();
            } else if (!changeFeedOptions.isStartFromBeginning()) {
                ifNoneMatchValue = "*";
            }
            // On REST level, change feed is using IfNoneMatch/ETag instead of
            // continuation.
            if (ifNoneMatchValue != null) {
                headers.put(HttpConstants.HttpHeaders.IF_NONE_MATCH, ifNoneMatchValue);
            }

            headers.put(HttpConstants.HttpHeaders.A_IM, INCREMENTAL_FEED_HEADER_VALUE);
        } else if (options.getRequestContinuation() != null) {
            headers.put(HttpConstants.HttpHeaders.CONTINUATION, options.getRequestContinuation());
        }

        FeedOptions feedOptions = options instanceof FeedOptions ? (FeedOptions) options : null;
        if (feedOptions != null) {
            if (feedOptions.getSessionToken() != null) {
                headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, feedOptions.getSessionToken());
            }

            if (feedOptions.getEnableScanInQuery() != null) {
                headers.put(HttpConstants.HttpHeaders.ENABLE_SCAN_IN_QUERY,
                        feedOptions.getEnableScanInQuery().toString());
            }

            if (feedOptions.getEmitVerboseTracesInQuery() != null) {
                headers.put(HttpConstants.HttpHeaders.EMIT_VERBOSE_TRACES_IN_QUERY,
                        feedOptions.getEmitVerboseTracesInQuery().toString());
            }

            if (feedOptions.getEnableCrossPartitionQuery() != null) {
                headers.put(HttpConstants.HttpHeaders.ENABLE_CROSS_PARTITION_QUERY,
                        feedOptions.getEnableCrossPartitionQuery().toString());
            }

            if (feedOptions.getMaxDegreeOfParallelism() != 0) {
                headers.put(HttpConstants.HttpHeaders.PARALLELIZE_CROSS_PARTITION_QUERY, Boolean.TRUE.toString());
            }
        }

        return headers;
    }

    public static <T extends Resource> boolean noChanges(FeedResponse<T> page) {
        return page.nochanges;
    }

    public static <T extends Resource> boolean noChanges(RxDocumentServiceResponse rsp) {
        return rsp.getStatusCode() == HttpConstants.StatusCodes.NOT_MODIFIED;
    }

    public static <T extends Resource> FeedResponse<T> createFeedResponse(List<T> results, Map<String, String> headers ) {
        return new FeedResponse<>(results, headers);
    }
}
