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
package com.microsoft.azure.documentdb;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.microsoft.azure.documentdb.internal.AbstractDocumentServiceRequest;
import com.microsoft.azure.documentdb.internal.CollectionCacheInternal;
import com.microsoft.azure.documentdb.internal.DocumentServiceResponse;
import com.microsoft.azure.documentdb.internal.EndpointManager;
import com.microsoft.azure.documentdb.internal.UserAgentContainer;
import com.microsoft.azure.documentdb.internal.routing.ClientCollectionCache;
import com.microsoft.azure.documentdb.rx.AsyncDocumentClient;
import com.microsoft.azure.documentdb.rx.internal.Constants;
import com.microsoft.azure.documentdb.rx.internal.RxDocumentClientImpl;

/**
 * This is meant to be used only internally as a bridge access to 
 * classes in com.microsoft.azure.documentdb
 **/
public class BridgeInternal {

    public static Document documentFromObject(Object document) {
        return Document.FromObject(document);
    }
    
    public static DocumentClient createDocumentClient(String serviceEndpoint, String masterKey, ConnectionPolicy connectionPolicy, ConsistencyLevel consistencyLevel) {
        return new DocumentClient(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, null, null,
                new UserAgentContainer(Constants.Versions.SDK_NAME, Constants.Versions.SDK_VERSION));
    }

    public static <T extends Resource> ResourceResponse<T> toResourceResponse(DocumentServiceResponse response, Class<T> cls) {
        return new ResourceResponse<T>(response, cls);
    }
    
    public static void validateResource(Resource resource){
        DocumentClient.validateResource(resource);
    }
    
    public static void addPartitionKeyInformation(AbstractDocumentServiceRequest request,
            Document document,
            RequestOptions options, DocumentCollection collection){
        DocumentClient.addPartitionKeyInformation(request, document, options, collection);
    }

    public static ClientCollectionCache createClientCollectionCache(AsyncDocumentClient asyncClient, ExecutorService executorService) {
        CollectionCacheInternal collectionReader = new CollectionCacheInternal() {
            
            @Override
            public ResourceResponse<DocumentCollection> readCollection(String collectionLink, RequestOptions options)
                    throws DocumentClientException {
                return asyncClient.readCollection(collectionLink, options).toBlocking().single();
            }
        };
        return new ClientCollectionCache(collectionReader, executorService);
    }
    
    public static Map<String, String> getRequestHeaders(RequestOptions options) {
        return DocumentClient.getRequestHeaders(options);
    }
    
    public static EndpointManager createGlobalEndpointManager(RxDocumentClientImpl asyncClient) {
        
        DatabaseAccountManagerInternal databaseAccountManager = new DatabaseAccountManagerInternal() {
            
            @Override
            public URI getServiceEndpoint() {
                return asyncClient.getServiceEndpoint();
            }
            
            @Override
            public DatabaseAccount getDatabaseAccountFromEndpoint(URI endpoint) throws DocumentClientException {
                return asyncClient.getDatabaseAccountFromEndpoint(endpoint).toBlocking().single();
            }
            
            @Override
            public ConnectionPolicy getConnectionPolicy() {
                return asyncClient.getConnectionPolicy();
            }
        };
        return new GlobalEndpointManager(databaseAccountManager);
    }
}
