// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.azure.core.test.TestBase;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.render.models.Copyright;
import com.azure.maps.render.models.CopyrightCaption;
import com.azure.maps.render.models.MapAttribution;
import com.azure.maps.render.models.MapTileset;

import org.junit.jupiter.params.provider.Arguments;

public class TestUtils {
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    static MapTileset getExpectedMapTileset() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("maptileset.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<MapTileset> interimType = new TypeReference<MapTileset>() { };
        is.close();
        return jacksonAdapter.<MapTileset>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static MapAttribution getExpectedMapAttribution() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("mapattribution.json");
        byte[] data = toByteArray(is);
        JsonSerializer jsonProvider = JsonSerializerProviders.createInstance(true);
        is.close();
        return jsonProvider.deserializeFromBytes(data, TypeReference.createInstance(MapAttribution.class));

    }

    static CopyrightCaption getExpectedCopyrightCaption() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("copyrightcaption.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<CopyrightCaption> interimType = new TypeReference<CopyrightCaption>() { };
        is.close();
        return jacksonAdapter.<CopyrightCaption>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static Copyright getExpectedCopyrightFromBoundingBox() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getcopyrightfromboundingbox.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<Copyright> interimType = new TypeReference<Copyright>() { };
        is.close();
        return jacksonAdapter.<Copyright>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static Copyright getExpectedCopyrightForTile() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getcopyrightfortile.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<Copyright> interimType = new TypeReference<Copyright>() { };
        is.close();
        return jacksonAdapter.<Copyright>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static Copyright getExpectedCopyrightForWorld() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getcopyrightforworld.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        // public static JsonSerializer createInstance(boolean useDefaultIfAbsent)
        TypeReference<Copyright> interimType = new TypeReference<Copyright>() { };
        is.close();
        return jacksonAdapter.<Copyright>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    public static Stream<Arguments> getTestParameters() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();
        TestBase.getHttpClients()
            .forEach(httpClient -> {
                Arrays.stream(MapsRenderServiceVersion.values())
                    .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });
        return argumentsList.stream();
    }

    /**
     * Code referenced from
     * https://www.techiedelight.com/convert-inputstream-byte-array-java/#:~:text=Convert%20InputStream%20to%20byte%20array%20in%20Java%201,Commons%20IO%20...%204%204.%20Using%20sun.misc.IOUtils%20
     * @param InputStream in
     * @return byte[]
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        // read bytes from the input stream and store them in the buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into the output stream
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }
}
