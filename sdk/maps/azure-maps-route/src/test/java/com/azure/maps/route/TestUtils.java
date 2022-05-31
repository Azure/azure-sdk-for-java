package com.azure.maps.route;

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
import com.azure.maps.route.models.RouteDirections;
import com.azure.maps.route.models.RouteDirectionsBatchResult;
import com.azure.maps.route.models.RouteMatrixResult;
import com.azure.maps.route.models.RouteRangeResult;

import org.junit.jupiter.params.provider.Arguments;

public class TestUtils {
    static final String FAKE_API_KEY = "1234567890";
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
                Arrays.stream(RouteServiceVersion.values())
                    .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });
        return argumentsList.stream();
    }

    static RouteMatrixResult getExpectedBeginRequestRouteMatrix() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("beginrequestroutematrix.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<RouteMatrixResult> interimType = new TypeReference<RouteMatrixResult>(){};
        RouteMatrixResult result = jacksonAdapter.<RouteMatrixResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static RouteMatrixResult getExpectedGetRequestRouteMatrix() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getrequestroutematrix.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<RouteMatrixResult> interimType = new TypeReference<RouteMatrixResult>(){};
        RouteMatrixResult result = jacksonAdapter.<RouteMatrixResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static RouteDirections getExpectedRouteDirections() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getroutedirections.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<RouteDirections> interimType = new TypeReference<RouteDirections>(){};
        RouteDirections result = jacksonAdapter.<RouteDirections>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static RouteDirections getExpectedRouteDirectionsWithAdditionalParameters() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getroutedirectionsadditionalparams.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<RouteDirections> interimType = new TypeReference<RouteDirections>(){};
        RouteDirections result = jacksonAdapter.<RouteDirections>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static RouteRangeResult getExpectedRouteRange() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getrouterange.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<RouteRangeResult> interimType = new TypeReference<RouteRangeResult>(){};
        RouteRangeResult result = jacksonAdapter.<RouteRangeResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static RouteDirectionsBatchResult getExpectedBeginRequestRouteDirectionsBatch() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("beginrequestroutedirectionsbatch.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<RouteDirectionsBatchResult> interimType = new TypeReference<RouteDirectionsBatchResult>(){};
        RouteDirectionsBatchResult result = jacksonAdapter.<RouteDirectionsBatchResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static RouteDirectionsBatchResult getExpectedBeginRequestRouteDirectionsBatchBatchId() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("beginrequestroutedirectionsbatchbatchid.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<RouteDirectionsBatchResult> interimType = new TypeReference<RouteDirectionsBatchResult>(){};
        RouteDirectionsBatchResult result = jacksonAdapter.<RouteDirectionsBatchResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
        is.close();
        return result;
    }

    // Code referenced from 
    // https://www.techiedelight.com/convert-inputstream-byte-array-java/#:~:text=Convert%20InputStream%20to%20byte%20array%20in%20Java%201,Commons%20IO%20...%204%204.%20Using%20sun.misc.IOUtils%20
    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        // read bytes from the input stream and store them in the buffer
        while ((len = in.read(buffer)) != -1)
        {
            // write bytes from the buffer into the output stream
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }
}