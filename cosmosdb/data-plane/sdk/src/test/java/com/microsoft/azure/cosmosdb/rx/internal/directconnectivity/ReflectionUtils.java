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

package com.microsoft.azure.cosmosdb.rx.internal.directconnectivity;

import com.microsoft.azure.cosmosdb.internal.directconnectivity.HttpTransportClient;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.ServerStoreModel;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.StoreClient;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.TransportClient;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentClientImpl;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 *
 * TransportClient transportClient = ReflectionUtils.getDirectHttpsHttpClient(documentClient);
 * TransportClient spyTransportClient = Mockito.spy(transportClient);
 * ReflectionUtils.setTransportClient(documentClient, spyTransportClient);
 *
 * // use the documentClient
 * // do assertion on the request and response spyTransportClient recieves using Mockito
 */
public class ReflectionUtils {

    private static <T> void set(Object object, T newValue, String fieldName) {
        try {
            FieldUtils.writeField(object, fieldName, newValue, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T get(Class<T> klass, Object object, String fieldName) {
        try {
            return (T) FieldUtils.readField(object, fieldName, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static ServerStoreModel getServerStoreModel(RxDocumentClientImpl client) {
        return get(ServerStoreModel.class, client, "storeModel");
    }

    public static StoreClient getStoreClient(RxDocumentClientImpl client) {
        ServerStoreModel serverStoreModel = getServerStoreModel(client);
        return get(StoreClient.class, serverStoreModel, "storeClient");
    }

    public static TransportClient getTransportClient(RxDocumentClientImpl client) {
        StoreClient storeClient = getStoreClient(client);
        return get(TransportClient.class, storeClient, "transportClient");
    }

    public static void setTransportClient(RxDocumentClientImpl client, TransportClient transportClient) {
        StoreClient storeClient = getStoreClient(client);
        set(storeClient, transportClient, "transportClient");
    }

    public static CompositeHttpClient<ByteBuf, ByteBuf> getDirectHttpsHttpClient(RxDocumentClientImpl client) {
        TransportClient transportClient = getTransportClient(client);
        assert transportClient instanceof HttpTransportClient;
        return get(CompositeHttpClient.class, transportClient, "httpClient");
    }

    public static void setDirectHttpsHttpClient(RxDocumentClientImpl client, CompositeHttpClient<ByteBuf, ByteBuf> newHttpClient) {
        TransportClient transportClient = getTransportClient(client);
        assert transportClient instanceof HttpTransportClient;
        set(transportClient, newHttpClient, "httpClient");
    }
}
