/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
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
package com.microsoft.azure.documentdb.rx.internal;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.microsoft.azure.documentdb.Resource;
import com.microsoft.azure.documentdb.SqlQuerySpec;
import com.microsoft.azure.documentdb.internal.AbstractDocumentServiceRequest;
import com.microsoft.azure.documentdb.internal.OperationType;
import com.microsoft.azure.documentdb.internal.PathsHelper;
import com.microsoft.azure.documentdb.internal.QueryCompatibilityMode;
import com.microsoft.azure.documentdb.internal.ResourceType;
import com.microsoft.azure.documentdb.internal.Utils;

import rx.Observable;
import rx.observables.StringObservable;

/**
 * This is core Transport/Connection agnostic request to the Azure DocumentDB database service.
 */
class RxDocumentServiceRequest extends AbstractDocumentServiceRequest {

    private final Observable<byte[]> contentObservable;
    private final byte[] byteContent;
    
    /**
     * Creates a DocumentServiceRequest
     *
     * @param resourceId        the resource Id.
     * @param resourceType      the resource type.
     * @param content           the byte content observable\
     * @param contentObservable the byte content observable
     * @param headers           the request headers.
     */
    private RxDocumentServiceRequest(OperationType operationType,
            String resourceId,
            ResourceType resourceType,
            Observable<byte[]> contentObservable,
            byte[] content,
            String path,
            Map<String, String> headers) {
        super( operationType,
             resourceId,
             resourceType,
             path,
             headers);
        this.byteContent = content;
        this.contentObservable = null;
    }

    /**
     * Creates a DocumentServiceRequest with an HttpEntity.
     *
     * @param resourceType          the resource type.
     * @param path                  the relative URI path.
     * @param contentObservable     the byte content observable
     * @param headers               the request headers.
     */
    private RxDocumentServiceRequest(OperationType operationType,
            ResourceType resourceType,
            String path,
            Observable<byte[]> contentObservable,
            Map<String, String> headers) {
        this(operationType, extractIdFromUri(path), resourceType, contentObservable, null, path, headers);
    }
    
    /**
     * Creates a DocumentServiceRequest with an HttpEntity.
     *
     * @param resourceType the resource type.
     * @param path         the relative URI path.
     * @param byteContent  the byte content.
     * @param headers      the request headers.
     */
    private RxDocumentServiceRequest(OperationType operationType,
            ResourceType resourceType,
            String path,
            byte[] byteContent,
            Map<String, String> headers) {
        this(operationType, extractIdFromUri(path), resourceType, null, byteContent, path, headers);
    }
    
    /**
     * Creates a DocumentServiceRequest with an HttpEntity.
     *
     * @param resourceType          the resource type.
     * @param path                  the relative URI path.
     * @param headers               the request headers.
     */
    private RxDocumentServiceRequest(OperationType operationType,
            ResourceType resourceType,
            String path,
            Map<String, String> headers) {
        this(operationType, extractIdFromUri(path), resourceType, null , null, path, headers);
    }

    /**
     * Creates a DocumentServiceRequest with a stream.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param content      the content observable
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            Observable<byte[]> content,
            Map<String, String> headers) {
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, content, headers);
    }
    
    /**
     * Creates a DocumentServiceRequest with a stream.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param inputStream  the input stream.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            InputStream inputStream,
            Map<String, String> headers) {
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, StringObservable.from(inputStream), headers);
    }
    
    /**
     * Creates a DocumentServiceRequest with a resource.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param resource     the resource of the request.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            Resource resource,
            Map<String, String> headers) {

        return new RxDocumentServiceRequest(operation, resourceType, relativePath,
                // TODO: this re-encodes, can we improve performance here?
                resource.toJson().getBytes(StandardCharsets.UTF_8), headers);
    }

    /**
     * Creates a DocumentServiceRequest with a query.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param query        the query.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            String query,
            Map<String, String> headers) {
        
        return new RxDocumentServiceRequest(operation, resourceType, relativePath,
                query.getBytes(StandardCharsets.UTF_8), headers);
    }

    /**
     * Creates a DocumentServiceRequest with a query.
     *
     * @param resourceType           the resource type.
     * @param relativePath           the relative URI path.
     * @param querySpec              the query.
     * @param queryCompatibilityMode the QueryCompatibilityMode mode.
     * @param headers                the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(ResourceType resourceType,
            String relativePath,
            SqlQuerySpec querySpec,
            QueryCompatibilityMode queryCompatibilityMode,
            Map<String, String> headers) {
        OperationType operation;
        String queryText;
        switch (queryCompatibilityMode) {
        case SqlQuery:
            if (querySpec.getParameters() != null && querySpec.getParameters().size() > 0) {
                throw new IllegalArgumentException(
                        String.format("Unsupported argument in query compatibility mode '{%s}'",
                                queryCompatibilityMode.name()));
            }

            operation = OperationType.SqlQuery;
            queryText = querySpec.getQueryText();
            break;

        case Default:
        case Query:
        default:
            operation = OperationType.Query;
            queryText = querySpec.toJson();
            break;
        }

        Observable<byte[]> body = StringObservable.encode(Observable.just(queryText), StandardCharsets.UTF_8);
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, body, headers);
    }

    /**
     * Creates a DocumentServiceRequest without body.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            Map<String, String> headers) {
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, headers);
    }

    /**
     * Creates a DocumentServiceRequest with a resourceId.
     *
     * @param operation    the operation type.
     * @param resourceId   the resource id.
     * @param resourceType the resource type.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            String resourceId,
            ResourceType resourceType,
            Map<String, String> headers) {
        String path = PathsHelper.generatePath(resourceType, resourceId, Utils.isFeedRequest(operation));
        return new RxDocumentServiceRequest(operation, resourceId, resourceType, null, null, path, headers);
    }

    public Observable<byte[]> getContentObservable() {
        return contentObservable;
    }
    
    public byte[] getContent() {
        return byteContent;
    }
}
