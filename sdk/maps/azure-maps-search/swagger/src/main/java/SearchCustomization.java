// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import java.util.Arrays;

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.MethodCustomization;
import org.slf4j.Logger;

/**
 * Customization class for Queue Storage.
 */
public class SearchCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.search.models");

        // customize Address
        customizeAddress(models);

        // customize AddressRanges
        customizeAddressRanges(models);

        // customize EntryPoint
        customizeEntryPoint(models);

        // customize SearchAddressResultItem
        customizeSearchAddressResultItem(models);

        // customize SearchSummary
        customizeSearchSummary(models);

        /*
        // customize route range
        customizeRouteRange(models);

        // customize route matrix
        customizeRouteMatrix(models);

        // customize route batch item
        customizeDirectionsBatchItem(models);
        */
    }

    // Customizes the Address class by changing the type of BoundingBox
    private void customizeAddress(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("Address");
        MethodCustomization methodCustomization = classCustomization.getMethod("getBoundingBox");
        methodCustomization.setReturnType("GeoBoundingBox",
            "com.azure.maps.search.implementation.helpers.Utility.toGeoBoundingBox(returnValue)");
        classCustomization.addImports("com.azure.core.models.GeoBoundingBox");
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

    // Customizes the RouteLeg class
    private void customizeRouteLeg(PackageCustomization models) {
        final String getPointsMethod =
            "/** " +
            "* Returns a list of {@link GeoPosition} coordinates." +
            "*" +
            "* return the coordinates" +
            "*/" +
            "public List<GeoPosition> getPoints() {" +
            "    return this.points" +
            "        .stream()" +
            "        .map(item -> new GeoPosition(item.getLongitude(), item.getLatitude()))" +
            "        .collect(Collectors.toList());" +
            "}";

        ClassCustomization classCustomization = models.getClass("RouteLeg");
        classCustomization.removeMethod("getPoints");
        classCustomization.addMethod(getPointsMethod, Arrays.asList("java.util.List",
            "java.util.stream.Collectors", "java.util.Arrays", "com.azure.core.models.GeoPosition"));
    }

    // Customizes the RouteInstruction class
    private void customizeRouteInstruction(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("RouteInstruction");
        MethodCustomization methodCustomization = classCustomization.getMethod("getPoint");
        methodCustomization.setReturnType("GeoPosition", "new GeoPosition(returnValue.getLongitude(), " +
            "returnValue.getLatitude())");
        classCustomization.removeMethod("setPoint");
    }

    // Customizes the RouteRange class
    private void customizeRouteRange(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("RouteRange");

        // replaces getBoundary()
        final String getBoundaryMethod =
            "/** " +
            "* Returns a list of {@link GeoPosition} coordinates." +
            "* " +
            "* return the coordinates" +
            "*/" +
            "public List<GeoPosition> getBoundary() {" +
            "    return this.boundary" +
            "        .stream()" +
            "        .map(item -> new GeoPosition(item.getLongitude(), item.getLatitude()))" +
            "        .collect(Collectors.toList());" +
            "}";

        classCustomization.removeMethod("getBoundary");
        classCustomization.addMethod(getBoundaryMethod, Arrays.asList("java.util.List",
            "java.util.stream.Collectors", "java.util.Arrays"));

        // changes the Center property to be GeoPosition
        MethodCustomization methodCustomization = classCustomization.getMethod("getCenter");
        methodCustomization.setReturnType("GeoPosition", "new GeoPosition(returnValue.getLongitude(), " +
            "returnValue.getLatitude())");
        classCustomization.removeMethod("setCenter");
    }

    // Customizes the RouteDirectionsBatchItem class
    private void customizeDirectionsBatchItem(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("RouteDirectionsBatchItem");

        // replaces getResponse() with getError
        final String getErrorMethod =
            "/** " +
            "* Returns the {@link ErrorDetail} in case of an error response." +
            "* " +
            "* return {@code ErrorDetail}" +
            "*/" +
            "public ErrorDetail getError() {" +
            "    return this.response.getError();" +
            "}";

        // classCustomization.removeMethod("getResponse");
        classCustomization.addMethod(getErrorMethod);

        // Adds getRouteDirections()
        final String getRouteDirectionsMethod =
            "/** " +
            "* Returns the {@link RouteDirections} associated with the response." +
            "* " +
            "* return {@code RouteDirections}" +
            "*/" +
            "public RouteDirections getRouteDirections() {" +
            "    return (RouteDirections)this.response;" +
            "}";
        classCustomization.addMethod(getRouteDirectionsMethod);

        // remove getResponse
        classCustomization.removeMethod("getResponse");
    }
}
