// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AgeResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AreaResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.CurrencyResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.DateTimeResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.InformationResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.LengthResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.NumberResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.NumericRangeResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.OrdinalResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.SpeedResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TemperatureResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TemporalSpanResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.VolumeResolutionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.WeightResolutionPropertiesHelper;
import com.azure.ai.textanalytics.models.AgeResolution;
import com.azure.ai.textanalytics.models.AgeUnit;
import com.azure.ai.textanalytics.models.AreaResolution;
import com.azure.ai.textanalytics.models.AreaUnit;
import com.azure.ai.textanalytics.models.BaseResolution;
import com.azure.ai.textanalytics.models.CurrencyResolution;
import com.azure.ai.textanalytics.models.DateTimeResolution;
import com.azure.ai.textanalytics.models.DateTimeSubKind;
import com.azure.ai.textanalytics.models.InformationResolution;
import com.azure.ai.textanalytics.models.InformationUnit;
import com.azure.ai.textanalytics.models.LengthResolution;
import com.azure.ai.textanalytics.models.LengthUnit;
import com.azure.ai.textanalytics.models.NumberKind;
import com.azure.ai.textanalytics.models.NumberResolution;
import com.azure.ai.textanalytics.models.NumericRangeResolution;
import com.azure.ai.textanalytics.models.OrdinalResolution;
import com.azure.ai.textanalytics.models.RangeKind;
import com.azure.ai.textanalytics.models.RelativeTo;
import com.azure.ai.textanalytics.models.ResolutionKind;
import com.azure.ai.textanalytics.models.SpeedResolution;
import com.azure.ai.textanalytics.models.SpeedUnit;
import com.azure.ai.textanalytics.models.TemperatureResolution;
import com.azure.ai.textanalytics.models.TemperatureUnit;
import com.azure.ai.textanalytics.models.TemporalSpanResolution;
import com.azure.ai.textanalytics.models.VolumeResolution;
import com.azure.ai.textanalytics.models.VolumeUnit;
import com.azure.ai.textanalytics.models.WeightResolution;
import com.azure.ai.textanalytics.models.WeightUnit;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for Entity resolutions.
 */
