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
        PackageCustomization implModels = customization.getPackage("com.azure.maps.route.implementation.models");

        // customize route report
        customizeRouteReport(models);

        // customize route leg
        customizeRouteLeg(models);

        // customize route instruction
        customizeRouteInstruction(models);

        // customize route range
        customizeRouteRange(models);

        // customize route matrix
        customizeRouteMatrix(models);

        // customize error detail
        customizeErrorDetail(implModels);

        // customize route batch item
        customizeDirectionsBatchItem(models);

        // customize route directions batch item response
        customizeRouteDirectionsBatchItemResponse(implModels);

        // customize response section type
        customizeResponseSectionType(models);

        // customize response travel mode
        customizeResponseTravelMode(models);

        // customize route
        customizeRoute(models);

        // customize simple category
        customizeSimpleCategory(models);

        // customize route summary
        customizeRouteSummary(models);

        // customize vehicle load type
        customizeVehicleLoadType(models);
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
            "public ResponseError getError() {" +
            "    return this.response.getError();" +
            "}";

        classCustomization.addMethod(getErrorMethod);

        // javadoc
        final String getErrorJavadocDescription = "Returns the {@link ResponseError} in case of an error response.";
        JavadocCustomization errorDoc = classCustomization.getMethod("getError").getJavadoc();
        errorDoc.setDescription(getErrorJavadocDescription);
        errorDoc.setReturn("the error detail as a {@code ResponseError}");

        // Adds getRouteDirections()
        final String getRouteDirectionsMethod =
            "public RouteDirections getRouteDirections() {" +
            "    return (RouteDirections)this.response;" +
            "}";
        classCustomization.addMethod(getRouteDirectionsMethod, Arrays.asList("com.azure.core.models.ResponseError"));

        // javadoc
        final String getRouteDirectionsJavadocDescription = "Returns the {@link RouteDirections} associated with the response.";
        JavadocCustomization directionsDoc = classCustomization.getMethod("getRouteDirections").getJavadoc();
        directionsDoc.setDescription(getRouteDirectionsJavadocDescription);
        directionsDoc.setReturn("the route directions as a {@code RouteDirections}");

        // remove getResponse
        classCustomization.removeMethod("getResponse");
    }

    // Customizes the ErrorDetail class
    private void customizeErrorDetail(PackageCustomization implModels) {
        ClassCustomization classCustomization = implModels.getClass("ErrorDetail");

        final String setCodeMethod =
            "public ErrorDetail setCode(String code) {" +
            "    this.code = code;" +
            "    return this;" +
            "}";
        
        classCustomization.addMethod(setCodeMethod);

        // javadoc
        final String setCodeJavadocDescription = "Set the code property: The error code.";
        JavadocCustomization setCodeJavadoc = classCustomization.getMethod("setCode").getJavadoc();
        setCodeJavadoc.setDescription(setCodeJavadocDescription);
        setCodeJavadoc.setParam("code", "The code value");

        final String setMessageMethod =
            "public ErrorDetail setMessage(String message) {" +
            "    this.message = message;" +
            "    return this;" +
            "}";
        
        classCustomization.addMethod(setMessageMethod);

        // javadoc
        final String setMessageJavadocDescription = "Set the message property: The error message.";
        JavadocCustomization setMessageJavadoc = classCustomization.getMethod("setMessage").getJavadoc();
        setMessageJavadoc.setDescription(setMessageJavadocDescription);
        setMessageJavadoc.setParam("message", "The message value");
    }

    // Customizes the RouteDirectionsBatchItemResponse class
    private void customizeRouteDirectionsBatchItemResponse(PackageCustomization implModels) {
        ClassCustomization classCustomization = implModels.getClass("RouteDirectionsBatchItemResponse");

        classCustomization.addConstructor(
            "public RouteDirectionsBatchItemResponse(ErrorDetail error) {\n" +
            "   this.error = error;\n" +
            "}")
            .getJavadoc()
            .setDescription("Constructor with error")
            .setParam("error", "The error object");
        
        classCustomization.addConstructor(
            "public RouteDirectionsBatchItemResponse() {" +
            "}")
            .getJavadoc()
            .setDescription("Empty constructor");
        
        // Change return type to ResponseError for getError
        final String getErrorMethod = 
            "public ResponseError getError() {" +
            "   return new ResponseError(this.error.getCode(), this.error.getMessage());" +
            "}";
        classCustomization.removeMethod("getError");
        classCustomization.addMethod(getErrorMethod, Arrays.asList("com.azure.core.models.ResponseError"));

        // javadoc customization to pass Checkstyle
        final String getErrorJavadocDescription = "Get the error property: The error object.";
        JavadocCustomization getErrorJavadoc = classCustomization.getMethod("getError").getJavadoc();
        getErrorJavadoc.setDescription(getErrorJavadocDescription);
        getErrorJavadoc.setReturn("the error value.");

        // setError with ResponseError
        final String setErrorMethod = 
            "public RouteDirectionsBatchItemResponse setError(ResponseError error) {" +
            "    this.error = new ErrorDetail().setCode(error.getCode()).setMessage(error.getMessage());" +
            "    return this;" +
            "}";
        classCustomization.removeMethod("setError");
        classCustomization.addMethod(setErrorMethod);

        // javadoc customization to pass Checkstyle
        final String setErrorJavadocDescription = "Set the error property: The error object.";
        JavadocCustomization setErrorJavadoc = classCustomization.getMethod("getError").getJavadoc();
        setErrorJavadoc.setDescription(setErrorJavadocDescription);
        setErrorJavadoc.setParam("error", "error the error value to set.");
        setErrorJavadoc.setReturn("the RouteDirectionsBatchItemResponse object itself.");
    }

    // Customizes the ResponseSectionType class by changing the class name
    private void customizeResponseSectionType(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ResponseSectionType");
        classCustomization.rename("RouteSectionType");
    }

    // Customizes the ResponseTravelMode class by changing the class name
    private void customizeResponseTravelMode(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ResponseTravelMode");
        classCustomization.rename("RouteTravelMode");
    }

    // Customizes the Route class by changing the class name
    private void customizeRoute(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("Route");
        classCustomization.rename("MapsSearchRoute");
    }

    // Customizes the SimpleCategory class by changing the class name
    private void customizeSimpleCategory(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("SimpleCategory");
        classCustomization.rename("RouteDelayReason");
    }

    // Customizes the RouteReport class
    private void customizeRouteReport(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("RouteReport");

        classCustomization.addImports("java.util.Map;", "java.util.HashMap;");

        // Change return type to Map<String, String> for getEffectiveSettings
        final String getEffectiveSettingsMethod = 
            "public Map<String, String> getEffectiveSettings() {" +
            "   Map<String, String> map = new HashMap<>();" +
            "   for (EffectiveSetting effectiveSetting : this.effectiveSettings) {" +
            "       map.put(effectiveSetting.getKey(), effectiveSetting.getValue());" +
            "   }" +
            "   return map;" +
            "}";
        classCustomization.addMethod(getEffectiveSettingsMethod);

        classCustomization.removeMethod("getEffectiveSettings");

        // javadoc customization to pass Checkstyle
        final String getEffectiveSettingsMethodJavadocDescription = "Get the effectiveSettings property: Effective parameters or data used when calling this Route API.";
        JavadocCustomization getEffectiveSettingsJavadoc = classCustomization.getMethod("getEffectiveSettings").getJavadoc();
        getEffectiveSettingsJavadoc.setDescription(getEffectiveSettingsMethodJavadocDescription);
        getEffectiveSettingsJavadoc.setReturn("the effectiveSettings value.");
    }

    // Customizes the RouteSummary class
    private void customizeRouteSummary(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("RouteSummary");

        // Change return type to Duration for getTravelTimeInSeconds
        final String getTravelTimeInSecondsMethod = 
            "public Duration getTravelTimeInSeconds() {\n" +
            "   return Duration.ofSeconds(this.travelTimeInSeconds);\n" +
            "}\n";
        classCustomization.removeMethod("getTravelTimeInSeconds");
        classCustomization.addMethod(getTravelTimeInSecondsMethod, Arrays.asList("java.time.Duration"));
        // javadoc customization to pass Checkstyle
        final String getTravelTimeInSecondsMethodJavadocDescription = "Get the travelTimeInSeconds property: Estimated travel time in seconds property that includes the delay due to\n" + 
            "real-time traffic. Note that even when traffic=false travelTimeInSeconds still includes the delay due to traffic.\n" +
            "If DepartAt is in the future, travel time is calculated using time-dependent historic traffic data.\n";
        JavadocCustomization getTravelTimeInSecondsMethodJavadoc = classCustomization.getMethod("getTravelTimeInSeconds").getJavadoc();
        getTravelTimeInSecondsMethodJavadoc.setDescription(getTravelTimeInSecondsMethodJavadocDescription);
        getTravelTimeInSecondsMethodJavadoc.setReturn("the travelTimeInSeconds value.");

        // Change return type to Duration for getTrafficDelayInSeconds
        final String getTrafficDelayInSecondsMethod = 
            "public Duration getTrafficDelayInSeconds() {\n" +
            "   return Duration.ofSeconds(this.trafficDelayInSeconds);\n" +
            "}\n";
        classCustomization.removeMethod("getTrafficDelayInSeconds");
        classCustomization.addMethod(getTrafficDelayInSecondsMethod);
        // javadoc customization to pass Checkstyle
        final String getTrafficDelayInSecondsMethodJavadocDescription = "Get the trafficDelayInSeconds property: Estimated delay in seconds caused by the real-time incident(s) according\n" + 
            "to traffic information. For routes planned with departure time in the future, delays is always 0. To return\n" +
            "additional travel times using different types of traffic information, parameter computeTravelTimeFor=all needs to\n" +
            "be added.\n";
        JavadocCustomization getTrafficDelayInSecondsMethodJavadoc = classCustomization.getMethod("getTrafficDelayInSeconds").getJavadoc();
        getTrafficDelayInSecondsMethodJavadoc.setDescription(getTrafficDelayInSecondsMethodJavadocDescription);
        getTrafficDelayInSecondsMethodJavadoc.setReturn("the trafficDelayInSeconds value.");
    }

    // Customizes the VehicleLoadType class
    private void customizeVehicleLoadType(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("VehicleLoadType");
        classCustomization.getConstant("USHAZMAT_CLASS1").rename("US_HAZMAT_CLASS1");
        classCustomization.getConstant("USHAZMAT_CLASS2").rename("US_HAZMAT_CLASS2");
        classCustomization.getConstant("USHAZMAT_CLASS3").rename("US_HAZMAT_CLASS3");
        classCustomization.getConstant("USHAZMAT_CLASS4").rename("US_HAZMAT_CLASS4");
        classCustomization.getConstant("USHAZMAT_CLASS5").rename("US_HAZMAT_CLASS5");
        classCustomization.getConstant("USHAZMAT_CLASS6").rename("US_HAZMAT_CLASS6");
        classCustomization.getConstant("USHAZMAT_CLASS7").rename("US_HAZMAT_CLASS7");
        classCustomization.getConstant("USHAZMAT_CLASS8").rename("US_HAZMAT_CLASS8");
        classCustomization.getConstant("USHAZMAT_CLASS9").rename("US_HAZMAT_CLASS9");
    }
}