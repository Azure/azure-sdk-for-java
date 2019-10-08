// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.search.data.implementation.SerializationUtil;
import com.azure.search.data.customization.models.GeoPoint;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Functional tests that ensure expected behavior of deserializing a document.
 */
public class DocumentConverterTests {

    private final String testDateString = "2016-10-10T17:41:05.123-07:00";
    private final OffsetDateTime testDate = OffsetDateTime.of(2016, 10, 10, 17, 41, 5, 123 * 1_000_000, ZoneOffset.of("-07:00"));

    private Document deserialize(String json) {
        // Deserialization of the search result is done with azure-core (using Jackson as well)
        // the result object is a map of key:value, get deserialized directly into the Document object
        // Document is simply a Hash Map.
        // in this case we simulate creation of the object created by azure-core

        JacksonAdapter adapter = new JacksonAdapter();
        SerializationUtil.configureMapper(adapter.serializer());

        Document doc = new Document();
        try {
            doc = adapter.deserialize(json, Document.class, SerializerEncoding.JSON);
            // Skip OData @search annotations. These are deserialized separately.
            doc = cleanupODataAnnotation(doc);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    private Document cleanupODataAnnotation(Document document) {
        // Skip OData @search annotations. These are deserialized separately.
        List<String> keys = document.keySet().stream().filter(key -> key.startsWith("@search")).collect(Collectors.toList());
        keys.forEach(key -> document.remove(key));
        return document;
    }

    @Test
    public void annotationsAreExcludedFromDocument() {
        String json = "{ \"@search.score\": 3.14, \"field1\": \"value1\", \"field2\": 123, \"@search.someOtherAnnotation\": { \"a\": \"b\" }, \"field3\": 2.78 }";
        Document expectedDoc = new Document() {
            {
                put("field1", "value1");
                put("field2", 123);
                put("field3", 2.78);
            }
        };

        Document actualDoc = deserialize(json);
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void canReadNullValues() {
        String json = "{\"field1\": null,\"field2\": [ \"hello\", null ], \"field3\": [ null, 123, null ], \"field4\": [ null, { \"name\": \"Bob\" } ]}";
        Document expectedDoc = new Document() {
            {
                put("field1", null);
                put("field2", Arrays.asList("hello", null));
                put("field3", Arrays.asList(null, 123, null));
                put("field4", Arrays.asList(null, new Document() {
                    {
                        put("name", "Bob");
                    }
                }));
            }
        };

        Document actualDoc = deserialize(json);
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void canReadPrimitiveTypes() {
        Map<String, Object> values = new HashMap<String, Object>() {
            {
                put("123", 123);
                put("9999999999999", 9_999_999_999_999L);
                put("3.14", 3.14);
                put("\"hello\"", "hello");
                put("true", true);
                put("false", false);
            }
        };

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String jsonValue = entry.getKey();
            Object expectedObject = entry.getValue();
            String json = "{\"field\" :".concat(jsonValue).concat("}");
            Document expectedDoc = new Document() {
                {
                    put("field", expectedObject);
                }
            };

            Document actualDoc = deserialize(json);
            Assert.assertEquals(expectedDoc, actualDoc);
        }
    }

    @Test
    public void canReadArraysOfPrimitiveTypes() {
        Map<String, Object> values = new HashMap<String, Object>() {
            {
                put("[\"hello\", \"goodbye\"]", Arrays.asList("hello", "goodbye"));
                put("[123, 456]", Arrays.asList(123, 456));
                put("[9999999999999, -12]", Arrays.asList(9_999_999_999_999L, -12));
                put("[3.14, 2.78]", Arrays.asList(3.14, 2.78));
                put("[true, false]", Arrays.asList(true, false));
            }
        };

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String jsonArray = entry.getKey();
            Object expectedArray = entry.getValue();
            String json = "{\"field\" :".concat(jsonArray).concat("}");
            Document expectedDoc = new Document() {
                {
                    put("field", expectedArray);
                }
            };

            Document actualDoc = deserialize(json);
            Assert.assertEquals(expectedDoc, actualDoc);
        }
    }


    @Test
    public void canReadGeoPoint() {
        String json = "{ \"field\": { \"type\": \"Point\", \"coordinates\": [-122.131577, 47.678581] } }";
        Document expectedDoc = new Document() {
            {
                put("field", GeoPoint.create(47.678581, -122.131577));
            }
        };

        Document actualDoc = deserialize(json);
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void canReadGeoPointCollection() {
        String json = "{ \"field\": [{ \"type\": \"Point\", \"coordinates\": [-122.131577, 47.678581] }, { \"type\": \"Point\", \"coordinates\": [-121, 49] }]}";
        Document expectedDoc = new Document() {
            {
                put("field", Arrays.asList(
                    GeoPoint.create(47.678581, -122.131577),
                    GeoPoint.create(49, -121))
                );
            }
        };

        Document actualDoc = deserialize(json);
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void canReadComplexObject() {
        String json = "{\"name\" : \"Boots\", \"details\": {\"sku\" : 123, \"seasons\" : [\"fall\", \"winter\"]}}";
        Document expectedDoc = new Document() {
            {
                put("name", "Boots");
                put("details", new Document() {
                    {
                        put("sku", 123);
                        put("seasons", Arrays.asList("fall", "winter"));
                    }
                });
            }
        };

        Document actualDoc = deserialize(json);
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void canReadComplexCollection() {
        String json = "{\"stores\" : [{\"name\" : \"North\", \"address\" : {\"city\" : \"Vancouver\", \"country\": \"Canada\"}, \"location\": {\"type\" : \"Point\", \"coordinates\": [-121, 49]}},{\"name\" : \"South\", \"address\" : {\"city\": \"Seattle\", \"country\" : \"USA\"}, \"location\" : {\"type\" : \"Point\", \"coordinates\": [-122.5, 47.6]}}]}";
        Document expectedDoc = new Document() {
            {
                put("stores", Arrays.asList(
                    new Document() {
                        {
                            put("name", "North");
                            put("address", new Document() {
                                    {
                                        put("city", "Vancouver");
                                        put("country", "Canada");
                                    }
                                }
                            );
                            put("location", new Document() {
                                    {
                                        put("type", "Point");
                                        put("coordinates", Arrays.asList(-121, 49));
                                    }
                                }
                            );
                        }
                    },
                    new Document() {
                        {
                            put("name", "South");
                            put("address", new Document() {
                                {
                                    put("city", "Seattle");
                                    put("country", "USA");
                                }
                            });
                            put("location", new Document() {
                                {
                                    put("type", "Point");
                                    put("coordinates", Arrays.asList(-122.5, 47.6));
                                }
                            });
                        }
                    }
                ));
            }
        };

        Document actualDoc = deserialize(json);
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void dateTimeStringsAreReadAsDateTime() {
        String json = "{\"field1\":\"".concat(testDateString).concat("\",\"field2\" : [\"").concat(testDateString).concat("\", \"").concat(testDateString).concat("\"]}");
        Document expectedDoc = new Document() {
            {
                put("field1", testDate);
                put("field2", Arrays.asList(testDate, testDate));
            }
        };

        Document actualDoc = deserialize(json);
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void specialDoublesAreReadAsStrings() {
        String json = "{\"field1\" : \"NaN\", \"field2\": \"INF\", \"field3\": \"-INF\", \"field4\": [\"NaN\", \"INF\", \"-INF\"], \"field5\": {\"value\":\"-INF\"}}";
        Document expectedDoc = new Document() {
            {
                put("field1", "NaN");
                put("field2", "INF");
                put("field3", "-INF");
                put("field4", Arrays.asList("NaN", "INF", "-INF"));
                put("field5", new Document() {
                    {
                        put("value", "-INF");
                    }
                });
            }
        };

        Document actualDoc = deserialize(json);
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void dateTimeStringsInArraysAreReadAsDateTime() {
        String json = "{ \"field\": [ \"hello\", \"".concat(testDateString).concat("\", \"123\" ] }}");
        Document expectedDoc = new Document() {
            {
                put("field", Arrays.asList("hello", testDate, "123"));
            }
        };

        Document actualDoc = deserialize(json);
        Assert.assertEquals(expectedDoc, actualDoc);
    }
}
