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
public class SearchCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.search.models");
        PackageCustomization implementationModels = customization.getPackage("com.azure.maps.search.implementation.models");

        customizeErrorDetail(implementationModels);
        customizeReverseGeocodingBatchRequestItem(implementationModels);
        customizeBoundaryProperties(implementationModels);
        customizeGeoJsonObject(models);
        customizeBoundary(models);

    }

    private void customizeGeoJsonObject(PackageCustomization models) {
        models.getClass("GeoJsonObject").customizeAst(ast -> {
            ast.addImport("com.azure.maps.search.implementation.models.GeoJsonPoint");
            ast.addImport("com.azure.maps.search.implementation.models.GeoJsonMultiPoint");
            ast.addImport("com.azure.maps.search.implementation.models.GeoJsonLineString");
            ast.addImport("com.azure.maps.search.implementation.models.GeoJsonMultiLineString");
            ast.addImport("com.azure.maps.search.implementation.models.GeoJsonPolygon");
            ast.addImport("com.azure.maps.search.implementation.models.GeoJsonMultiPolygon");
            ast.addImport("com.azure.maps.search.implementation.models.GeoJsonGeometryCollection");
        });
    }

    private void customizeBoundary(PackageCustomization models) {
        models.getClass("Boundary").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoBoundingBox");

            ast.getClassByName("Boundary").ifPresent(clazz -> {
                clazz.getFieldByName("type").ifPresent(field -> field.getVariable(0)
                    .setInitializer(StaticJavaParser.parseExpression("GeoJsonObjectType.fromString(\"Boundary\")")));
                clazz.getMethodsByName("getCopyrightURL").forEach(method -> method.setName("getCopyrightUrl"));
                clazz.getMethodsByName("setCopyrightURL").forEach(method -> method.setName("setCopyrightUrl"));
                clazz.getMethodsByName("setBbox").forEach(method -> {
                    method.getAnnotationByName("Override").ifPresent(Node::remove);
                    method.setParameters(new NodeList<>(new Parameter().setName("bbox").setType("GeoBoundingBox")))
                        .setBody(StaticJavaParser.parseBlock(
                            "{ List<Double> bboxList = new ArrayList<>(); bboxList.add(bbox.getNorth());"
                                + "bboxList.add(bbox.getWest()); bboxList.add(bbox.getSouth()); bboxList.add(bbox.getEast());"
                                + "super.setBbox(bboxList); return this; }"))
                        .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                            "Sets the bounding box of this feature using a {@link GeoBoundingBox}."))
                            .addBlockTag("param", "bbox", "The bounding box to set.")
                            .addBlockTag("return", "The updated Boundary object."));
                });
            });
        });
    }

    private void customizeBoundaryProperties(PackageCustomization models) {
        models.getClass("BoundaryProperties").customizeAst(ast -> ast.getClassByName("BoundaryProperties").ifPresent(clazz -> {
            clazz.getMethodsByName("getCopyrightURL").forEach(method -> method.setName("getCopyrightUrl"));
            clazz.getMethodsByName("setCopyrightURL").forEach(method -> method.setName("setCopyrightUrl"));
        }));
    }

    private void customizeReverseGeocodingBatchRequestItem(PackageCustomization models) {
        models.getClass("ReverseGeocodingBatchRequestItem").customizeAst(ast -> {
                ast.addImport("com.azure.core.models.GeoPosition");

                ast.getClassByName("ReverseGeocodingBatchRequestItem").ifPresent(clazz -> {
                    clazz.getMethodsByName("getCoordinates").forEach(method -> method.setType("GeoPosition")
                        .setBody(StaticJavaParser.parseBlock("{ return com.azure.maps.search.implementation.helpers.Utility.fromDoubleList(this.coordinates); }")));
                    clazz.getMethodsByName("setCoordinates").forEach(method ->
                        method.setParameters(new NodeList<>(new Parameter().setName("coordinates").setType("GeoPosition")))
                        .setBody(StaticJavaParser.parseBlock(
                            "{ this.coordinates = new ArrayList<>(); this.coordinates.add(coordinates.getLongitude()); this.coordinates.add(coordinates.getLatitude()); return this; }")));
                });
        });
    }

    // customize error detail
    private void customizeErrorDetail(PackageCustomization implementationModels) {
        implementationModels.getClass("ErrorDetail").customizeAst(ast -> ast.getClassByName("ErrorDetail").ifPresent(clazz -> {
            clazz.addMethod("setCode", Modifier.Keyword.PUBLIC)
                .setType("ErrorDetail")
                .addParameter("String", "code")
                .setBody(StaticJavaParser.parseBlock("{ this.code = code; return this; }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Set the code property: The code object."))
                    .addBlockTag("param", "code", "the code value to set.")
                    .addBlockTag("return", "the ErrorDetail object itself."));

            clazz.addMethod("setMessage", Modifier.Keyword.PUBLIC)
                .setType("ErrorDetail")
                .addParameter("String", "message")
                .setBody(StaticJavaParser.parseBlock("{ this.message = message; return this; }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Set the message property: The message object."))
                    .addBlockTag("param", "message", "the message value to set.")
                    .addBlockTag("return", "the ErrorDetail object itself."));
        }));
    }
}
