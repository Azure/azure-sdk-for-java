// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * Customization class for Queue Storage.
 */
public class WeatherCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.weather.models");

        customizeActiveStorm(models);
        customizeActiveStormResult(models);
        customizeAirAndPollen(models);
        customizeAirQuality(models);
        customizeAirQualityResult(models);
        customizeAlertDetails(models);
        customizeColorValue(models);
        customizeCurrentConditions(models);
        customizeDailyAirQuality(models);
        customizeDailyAirQualityForecastResult(models);
        customizeDailyForecast(models);
        customizeDailyForecastDetail(models);
        customizeWeatherValue(models);
        customizeDailyForecastSummary(models);
        customizeDailyHistoricalActuals(models);
        customizeDailyHistoricalActualsResult(models);
        customizeDailyHistoricalNormals(models);
        customizeDailyHistoricalNormalsResult(models);
        customizeDailyHistoricalRecords(models);
        customizeDailyHistoricalRecordsResult(models);
        customizeDailyIndex(models);
        customizeDegreeDaySummary(models);
        customizeForecastInterval(models);
        customizeHazardDetail(models);
        customizeHourlyForecast(models);
        customizeIntervalSummary(models);
        customizeLatestStatus(models);
        customizeLocalSource(models);
        customizeMinuteForecastSummary(models);
        customizePollutant(models);
        customizePrecipitationSummary(models);
        customizePressureTendency(models);
        customizePastHoursTemperature(models);
        customizeQuarterDayForecast(models);
        customizeRadiusSector(models);
        customizeSevereWeatherAlert(models);
        customizeSevereWeatherAlertDescription(models);
        customizeStormForecast(models);
        customizeStormForecastResult(models);
        customizeStormLocation(models);
        customizeStormLocationsResult(models);
        customizeStormSearchResult(models);
        customizeStormSearchResultItem(models);
        customizeStormWindRadiiSummary(models);
        customizeSunGlare(models);
        customizeTemperatureSummary(models);
        customizeWeatherAlongRoutePrecipitation(models);
        customizeWeatherHazards(models);
        customizeWeatherNotification(models);
        customizeWeatherValueMaxMinAvg(models);
        customizeWeatherValueRange(models);
        customizeWeatherValueYear(models);
        customizeWeatherValueYearMax(models);
        customizeWeatherValueYearMaxMinAvg(models);
        customizeWeatherWindow(models);
        customizeWindDetails(models);
        customizeWindDirection(models);
        customizeWaypointForecast(models);

        // customize to make default constructor private
        bulkPrivateConstructors(models, "WeatherUnitDetails", "WeatherAlongRouteSummary", "WeatherAlongRouteResult",
            "SevereWeatherAlertsResult", "QuarterDayForecastResult", "MinuteForecastResult", "HourlyForecastResult",
            "DailyIndicesResult", "DailyForecastResult", "CurrentConditionsResult");

        addToIntMethod(models, "DayQuarter", "HazardIndex", "IconCode", "UnitType");
    }

    // Customizes the StormForecast class
    @SuppressWarnings("unchecked")
    private void customizeLatLongPairClasses(ClassOrInterfaceDeclaration clazz, String propertyName, String getter,
        String setter) {
        clazz.findAncestor(CompilationUnit.class).ifPresent(p -> p.addImport("com.azure.core.models.GeoPosition"));
        clazz.getMethodsByName(getter)
            .get(0)
            .setType("GeoPosition")
            .setBody(StaticJavaParser.parseBlock(
                "{ return new GeoPosition(this." + propertyName + ".getLongitude(), this." + propertyName
                    + ".getLatitude()); }"));
        clazz.getMethodsByName(setter).forEach(Node::remove);
    }

    // Customizes the WeatherWindow and StormWindRadiiSummary classes
    // Have to customize it this way because setting return type imports the wrong Utility package.
    @SuppressWarnings("unchecked")
    private void customizeGeoJsonGeometryProperty(ClassOrInterfaceDeclaration clazz, String getter, String setter,
        String property) {
        clazz.findAncestor(CompilationUnit.class).ifPresent(p -> {
            p.addImport("com.azure.core.models.GeoPolygon");
            p.addImport("com.azure.maps.weather.implementation.helpers.Utility");
        });

        clazz.getMethodsByName(getter).forEach(Node::remove);
        clazz.getMethodsByName(setter).forEach(Node::remove);
        clazz.addMethod("getPolygon", Modifier.Keyword.PUBLIC)
            .setType("GeoPolygon")
            .setBody(StaticJavaParser.parseBlock("{ return Utility.toGeoPolygon(this." + property + "); }"))
            .setJavadocComment(new Javadoc(JavadocDescription.parseText("Return GeoPolygon")).addBlockTag("return",
                "Returns a {@link GeoPolygon} for this weather window"));
    }

    // Customizes classes with getYear() as a String
    private void customizeClassesWithString(ClassOrInterfaceDeclaration clazz) {
        clazz.getMethodsByName("getYear")
            .get(0)
            .setType("Integer")
            .setBody(StaticJavaParser.parseBlock("{ return Integer.valueOf(" + "year" + "); }"));
        clazz.getMethodsByName("setYear").forEach(Node::remove);
    }

    // Customizes to private constructor class
    private void customizePrivateConstructor(ClassOrInterfaceDeclaration clazz) {
        clazz.getConstructors()
            .get(0)
            .setModifiers(Modifier.Keyword.PRIVATE)
            .setJavadocComment("Set default " + clazz.getNameAsString() + " constructor to private");
    }

    // Customizes to remove setter in ActiveStorm
    private void customizeActiveStorm(PackageCustomization models) {
        customizeClass(models, "ActiveStorm", clazz -> {
            customizePrivateConstructor(clazz);
            customizeClassesWithString(clazz);
            bulkRemoveMethods(clazz, "setBasinId", "setName", "setIsActive", "setIsSubtropical", "setGovId");
        });
    }

    // Customizes to remove setter in ActiveStormResult
    private void customizeActiveStormResult(PackageCustomization models) {
        customizeClass(models, "ActiveStormResult", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setActiveStorms", "setNextLink");
        });
    }

    // Customizes to remove setter in AirAndPollen
    private void customizeAirAndPollen(PackageCustomization models) {
        customizeClass(models, "AirAndPollen", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setDescription", "setValue", "setCategory", "setCategoryValue",
                "setAirQualityType");
        });
    }

    // Customizes to remove setter in AirQuality
    private void customizeAirQuality(PackageCustomization models) {
        customizeClass(models, "AirQuality", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setTimestamp", "setIndex", "setGlobalIndex", "setDominantPollutant",
                "setCategory", "setCategoryColor", "setDescription", "setPollutants");
        });
    }

    // Customizes to remove setter in AirQualityResult
    private void customizeAirQualityResult(PackageCustomization models) {
        customizeClass(models, "AirQualityResult", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setAirQualityResults", "setNextLink");
        });
    }

    // Customizes to remove setter in AlertDetails
    private void customizeAlertDetails(PackageCustomization models) {
        customizeClass(models, "AlertDetails", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setName", "setDescription", "setStartTime", "setEndTime", "setLatestStatus",
                "setDetails", "setLanguage");
        });
    }

    // Customizes to remove setter in ColorValue
    private void customizeColorValue(PackageCustomization models) {
        customizeClass(models, "ColorValue", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setRed", "setGreen", "setBlue", "setHex");
        });
    }

    // Customizes to remove setter in CurrentConditions
    private void customizeCurrentConditions(PackageCustomization models) {
        customizeClass(models, "CurrentConditions", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setDateTime", "setDescription", "setIconCode", "setHasPrecipitation",
                "setIsDaytime", "setTemperature", "setRealFeelTemperature", "setRealFeelTemperatureShade",
                "setRelativeHumidity", "setDewPoint", "setWind", "setWindGust", "setUvIndex", "setUvIndexDescription",
                "setVisibility", "setObstructionsToVisibility", "setCloudCover", "setCloudCeiling", "setPressure",
                "setPressureTendency", "setPastTwentyFourHourTemperatureDeparture", "setApparentTemperature",
                "setWindChillTemperature", "setWetBulbTemperature", "setPrecipitationSummary", "setTemperatureSummary");
        });
    }

    // Customizes to remove setter in DailyAirQuality
    private void customizeDailyAirQuality(PackageCustomization models) {
        customizeClass(models, "DailyAirQuality", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setTimestamp", "setIndex", "setGlobalIndex", "setDominantPollutant",
                "setCategory", "setCategoryColor", "setDescription");
        });
    }

    // Customizes to remove setter in DailyAirQualityForecastResult
    private void customizeDailyAirQualityForecastResult(PackageCustomization models) {
        customizeClass(models, "DailyAirQualityForecastResult", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setAirQualityResults", "setNextLink");
        });
    }

    // Customizes to remove setter in DailyForecast
    private void customizeDailyForecast(PackageCustomization models) {
        customizeClass(models, "DailyForecast", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setDateTime", "setTemperature", "setRealFeelTemperature",
                "setRealFeelTemperatureShade", "setHoursOfSun", "setMeanTemperatureDeviation", "setAirQuality",
                "setDaytimeForecast", "setNighttimeForecast", "setSources");
        });
    }

    // Customizes to remove setter in DailyForecastDetail
    private void customizeDailyForecastDetail(PackageCustomization models) {
        customizeClass(models, "DailyForecastDetail", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setIconCode", "setIconPhrase", "setLocalSource", "setHasPrecipitation",
                "setPrecipitationType", "setPrecipitationIntensity", "setShortDescription", "setLongPhrase",
                "setPrecipitationProbability", "setThunderstormProbability", "setRainProbability", "setSnowProbability",
                "setIceProbability", "setWind", "setWindGust", "setTotalLiquid", "setRain", "setSnow", "setIce",
                "setHoursOfPrecipitation", "setHoursOfRain", "setHoursOfSnow", "setHoursOfIce", "setCloudCover");
            clazz.getMethodsByName("isHasPrecipitation").get(0).setName("hasPrecipitation");
            clazz.getMethodsByName("getRain").get(0).setName("getRainUnitDetails");
            clazz.getMethodsByName("getSnow").get(0).setName("getSnowUnitDetails");
            clazz.getMethodsByName("getIce").get(0).setName("getIceUnitDetails");
            clazz.getMethodsByName("getWind").get(0).setName("getWindUnitDetails");
        });
    }

    // Customize WeatherValue
    private void customizeWeatherValue(PackageCustomization models) {
        customizeClass(models, "WeatherValue",
            clazz -> bulkRemoveMethods(clazz, "setValue", "setUnitLabel", "setUnitType"));

        models.getClass("WeatherValue").rename("WeatherUnitDetails");
    }

    // Remove setters from DailyForecastSummary
    private void customizeDailyForecastSummary(PackageCustomization models) {
        customizeClass(models, "DailyForecastSummary", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setStartDate", "setEndDate", "setSeverity", "setPhrase", "setCategory");
        });
    }

    // Remove setters from DailyHistoricalActuals
    private void customizeDailyHistoricalActuals(PackageCustomization models) {
        customizeClass(models, "DailyHistoricalActuals", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setTimestamp", "setTemperature", "setDegreeDaySummary", "setPrecipitation",
                "setSnowfall", "setSnowDepth");
        });
    }

    // Remove setters from DailyHistoricalActualsResult
    private void customizeDailyHistoricalActualsResult(PackageCustomization models) {
        customizeClass(models, "DailyHistoricalActualsResult", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setHistoricalActuals", "setNextLink");
        });
    }

    // Remove setters from DailyHistoricalNormals
    private void customizeDailyHistoricalNormals(PackageCustomization models) {
        customizeClass(models, "DailyHistoricalNormals", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setTimestamp", "setTemperature", "setDegreeDaySummary", "setPrecipitation");
        });
    }

    // Remove setters from DailyHistoricalNormalsResult
    private void customizeDailyHistoricalNormalsResult(PackageCustomization models) {
        customizeClass(models, "DailyHistoricalNormalsResult", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setHistoricalNormals", "setNextLink");
        });
    }

    // Remove setters from DailyHistoricalRecords
    private void customizeDailyHistoricalRecords(PackageCustomization models) {
        customizeClass(models, "DailyHistoricalRecords", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setTimestamp", "setTemperature", "setSnowfall", "setPrecipitation");
        });
    }

    // Remove setters from DailyHistoricalRecordsResult
    private void customizeDailyHistoricalRecordsResult(PackageCustomization models) {
        customizeClass(models, "DailyHistoricalRecordsResult", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setHistoricalRecords", "setNextLink");
        });
    }

    // Remove setters from DailyIndex
    private void customizeDailyIndex(PackageCustomization models) {
        customizeClass(models, "DailyIndex", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setIndexName", "setIndexId", "setDateTime", "setValue", "setCategoryDescription",
                "setCategoryValue", "setIsAscending", "setDescription");
        });
    }

    // Remove setters from DegreeDaySummary
    private void customizeDegreeDaySummary(PackageCustomization models) {
        customizeClass(models, "DegreeDaySummary", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setHeating", "setCooling");
        });
    }

    // Remove setters from ForecastInterval
    private void customizeForecastInterval(PackageCustomization models) {
        customizeClass(models, "ForecastInterval", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setStartMinute", "setMinute", "setDecibelRelativeToZ", "setShortDescription",
                "setThreshold", "setColor", "setSimplifiedColor", "setPrecipitationType", "setIconCode",
                "setCloudCover");
        });
    }

    // Remove setters from HazardDetail
    private void customizeHazardDetail(PackageCustomization models) {
        customizeClass(models, "HazardDetail", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setHazardIndex", "setHazardCode", "setShortDescription");
        });
    }

    // Remove setters from HourlyForecast
    private void customizeHourlyForecast(PackageCustomization models) {
        customizeClass(models, "HourlyForecast", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setTimestamp", "setIconCode", "setIconPhrase", "setHasPrecipitation",
                "setIsDaylight", "setTemperature", "setRealFeelTemperature", "setWetBulbTemperature", "setDewPoint",
                "setWind", "setWindGust", "setRelativeHumidity", "setVisibility", "setCloudCeiling", "setUvIndex",
                "setUvIndexDescription", "setPrecipitationProbability", "setRainProbability", "setSnowProbability",
                "setIceProbability", "setTotalLiquid", "setRain", "setSnow", "setIce", "setCloudCover");
        });
    }

    // Remove setters from IntervalSummary
    private void customizeIntervalSummary(PackageCustomization models) {
        customizeClass(models, "IntervalSummary", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setStartMinute", "setEndMinute", "setTotalMinutes", "setShortDescription",
                "setBriefDescription", "setLongPhrase", "setIconCode");
        });
    }

    // Remove setters from LatestStatus
    private void customizeLatestStatus(PackageCustomization models) {
        customizeClass(models, "LatestStatus", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setLocalized", "setEnglish");
        });
    }

    // Remove setters from LocalSource
    private void customizeLocalSource(PackageCustomization models) {
        customizeClass(models, "LocalSource", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setId", "setName", "setWeatherCode");
        });
    }

    // Remove setters from MinuteForecastSummary
    private void customizeMinuteForecastSummary(PackageCustomization models) {
        customizeClass(models, "MinuteForecastSummary", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setBriefPhrase60", "setShortDescription", "setBriefDescription", "setLongPhrase",
                "setIconCode");
        });
    }

    // Remove setters from Pollutant
    private void customizePollutant(PackageCustomization models) {
        customizeClass(models, "Pollutant", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setType", "setName", "setIndex", "setGlobalIndex", "setConcentration");
        });
    }

    // Remove setters from PrecipitationSummary
    private void customizePrecipitationSummary(PackageCustomization models) {
        customizeClass(models, "PrecipitationSummary", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setPastHour", "setPastThreeHours", "setPastSixHours", "setPastNineHours",
                "setPastTwelveHours", "setPastEighteenHours", "setPastTwentyFourHours");
        });
    }

    // Remove setters from PressureTendency
    private void customizePressureTendency(PackageCustomization models) {
        customizeClass(models, "PressureTendency", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setDescription", "setCode");
        });
    }

    // Remove setters from PastHoursTemperature
    private void customizePastHoursTemperature(PackageCustomization models) {
        customizeClass(models, "PastHoursTemperature", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setMinimum", "setMaximum");
        });
    }

    // Remove setters from QuarterDayForecast
    private void customizeQuarterDayForecast(PackageCustomization models) {
        customizeClass(models, "QuarterDayForecast", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setDateTime", "setEffectiveDate", "setQuarter", "setIconCode", "setIconPhrase",
                "setPhrase", "setTemperature", "setRealFeelTemperature", "setDewPoint", "setRelativeHumidity",
                "setWind", "setWindGust", "setVisibility", "setCloudCover", "setPrecipitationType",
                "setHasPrecipitation", "setPrecipitationIntensity", "setPrecipitationProbability",
                "setThunderstormProbability", "setTotalLiquid", "setRain", "setSnow", "setIce");
        });
    }

    // Remove setters from RadiusSector
    private void customizeRadiusSector(PackageCustomization models) {
        customizeClass(models, "RadiusSector", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setBeginBearing", "setEndBearing", "setRadius");
        });
    }

    // Remove setters from SevereWeatherAlert
    private void customizeSevereWeatherAlert(PackageCustomization models) {
        customizeClass(models, "SevereWeatherAlert", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setCountryCode", "setAlertId", "setDescription", "setCategory", "setPriority",
                "setClassification", "setLevel", "setSource", "setSourceId", "setDisclaimer", "setAlertDetails");
        });
    }

    // Remove setters from SevereWeatherAlertDescription
    private void customizeSevereWeatherAlertDescription(PackageCustomization models) {
        customizeClass(models, "SevereWeatherAlertDescription", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setDescription", "setStatus");
        });
    }

    // Remove setters from StormForecast
    private void customizeStormForecast(PackageCustomization models) {
        customizeClass(models, "StormForecast", clazz -> {
            customizePrivateConstructor(clazz);
            customizeLatLongPairClasses(clazz, "coordinates", "getCoordinates", "setCoordinates");
            bulkRemoveMethods(clazz, "setCoordinates", "setTimestamp", "setInitializedTimestamp", "setMaxWindGust",
                "setSustainedWind", "setStatus", "setWeatherWindow", "setWindRadiiSummary");
        });
    }

    // Remove setters from StormForecastResult
    private void customizeStormForecastResult(PackageCustomization models) {
        customizeClass(models, "StormForecastResult", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setStormForecasts", "setNextLink");
        });
    }

    // Remove setters from StormLocation
    private void customizeStormLocation(PackageCustomization models) {
        customizeClass(models, "StormLocation", clazz -> {
            customizePrivateConstructor(clazz);
            customizeLatLongPairClasses(clazz, "coordinates", "getCoordinates", "setCoordinates");
            bulkRemoveMethods(clazz, "setTimestamp", "setMaxWindGust", "setSustainedWind", "setMinimumPressure",
                "setMovement", "setStatus", "setIsSubtropical", "setHasTropicalPotential", "setIsPostTropical",
                "setWindRadiiSummary");
        });
    }

    // Remove setters from StormLocationsResult
    private void customizeStormLocationsResult(PackageCustomization models) {
        customizeClass(models, "StormLocationsResult", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setStormLocations", "setNextLink");
        });
    }

    // Remove setters from StormSearchResult
    private void customizeStormSearchResult(PackageCustomization models) {
        customizeClass(models, "StormSearchResult", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setStorms", "setNextLink");
        });
    }

    // Remove setters from StormSearchResultItem
    private void customizeStormSearchResultItem(PackageCustomization models) {
        models.getClass("StormSearchResultItem")
            .customizeAst(ast -> ast.getClassByName("StormSearchResultItem").ifPresent(clazz -> {
                customizePrivateConstructor(clazz);
                customizeClassesWithString(clazz);
                bulkRemoveMethods(clazz, "setBasinId", "setName", "setIsActive", "setIsRetired", "setIsSubtropical",
                    "setGovId");
            }));
    }

    // Remove setters from StormWindRadiiSummary
    private void customizeStormWindRadiiSummary(PackageCustomization models) {
        models.getClass("StormWindRadiiSummary")
            .customizeAst(ast -> ast.getClassByName("StormWindRadiiSummary").ifPresent(clazz -> {
                customizePrivateConstructor(clazz);
                customizeGeoJsonGeometryProperty(clazz, "getRadiiGeometry", "setRadiiGeometry", "radiiGeometry");
                bulkRemoveMethods(clazz, "setTimestamp", "setWindSpeed", "setRadiusSectorData");
            }));
    }

    // Remove setters from SunGlare
    private void customizeSunGlare(PackageCustomization models) {
        customizeClass(models, "SunGlare", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setCalculatedVehicleHeading", "setGlareIndex");
        });
    }

    // Remove setters from TemperatureSummary
    private void customizeTemperatureSummary(PackageCustomization models) {
        customizeClass(models, "TemperatureSummary", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setPastSixHours", "setPastTwelveHours", "setPastTwentyFourHours");
        });
    }

    // Remove setters from WeatherAlongRoutePrecipitation
    private void customizeWeatherAlongRoutePrecipitation(PackageCustomization models) {
        customizeClass(models, "WeatherAlongRoutePrecipitation", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setDbz", "setType");
            clazz.getMethodsByName("getDbz").get(0).setName("getDeciblesRelativeToZ");
        });
    }

    // Remove setters from WeatherAlongRouteSummary
    private void customizeWeatherAlongRouteSummary(PackageCustomization models) {
        customizeClass(models, "WeatherAlongRouteSummary",
            clazz -> bulkRemoveMethods(clazz, "setIconCode", "setHazards"));
    }

    // Remove setters from WeatherHazards
    private void customizeWeatherHazards(PackageCustomization models) {
        customizeClass(models, "WeatherHazards", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setMaxHazardIndex", "setDetails");
        });
    }

    // Remove setters from WeatherNotification
    private void customizeWeatherNotification(PackageCustomization models) {
        customizeClass(models, "WeatherNotification", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setType", "setHazardIndex", "setHazardCode", "setShortDescription");
        });
    }

    // Remove setters from WeatherValueMaxMinAvg
    private void customizeWeatherValueMaxMinAvg(PackageCustomization models) {
        customizeClass(models, "WeatherValueMaxMinAvg", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setMaximum", "setMinimum", "setAverage");
        });
    }

    // Remove setters from WeatherValueRange
    private void customizeWeatherValueRange(PackageCustomization models) {
        customizeClass(models, "WeatherValueRange", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setMinimum", "setMaximum");
        });
    }

    // Remove setters from WeatherValueYear
    private void customizeWeatherValueYear(PackageCustomization models) {
        customizeClass(models, "WeatherValueYear", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setValue", "setUnit", "setUnitType", "setYear");

            MethodDeclaration fromJson = clazz.getMethodsByName("fromJson").get(0);
            String body = fromJson.getBody().get().toString();
            body = body.replace("deserializedWeatherValueYear.year = reader.getNullable(JsonReader::getInt);",
                String.join("\n",
                    "if (reader.currentToken() == JsonToken.NUMBER) {",
                    "    deserializedWeatherValueYear.year = reader.getNullable(JsonReader::getInt);",
                    "} else if (reader.currentToken() == JsonToken.STRING) {",
                    "    deserializedWeatherValueYear.year = Integer.parseInt(reader.getString());",
                    "}"));
            fromJson.setBody(StaticJavaParser.parseBlock(body));
        });
    }

    // Remove setters from WeatherValueYearMax
    private void customizeWeatherValueYearMax(PackageCustomization models) {
        customizeClass(models, "WeatherValueYearMax", clazz -> {
            customizePrivateConstructor(clazz);
            clazz.getMethodsByName("setMaximum").forEach(Node::remove);
        });
    }

    // Remove setters from WeatherValueYearMaxMinAvg
    private void customizeWeatherValueYearMaxMinAvg(PackageCustomization models) {
        customizeClass(models, "WeatherValueYearMaxMinAvg", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setMaximum", "setMinimum", "setAverage");
        });
    }

    // Remove setters from WeatherWindow
    private void customizeWeatherWindow(PackageCustomization models) {
        customizeClass(models, "WeatherWindow", clazz -> {
            customizePrivateConstructor(clazz);
            customizeLatLongPairClasses(clazz, "topLeft", "getTopLeft", "setTopLeft");
            customizeLatLongPairClasses(clazz, "bottomRight", "getBottomRight", "setBottomRight");
            customizeGeoJsonGeometryProperty(clazz, "getGeometry", "setGeometry", "geometry");

            bulkRemoveMethods(clazz, "setBeginTimestamp", "setEndTimestamp", "setBeginStatus", "setEndStatus");
        });
    }

    // Remove setters from WindDetails
    private void customizeWindDetails(PackageCustomization models) {
        customizeClass(models, "WindDetails", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setDirection", "setSpeed");
        });
    }

    // Remove setters from WindDirection
    private void customizeWindDirection(PackageCustomization models) {
        customizeClass(models, "WindDirection", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setDegrees", "setDescription");
        });
    }

    // Remove setters from WaypointForecast
    private void customizeWaypointForecast(PackageCustomization models) {
        customizeClass(models, "WaypointForecast", clazz -> {
            customizePrivateConstructor(clazz);
            bulkRemoveMethods(clazz, "setIconCode", "setIsDaytime", "setCloudCover", "setTemperature", "setWind",
                "setWindGust", "setPrecipitation", "setLightningCount", "setSunGlare", "setHazards",
                "setNotifications");
        });
    }

    private static void customizeClass(PackageCustomization models, String className,
        Consumer<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationConsumer) {
        models.getClass(className)
            .customizeAst(ast -> ast.getClassByName(className).ifPresent(classOrInterfaceDeclarationConsumer));
    }

    private static void bulkRemoveMethods(ClassOrInterfaceDeclaration clazz, String... methodsToRemove) {
        for (String methodToRemove : methodsToRemove) {
            clazz.getMethodsByName(methodToRemove).forEach(Node::remove);
        }
    }

    private void bulkPrivateConstructors(PackageCustomization models, String... classNames) {
        for (String className : classNames) {
            customizeClass(models, className, this::customizePrivateConstructor);
        }
    }

    private void addToIntMethod(PackageCustomization models, String... classNames) {
        for (String className : classNames) {
            customizeClass(models, className, clazz -> clazz.addMethod("toInt").setType("int")
                .setBody(StaticJavaParser.parseBlock("{ return Integer.parseInt(toString()); }")));
        }
    }
}
