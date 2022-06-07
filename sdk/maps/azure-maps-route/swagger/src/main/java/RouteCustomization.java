// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import java.util.Arrays;

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.JavadocCustomization;
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
            "public RouteLegSummary getSummary() {" +
            "    return this.response.getSummary();" +
            "}";
        classCustomization.addMethod(getSummaryMethod);

        // javadoc
        final String getSummaryJavadocDescription = "Returns a {@link RouteLegSummary} summarizing this route section.";
        JavadocCustomization summaryDoc = classCustomization.getMethod("getSummary").getJavadoc();
        summaryDoc.setDescription(getSummaryJavadocDescription);
        summaryDoc.setReturn("a {@code RouteLegSummary} with the summary of this route leg.");
    }

    // Customizes the RouteLeg class
    private void customizeRouteLeg(PackageCustomization models) {
        final String getPointsMethod =
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

        // javadoc
        final String getPointsJavadocDescription = "Returns a list of {@link GeoPosition} coordinates.";
        JavadocCustomization pointsDoc = classCustomization.getMethod("getPoints").getJavadoc();
        pointsDoc.setDescription(getPointsJavadocDescription);
        pointsDoc.setReturn("a list of {@code GeoPosition} coordinates.");
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
            "public List<GeoPosition> getBoundary() {" +
            "    return this.boundary" +
            "        .stream()" +
            "        .map(item -> new GeoPosition(item.getLongitude(), item.getLatitude()))" +
            "        .collect(Collectors.toList());" +
            "}";

        classCustomization.removeMethod("getBoundary");
        classCustomization.addMethod(getBoundaryMethod, Arrays.asList("java.util.List",
            "java.util.stream.Collectors", "java.util.Arrays"));

        // get boundary javadoc
        final String getBoundaryJavadocDescription = "Returns a list of {@link GeoPosition} coordinates.";
        JavadocCustomization boundsDoc = classCustomization.getMethod("getBoundary").getJavadoc();
        boundsDoc.setDescription(getBoundaryJavadocDescription);
        boundsDoc.setReturn("a list of {@code GeoPosition} representing the boundary.");

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
            "public ErrorDetail getError() {" +
            "    return this.response.getError();" +
            "}";

        classCustomization.addMethod(getErrorMethod);

        // javadoc
        final String getErrorJavadocDescription = "Returns the {@link ErrorDetail} in case of an error response.";
        JavadocCustomization errorDoc = classCustomization.getMethod("getError").getJavadoc();
        errorDoc.setDescription(getErrorJavadocDescription);
        errorDoc.setReturn("the error detail as a {@code ErrorDetail}");

        // Adds getRouteDirections()
        final String getRouteDirectionsMethod =
            "public RouteDirections getRouteDirections() {" +
            "    return (RouteDirections)this.response;" +
            "}";
        classCustomization.addMethod(getRouteDirectionsMethod);

        // javadoc
        final String getRouteDirectionsJavadocDescription = "Returns the {@link RouteDirections} associated with the response.";
        JavadocCustomization directionsDoc = classCustomization.getMethod("getRouteDirections").getJavadoc();
        directionsDoc.setDescription(getRouteDirectionsJavadocDescription);
        directionsDoc.setReturn("the route directions as a {@code RouteDirections}");

        // remove getResponse
        classCustomization.removeMethod("getResponse");
    }
}
