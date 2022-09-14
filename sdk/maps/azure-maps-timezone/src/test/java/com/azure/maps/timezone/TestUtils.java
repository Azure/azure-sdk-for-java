package com.azure.maps.timezone;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.azure.core.test.TestBase;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.timezone.models.IanaId;
import com.azure.maps.timezone.models.TimezoneIanaVersionResult;
import com.azure.maps.timezone.models.TimezoneResult;
import com.azure.maps.timezone.models.TimezoneWindows;
import com.fasterxml.jackson.databind.ObjectMapper;

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
                Arrays.stream(TimezoneServiceVersion.values())
                    .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });
        return argumentsList.stream();
    }

    static TimezoneResult getExpectedTimezoneById() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("gettimezonebyid.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<TimezoneResult> interimType = new TypeReference<TimezoneResult>(){};
        is.close();
        return jacksonAdapter.<TimezoneResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static TimezoneResult getExpectedTimezoneByCoordinates() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("gettimezonebycoordinates.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<TimezoneResult> interimType = new TypeReference<TimezoneResult>(){};
        is.close();
        return jacksonAdapter.<TimezoneResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }
    
    static List<TimezoneWindows> getExpectedWindowsTimezoneIds() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getwindowstimezonesids.json");
        String jsonArrayString = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        ObjectMapper mapper = new ObjectMapper();
        List<TimezoneWindows> expectedResult = new ArrayList<>();
        try {
            expectedResult = Arrays.asList(mapper.readValue(jsonArrayString, TimezoneWindows[].class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        is.close();
        return expectedResult;
    }

    static List<IanaId> getExpectedIanaTimezoneIds() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getianatimezoneids.json");
        String jsonArrayString = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        ObjectMapper mapper = new ObjectMapper();
        List<IanaId> expectedResult = new ArrayList<>();
        try {
            expectedResult = Arrays.asList(mapper.readValue(jsonArrayString, IanaId[].class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        is.close();
        return expectedResult;
    }

    static TimezoneIanaVersionResult getExpectedIanaVersion() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("gettimezoneianaversionresult.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<TimezoneIanaVersionResult> interimType = new TypeReference<TimezoneIanaVersionResult>(){};
        is.close();
        return jacksonAdapter.<TimezoneIanaVersionResult>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static List<IanaId> getExpectedConvertWindowsTimezoneToIana() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getconvertwindowstimezonetoiana.json");
        String jsonArrayString = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        ObjectMapper mapper = new ObjectMapper();
        List<IanaId> expectedResult = new ArrayList<>();
        try {
            expectedResult = Arrays.asList(mapper.readValue(jsonArrayString, IanaId[].class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        is.close();
        return expectedResult;
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
