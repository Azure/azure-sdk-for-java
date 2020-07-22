// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.jackson;

import com.azure.core.serializer.json.gson.GsonJsonSerializer;
import com.azure.search.documents.serializer.SearchSerializerProviders;
import com.azure.search.documents.serializer.jackson.models.Author;
import com.azure.search.documents.serializer.jackson.models.Book;
import com.azure.search.documents.serializer.jackson.models.DateLib;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class SearchGsonSerializerTests {
    private static GsonJsonSerializer gsonJsonSerializer;
    static final String BOOK_DATA_JSON = "Book.json";
    static final String DATE_LIB_DATA_JSON = "DateLib.json";
    private static Date date;
    @BeforeAll
    public static void setup() {
        gsonJsonSerializer = SearchGsonSerializerProviders.createInstance();

        Date dateEpoch = Date.from(Instant.ofEpochMilli(1468800000L));
        date = new Date(dateEpoch.getYear(), dateEpoch.getMonth(), dateEpoch.getDate(),
            dateEpoch.getHours(), dateEpoch.getMinutes(), dateEpoch.getSeconds());
    }

    @Test
    public void testSerialize() {
        Book book = new Book().author(new Author().firstName("my").lastName("name"))
            .isbn("ISBN").title("Book1").publishDate(OffsetDateTime.parse("2010-06-27T00:00:00Z"));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        StepVerifier.create(gsonJsonSerializer.serialize(buffer, book))
            .assertNext(outputStream -> {
                assert book.toString().equals(outputStream.toString());
            })
            .verifyComplete();
    }

    @Test
    public void testDeserializeClazz()  {
        Book book = new Book().author(new Author().firstName("my").lastName("name"))
            .isbn("1").title("Book1").publishDate(OffsetDateTime.parse("2010-06-27T00:00:00Z"));
        InputStream inputStream = Objects.requireNonNull(SearchGsonSerializerTests.class.getClassLoader()
            .getResourceAsStream(BOOK_DATA_JSON));

        StepVerifier.create(gsonJsonSerializer.deserialize(inputStream, Book.class))
            .assertNext(bookObject -> {
                assert book.toString().equals(bookObject.toString());
            })
            .verifyComplete();
    }

    @Test
    public void testDeserializeType() {
        Map<String, Object> bookMap = new HashMap<>();
        bookMap.put("author", new Author().firstName("my").lastName("name"));
        bookMap.put("ISBN", "1");
        bookMap.put("title", "Book1");
        bookMap.put("publishDate", OffsetDateTime.parse("2010-06-27T00:00:00Z"));

        InputStream inputStream = Objects.requireNonNull(SearchGsonSerializerTests.class.getClassLoader()
            .getResourceAsStream(BOOK_DATA_JSON));

        StepVerifier.create(gsonJsonSerializer.deserializeToMap(inputStream))
            .assertNext(objectMap -> {
                assert objectMap.get("Author").toString().contains(((Author) bookMap.get("author")).firstName());
                assert objectMap.get("Author").toString().contains(((Author) bookMap.get("author")).lastName());

                assert bookMap.get("ISBN").equals(objectMap.get("ISBN"));
                assert bookMap.get("title").equals(objectMap.get("Title"));
                assert ((OffsetDateTime) bookMap.get("publishDate")).format(DateTimeFormatter.ISO_INSTANT)
                    .equals(objectMap.get("PublishDate"));
            })
            .verifyComplete();
    }

    @Test
    public void testDateTimeSerializer() {
        DateLib dateLib = new DateLib().setDate(date).setId("1");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        StepVerifier.create(gsonJsonSerializer.serialize(buffer, dateLib))
            .assertNext(outputStream -> {
                dateLib.toString().equals(outputStream.toString());
            })
            .verifyComplete();
    }

    @Test
    public void testDateTimeDeserializerWithClass() {
        DateLib dateLib = new DateLib().setDate(date).setId("1");
        InputStream inputStream = Objects.requireNonNull(SearchGsonSerializerTests.class.getClassLoader()
            .getResourceAsStream(DATE_LIB_DATA_JSON));
        StepVerifier.create(gsonJsonSerializer.deserialize(inputStream, DateLib.class))
            .assertNext(returnedDateLib -> {
                assert dateLib.toString().equals(returnedDateLib.toString());
            })
            .verifyComplete();
    }

    @Test
    public void testDateTimeDeserializerWithMap() {
        Map<String, Object> dateMap = new HashMap<>();
        dateMap.put("date", date.toInstant().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        dateMap.put("id", "1");

        InputStream inputStream = Objects.requireNonNull(SearchGsonSerializerTests.class.getClassLoader().getResourceAsStream(DATE_LIB_DATA_JSON));
        StepVerifier.create(gsonJsonSerializer.deserializeToMap(inputStream))
            .assertNext(returnedDateMap -> {
                dateMap.forEach((key, value) -> {
                    assert value.toString().equals(returnedDateMap.get(key).toString());
                });
            })
            .verifyComplete();
    }

    @Test
    public void testObjectWithSerializeNull() {
        Book book = new Book()
            .isbn(null).title("Book1");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        StepVerifier.create(gsonJsonSerializer.serialize(buffer, book))
            .assertNext(outputStream -> {
                assert outputStream.toString().contains("Title");
                assert !outputStream.toString().contains("ISBN");
                assert !outputStream.toString().contains("Author");
                assert !outputStream.toString().contains("PublishDate");
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
                assert !outputStream.toString().contains("ISBN");
                assert !outputStream.toString().contains("Author");
                assert !outputStream.toString().contains("PublishDate");
            })
            .verifyComplete();
    }
}
