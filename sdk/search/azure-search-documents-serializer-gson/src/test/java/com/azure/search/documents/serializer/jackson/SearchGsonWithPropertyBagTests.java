// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.jackson;

import com.azure.core.experimental.serializer.JsonOptions;
import com.azure.core.serializer.json.gson.GsonJsonSerializer;
import com.azure.search.documents.serializer.SearchSerializerProviders;
import com.azure.search.documents.serializer.jackson.models.Book;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SearchGsonWithPropertyBagTests {
    private static GsonJsonSerializer gsonJsonSerializer;
    @BeforeAll
    public static void setup() {
        gsonJsonSerializer = SearchGsonSerializerProvider.createInstance(new JsonOptions().includeNulls());
    }

    @Test
    public void testObjectWithSerializeNull() {
        Book book = new Book()
            .isbn(null).title("Book1");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        StepVerifier.create(gsonJsonSerializer.serialize(buffer, book))
            .assertNext(outputStream -> {
                assert outputStream.toString().contains("Title");
                assert outputStream.toString().contains("ISBN");
                assert outputStream.toString().contains("Author");
                assert outputStream.toString().contains("PublishDate");
            })
            .verifyComplete();
    }

    @Test
    public void testMapWithSerializeNull() {
        Map<String, Object> bookMap = new HashMap<>();
        bookMap.put("ISBN", null);
        bookMap.put("title", "Book1");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        StepVerifier.create(gsonJsonSerializer.serialize(buffer, bookMap))
            .assertNext(outputStream -> {
                assert outputStream.toString().contains("title");
                assert outputStream.toString().contains("ISBN");
                assert !outputStream.toString().contains("Author");
                assert !outputStream.toString().contains("PublishDate");
            })
            .verifyComplete();
    }

}
