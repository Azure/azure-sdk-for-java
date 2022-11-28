// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic;

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
import com.azure.maps.traffic.models.TrafficIncidentDetail;
import com.azure.maps.traffic.models.TrafficFlowSegmentData;
import com.azure.maps.traffic.models.TrafficIncidentViewport;

import org.junit.jupiter.params.provider.Arguments;

public class TestUtils {
    static final String FAKE_API_KEY = "fakeApiKey";
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    static TrafficFlowSegmentData getExpectedTrafficFlowSegment() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("trafficflowsegment.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<TrafficFlowSegmentData> interimType = new TypeReference<TrafficFlowSegmentData>(){};
        is.close();
        return jacksonAdapter.<TrafficFlowSegmentData>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static TrafficIncidentDetail getExpectedTrafficIncidentDetail() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("trafficincidentdetail.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<TrafficIncidentDetail> interimType = new TypeReference<TrafficIncidentDetail>(){};
        is.close();
        return jacksonAdapter.<TrafficIncidentDetail>deserialize(data, interimType.getJavaType(),
        SerializerEncoding.JSON);
    }

    static TrafficIncidentViewport getExpectedTrafficIncidentViewport() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("trafficincidentviewport.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<TrafficIncidentViewport> interimType = new TypeReference<TrafficIncidentViewport>(){};
        is.close();
        return jacksonAdapter.<TrafficIncidentViewport>deserialize(data, interimType.getJavaType(),
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
                Arrays.stream(TrafficServiceVersion.values())
                    .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });
        return argumentsList.stream();
    }

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
