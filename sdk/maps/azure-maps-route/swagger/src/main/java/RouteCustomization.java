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
public class RouteCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.route.models");

        // customize route leg
        customizeRouteLeg(models);

        // customize route instruction
        customizeRouteInstruction(models);

        // customize route range
        customizeRouteRange(models);

        // customize route matrix
        customizeRouteMatrix(models);

        // customize route batch item
        customizeDirectionsBatchItem(models);
    }

    // Customizes the RouteMatrix class by flattening the Response property.
    private void customizeRouteMatrix(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("RouteMatrix");
        classCustomization.removeMethod("getResponse");
        final String getSummaryMethod =
            "/** " +
            "* Returns a {@link RouteLegSummary} summarizing this route section." +
            "*" +
            "* return RouteLegSummary" +
            "*/" +
            "public RouteLegSummary getSummary() {" +
            "    return this.response.getSummary();" +
            "}";
        classCustomization.addMethod(getSummaryMethod);
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
            "java.util.stream.Collectors", "java.util.Arrays"));
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
