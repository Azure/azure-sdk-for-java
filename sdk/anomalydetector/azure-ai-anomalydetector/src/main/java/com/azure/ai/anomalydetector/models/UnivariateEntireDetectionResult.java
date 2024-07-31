// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/** The response of entire anomaly detection. */
@Immutable
public final class UnivariateEntireDetectionResult implements JsonSerializable<UnivariateEntireDetectionResult> {
    /*
     * Frequency extracted from the series, zero means no recurrent pattern has been found.
     */
    private final int period;

    /*
     * ExpectedValues contain expected value for each input point. The index of the array is consistent with the input
     * series.
     */
    private final List<Double> expectedValues;

    /*
     * UpperMargins contain upper margin of each input point. UpperMargin is used to calculate upperBoundary, which
     * equals to expectedValue + (100 - marginScale)*upperMargin. Anomalies in response can be filtered by upperBoundary
     * and lowerBoundary. By adjusting marginScale value, less significant anomalies can be filtered in client side. The
     * index of the array is consistent with the input series.
     */
    private final List<Double> upperMargins;

    /*
     * LowerMargins contain lower margin of each input point. LowerMargin is used to
     * calculate lowerBoundary, which equals to expectedValue - (100 -
     * marginScale)*lowerMargin. Points between the boundary can be marked as normal
     * ones in client side. The index of the array is consistent with the input
     * series.
     */
    private final List<Double> lowerMargins;

    /*
     * IsAnomaly contains anomaly properties for each input point. True means an
     * anomaly either negative or positive has been detected. The index of the array
     * is consistent with the input series.
     */
    private final List<Boolean> isAnomaly;

    /*
     * IsNegativeAnomaly contains anomaly status in negative direction for each input
     * point. True means a negative anomaly has been detected. A negative anomaly
     * means the point is detected as an anomaly and its real value is smaller than
     * the expected one. The index of the array is consistent with the input series.
     */
    private final List<Boolean> isNegativeAnomaly;

    /*
     * IsPositiveAnomaly contain anomaly status in positive direction for each input
     * point. True means a positive anomaly has been detected. A positive anomaly
     * means the point is detected as an anomaly and its real value is larger than the
     * expected one. The index of the array is consistent with the input series.
     */
    private final List<Boolean> isPositiveAnomaly;

    /*
     * The severity score for each input point. The larger the value is, the more
     * sever the anomaly is. For normal points, the "severity" is always 0.
     */
    private List<Double> severity;

    /**
     * Creates an instance of UnivariateEntireDetectionResult class.
     *
     * @param period the period value to set.
     * @param expectedValues the expectedValues value to set.
     * @param upperMargins the upperMargins value to set.
     * @param lowerMargins the lowerMargins value to set.
     * @param isAnomaly the isAnomaly value to set.
     * @param isNegativeAnomaly the isNegativeAnomaly value to set.
     * @param isPositiveAnomaly the isPositiveAnomaly value to set.
     */
    private UnivariateEntireDetectionResult(int period, List<Double> expectedValues, List<Double> upperMargins,
        List<Double> lowerMargins, List<Boolean> isAnomaly, List<Boolean> isNegativeAnomaly,
        List<Boolean> isPositiveAnomaly) {
        this.period = period;
        this.expectedValues = expectedValues;
        this.upperMargins = upperMargins;
        this.lowerMargins = lowerMargins;
        this.isAnomaly = isAnomaly;
        this.isNegativeAnomaly = isNegativeAnomaly;
        this.isPositiveAnomaly = isPositiveAnomaly;
    }

    /**
     * Get the period property: Frequency extracted from the series, zero means no recurrent pattern has been found.
     *
     * @return the period value.
     */
    public int getPeriod() {
        return this.period;
    }

    /**
     * Get the expectedValues property: ExpectedValues contain expected value for each input point. The index of the
     * array is consistent with the input series.
     *
     * @return the expectedValues value.
     */
    public List<Double> getExpectedValues() {
        return this.expectedValues;
    }

    /**
     * Get the upperMargins property: UpperMargins contain upper margin of each input point. UpperMargin is used to
     * calculate upperBoundary, which equals to expectedValue + (100 - marginScale)*upperMargin. Anomalies in response
     * can be filtered by upperBoundary and lowerBoundary. By adjusting marginScale value, less significant anomalies
     * can be filtered in client side. The index of the array is consistent with the input series.
     *
     * @return the upperMargins value.
     */
    public List<Double> getUpperMargins() {
        return this.upperMargins;
    }