public class EntityResolutionUnitTest {
    /**
     * Test all resolution types and properties.
     */
    @Test
    public void resolutionAllKindsTest() {
        ResolutionKind[] resolutionKinds = ResolutionKind.values();
        List<BaseResolution> resolutions = new ArrayList<>();
        int size = resolutionKinds.length;

        for (int i = 0; i < size; i++) {
            ResolutionKind kind = resolutionKinds[i];
            switch (kind) {
                case AGE_RESOLUTION:
                    AgeResolution ageResolution = new AgeResolution();
                    AgeResolutionPropertiesHelper.setUnit(ageResolution, AgeUnit.DAY);
                    AgeResolutionPropertiesHelper.setValue(ageResolution, 100.0);
                    resolutions.add(ageResolution);
                    break;
                case AREA_RESOLUTION:
                    AreaResolution areaResolution = new AreaResolution();
                    AreaResolutionPropertiesHelper.setUnit(areaResolution, AreaUnit.SQUARE_FOOT);
                    AreaResolutionPropertiesHelper.setValue(areaResolution, 2000.4);
                    resolutions.add(areaResolution);
                    break;
                case CURRENCY_RESOLUTION:
                    CurrencyResolution currencyResolution = new CurrencyResolution();
                    // TODO: thinking about replace string by enums,
                    CurrencyResolutionPropertiesHelper.setIso4217(currencyResolution, "USD");
                    CurrencyResolutionPropertiesHelper.setUnit(currencyResolution, "dollar");
                    CurrencyResolutionPropertiesHelper.setValue(currencyResolution, 1000);
                    resolutions.add(currencyResolution);
                    break;
                case DATE_TIME_RESOLUTION:
                    DateTimeResolution dateTimeResolution = new DateTimeResolution();
                    // https://learn.microsoft.com/en-us/azure/cognitive-services/language-service/named-entity-recognition/concepts/entity-resolutions#set
                    DateTimeResolutionPropertiesHelper.setTimex(dateTimeResolution, "XXXX-WXX-1T18");
                    DateTimeResolutionPropertiesHelper.setDateTimeSubKind(dateTimeResolution, DateTimeSubKind.SET);
                    DateTimeResolutionPropertiesHelper.setValue(dateTimeResolution, "not resolved");
                    resolutions.add(dateTimeResolution);
                    break;
                case INFORMATION_RESOLUTION:
                    InformationResolution informationResolution = new InformationResolution();
                    InformationResolutionPropertiesHelper.setUnit(informationResolution, InformationUnit.GIGABIT);
                    InformationResolutionPropertiesHelper.setValue(informationResolution, 10.2);
                    resolutions.add(informationResolution);
                    break;
                case LENGTH_RESOLUTION:
                    LengthResolution lengthResolution = new LengthResolution();
                    LengthResolutionPropertiesHelper.setUnit(lengthResolution, LengthUnit.MILE);
                    LengthResolutionPropertiesHelper.setValue(lengthResolution, 10.0);
                    resolutions.add(lengthResolution);
                    break;
                case NUMBER_RESOLUTION:
                    NumberResolution numberResolution = new NumberResolution();
                    NumberResolutionPropertiesHelper.setNumberKind(numberResolution, NumberKind.INTEGER);
                    NumberResolutionPropertiesHelper.setValue(numberResolution, 10);
                    resolutions.add(numberResolution);
                    break;
                case NUMERIC_RANGE_RESOLUTION:
                    NumericRangeResolution numericRangeResolution = new NumericRangeResolution();
                    NumericRangeResolutionPropertiesHelper.setRangeKind(numericRangeResolution, RangeKind.AGE);
                    NumericRangeResolutionPropertiesHelper.setMaximum(numericRangeResolution, 18);
                    NumericRangeResolutionPropertiesHelper.setMinimum(numericRangeResolution, 13);
                    resolutions.add(numericRangeResolution);
                    break;
                case ORDINAL_RESOLUTION:
                    // https://learn.microsoft.com/en-us/azure/cognitive-services/language-service/named-entity-recognition/concepts/entity-resolutions#ordinal
                    OrdinalResolution ordinalResolution = new OrdinalResolution();
                    OrdinalResolutionPropertiesHelper.setRelativeTo(ordinalResolution, RelativeTo.START);
                    OrdinalResolutionPropertiesHelper.setValue(ordinalResolution, "3");
                    OrdinalResolutionPropertiesHelper.setOffset(ordinalResolution, "3");
                    resolutions.add(ordinalResolution);
                    break;
                case SPEED_RESOLUTION:
                    SpeedResolution speedResolution = new SpeedResolution();
                    SpeedResolutionPropertiesHelper.setUnit(speedResolution, SpeedUnit.MILES_PER_HOUR);
                    SpeedResolutionPropertiesHelper.setValue(speedResolution, 60);
                    resolutions.add(speedResolution);
                    break;
                case TEMPORAL_SPAN_RESOLUTION:
                    TemporalSpanResolution temporalSpanResolution = new TemporalSpanResolution();
                    TemporalSpanResolutionPropertiesHelper.setDuration(temporalSpanResolution, "PT2702H");
                    TemporalSpanResolutionPropertiesHelper.setBegin(temporalSpanResolution, "2022-01-03 06:00:00");
                    TemporalSpanResolutionPropertiesHelper.setEnd(temporalSpanResolution, "2022-04-25 20:00:00");
                    resolutions.add(temporalSpanResolution);
                    break;
                case TEMPERATURE_RESOLUTION:
                    TemperatureResolution temperatureResolution = new TemperatureResolution();
                    TemperatureResolutionPropertiesHelper.setUnit(temperatureResolution, TemperatureUnit.FAHRENHEIT);
                    TemperatureResolutionPropertiesHelper.setValue(temperatureResolution, 88);
                    resolutions.add(temperatureResolution);
                    break;
                case VOLUME_RESOLUTION:
                    VolumeResolution volumeResolution = new VolumeResolution();
                    VolumeResolutionPropertiesHelper.setUnit(volumeResolution, VolumeUnit.TEASPOON);
                    VolumeResolutionPropertiesHelper.setValue(volumeResolution, 3);
                    resolutions.add(volumeResolution);
                    break;
                case WEIGHT_RESOLUTION:
                    WeightResolution weightResolution = new WeightResolution();
                    WeightResolutionPropertiesHelper.setUnit(weightResolution, WeightUnit.KILOGRAM);
                    WeightResolutionPropertiesHelper.setValue(weightResolution, 100);
                    resolutions.add(weightResolution);
                    break;
                default:
                    assertFalse(true, "Unknown new resolution type, " + kind);
                    break;
            }
        }

        resolutions.forEach(resolution -> {
            switch (resolution.getType()) {
                case AGE_RESOLUTION:
                    AgeResolution ageResolution = (AgeResolution) resolution;
                    assertEquals(AgeUnit.DAY, ageResolution.getUnit());
                    assertEquals(100.0, ageResolution.getValue());
                    break;
                case AREA_RESOLUTION:
                    AreaResolution areaResolution = (AreaResolution) resolution;
                    assertEquals(AreaUnit.SQUARE_FOOT, areaResolution.getUnit());
                    assertEquals(2000.4, areaResolution.getValue());
                    break;
                case CURRENCY_RESOLUTION:
                    CurrencyResolution currencyResolution = (CurrencyResolution) resolution;
                    assertEquals("USD", currencyResolution.getIso4217());
                    assertEquals("dollar", currencyResolution.getUnit());
                    assertEquals(1000, currencyResolution.getValue());
                    break;
                case DATE_TIME_RESOLUTION:
                    DateTimeResolution dateTimeResolution = (DateTimeResolution) resolution;
                    assertEquals("XXXX-WXX-1T18", dateTimeResolution.getTimex());
                    assertEquals(DateTimeSubKind.SET, dateTimeResolution.getDateTimeSubKind());
                    assertEquals("not resolved", dateTimeResolution.getValue());
                    break;
                case INFORMATION_RESOLUTION:
                    InformationResolution informationResolution = (InformationResolution) resolution;
                    assertEquals(InformationUnit.GIGABIT, informationResolution.getUnit());
                    assertEquals(10.2, informationResolution.getValue());
                    break;
                case LENGTH_RESOLUTION:
                    LengthResolution lengthResolution = (LengthResolution) resolution;
                    assertEquals(LengthUnit.MILE, lengthResolution.getUnit());
                    assertEquals(10.0, lengthResolution.getValue());
                    break;
                case NUMBER_RESOLUTION:
                    NumberResolution numberResolution = (NumberResolution) resolution;
                    assertEquals(NumberKind.INTEGER, numberResolution.getNumberKind());
                    assertEquals(10, numberResolution.getValue());
                    break;
                case NUMERIC_RANGE_RESOLUTION:
                    NumericRangeResolution numericRangeResolution = (NumericRangeResolution) resolution;
                    assertEquals(RangeKind.AGE, numericRangeResolution.getRangeKind());
                    assertEquals(18, numericRangeResolution.getMaximum());
                    assertEquals(13, numericRangeResolution.getMinimum());
                    break;
                case ORDINAL_RESOLUTION:
                    OrdinalResolution ordinalResolution =  (OrdinalResolution) resolution;
                    assertEquals(RelativeTo.START, ordinalResolution.getRelativeTo());
                    assertEquals("3", ordinalResolution.getValue());
                    assertEquals("3", ordinalResolution.getOffset());
                    break;
                case SPEED_RESOLUTION:
                    SpeedResolution speedResolution = (SpeedResolution) resolution;
                    assertEquals(SpeedUnit.MILES_PER_HOUR, speedResolution.getUnit());
                    assertEquals(60, speedResolution.getValue());
                    break;
                case TEMPORAL_SPAN_RESOLUTION:
                    TemporalSpanResolution temporalSpanResolution = (TemporalSpanResolution) resolution;
                    assertEquals("PT2702H", temporalSpanResolution.getDuration());
                    assertEquals("2022-01-03 06:00:00", temporalSpanResolution.getBegin());
                    assertEquals("2022-04-25 20:00:00", temporalSpanResolution.getEnd());
                    break;
                case TEMPERATURE_RESOLUTION:
                    TemperatureResolution temperatureResolution = (TemperatureResolution) resolution;
                    assertEquals(TemperatureUnit.FAHRENHEIT, temperatureResolution.getUnit());
                    assertEquals(88, temperatureResolution.getValue());
                    break;
                case VOLUME_RESOLUTION:
                    VolumeResolution volumeResolution = (VolumeResolution) resolution;
                    assertEquals(VolumeUnit.TEASPOON, volumeResolution.getUnit());
                    assertEquals(3, volumeResolution.getValue());
                    break;
                case WEIGHT_RESOLUTION:
                    WeightResolution weightResolution = (WeightResolution) resolution;
                    assertEquals(WeightUnit.KILOGRAM, weightResolution.getUnit());
                    assertEquals(100, weightResolution.getValue());
                    break;
                default:
                    assertFalse(true, "un-handled resolution type, " + resolution.getType());
            }
        });
    }
}
