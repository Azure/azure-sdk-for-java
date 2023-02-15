// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather;

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
import com.azure.maps.weather.models.ActiveStormResult;
import com.azure.maps.weather.models.AirQualityResult;
import com.azure.maps.weather.models.CurrentConditionsResult;
import com.azure.maps.weather.models.DailyAirQualityForecastResult;
import com.azure.maps.weather.models.DailyForecastResult;
import com.azure.maps.weather.models.DailyHistoricalActualsResult;
import com.azure.maps.weather.models.DailyHistoricalNormalsResult;
import com.azure.maps.weather.models.DailyHistoricalRecordsResult;
import com.azure.maps.weather.models.DailyIndicesResult;
import com.azure.maps.weather.models.HourlyForecastResult;
import com.azure.maps.weather.models.MinuteForecastResult;
import com.azure.maps.weather.models.QuarterDayForecastResult;
import com.azure.maps.weather.models.SevereWeatherAlertsResult;
import com.azure.maps.weather.models.StormForecastResult;
import com.azure.maps.weather.models.StormLocationsResult;
import com.azure.maps.weather.models.StormSearchResult;
import com.azure.maps.weather.models.WeatherAlongRouteResult;

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
                Arrays.stream(WeatherServiceVersion.values())
                    .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });
        return argumentsList.stream();
    }

    static HourlyForecastResult getExpectedHourlyForecast() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("gethourlyforecast.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<HourlyForecastResult> interimType = new TypeReference<HourlyForecastResult>(){};
        HourlyForecastResult result = jacksonAdapter.<HourlyForecastResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static MinuteForecastResult getExpectedMinuteForecast() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getminuteforecast.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<MinuteForecastResult> interimType = new TypeReference<MinuteForecastResult>(){};
        MinuteForecastResult result = jacksonAdapter.<MinuteForecastResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static QuarterDayForecastResult getExpectedQuarterDayForecast() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getquarterdayforecast.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<QuarterDayForecastResult> interimType = new TypeReference<QuarterDayForecastResult>(){};
        QuarterDayForecastResult result = jacksonAdapter.<QuarterDayForecastResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static CurrentConditionsResult getExpectedCurrentConditions() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getcurrentconditions.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<CurrentConditionsResult> interimType = new TypeReference<CurrentConditionsResult>(){};
        CurrentConditionsResult result = jacksonAdapter.<CurrentConditionsResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static DailyForecastResult getExpectedDailyForecast() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getdailyforecast.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<DailyForecastResult> interimType = new TypeReference<DailyForecastResult>(){};
        DailyForecastResult result = jacksonAdapter.<DailyForecastResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static WeatherAlongRouteResult getExpectedWeatherAlongRoute() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getweatheralongroute.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<WeatherAlongRouteResult> interimType = new TypeReference<WeatherAlongRouteResult>(){};
        WeatherAlongRouteResult result = jacksonAdapter.<WeatherAlongRouteResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static SevereWeatherAlertsResult getExpectedSevereWeatherAlerts() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getsevereweatheralerts.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<SevereWeatherAlertsResult> interimType = new TypeReference<SevereWeatherAlertsResult>(){};
        SevereWeatherAlertsResult result = jacksonAdapter.<SevereWeatherAlertsResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static DailyIndicesResult getExpectedDailyIndices() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getdailyindices.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<DailyIndicesResult> interimType = new TypeReference<DailyIndicesResult>(){};
        DailyIndicesResult result = jacksonAdapter.<DailyIndicesResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static ActiveStormResult getExpectedTropicalStormActive() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("gettropicalstormactive.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<ActiveStormResult> interimType = new TypeReference<ActiveStormResult>(){};
        ActiveStormResult result = jacksonAdapter.<ActiveStormResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static StormSearchResult getExpectedSearchTropicalStorm() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("gettropicalstormactive.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<StormSearchResult> interimType = new TypeReference<StormSearchResult>(){};
        StormSearchResult result = jacksonAdapter.<StormSearchResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static StormForecastResult getExpectedTropicalStormForecast() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("gettropicalstormforecast.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<StormForecastResult> interimType = new TypeReference<StormForecastResult>(){};
        StormForecastResult result = jacksonAdapter.<StormForecastResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static StormLocationsResult getExpectedTropicalStormLocations() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("gettropicalstormlocations.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<StormLocationsResult> interimType = new TypeReference<StormLocationsResult>(){};
        StormLocationsResult result = jacksonAdapter.<StormLocationsResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static AirQualityResult getExpectedCurrentAirQuality() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getcurrentairquality.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<AirQualityResult> interimType = new TypeReference<AirQualityResult>(){};
        AirQualityResult result = jacksonAdapter.<AirQualityResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static DailyAirQualityForecastResult getExpectedAirQualityDailyForecasts() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getairqualitydailyforecasts.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<DailyAirQualityForecastResult> interimType = new TypeReference<DailyAirQualityForecastResult>(){};
        DailyAirQualityForecastResult result = jacksonAdapter.<DailyAirQualityForecastResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static AirQualityResult getExpectedAirQualityHourlyForecasts() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getairqualityhourlyforecasts.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<AirQualityResult> interimType = new TypeReference<AirQualityResult>(){};
        AirQualityResult result = jacksonAdapter.<AirQualityResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static DailyHistoricalRecordsResult getExpectedDailyHistoricalRecords() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getdailyhistoricalrecords.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<DailyHistoricalRecordsResult> interimType = new TypeReference<DailyHistoricalRecordsResult>(){};
        DailyHistoricalRecordsResult result = jacksonAdapter.<DailyHistoricalRecordsResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }
    static DailyHistoricalActualsResult getExpectedDailyHistoricalActuals() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getdailyhistoricalactuals.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<DailyHistoricalActualsResult> interimType = new TypeReference<DailyHistoricalActualsResult>(){};
        DailyHistoricalActualsResult result = jacksonAdapter.<DailyHistoricalActualsResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        is.close();
        return result;
    }

    static DailyHistoricalNormalsResult getExpectedDailyHistoricalNormalsResult() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getdailyhistoricalnormals.json");
        byte[] data = toByteArray(is);
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<DailyHistoricalNormalsResult> interimType = new TypeReference<DailyHistoricalNormalsResult>(){};
        DailyHistoricalNormalsResult result = jacksonAdapter.<DailyHistoricalNormalsResult>deserialize(data, interimType.getJavaType(),
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
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into the output stream
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }
}