    /**
     * Get the lowerMargins property: LowerMargins contain lower margin of each input point. LowerMargin is used to
     * calculate lowerBoundary, which equals to expectedValue - (100 - marginScale)*lowerMargin. Points between the
     * boundary can be marked as normal ones in client side. The index of the array is consistent with the input series.
     *
     * @return the lowerMargins value.
     */
    public List<Double> getLowerMargins() {
        return this.lowerMargins;
    }

    /**
     * Get the isAnomaly property: IsAnomaly contains anomaly properties for each input point. True means an anomaly
     * either negative or positive has been detected. The index of the array is consistent with the input series.
     *
     * @return the isAnomaly value.
     */
    public List<Boolean> getIsAnomaly() {
        return this.isAnomaly;
    }

    /**
     * Get the isNegativeAnomaly property: IsNegativeAnomaly contains anomaly status in negative direction for each
     * input point. True means a negative anomaly has been detected. A negative anomaly means the point is detected as
     * an anomaly and its real value is smaller than the expected one. The index of the array is consistent with the
     * input series.
     *
     * @return the isNegativeAnomaly value.
     */
    public List<Boolean> getIsNegativeAnomaly() {
        return this.isNegativeAnomaly;
    }

    /**
     * Get the isPositiveAnomaly property: IsPositiveAnomaly contain anomaly status in positive direction for each input
     * point. True means a positive anomaly has been detected. A positive anomaly means the point is detected as an
     * anomaly and its real value is larger than the expected one. The index of the array is consistent with the input
     * series.
     *
     * @return the isPositiveAnomaly value.
     */
    public List<Boolean> getIsPositiveAnomaly() {
        return this.isPositiveAnomaly;
    }

    /**
     * Get the severity property: The severity score for each input point. The larger the value is, the more sever the
     * anomaly is. For normal points, the "severity" is always 0.
     *
     * @return the severity value.
     */
    public List<Double> getSeverity() {
        return this.severity;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeIntField("period", period)
            .writeArrayField("expectedValues", expectedValues, JsonWriter::writeNumber)
            .writeArrayField("upperMargins", upperMargins, JsonWriter::writeNumber)
            .writeArrayField("lowerMargins", lowerMargins, JsonWriter::writeNumber)
            .writeArrayField("isAnomaly", isAnomaly, JsonWriter::writeBoolean)
            .writeArrayField("isNegativeAnomaly", isNegativeAnomaly, JsonWriter::writeBoolean)
            .writeArrayField("isPositiveAnomaly", isPositiveAnomaly, JsonWriter::writeBoolean)
            .writeArrayField("severity", severity, JsonWriter::writeNumber)
            .writeEndObject();
    }

    /**
     * Reads an instance of UnivariateEntireDetectionResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of UnivariateEntireDetectionResult if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the UnivariateEntireDetectionResult.
     */
    public static UnivariateEntireDetectionResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            int period = 0;
            List<Double> expectedValues = null;
            List<Double> upperMargins = null;
            List<Double> lowerMargins = null;
            List<Boolean> isAnomaly = null;
            List<Boolean> isNegativeAnomaly = null;
            List<Boolean> isPositiveAnomaly = null;
            List<Double> severity = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("period".equals(fieldName)) {
                    period = reader.getInt();
                } else if ("expectedValues".equals(fieldName)) {
                    expectedValues = reader.readArray(JsonReader::getDouble);
                } else if ("upperMargins".equals(fieldName)) {
                    upperMargins = reader.readArray(JsonReader::getDouble);
                } else if ("lowerMargins".equals(fieldName)) {
                    lowerMargins = reader.readArray(JsonReader::getDouble);
                } else if ("isAnomaly".equals(fieldName)) {
                    isAnomaly = reader.readArray(JsonReader::getBoolean);
                } else if ("isNegativeAnomaly".equals(fieldName)) {
                    isNegativeAnomaly = reader.readArray(JsonReader::getBoolean);
                } else if ("isPositiveAnomaly".equals(fieldName)) {
                    isPositiveAnomaly = reader.readArray(JsonReader::getBoolean);
                } else if ("severity".equals(fieldName)) {
                    severity = reader.readArray(JsonReader::getDouble);
                } else {
                    reader.skipChildren();
                }
            }

            UnivariateEntireDetectionResult result = new UnivariateEntireDetectionResult(period, expectedValues,
                upperMargins, lowerMargins, isAnomaly, isNegativeAnomaly, isPositiveAnomaly);
            result.severity = severity;
            return result;
        });
    }
}
