// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
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
        customizeRouteDirectionsBatchItemResponse(models);

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
        models.getClass("RouteMatrix").customizeAst(ast -> ast.getClassByName("RouteMatrix").ifPresent(clazz -> {
            clazz.getMethodsByName("getResponse").get(0).remove();
            clazz.addMethod("getSummary", Modifier.Keyword.PUBLIC)
                .setType("RouteLegSummary")
                .setBody(StaticJavaParser.parseBlock("{ return this.response.getSummary(); }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Returns a {@link RouteLegSummary} summarizing this route section."))
                    .addBlockTag("return", "a {@code RouteLegSummary} with the summary of this route leg."));
        }));
    }

    // Customizes the RouteLeg class
    private void customizeRouteLeg(PackageCustomization models) {
        models.getClass("RouteLeg").customizeAst(ast -> {
            ast.addImport("java.util.List");
            ast.addImport("java.util.stream.Collectors");
            ast.addImport("com.azure.core.models.GeoPosition");

            ast.getClassByName("RouteLeg").ifPresent(clazz -> clazz.getMethodsByName("getPoints").get(0)
                .setType("List<GeoPosition>")
                .setBody(StaticJavaParser.parseBlock("{ return this.points.stream().map(item -> new GeoPosition(item.getLongitude(), item.getLatitude())).collect(Collectors.toList()); }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Returns a list of {@link GeoPosition} coordinates."))
                    .addBlockTag("return", "a list of {@code GeoPosition} coordinates.")));
        });
    }

    // Customizes the RouteInstruction class
    private void customizeRouteInstruction(PackageCustomization models) {
        models.getClass("RouteInstruction").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoPosition");

            ast.getClassByName("RouteInstruction").ifPresent(clazz -> {
                clazz.getMethodsByName("setPoint").get(0).remove();
                clazz.getMethodsByName("getPoint").get(0).setType("GeoPosition")
                    .setBody(StaticJavaParser.parseBlock("{ return new GeoPosition(this.point.getLongitude(), this.point.getLatitude()); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Returns the {@link GeoPosition} coordinates of this instruction."))
                        .addBlockTag("return", "a {@code GeoPosition} with the coordinates of this instruction."));
            });
        });
    }

    // Customizes the RouteRange class
    private void customizeRouteRange(PackageCustomization models) {
        models.getClass("RouteRange").customizeAst(ast -> {
            ast.addImport("java.util.List");
            ast.addImport("java.util.stream.Collectors");
            ast.addImport("com.azure.core.models.GeoPosition");

            ast.getClassByName("RouteRange").ifPresent(clazz -> {
                clazz.getMethodsByName("setCenter").forEach(Node::remove);

                clazz.getMethodsByName("getBoundary").get(0)
                    .setType("List<GeoPosition>")
                    .setBody(StaticJavaParser.parseBlock("{ return this.boundary.stream().map(item -> new GeoPosition(item.getLongitude(), item.getLatitude())).collect(Collectors.toList()); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Returns a list of {@link GeoPosition} coordinates."))
                        .addBlockTag("return", "a list of {@code GeoPosition} representing the boundary."));
                clazz.getMethodsByName("getCenter").get(0)
                    .setType("GeoPosition")
                    .setBody(StaticJavaParser.parseBlock("{ return new GeoPosition(this.center.getLongitude(), this.center.getLatitude()); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Returns the {@link GeoPosition} coordinates of the center of the range."))
                        .addBlockTag("return", "a {@code GeoPosition} with the coordinates of the center."));
            });
        });
    }

    // Customizes the RouteDirectionsBatchItem class
    private void customizeDirectionsBatchItem(PackageCustomization models) {
        models.getClass("RouteDirectionsBatchItem").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.ResponseError");

            ast.getClassByName("RouteDirectionsBatchItem").ifPresent(clazz -> {
                clazz.getMethodsByName("getResponse").get(0).remove();

                clazz.addMethod("getError", Modifier.Keyword.PUBLIC)
                    .setType("ResponseError")
                    .setBody(StaticJavaParser.parseBlock("{ return this.response.getError(); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Returns the {@link ResponseError} in case of an error response."))
                        .addBlockTag("return", "the error detail as a {@code ResponseError}"));

                clazz.addMethod("getRouteDirections", Modifier.Keyword.PUBLIC)
                    .setType("RouteDirections")
                    .setBody(StaticJavaParser.parseBlock("{ return (RouteDirections) this.response; }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Returns the {@link RouteDirections} associated with the response."))
                        .addBlockTag("return", "the route directions as a {@code RouteDirections}"));
            });
        });
    }

    // Customizes the ErrorDetail class
    private void customizeErrorDetail(PackageCustomization implModels) {
        implModels.getClass("ErrorDetail").customizeAst(ast -> ast.getClassByName("ErrorDetail").ifPresent(clazz -> {
            clazz.addMethod("setCode", Modifier.Keyword.PUBLIC)
                .setType("ErrorDetail")
                .addParameter("String", "code")
                .setBody(StaticJavaParser.parseBlock("{ this.code = code; return this; }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Set the code property: The error code."))
                    .addBlockTag("param", "code the code value")
                    .addBlockTag("return", "the ErrorDetail object itself."));

            clazz.addMethod("setMessage", Modifier.Keyword.PUBLIC)
                .setType("ErrorDetail")
                .addParameter("String", "message")
                .setBody(StaticJavaParser.parseBlock("{ this.message = message; return this; }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Set the message property: The error message."))
                    .addBlockTag("param", "message the message value")
                    .addBlockTag("return", "the ErrorDetail object itself."));
        }));
    }

    // Customizes the RouteDirectionsBatchItemResponse class
    private void customizeRouteDirectionsBatchItemResponse(PackageCustomization models) {
        models.getClass("RouteDirectionsBatchItemResponse").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.ResponseError");

            ast.getClassByName("RouteDirectionsBatchItemResponse").ifPresent(clazz -> {
                clazz.setModifiers(Modifier.Keyword.FINAL);

                clazz.getConstructors().get(0).setModifiers();
                clazz.addConstructor()
                    .addParameter("ErrorDetail", "error")
                    .setBody(StaticJavaParser.parseBlock("{ this.error = error; }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Constructor with error."))
                        .addBlockTag("param", "error the error object"));

                clazz.getMethodsByName("getError").get(0)
                    .setType("ResponseError")
                    .setBody(StaticJavaParser.parseBlock("{ return new ResponseError(this.error.getCode(), this.error.getMessage()); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the error property: The error object."))
                        .addBlockTag("return", "the error value."));

                clazz.getMethodsByName("setError").get(0)
                    .setType("RouteDirectionsBatchItemResponse")
                    .setParameters(new NodeList<>(new Parameter().setType("ResponseError").setName("error")))
                    .setBody(StaticJavaParser.parseBlock("{ this.error = new ErrorDetail().setCode(error.getCode()).setMessage(error.getMessage()); return this; }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Set the error property: The error object."))
                        .addBlockTag("param", "error the error value to set.")
                        .addBlockTag("return", "the RouteDirectionsBatchItemResponse object itself."));
            });
        });
    }

    // Customizes the ResponseSectionType class by changing the class name
    private void customizeResponseSectionType(PackageCustomization models) {
        models.getClass("ResponseSectionType").rename("RouteSectionType");
    }

    // Customizes the ResponseTravelMode class by changing the class name
    private void customizeResponseTravelMode(PackageCustomization models) {
        models.getClass("ResponseTravelMode").rename("RouteTravelMode");
    }

    // Customizes the Route class by changing the class name
    private void customizeRoute(PackageCustomization models) {
        models.getClass("Route").rename("MapsSearchRoute");
    }

    // Customizes the SimpleCategory class by changing the class name
    private void customizeSimpleCategory(PackageCustomization models) {
        models.getClass("SimpleCategory").rename("RouteDelayReason");
    }

    // Customizes the RouteReport class
    private void customizeRouteReport(PackageCustomization models) {
        models.getClass("RouteReport").customizeAst(ast -> {
            ast.addImport("java.util.Map");
            ast.addImport("java.util.HashMap");

            ast.getClassByName("RouteReport").ifPresent(clazz -> {
                clazz.getMethodsByName("getEffectiveSettings").get(0)
                    .setType("Map<String, String>")
                    .setBody(StaticJavaParser.parseBlock(String.join("\n",
                        "{",
                        "    Map<String, String> map = new HashMap<>();",
                        "    for (EffectiveSetting effectiveSetting : this.effectiveSettings) {",
                        "        map.put(effectiveSetting.getKey(), effectiveSetting.getValue());",
                        "    }",
                        "    return map;",
                        "}")));
            });
        });
    }

    // Customizes the RouteSummary class
    private void customizeRouteSummary(PackageCustomization models) {
        models.getClass("RouteSummary").customizeAst(ast -> {
            ast.addImport("java.time.Duration");

            ast.getClassByName("RouteSummary").ifPresent(clazz -> {
                clazz.getMethodsByName("getTravelTimeInSeconds").get(0)
                    .setType("Duration")
                    .setBody(StaticJavaParser.parseBlock("{ return Duration.ofSeconds(this.travelTimeInSeconds); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Get the travelTimeInSeconds property: Estimated travel time in seconds property that includes the delay due to\n"
                            + "real-time traffic. Note that even when traffic=false travelTimeInSeconds still includes the delay due to traffic.\n"
                            + "If DepartAt is in the future, travel time is calculated using time-dependent historic traffic data."))
                        .addBlockTag("return", "the travelTimeInSeconds value."));

                clazz.getMethodsByName("getTrafficDelayInSeconds").get(0)
                    .setType("Duration")
                    .setBody(StaticJavaParser.parseBlock("{ return Duration.ofSeconds(this.trafficDelayInSeconds); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Get the trafficDelayInSeconds property: Estimated delay in seconds caused by the real-time incident(s) according\n"
                            + "to traffic information. For routes planned with departure time in the future, delays is always 0. To return\n"
                            + "additional travel times using different types of traffic information, parameter computeTravelTimeFor=all needs to\n"
                            + "be added."))
                        .addBlockTag("return", "the trafficDelayInSeconds value."));
            });
        });
    }

    // Customizes the VehicleLoadType class
    private void customizeVehicleLoadType(PackageCustomization models) {
        models.getClass("VehicleLoadType").customizeAst(ast -> ast.getClassByName("VehicleLoadType").ifPresent(clazz -> {
            clazz.getFieldByName("USHAZMAT_CLASS1").ifPresent(field -> field.getVariable(0).setName("US_HAZMAT_CLASS1"));
            clazz.getFieldByName("USHAZMAT_CLASS2").ifPresent(field -> field.getVariable(0).setName("US_HAZMAT_CLASS2"));
            clazz.getFieldByName("USHAZMAT_CLASS3").ifPresent(field -> field.getVariable(0).setName("US_HAZMAT_CLASS3"));
            clazz.getFieldByName("USHAZMAT_CLASS4").ifPresent(field -> field.getVariable(0).setName("US_HAZMAT_CLASS4"));
            clazz.getFieldByName("USHAZMAT_CLASS5").ifPresent(field -> field.getVariable(0).setName("US_HAZMAT_CLASS5"));
            clazz.getFieldByName("USHAZMAT_CLASS6").ifPresent(field -> field.getVariable(0).setName("US_HAZMAT_CLASS6"));
            clazz.getFieldByName("USHAZMAT_CLASS7").ifPresent(field -> field.getVariable(0).setName("US_HAZMAT_CLASS7"));
            clazz.getFieldByName("USHAZMAT_CLASS8").ifPresent(field -> field.getVariable(0).setName("US_HAZMAT_CLASS8"));
            clazz.getFieldByName("USHAZMAT_CLASS9").ifPresent(field -> field.getVariable(0).setName("US_HAZMAT_CLASS9"));
        }));
    }
}
