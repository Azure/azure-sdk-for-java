// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
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
        customizeGeoJsonObject(implementationModels);
        customizeGeoJsonGeometry(models);
        customizeGeoJsonFeature(models);

    }

    private void customizeGeoJsonObject(PackageCustomization models) {
        models.getClass("GeoJsonObject").customizeAst(ast -> {
            ast.addImport("com.azure.maps.search.models.Boundary");
        });
    }

    private void customizeGeoJsonGeometry(PackageCustomization models) {
        models.getClass("GeoJsonGeometry").customizeAst(ast -> {
            ast.getClassByName("GeoJsonGeometry").ifPresent(clazz -> clazz.getMethodsByName("fromJsonKnownDiscriminator").get(0)
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Reads an instance of GeoJsonGeometry from the JsonReader.")))
                .setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC));
        });
    }

    private void customizeGeoJsonFeature(PackageCustomization models) {
        models.getClass("GeoJsonFeature").customizeAst(ast -> {
            ast.getClassByName("GeoJsonFeature").ifPresent(clazz -> clazz.getMethodsByName("fromJsonKnownDiscriminator").get(0)
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Copy string literal text to the clipboard")))
                .setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC));
        });
    }

    private void customizeReverseGeocodingBatchRequestItem(PackageCustomization models) {
        models.getClass("ReverseGeocodingBatchRequestItem").customizeAst(ast -> {
                ast.addImport("com.azure.core.models.GeoPosition");

                ast.getClassByName("ReverseGeocodingBatchRequestItem").ifPresent(clazz -> clazz.getMethodsByName("getCoordinates").get(0)
                    .setType("GeoPosition")
                    .setBody(StaticJavaParser.parseBlock("{ return com.azure.maps.search.implementation.helpers.Utility.fromDoubleList(this.coordinates); }")));

                ast.getClassByName("ReverseGeocodingBatchRequestItem").ifPresent(clazz -> clazz.getMethodsByName("setCoordinates").get(0)
                    .setParameters(new NodeList<>(new Parameter().setName("coordinates").setType("GeoPosition")))
                    .setBody(StaticJavaParser.parseBlock(
                        "{ this.coordinates = new ArrayList<>(); this.coordinates.add(coordinates.getLongitude()); this.coordinates.add(coordinates.getLatitude()); return this; }")));
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
