// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.elevation;

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
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.elevation.models.ElevationResult;

import org.junit.jupiter.params.provider.Arguments;

public class TestUtils {
    static final String FAKE_API_KEY = "fakeKeyPlaceholder";
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

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
                Arrays.stream(ElevationServiceVersion.values())
                    .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });
        return argumentsList.stream();
    }

    static ElevationResult getExpectedDataForPoints() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getdataforpoints.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<ElevationResult> interimType = new TypeReference<ElevationResult>(){};
        is.close();
        return jacksonAdapter.<ElevationResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static ElevationResult getExpectedDataForPolyline() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getdataforpolyline.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<ElevationResult> interimType = new TypeReference<ElevationResult>(){};
        is.close();
        return jacksonAdapter.<ElevationResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static ElevationResult getExpectedDataForBoundingBox() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getdataforboundingbox.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<ElevationResult> interimType = new TypeReference<ElevationResult>(){};
        is.close();
        return jacksonAdapter.<ElevationResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static ElevationResult getExpectedPostDataForPoints() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("postdataforpoints.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<ElevationResult> interimType = new TypeReference<ElevationResult>(){};
        is.close();
        return jacksonAdapter.<ElevationResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static ElevationResult getExpectedPostDataForPolyline() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("postdataforpolyline.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<ElevationResult> interimType = new TypeReference<ElevationResult>(){};
        is.close();
        return jacksonAdapter.<ElevationResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    // Code referenced from
    // https://www.techiedelight.com/convert-inputstream-byte-array-java/#:~:text=Convert%20InputStream%20to%20byte%20array%20in%20Java%201,Commons%20IO%20...%204%204.%20Using%20sun.misc.IOUtils%20
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
