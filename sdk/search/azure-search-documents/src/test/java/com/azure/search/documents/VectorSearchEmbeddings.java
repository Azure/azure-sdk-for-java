// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

public class VectorSearchEmbeddings {
    public static final List<Float> HOTEL1_VECTORIZE_DESCRIPTION;
    public static final List<Float> HOTEL2_VECTORIZE_DESCRIPTION;
    public static final List<Float> HOTEL3_VECTORIZE_DESCRIPTION;
    public static final List<Float> HOTEL4_VECTORIZE_DESCRIPTION;
    public static final List<Float> HOTEL5_VECTORIZE_DESCRIPTION;
    public static final List<Float> HOTEL6_VECTORIZE_DESCRIPTION;
    public static final List<Float> HOTEL7_VECTORIZE_DESCRIPTION;
    public static final List<Float> HOTEL8_VECTORIZE_DESCRIPTION;
    public static final List<Float> HOTEL9_VECTORIZE_DESCRIPTION;
    public static final List<Float> HOTEL10_VECTORIZE_DESCRIPTION;
    public static final List<Float> SEARCH_VECTORIZE_DESCRIPTION;
    public static final List<Float> DEFAULT_VECTORIZE_DESCRIPTION;

    static {
        InputStream stream
            = VectorSearchEmbeddings.class.getClassLoader().getResourceAsStream("VectorSearchEmbeddings.json");

        try (JsonReader jsonReader = JsonProviders.createReader(stream)) {
            Map<String, List<Float>> embeddings = jsonReader.readMap(reader -> reader.readArray(JsonReader::getFloat));

            HOTEL1_VECTORIZE_DESCRIPTION = embeddings.get("HOTEL1_VECTORIZE_DESCRIPTION");
            HOTEL2_VECTORIZE_DESCRIPTION = embeddings.get("HOTEL2_VECTORIZE_DESCRIPTION");
            HOTEL3_VECTORIZE_DESCRIPTION = embeddings.get("HOTEL3_VECTORIZE_DESCRIPTION");
            HOTEL4_VECTORIZE_DESCRIPTION = embeddings.get("HOTEL4_VECTORIZE_DESCRIPTION");
            HOTEL5_VECTORIZE_DESCRIPTION = embeddings.get("HOTEL5_VECTORIZE_DESCRIPTION");
            HOTEL6_VECTORIZE_DESCRIPTION = embeddings.get("HOTEL6_VECTORIZE_DESCRIPTION");
            HOTEL7_VECTORIZE_DESCRIPTION = embeddings.get("HOTEL7_VECTORIZE_DESCRIPTION");
            HOTEL8_VECTORIZE_DESCRIPTION = embeddings.get("HOTEL8_VECTORIZE_DESCRIPTION");
            HOTEL9_VECTORIZE_DESCRIPTION = embeddings.get("HOTEL9_VECTORIZE_DESCRIPTION");
            HOTEL10_VECTORIZE_DESCRIPTION = embeddings.get("HOTEL10_VECTORIZE_DESCRIPTION");
            SEARCH_VECTORIZE_DESCRIPTION = embeddings.get("SEARCH_VECTORIZE_DESCRIPTION");
            DEFAULT_VECTORIZE_DESCRIPTION = embeddings.get("DEFAULT_VECTORIZE_DESCRIPTION");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
