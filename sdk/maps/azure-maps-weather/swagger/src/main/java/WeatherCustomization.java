// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import java.util.Arrays;

import com.azure.autorest.customization.ConstructorCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.MethodCustomization;
import org.slf4j.Logger;

/**
 * Customization class for Queue Storage.
 */
public class WeatherCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.weather.models");

        // customize classes with latlongpair
        customizeLatLongPairClasses(models, "StormForecast", "getCoordinates", "setCoordinates");
        customizeLatLongPairClasses(models, "StormLocation", "getCoordinates", "setCoordinates");
        customizeLatLongPairClasses(models, "WeatherWindow", "getTopLeft", "setTopLeft");
        customizeLatLongPairClasses(models, "WeatherWindow", "getBottomRight", "setBottomRight");

        // customize WeatherWindow
        customizeGeoJsonGeometryProperty(models, "WeatherWindow", "getGeometry", "setGeometry", "geometry");
        customizeGeoJsonGeometryProperty(models, "StormWindRadiiSummary", "getRadiiGeometry", "setRadiiGeometry",
            "radiiGeometry");

        // customize classes with the wrong getYear()
        customizeClassesWithString(models, "ActiveStorm", "getYear", "setYear");
        customizeClassesWithString(models, "StormSearchResultItem", "getYear", "setYear");

        // Remove setters in ActiveStorm
        customizeActiveStorm(models);

        // Remove setters in ActiveStormResult
        customizeActiveStormResult(models);

        // Remove setters in AirAndPollen
        customizeAirAndPollen(models);

        // Remove setters in AirQuality
        customizeAirQuality(models);

        // Remove setters in AirQualityResult
        customizeAirQualityResult(models);

        // Remove setters in AlertDetails
        customizeAlertDetails(models);

        // Remove setters in ColorValue
        customizeColorValue(models);

        // Remove setters in CurrentConditions
        customizeCurrentConditions(models);

        // Remove setters in DailyAirQuality
        customizeDailyAirQuality(models);

        // Remove setters in DailyAirQualityForecastResult
        customizeDailyAirQualityForecastResult(models);

        // Remove setters in DailyForecast
        customizeDailyForecast(models);
                 
        // Remove setters in DailyForecastDetail
        customizeDailyForecastDetail(models);

        // customize WeatherValue
        customizeWeatherValue(models);

        // Remove setters in DailyForecastSummary
        customizeDailyForecastSummary(models);

        // Remove setters in DailyHistoricalActuals
        customizeDailyHistoricalActuals(models);

        // Remove setters in DailyHistoricalActualsResult
        customizeDailyHistoricalActualsResult(models);

        // Remove setters in DailyHistoricalNormals
        customizeDailyHistoricalNormals(models);

        // Remove setters in DailyHistoricalNormalsResult
        customizeDailyHistoricalNormalsResult(models);

        // Remove setters in DailyHistoricalRecords
        customizeDailyHistoricalRecords(models);

        // Remove setters in DailyHistoricalRecordsResult
        customizeDailyHistoricalRecordsResult(models);

        // Remove setters in DailyIndex
        customizeDailyIndex(models);

        // Remove setters in DegreeDaySummary
        customizeDegreeDaySummary(models);

        // Remove setters in ForecastInterval
        customizeForecastInterval(models);

        // Remove setters in HazardDetail
        customizeHazardDetail(models);

        // Remove setters in HourlyForecast
        customizeHourlyForecast(models);

        // Remove setters in IntervalSummary
        customizeIntervalSummary(models);

        // Remove setters in LatestStatus
        customizeLatestStatus(models);

        // Remove setters in LocalSource
        customizeLocalSource(models);

        // Remove setters in MinuteForecastSummary
        customizeMinuteForecastSummary(models);

        // Remove setters in Pollutant
        customizePollutant(models);

        // Remove setters in PrecipitationSummary
        customizePrecipitationSummary(models);

        // Remove setters in PressureTendency
        customizePressureTendency(models);

        // Remove setters in PastHoursTemperature
        customizePastHoursTemperature(models);

        // Remove setters in QuarterDayForecast
        customizeQuarterDayForecast(models);

        // Remove setters in RadiusSector
        customizeRadiusSector(models);

        // Remove setters in SevereWeatherAlert
        customizeSevereWeatherAlert(models);

        // Remove setters in SevereWeatherAlertDescription
        customizeSevereWeatherAlertDescription(models);

        // Remove setters in StormForecast
        customizeStormForecast(models);

        // Remove setters in StormForecastResult
        customizeStormForecastResult(models);

        // Remove setters in StormLocation
        customizeStormLocation(models);

        // Remove setters in StormLocationsResult
        customizeStormLocationsResult(models);

        // Remove setters in StormSearchResult
        customizeStormSearchResult(models);

        // Remove setters in StormSearchResultItem
        customizeStormSearchResultItem(models);

        // Remove setters in StormWindRadiiSummary
        customizeStormWindRadiiSummary(models);

        // Remove setters in SunGlare
        customizeSunGlare(models);

        // Remove setters in TemperatureSummary
        customizeTemperatureSummary(models);

        // Remove setters in WeatherAlongRoutePrecipitation
        customizeWeatherAlongRoutePrecipitation(models);

        // Remove setters in WeatherHazards
        customizeWeatherHazards(models);

        // Remove setters in WeatherNotification
        customizeWeatherNotification(models);

        // Remove setters in WeatherValueMaxMinAvg
        customizeWeatherValueMaxMinAvg(models);

        // Remove setters in WeatherValueRange
        customizeWeatherValueRange(models);

        // Remove setters in WeatherValueYear
        customizeWeatherValueYear(models);

        // Remove setters in WeatherValueYearMax
        customizeWeatherValueYearMax(models);

        // Remove setters in WeatherValueYearMaxMinAvg
        customizeWeatherValueYearMaxMinAvg(models);

        // Remove setters in WeatherWindow
        customizeWeatherWindow(models);

        // Remove setters in WindDetails
        customizeWindDetails(models);

        // Remove setters in WindDirection
        customizeWindDirection(models);

        // Remove setters in WaypointForecast
        customizeWaypointForecast(models);

        // customize to make default constructor private
        customizePrivateConstructor(models, "WindDirection");
        customizePrivateConstructor(models, "WindDetails");
        customizePrivateConstructor(models, "WeatherWindow");
        customizePrivateConstructor(models, "WeatherValueYearMaxMinAvg");
        customizePrivateConstructor(models, "WeatherValueYearMax");
        customizePrivateConstructor(models, "WeatherValueYear");
        customizePrivateConstructor(models, "WeatherValueRange");
        customizePrivateConstructor(models, "WeatherValueMaxMinAvg");
        customizePrivateConstructor(models, "WeatherUnitDetails");
        customizePrivateConstructor(models, "WeatherNotification");
        customizePrivateConstructor(models, "WeatherHazards");
        customizePrivateConstructor(models, "WeatherAlongRouteSummary");
        customizePrivateConstructor(models, "WeatherAlongRouteResult");
        customizePrivateConstructor(models, "WeatherAlongRoutePrecipitation");
        customizePrivateConstructor(models, "WaypointForecast");
        customizePrivateConstructor(models, "TemperatureSummary");
        customizePrivateConstructor(models, "SunGlare");
        customizePrivateConstructor(models, "StormWindRadiiSummary");
        customizePrivateConstructor(models, "StormSearchResultItem");
        customizePrivateConstructor(models, "StormSearchResult");
        customizePrivateConstructor(models, "StormLocationsResult");
        customizePrivateConstructor(models, "StormLocation");
        customizePrivateConstructor(models, "StormForecastResult");
        customizePrivateConstructor(models, "StormForecast");
        customizePrivateConstructor(models, "SevereWeatherAlertsResult");
        customizePrivateConstructor(models, "SevereWeatherAlertDescription");
        customizePrivateConstructor(models, "SevereWeatherAlert");
        customizePrivateConstructor(models, "RadiusSector");
        customizePrivateConstructor(models, "QuarterDayForecastResult");
        customizePrivateConstructor(models, "QuarterDayForecast");
        customizePrivateConstructor(models, "PressureTendency");
        customizePrivateConstructor(models, "PrecipitationSummary");
        customizePrivateConstructor(models, "Pollutant");
        customizePrivateConstructor(models, "PastHoursTemperature");
        customizePrivateConstructor(models, "MinuteForecastSummary");
        customizePrivateConstructor(models, "MinuteForecastResult");
        customizePrivateConstructor(models, "LocalSource");
        customizePrivateConstructor(models, "LatestStatus");
        customizePrivateConstructor(models, "IntervalSummary");
        customizePrivateConstructor(models, "HourlyForecastResult");
        customizePrivateConstructor(models, "HourlyForecast");
        customizePrivateConstructor(models, "HazardDetail");
        customizePrivateConstructor(models, "ForecastInterval");
        customizePrivateConstructor(models, "DegreeDaySummary");
        customizePrivateConstructor(models, "DailyIndicesResult");
        customizePrivateConstructor(models, "DailyIndex");
        customizePrivateConstructor(models, "DailyHistoricalRecordsResult");
        customizePrivateConstructor(models, "DailyHistoricalRecords");
        customizePrivateConstructor(models, "DailyHistoricalNormalsResult");
        customizePrivateConstructor(models, "DailyHistoricalNormals");
        customizePrivateConstructor(models, "DailyHistoricalActualsResult");
        customizePrivateConstructor(models, "DailyHistoricalActuals");
        customizePrivateConstructor(models, "DailyForecastSummary");
        customizePrivateConstructor(models, "DailyForecastResult");
        customizePrivateConstructor(models, "DailyForecastDetail");
        customizePrivateConstructor(models, "DailyForecast");
        customizePrivateConstructor(models, "DailyAirQualityForecastResult");
        customizePrivateConstructor(models, "DailyAirQuality");
        customizePrivateConstructor(models, "CurrentConditionsResult");
        customizePrivateConstructor(models, "CurrentConditions");
        customizePrivateConstructor(models, "ColorValue");
        customizePrivateConstructor(models, "AlertDetails");
        customizePrivateConstructor(models, "AirQualityResult");
        customizePrivateConstructor(models, "AirQuality");
        customizePrivateConstructor(models, "AirAndPollen");
        customizePrivateConstructor(models, "ActiveStormResult");
        customizePrivateConstructor(models, "ActiveStorm");
    }

    // Customizes the StormForecast class
    private void customizeLatLongPairClasses(PackageCustomization models, String clazz, String getter, String setter) {
        ClassCustomization classCustomization = models.getClass(clazz);
        MethodCustomization methodCustomization = classCustomization.getMethod(getter);
        methodCustomization.setReturnType("GeoPosition", "new GeoPosition(returnValue.getLongitude(), " +
            "returnValue.getLatitude())");
        classCustomization.removeMethod(setter);
    }

    // Customizes the WeatherWindow and StormWindRadiiSummary classes
    // Have to customize it this way because setting return type imports the wrong Utility package.
    private void customizeGeoJsonGeometryProperty(PackageCustomization models,String clazz, String getter,
            String setter, String property) {
        ClassCustomization classCustomization = models.getClass(clazz);
        classCustomization.removeMethod(getter);
        classCustomization.removeMethod(setter);
        classCustomization.addMethod(
                "public GeoPolygon getPolygon() {\n" +
                "    return Utility.toGeoPolygon(this." + property + ");\n" +
                "}")
            .getJavadoc()
            .setDescription("Return GeoPolygon")
            .setReturn("Returns a {@link GeoPolygon} for this weather window");
        classCustomization.addImports("com.azure.maps.weather.implementation.helpers.Utility");
        classCustomization.addImports("com.azure.core.models.GeoPolygon");
    }

    // Customizes classes with getYear() as a String
    private void customizeClassesWithString(PackageCustomization models, String clazz, String getter, String setter) {
        ClassCustomization classCustomization = models.getClass(clazz);
        MethodCustomization methodCustomization = classCustomization.getMethod(getter);
        methodCustomization.setReturnType("Integer", "Integer.valueOf(returnValue)");
        classCustomization.removeMethod(setter);
    }

    // Customizes to private constructor class
    private void customizePrivateConstructor(PackageCustomization models, String className) {
        ClassCustomization classCustomization = models.getClass(className);
        String constructor = "public " + className + "()";
        ConstructorCustomization constructorCustomization = classCustomization.getConstructor(constructor);
        constructorCustomization.setModifier(2);
        String description = "Set default " + className + " constructor to private";
        constructorCustomization.getJavadoc().setDescription(description);
    }

    // Customizes to remove setter in ActiveStorm
    private void customizeActiveStorm(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ActiveStorm");
        classCustomization.removeMethod("setBasinId");
        classCustomization.removeMethod("setName");
        classCustomization.removeMethod("setIsActive");
        classCustomization.removeMethod("setIsSubtropical");
        classCustomization.removeMethod("setGovId");
    }

    // Customizes to remove setter in ActiveStormResult
    private void customizeActiveStormResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ActiveStormResult");
        classCustomization.removeMethod("setActiveStorms");
        classCustomization.removeMethod("setNextLink");
    }

    // Customizes to remove setter in AirAndPollen
    private void customizeAirAndPollen(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("AirAndPollen");
        classCustomization.removeMethod("setDescription");
        classCustomization.removeMethod("setValue");
        classCustomization.removeMethod("setCategory");
        classCustomization.removeMethod("setCategoryValue");
        classCustomization.removeMethod("setAirQualityType");
    }

    // Customizes to remove setter in AirQuality
    private void customizeAirQuality(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("AirQuality");
        classCustomization.removeMethod("setTimestamp");
        classCustomization.removeMethod("setIndex");
        classCustomization.removeMethod("setGlobalIndex");
        classCustomization.removeMethod("setDominantPollutant");
        classCustomization.removeMethod("setCategory");
        classCustomization.removeMethod("setCategoryColor");
        classCustomization.removeMethod("setDescription");
        classCustomization.removeMethod("setPollutants");
    }

    // Customizes to remove setter in AirQualityResult
    private void customizeAirQualityResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("AirQualityResult");
        classCustomization.removeMethod("setAirQualityResults");
        classCustomization.removeMethod("setNextLink");
    }

    // Customizes to remove setter in AlertDetails
    private void customizeAlertDetails(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("AlertDetails");
        classCustomization.removeMethod("setName");
        classCustomization.removeMethod("setDescription");
        classCustomization.removeMethod("setStartTime");
        classCustomization.removeMethod("setEndTime");
        classCustomization.removeMethod("setLatestStatus");
        classCustomization.removeMethod("setDetails");
        classCustomization.removeMethod("setLanguage");
    }

    // Customizes to remove setter in ColorValue
    private void customizeColorValue(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ColorValue");
        classCustomization.removeMethod("setRed");
        classCustomization.removeMethod("setGreen");
        classCustomization.removeMethod("setBlue");
        classCustomization.removeMethod("setHex");
    }

    // Customizes to remove setter in CurrentConditions
    private void customizeCurrentConditions(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("CurrentConditions");
        classCustomization.removeMethod("setDateTime");
        classCustomization.removeMethod("setDescription");
        classCustomization.removeMethod("setIconCode");
        classCustomization.removeMethod("setHasPrecipitation");
        classCustomization.removeMethod("setIsDaytime");
        classCustomization.removeMethod("setTemperature");
        classCustomization.removeMethod("setRealFeelTemperature");
        classCustomization.removeMethod("setRealFeelTemperatureShade");
        classCustomization.removeMethod("setRelativeHumidity");
        classCustomization.removeMethod("setDewPoint");
        classCustomization.removeMethod("setWind");
        classCustomization.removeMethod("setWindGust");
        classCustomization.removeMethod("setUvIndex");
        classCustomization.removeMethod("setUvIndexDescription");
        classCustomization.removeMethod("setVisibility");
        classCustomization.removeMethod("setObstructionsToVisibility");
        classCustomization.removeMethod("setCloudCover");
        classCustomization.removeMethod("setCloudCeiling");
        classCustomization.removeMethod("setPressure");
        classCustomization.removeMethod("setPressureTendency");
        classCustomization.removeMethod("setPastTwentyFourHourTemperatureDeparture");
        classCustomization.removeMethod("setApparentTemperature");
        classCustomization.removeMethod("setWindChillTemperature");
        classCustomization.removeMethod("setWetBulbTemperature");
        classCustomization.removeMethod("setPrecipitationSummary");
        classCustomization.removeMethod("setTemperatureSummary");
    }

    // Customizes to remove setter in DailyAirQuality
    private void customizeDailyAirQuality(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyAirQuality");
        classCustomization.removeMethod("setTimestamp");
        classCustomization.removeMethod("setIndex");
        classCustomization.removeMethod("setGlobalIndex");
        classCustomization.removeMethod("setDominantPollutant");
        classCustomization.removeMethod("setCategory");
        classCustomization.removeMethod("setCategoryColor");
        classCustomization.removeMethod("setDescription");
    }

    // Customizes to remove setter in DailyAirQualityForecastResult
    private void customizeDailyAirQualityForecastResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyAirQualityForecastResult");
        classCustomization.removeMethod("setAirQualityResults");
        classCustomization.removeMethod("setNextLink");
    }

    // Customizes to remove setter in DailyForecast
    private void customizeDailyForecast(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyForecast");
        classCustomization.removeMethod("setDateTime");
        classCustomization.removeMethod("setTemperature");
        classCustomization.removeMethod("setRealFeelTemperature");
        classCustomization.removeMethod("setRealFeelTemperatureShade");
        classCustomization.removeMethod("setHoursOfSun");
        classCustomization.removeMethod("setMeanTemperatureDeviation");
        classCustomization.removeMethod("setAirQuality");
        classCustomization.removeMethod("setDaytimeForecast");
        classCustomization.removeMethod("setNighttimeForecast");
        classCustomization.removeMethod("setSources");
    }

    // Customizes to remove setter in DailyForecastDetail
    private void customizeDailyForecastDetail(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyForecastDetail");
        classCustomization.removeMethod("setIconCode");
        classCustomization.removeMethod("setIconPhrase");
        classCustomization.removeMethod("setLocalSource");
        classCustomization.removeMethod("setHasPrecipitation");
        classCustomization.removeMethod("setPrecipitationType");
        classCustomization.removeMethod("setPrecipitationIntensity");
        classCustomization.removeMethod("setShortDescription");
        classCustomization.removeMethod("setLongPhrase");
        classCustomization.removeMethod("setPrecipitationProbability");
        classCustomization.removeMethod("setThunderstormProbability");
        classCustomization.removeMethod("setRainProbability");
        classCustomization.removeMethod("setSnowProbability");
        classCustomization.removeMethod("setIceProbability");
        classCustomization.removeMethod("setWind");
        classCustomization.removeMethod("setWindGust");
        classCustomization.removeMethod("setTotalLiquid");
        classCustomization.removeMethod("setRain");
        classCustomization.removeMethod("setSnow");
        classCustomization.removeMethod("setIce");
        classCustomization.removeMethod("setHoursOfPrecipitation");
        classCustomization.removeMethod("setHoursOfRain");
        classCustomization.removeMethod("setHoursOfSnow");
        classCustomization.removeMethod("setHoursOfIce");
        classCustomization.removeMethod("setCloudCover");
        classCustomization.getMethod("isHasPrecipitation").rename("hasPrecipitation");
        classCustomization.getMethod("getRain").rename("getRainUnitDetails");
        classCustomization.getMethod("getSnow").rename("getSnowUnitDetails");
        classCustomization.getMethod("getIce").rename("getIceUnitDetails");
        classCustomization.getMethod("getWind").rename("getWindUnitDetails");
    }

    // Customize WeatherValue
    private void customizeWeatherValue(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WeatherValue");
        classCustomization.rename("WeatherUnitDetails");
        classCustomization.removeMethod("setValue");
        classCustomization.removeMethod("setUnitLabel");
        classCustomization.removeMethod("setUnitType");
    }

    // Remove setters from DailyForecastSummary
    private void customizeDailyForecastSummary(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyForecastSummary");
        classCustomization.removeMethod("setStartDate");
        classCustomization.removeMethod("setEndDate");
        classCustomization.removeMethod("setSeverity");
        classCustomization.removeMethod("setPhrase");
        classCustomization.removeMethod("setCategory");
    }

    // Remove setters from DailyHistoricalActuals
    private void customizeDailyHistoricalActuals(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyHistoricalActuals");
        classCustomization.removeMethod("setTimestamp");
        classCustomization.removeMethod("setTemperature");
        classCustomization.removeMethod("setDegreeDaySummary");
        classCustomization.removeMethod("setPrecipitation");
        classCustomization.removeMethod("setSnowfall");
        classCustomization.removeMethod("setSnowDepth");
    }

    // Remove setters from DailyHistoricalActualsResult
    private void customizeDailyHistoricalActualsResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyHistoricalActualsResult");
        classCustomization.removeMethod("setHistoricalActuals");
        classCustomization.removeMethod("setNextLink");
    }

    // Remove setters from DailyHistoricalNormals
    private void customizeDailyHistoricalNormals(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyHistoricalNormals");
        classCustomization.removeMethod("setTimestamp");
        classCustomization.removeMethod("setTemperature");
        classCustomization.removeMethod("setDegreeDaySummary");
        classCustomization.removeMethod("setPrecipitation");
    }

    // Remove setters from DailyHistoricalNormalsResult
    private void customizeDailyHistoricalNormalsResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyHistoricalNormalsResult");
        classCustomization.removeMethod("setHistoricalNormals");
        classCustomization.removeMethod("setNextLink");
    }

    // Remove setters from DailyHistoricalRecords
    private void customizeDailyHistoricalRecords(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyHistoricalRecords");
        classCustomization.removeMethod("setTimestamp");
        classCustomization.removeMethod("setTemperature");
        classCustomization.removeMethod("setSnowfall");
        classCustomization.removeMethod("setPrecipitation");
    }

    // Remove setters from DailyHistoricalRecordsResult
    private void customizeDailyHistoricalRecordsResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyHistoricalRecordsResult");
        classCustomization.removeMethod("setHistoricalRecords");
        classCustomization.removeMethod("setNextLink");
    }

    // Remove setters from DailyIndex
    private void customizeDailyIndex(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DailyIndex");
        classCustomization.removeMethod("setIndexName");
        classCustomization.removeMethod("setIndexId");
        classCustomization.removeMethod("setDateTime");
        classCustomization.removeMethod("setValue");
        classCustomization.removeMethod("setCategoryDescription");
        classCustomization.removeMethod("setCategoryValue");
        classCustomization.removeMethod("setIsAscending");
        classCustomization.removeMethod("setDescription");
    }

    // Remove setters from DegreeDaySummary
    private void customizeDegreeDaySummary(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DegreeDaySummary");
        classCustomization.removeMethod("setHeating");
        classCustomization.removeMethod("setCooling");
    }

    // Remove setters from ForecastInterval
    private void customizeForecastInterval(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ForecastInterval");
        classCustomization.removeMethod("setStartTime");
        classCustomization.removeMethod("setMinute");
        classCustomization.removeMethod("setDecibelRelativeToZ");
        classCustomization.removeMethod("setShortDescription");
        classCustomization.removeMethod("setThreshold");
        classCustomization.removeMethod("setColor");
        classCustomization.removeMethod("setSimplifiedColor");
        classCustomization.removeMethod("setPrecipitationType");
        classCustomization.removeMethod("setIconCode");
        classCustomization.removeMethod("setCloudCover");
    }

    // Remove setters from HazardDetail
    private void customizeHazardDetail(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("HazardDetail");
        classCustomization.removeMethod("setHazardIndex");
        classCustomization.removeMethod("setHazardCode");
        classCustomization.removeMethod("setShortDescription");
    }

    // Remove setters from HourlyForecast
    private void customizeHourlyForecast(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("HourlyForecast");
        classCustomization.removeMethod("setTimestamp");
        classCustomization.removeMethod("setIconCode");
        classCustomization.removeMethod("setIconPhrase");
        classCustomization.removeMethod("setHasPrecipitation");
        classCustomization.removeMethod("setIsDaylight");
        classCustomization.removeMethod("setTemperature");
        classCustomization.removeMethod("setRealFeelTemperature");
        classCustomization.removeMethod("setWetBulbTemperature");
        classCustomization.removeMethod("setDewPoint");
        classCustomization.removeMethod("setWind");
        classCustomization.removeMethod("setWindGust");
        classCustomization.removeMethod("setRelativeHumidity");
        classCustomization.removeMethod("setVisibility");
        classCustomization.removeMethod("setCloudCeiling");
        classCustomization.removeMethod("setUvIndex");
        classCustomization.removeMethod("setUvIndexDescription");
        classCustomization.removeMethod("setPrecipitationProbability");
        classCustomization.removeMethod("setRainProbability");
        classCustomization.removeMethod("setSnowProbability");
        classCustomization.removeMethod("setIceProbability");
        classCustomization.removeMethod("setTotalLiquid");
        classCustomization.removeMethod("setRain");
        classCustomization.removeMethod("setSnow");
        classCustomization.removeMethod("setIce");
        classCustomization.removeMethod("setCloudCover");
    }

    // Remove setters from IntervalSummary
    private void customizeIntervalSummary(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("IntervalSummary");
        classCustomization.removeMethod("setStartMinute");
        classCustomization.removeMethod("setEndMinute");
        classCustomization.removeMethod("setTotalMinutes");
        classCustomization.removeMethod("setShortDescription");
        classCustomization.removeMethod("setBriefDescription");
        classCustomization.removeMethod("setLongPhrase");
        classCustomization.removeMethod("setIconCode");
    }

    // Remove setters from LatestStatus
    private void customizeLatestStatus(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("LatestStatus");
        classCustomization.removeMethod("setLocalized");
        classCustomization.removeMethod("setEnglish");
    }

    // Remove setters from LocalSource
    private void customizeLocalSource(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("LocalSource");
        classCustomization.removeMethod("setId");
        classCustomization.removeMethod("setName");
        classCustomization.removeMethod("setWeatherCode");
    }

    // Remove setters from MinuteForecastSummary
    private void customizeMinuteForecastSummary(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("MinuteForecastSummary");
        classCustomization.removeMethod("setBriefPhrase60");
        classCustomization.removeMethod("setShortDescription");
        classCustomization.removeMethod("setBriefDescription");
        classCustomization.removeMethod("setLongPhrase");
        classCustomization.removeMethod("setIconCode");
    }

    // Remove setters from Pollutant
    private void customizePollutant(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("Pollutant");
        classCustomization.removeMethod("setType");
        classCustomization.removeMethod("setName");
        classCustomization.removeMethod("setIndex");
        classCustomization.removeMethod("setGlobalIndex");
        classCustomization.removeMethod("setConcentration");
    }

    // Remove setters from PrecipitationSummary
    private void customizePrecipitationSummary(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("PrecipitationSummary");
        classCustomization.removeMethod("setPastHour");
        classCustomization.removeMethod("setPastThreeHours");
        classCustomization.removeMethod("setPastSixHours");
        classCustomization.removeMethod("setPastNineHours");
        classCustomization.removeMethod("setPastTwelveHours");
        classCustomization.removeMethod("setPastEighteenHours");
        classCustomization.removeMethod("setPastTwentyFourHours");
    }

    // Remove setters from PressureTendency
    private void customizePressureTendency(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("PressureTendency");
        classCustomization.removeMethod("setDescription");
        classCustomization.removeMethod("setCode");
    }

    // Remove setters from PastHoursTemperature
    private void customizePastHoursTemperature(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("PastHoursTemperature");
        classCustomization.removeMethod("setMinimum");
        classCustomization.removeMethod("setMaximum");
    }

    // Remove setters from QuarterDayForecast
    private void customizeQuarterDayForecast(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("QuarterDayForecast");
        classCustomization.removeMethod("setDateTime");
        classCustomization.removeMethod("setEffectiveDate");
        classCustomization.removeMethod("setQuarter");
        classCustomization.removeMethod("setIconCode");
        classCustomization.removeMethod("setIconPhrase");
        classCustomization.removeMethod("setPhrase");
        classCustomization.removeMethod("setTemperature");
        classCustomization.removeMethod("setRealFeelTemperature");
        classCustomization.removeMethod("setDewPoint");
        classCustomization.removeMethod("setRelativeHumidity");
        classCustomization.removeMethod("setWind");
        classCustomization.removeMethod("setWindGust");
        classCustomization.removeMethod("setVisibility");
        classCustomization.removeMethod("setCloudCover");
        classCustomization.removeMethod("setPrecipitationType");
        classCustomization.removeMethod("setHasPrecipitation");
        classCustomization.removeMethod("setPrecipitationIntensity");
        classCustomization.removeMethod("setPrecipitationProbability");
        classCustomization.removeMethod("setThunderstormProbability");
        classCustomization.removeMethod("setTotalLiquid");
        classCustomization.removeMethod("setRain");
        classCustomization.removeMethod("setSnow");
        classCustomization.removeMethod("setIce");
    }

    // Remove setters from RadiusSector
    private void customizeRadiusSector(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("RadiusSector");
        classCustomization.removeMethod("setBeginBearing");
        classCustomization.removeMethod("setEndBearing");
        classCustomization.removeMethod("setRadius");
    }

    // Remove setters from SevereWeatherAlert
    private void customizeSevereWeatherAlert(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("SevereWeatherAlert");
        classCustomization.removeMethod("setCountryCode");
        classCustomization.removeMethod("setAlertId");
        classCustomization.removeMethod("setDescription");
        classCustomization.removeMethod("setCategory");
        classCustomization.removeMethod("setPriority");
        classCustomization.removeMethod("setClassification");
        classCustomization.removeMethod("setLevel");
        classCustomization.removeMethod("setSource");
        classCustomization.removeMethod("setSourceId");
        classCustomization.removeMethod("setDisclaimer");
        classCustomization.removeMethod("setAlertDetails");
    }

    // Remove setters from SevereWeatherAlertDescription
    private void customizeSevereWeatherAlertDescription(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("SevereWeatherAlertDescription");
        classCustomization.removeMethod("setDescription");
        classCustomization.removeMethod("setStatus");
    }

    // Remove setters from StormForecast
    private void customizeStormForecast(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("StormForecast");
        classCustomization.removeMethod("setTimestamp");
        classCustomization.removeMethod("setInitializedTimestamp");
        classCustomization.removeMethod("setMaxWindGust");
        classCustomization.removeMethod("setSustainedWind");
        classCustomization.removeMethod("setStatus");
        classCustomization.removeMethod("setWeatherWindow");
        classCustomization.removeMethod("setWindRadiiSummary");
    }

    // Remove setters from StormForecastResult
    private void customizeStormForecastResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("StormForecastResult");
        classCustomization.removeMethod("setStormForecasts");
        classCustomization.removeMethod("setNextLink");
    }

    // Remove setters from StormLocation
    private void customizeStormLocation(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("StormLocation");
        classCustomization.removeMethod("setTimestamp");
        classCustomization.removeMethod("setMaxWindGust");
        classCustomization.removeMethod("setSustainedWind");
        classCustomization.removeMethod("setMinimumPressure");
        classCustomization.removeMethod("setMovement");
        classCustomization.removeMethod("setStatus");
        classCustomization.removeMethod("setIsSubtropical");
        classCustomization.removeMethod("setHasTropicalPotential");
        classCustomization.removeMethod("setIsPostTropical");
        classCustomization.removeMethod("setWindRadiiSummary");
    }

    // Remove setters from StormLocationsResult
    private void customizeStormLocationsResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("StormLocationsResult");
        classCustomization.removeMethod("setStormLocations");
        classCustomization.removeMethod("setNextLink");
    }

    // Remove setters from StormSearchResult
    private void customizeStormSearchResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("StormSearchResult");
        classCustomization.removeMethod("setStorms");
        classCustomization.removeMethod("setNextLink");
    }

    // Remove setters from StormSearchResultItem
    private void customizeStormSearchResultItem(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("StormSearchResultItem");
        classCustomization.removeMethod("setBasinId");
        classCustomization.removeMethod("setName");
        classCustomization.removeMethod("setIsActive");
        classCustomization.removeMethod("setIsRetired");
        classCustomization.removeMethod("setIsSubtropical");
        classCustomization.removeMethod("setGovId");
    }

    // Remove setters from StormWindRadiiSummary
    private void customizeStormWindRadiiSummary(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("StormWindRadiiSummary");
        classCustomization.removeMethod("setTimestamp");
        classCustomization.removeMethod("setWindSpeed");
        classCustomization.removeMethod("setRadiusSectorData");
    }

    // Remove setters from SunGlare
    private void customizeSunGlare(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("SunGlare");
        classCustomization.removeMethod("setCalculatedVehicleHeading");
        classCustomization.removeMethod("setGlareIndex");
    }

    // Remove setters from TemperatureSummary
    private void customizeTemperatureSummary(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TemperatureSummary");
        classCustomization.removeMethod("setPastSixHours");
        classCustomization.removeMethod("setPastTwelveHours");
        classCustomization.removeMethod("setPastTwentyFourHours");
    }

    // Remove setters from WeatherAlongRoutePrecipitation
    private void customizeWeatherAlongRoutePrecipitation(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WeatherAlongRoutePrecipitation");
        classCustomization.removeMethod("setDbz");
        classCustomization.removeMethod("setType");
        classCustomization.getMethod("getDbz").rename("getDeciblesRelativeToZ");
    }

    // Remove setters from WeatherAlongRouteSummary
    private void customizeWeatherAlongRouteSummary(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WeatherAlongRouteSummary");
        classCustomization.removeMethod("setIconCode");
        classCustomization.removeMethod("setHazards");
    }

    // Remove setters from WeatherHazards
    private void customizeWeatherHazards(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WeatherHazards");
        classCustomization.removeMethod("setMaxHazardIndex");
        classCustomization.removeMethod("setDetails");
    }

    // Remove setters from WeatherNotification
    private void customizeWeatherNotification(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WeatherNotification");
        classCustomization.removeMethod("setType");
        classCustomization.removeMethod("setHazardIndex");
        classCustomization.removeMethod("setHazardCode");
        classCustomization.removeMethod("setShortDescription");
    }

    // Remove setters from WeatherValueMaxMinAvg
    private void customizeWeatherValueMaxMinAvg(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WeatherValueMaxMinAvg");
        classCustomization.removeMethod("setMaximum");
        classCustomization.removeMethod("setMinimum");
        classCustomization.removeMethod("setAverage");
    }

    // Remove setters from WeatherValueRange
    private void customizeWeatherValueRange(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WeatherValueRange");
        classCustomization.removeMethod("setMinimum");
        classCustomization.removeMethod("setMaximum");
    }

    // Remove setters from WeatherValueYear
    private void customizeWeatherValueYear(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WeatherValueYear");
        classCustomization.removeMethod("setValue");
        classCustomization.removeMethod("setUnit");
        classCustomization.removeMethod("setUnitType");
        classCustomization.removeMethod("setYear");
    }

    // Remove setters from WeatherValueYearMax
    private void customizeWeatherValueYearMax(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WeatherValueYearMax");
        classCustomization.removeMethod("setMaximum");
    }

    // Remove setters from WeatherValueYearMaxMinAvg
    private void customizeWeatherValueYearMaxMinAvg(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WeatherValueYearMaxMinAvg");
        classCustomization.removeMethod("setMaximum");
        classCustomization.removeMethod("setMinimum");
        classCustomization.removeMethod("setAverage");
    }

    // Remove setters from WeatherWindow
    private void customizeWeatherWindow(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WeatherWindow");
        classCustomization.removeMethod("setBeginTimestamp");
        classCustomization.removeMethod("setEndTimestamp");
        classCustomization.removeMethod("setBeginStatus");
        classCustomization.removeMethod("setEndStatus");
    }

    // Remove setters from WindDetails
    private void customizeWindDetails(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WindDetails");
        classCustomization.removeMethod("setDirection");
        classCustomization.removeMethod("setSpeed");
    }

    // Remove setters from WindDirection
    private void customizeWindDirection(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WindDirection");
        classCustomization.removeMethod("setDegrees");
        classCustomization.removeMethod("setDescription");
    }

    // Remove setters from WaypointForecast
    private void customizeWaypointForecast(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("WaypointForecast");
        classCustomization.removeMethod("setIconCode");
        classCustomization.removeMethod("setShortDescription");
        classCustomization.removeMethod("setIsDaytime");
        classCustomization.removeMethod("setCloudCover");
        classCustomization.removeMethod("setTemperature");
        classCustomization.removeMethod("setWind");
        classCustomization.removeMethod("setWindGust");
        classCustomization.removeMethod("setPrecipitation");
        classCustomization.removeMethod("setLightningCount");
        classCustomization.removeMethod("setSunGlare");
        classCustomization.removeMethod("setHazards");
        classCustomization.removeMethod("setNotifications");
    }
}
