// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CloseableIterableStream;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HiddenGeneratedApiTests {
    @ParameterizedTest
    @ValueSource(classes = { KnowledgeBaseRetrievalClient.class, KnowledgeBaseRetrievalAsyncClient.class })
    public void nativeStreamMethodsRemainPublicAndRawProtocolsRemainHidden(Class<?> clientClass)
        throws NoSuchMethodException {
        boolean async = clientClass == KnowledgeBaseRetrievalAsyncClient.class;
        Class<?> streamType = async ? Flux.class : CloseableIterableStream.class;
        String returnType = streamType.getName() + "<" + KnowledgeBaseRetrievalStreamEvent.class.getName() + ">";
        for (Class<?>[] parameters : new Class<?>[][] {
            { KnowledgeBaseRetrievalOptions.class },
            { KnowledgeBaseRetrievalOptions.class, String.class },
            { KnowledgeBaseRetrievalOptions.class, String.class, String.class } }) {
            assertThrows(NoSuchMethodException.class,
                () -> clientClass.getDeclaredMethod("hiddenGeneratedRetrieveStream", parameters));
            Method method = clientClass.getDeclaredMethod("retrieveStream", parameters);
            assertTrue(Modifier.isPublic(method.getModifiers()));
            assertEquals(streamType, method.getReturnType());
            assertEquals(returnType, method.getGenericReturnType().getTypeName());
        }
        String protocolReturnType = Response.class.getName() + "<" + BinaryData.class.getName() + ">";
        if (async) {
            protocolReturnType = Mono.class.getName() + "<" + protocolReturnType + ">";
        }
        for (String operation : new String[] { "RetrieveStream", "Retrieve" }) {
            Method protocolMethod = clientClass.getDeclaredMethod("hiddenGenerated" + operation + "WithResponse",
                BinaryData.class, RequestOptions.class);
            assertEquals(0, protocolMethod.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE));
            assertEquals(async ? Mono.class : Response.class, protocolMethod.getReturnType());
            assertEquals(protocolReturnType, protocolMethod.getGenericReturnType().getTypeName());
            String publicName = Character.toLowerCase(operation.charAt(0)) + operation.substring(1) + "WithResponse";
            assertThrows(NoSuchMethodException.class,
                () -> clientClass.getDeclaredMethod(publicName, BinaryData.class, RequestOptions.class));
        }
    }
}
