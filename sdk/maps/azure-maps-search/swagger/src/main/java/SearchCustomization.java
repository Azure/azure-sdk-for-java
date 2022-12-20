// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.ConstructorCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

/**
 * Customization class for Queue Storage.
 */
public class SearchCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.search.models");
        PackageCustomization implementationModels = customization.getPackage("com.azure.maps.search.implementation.models");

        // customize AddressRanges
        customizeAddressRanges(models);

        // customize EntryPoint
        customizeEntryPoint(models);

        // customize SearchAddressResultItem
        customizeSearchAddressResultItem(models);

        // customize SearchSummary
        customizeSearchSummary(models);

        // customize ReverseSearchAddressResultItem
        customizeReverseSearchAddressResultItem(models);

        // customize ReverseSearchCrossStreetAddressResultItem
        customizeReverseSearchCrossStreetAddressResultItem(models);

        // customizePolygon
        customizePolygon(models);

        // customize operating hours time
        customizeOperatingHoursTime(implementationModels);

        // customize operating hours time range
        customizeOperatingHoursTimeRange(models);

        // customize data source
        customizeDataSource(models);

        // customize geometry identifier
        customizeGeometryIdentifier(models);

        // customize Reverse Search Address Batch Item Private Response
        customizeReverseSearchAddressBatchItemPrivateResponse(implementationModels);

        // customize Search Address Batch Item Private Response
        customizeSearchAddressBatchItemPrivateResponse(implementationModels);

        // customize Error Detail
        customizeErrorDetail(implementationModels);

        // customize Address
        customizeAddress(models);
    }

    // Customizes the Address class by changing the type of BoundingBox
    private void customizeAddress(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("Address");
        MethodCustomization methodCustomization = classCustomization.getMethod("getBoundingBox");
        methodCustomization.setReturnType("GeoBoundingBox",
            "com.azure.maps.search.implementation.helpers.Utility.toGeoBoundingBox(returnValue)");
        classCustomization.addImports("com.azure.core.models.GeoBoundingBox");

        // getCountryCodeISO3
        MethodCustomization getCountryCodeIso3NameCustomization = classCustomization.getMethod("getCountryCodeISO3");
        MethodCustomization getCountryCodeIso3Customization = getCountryCodeIso3NameCustomization.rename("getCountryCodeIso3");

        // customize Address class name
        ClassCustomization classCustomization2 = models.getClass("Address");
        classCustomization2.rename("MapsSearchAddress");
    }


    // Customizes the AddressRanges class
    private void customizeAddressRanges(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("AddressRanges");
        String[] methods = new String[] {"setRangeRight", "setRangeLeft", "setTo", "setFrom"};

        // getTo
        MethodCustomization toCustomization = classCustomization.getMethod("getTo");
        toCustomization.setReturnType("GeoPosition",
            "new GeoPosition(returnValue.getLon(), returnValue.getLat())");

        // getFrom
        MethodCustomization fromCustomization = classCustomization.getMethod("getFrom");
        fromCustomization.setReturnType("GeoPosition",
            "new GeoPosition(returnValue.getLon(), returnValue.getLat())");

        // remove setters
        for (String method : methods) {
            classCustomization.removeMethod(method);
        }
        classCustomization.addImports("com.azure.core.models.GeoPosition");
    }

    // Customizes the EntryPoint class
    private void customizeEntryPoint(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("EntryPoint");
        String[] methods = new String[] {"setPosition"};

        // getPosition
        MethodCustomization toCustomization = classCustomization.getMethod("getPosition");
        toCustomization.setReturnType("GeoPosition",
            "new GeoPosition(returnValue.getLon(), returnValue.getLat())");

        // remove setters
        for (String method : methods) {
            classCustomization.removeMethod(method);
        }
        classCustomization.addImports("com.azure.core.models.GeoPosition");
    }

    // Customizes the SearchAddressResultItem class
    private void customizeSearchAddressResultItem(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("SearchAddressResultItem");
        String[] methods = new String[] {"setPosition"};

        // getPosition
        MethodCustomization toCustomization = classCustomization.getMethod("getPosition");
        toCustomization.setReturnType("GeoPosition",
           "new GeoPosition(returnValue.getLon(), returnValue.getLat())");

        // getViewport
        MethodCustomization viewportCustomization = classCustomization.getMethod("getViewport");
        viewportCustomization.rename("getBoundingBox");
        viewportCustomization = classCustomization.getMethod("getBoundingBox");

        viewportCustomization.setReturnType("GeoBoundingBox",
            " new GeoBoundingBox(returnValue.getTopLeft().getLon(), " +
            " returnValue.getTopLeft().getLat(), " +
            " returnValue.getBottomRight().getLon(), " +
            " returnValue.getBottomRight().getLat())");

        // data sources
        MethodCustomization dsCustomization = classCustomization.getMethod("getDataSources");
        dsCustomization.rename("getDataSource");

        // remove setters
        for (String method : methods) {
            classCustomization.removeMethod(method);
        }
        classCustomization.addImports("com.azure.core.models.GeoPosition");
        classCustomization.addImports("com.azure.core.models.GeoBoundingBox");
    }

    // Customizes the SearchSummary class
    private void customizeSearchSummary(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("SearchSummary");

        // getPosition
        MethodCustomization toCustomization = classCustomization.getMethod("getGeoBias");
        toCustomization.setReturnType("GeoPosition",
            "returnValue != null ? new GeoPosition(returnValue.getLon(), returnValue.getLat()) : null");

        classCustomization.addImports("com.azure.core.models.GeoPosition");
    }

    // Customizes the ReverseSearchAddressResultItem class
    private void customizeReverseSearchAddressResultItem(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ReverseSearchAddressResultItem");

        // getPosition
        MethodCustomization toCustomization = classCustomization.getMethod("getPosition");
        toCustomization.setReturnType("GeoPosition",
            "com.azure.maps.search.implementation.helpers.Utility.fromCommaSeparatedString(returnValue)");

        classCustomization.addImports("com.azure.core.models.GeoPosition");
    }

    // Customizes the ReverseSearchCrossStreetAddressResultItem class
    private void customizeReverseSearchCrossStreetAddressResultItem(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ReverseSearchCrossStreetAddressResultItem");

        // getPosition
        MethodCustomization toCustomization = classCustomization.getMethod("getPosition");
        toCustomization.setReturnType("GeoPosition",
            "com.azure.maps.search.implementation.helpers.Utility.fromCommaSeparatedString(returnValue)");

        classCustomization.addImports("com.azure.core.models.GeoPosition");
    }

    // Customizes the Polygon class
    private void customizePolygon(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("Polygon");

        // getGeometryData
        MethodCustomization nameCustomization = classCustomization.getMethod("getGeometryData");
        MethodCustomization geometryCustomization = nameCustomization.rename("getGeometry");

        geometryCustomization.setReturnType("GeoObject",
            "com.azure.maps.search.implementation.helpers.Utility.toGeoObject(returnValue)");

        classCustomization.removeMethod("setGeometryData");
        classCustomization.addImports("com.azure.core.models.GeoObject");

        // getProviderId
        MethodCustomization providerIdNameCustomization = classCustomization.getMethod("getProviderID");
        MethodCustomization providerIdCustomization = providerIdNameCustomization.rename("getProviderId");

        // Change Polygon class name
        ClassCustomization classCustomization2 = models.getClass("Polygon");
        classCustomization2.rename("MapsPolygon");
    }

    // Customizes the OperatingHoursTime class
    private void customizeOperatingHoursTime(PackageCustomization implementationModels) {
        ClassCustomization classCustomization = implementationModels.getClass("OperatingHoursTime");
        classCustomization.addConstructor(
            "public OperatingHoursTime(String date, Integer hour, Integer minute) {\n" +
                "this.date = date;\n" +
                "this.hour = hour;\n" +
                "this.minute = minute;\n" +
            "}")
            .getJavadoc()
            .setDescription("OperatingHoursTime Constructor")
            .setParam("date", "The date in the format of yyyy-mm-dd represented by a string")
            .setParam("hour", "int representing the hour")
            .setParam("minute", "int representing the minute");

        ConstructorCustomization constructorCustomization = classCustomization.getConstructor("public OperatingHoursTime()");
        constructorCustomization.setModifier(2).addAnnotation("JsonCreator");
    }

    // Customizes the OperatingHoursTimeRange class
    private void customizeOperatingHoursTimeRange(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("OperatingHoursTimeRange");

        // add constructor
        classCustomization.addConstructor(
            "public OperatingHoursTimeRange(LocalDateTime startTime, LocalDateTime endTime) {\n" +
            "   this.startTime =  new OperatingHoursTime(startTime.toLocalDate().toString(), startTime.getHour(), startTime.getMinute());\n" +
            "   this.endTime =  new OperatingHoursTime(endTime.toLocalDate().toString(), endTime.getHour(), endTime.getMinute());\n" +
            "}")
            .getJavadoc()
            .setDescription("OperatingHoursTimeRange constructor")
            .setParam("startTime", "The point in the next 7 days range when a given POI is being opened, or the beginning of the range if it was opened before the range.")
            .setParam("endTime", "The point in the next 7 days range when a given POI is being closed, or the beginning of the range if it was closed before the range.");

        ConstructorCustomization constructorCustomization = classCustomization.getConstructor("public OperatingHoursTimeRange()");
        constructorCustomization.setModifier(2).addAnnotation("JsonCreator");

        classCustomization.removeMethod("getStartTime");
        classCustomization.removeMethod("getEndTime");
        classCustomization.removeMethod("setStartTime");
        classCustomization.removeMethod("setEndTime");

        // get start time
        classCustomization.addMethod(
            "public LocalDateTime getStartTime() {\n" +
            "   String[] date = this.startTime.toString().split(\"-\");\n" +
            "   int year = Integer.parseInt(date[0]);\n" +
            "   int month = Integer.parseInt(date[1]);\n" +
            "   int day = Integer.parseInt(date[2]);\n" +
            "   return LocalDateTime.of(year, month, day, this.startTime.getHour(), this.startTime.getMinute());\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the startTime property: The point in the next 7 days range when a given POI is being opened, or the beginning of the range if it was opened before the range.")
            .setReturn("the startTime value");

        // get end time
        classCustomization.addMethod(
            "public LocalDateTime getEndTime() {\n" +
            "   String[] date = this.endTime.toString().split(\"-\");\n" +
            "   int year = Integer.parseInt(date[0]);\n" +
            "   int month = Integer.parseInt(date[1]);\n" +
            "   int day = Integer.parseInt(date[2]);\n" +
            "   return LocalDateTime.of(year, month, day, this.endTime.getHour(), this.endTime.getMinute());\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the endTime property: The point in the next 7 days range when a given POI is being closed, or the beginning of the range if it was closed before the range")
            .setReturn("the endTime value.");

        // set start time
        classCustomization.addMethod(
            "public OperatingHoursTimeRange setStartTime(LocalDateTime startTime) {\n" +
            "    this.startTime = new OperatingHoursTime(startTime.toLocalDate().toString(), startTime.getHour(), startTime.getMinute());\n" +
            "    return this;\n" +
            "}")
            .getJavadoc()
            .setDescription("Set the startTime property: The point in the next 7 days range when a given POI is being opened, or the beginning of the range if it was opened before the range")
            .setParam("startTime", "the startTime value to set.")
            .setReturn("the OperatingHoursTimeRange object itself.");

            // set end time
            classCustomization.addMethod(
                "public OperatingHoursTimeRange setEndTime(LocalDateTime endTime) {\n" +
                "    this.endTime = new OperatingHoursTime(endTime.toLocalDate().toString(), endTime.getHour(), endTime.getMinute());\n" +
                "    return this;\n" +
                "}")
            .getJavadoc()
            .setDescription("Set the endTime property: The point in the next 7 days range when a given POI is being closed, or the beginning of the range if it was closed before the range.")
            .setParam("endTime", "the endTime value to set")
            .setReturn("the OperatingHoursTimeRange object itself");

            classCustomization.addImports("java.time.LocalDateTime");
    }

    // Customizes the GeometryIdentifier class
    private void customizeGeometryIdentifier(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("GeometryIdentifier")
            .removeAnnotation("@Immutable")
            .addAnnotation("@Fluent");

        // set id
        classCustomization.addMethod(
            "public GeometryIdentifier setId(String id) {\n" +
            "  this.id = id;\n" +
            "  return this;\n" +
            "}")
            .getJavadoc()
            .setDescription("Set the id property: Pass this as geometryId to the [Get Search Polygon] (https://docs.microsoft.com/rest/api/maps/search/getsearchpolygon) API to fetch geometry information for this result.")
            .setParam("id", "The geometryId")
            .setReturn("the updated GeometryIdentifier object");
    }

    // Customizes the DataSource class
    private void customizeDataSource(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("DataSource");

        classCustomization.addConstructor(
            "public DataSource(String geometry) {\n" +
            "    this.geometry = new GeometryIdentifier().setId(geometry);\n" +
            "}")
        .getJavadoc()
        .setDescription("@param geometry this is geometry id");

        // classCustomization.addConstructor(
        //     "public DataSource() {\n" +
        //     "   // Empty Constructor\n" +
        //     "}")
        // .getJavadoc()
        // .setDescription("Constructor");

        // get geometry
        classCustomization.removeMethod("getGeometry");
        classCustomization.addMethod(
            "public String getGeometry() {\n" +
            "   return this.geometry.toString();\n" +
            "}")
        .getJavadoc()
        .setDescription("Get the geometry property: Information about the geometric shape of the result. Only present if type == Geography.")
        .setReturn("the geometry value");

        // set geometry
        classCustomization.removeMethod("setGeometry");
        classCustomization.addMethod(
            "public DataSource setGeometry(String geometry) {\n" +
            "   this.geometry = new GeometryIdentifier().setId(geometry);" +
            "   return this;" +
            "}")
        .getJavadoc()
        .setDescription("Set the geometry property: Information about the geometric shape of the result. Only present if type == Geography.")
        .setParam("geometry", "the geometry value to set")
        .setReturn("The DataSource object itself.");
    }

    // Customizes the ReverseSearchAddressBatchItemPrivateResponse class
    private void customizeReverseSearchAddressBatchItemPrivateResponse(PackageCustomization implementationModels) {
        ClassCustomization classCustomization = implementationModels.getClass("ReverseSearchAddressBatchItemPrivateResponse");

        // get error
        classCustomization.removeMethod("getError");
        classCustomization.addMethod(
            "public ResponseError getError() {\n" +
            "   if (this.error == null) {\n" +
            "       return new ResponseError(\"\", \"\");\n" +
            "   }\n" +
            "   return new ResponseError(this.error.getCode(), this.error.getMessage());\n" +
            "}")
        .getJavadoc()
        .setDescription("Get the error property: The error object.")
        .setReturn("the error value.");

        // set error
        classCustomization.removeMethod("setError");
        classCustomization.addMethod(
            "public ReverseSearchAddressBatchItemPrivateResponse setError(ResponseError error) {\n" +
            "   this.error = new ErrorDetail().setCode(error.getCode()).setMessage(error.getMessage());\n" +
            "   return this;\n" +
            "}")
        .getJavadoc()
        .setDescription("Set the error property: The error object.")
        .setParam("error", "the error value to set.")
        .setReturn("the ReverseSearchAddressBatchItemPrivateResponse object itself.");

        classCustomization.addImports("com.azure.core.models.ResponseError");
    }

    // Customizes the SearchAddressBatchItemPrivateResponse class
    private void customizeSearchAddressBatchItemPrivateResponse(PackageCustomization implementationModels) {
        ClassCustomization classCustomization = implementationModels.getClass("SearchAddressBatchItemPrivateResponse");

        // get error
        classCustomization.removeMethod("getError");
        classCustomization.addMethod(
            "public ResponseError getError() {\n" +
            "   if (this.error == null) {\n" +
            "       return new ResponseError(\"\", \"\");\n" +
            "   }\n" +
            "   return new ResponseError(this.error.getCode(), this.error.getMessage());\n" +
            "}")
        .getJavadoc()
        .setDescription("Get the error property: The error object.")
        .setReturn("the error value.");

        // set error
        classCustomization.removeMethod("setError");
        classCustomization.addMethod(
            "public SearchAddressBatchItemPrivateResponse setError(ResponseError error) {\n" +
            "   this.error = new ErrorDetail().setCode(error.getCode()).setMessage(error.getMessage());\n" +
            "   return this;\n" +
            "}")
        .getJavadoc()
        .setDescription("Set the error property: The error object.")
        .setParam("error", "the error value to set.")
        .setReturn("the ReverseSearchAddressBatchItemPrivateResponse object itself.");

        classCustomization.addImports("com.azure.core.models.ResponseError");
    }

    // customize error detail
    private void customizeErrorDetail(PackageCustomization implementationModels) {
        ClassCustomization classCustomization = implementationModels.getClass("ErrorDetail");

        // set code
        classCustomization.addMethod(
            "public ErrorDetail setCode(String code) {\n" +
            "   this.code = code;\n" +
            "   return this;\n" +
            "}")
        .getJavadoc()
        .setDescription("Set the code property: The code object.")
        .setParam("code", "the code value to set.")
        .setReturn("the ErrorDetail object itself.");


        // set message
        classCustomization.addMethod(
            "public ErrorDetail setMessage(String message) {\n" +
            "   this.message = message;\n" +
            "   return this;\n" +
            "}")
        .getJavadoc()
        .setDescription("Set the message property: The message object.")
        .setParam("message", "the message value to set.")
        .setReturn("the ErrorDetail object itself.");
    }
}