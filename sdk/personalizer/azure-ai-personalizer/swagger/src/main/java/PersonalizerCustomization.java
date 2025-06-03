// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import org.slf4j.Logger;

/**
 * Contains customizations for Azure Personalizer swagger code generation.
 */
public class PersonalizerCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization adminModels = libraryCustomization.getPackage("com.azure.ai.personalizer.administration.models");
        PackageCustomization models = libraryCustomization.getPackage("com.azure.ai.personalizer.models");
        useBinaryDataForRankApis(models);
        hideMethods(adminModels, models);
        returnBaseClassTypesForMethodReturnValues(adminModels);
        hideClasses(adminModels, models);
    }

    private void useBinaryDataForRankApis(PackageCustomization models) {
        customizeToUseBinaryData(models, "PersonalizerRankableAction", "features");
        customizeToUseBinaryData(models, "PersonalizerSlotOptions", "features");

        customizeToUseBinaryData(models, "PersonalizerRankMultiSlotOptions", "contextFeatures");
        customizeToUseBinaryData(models, "PersonalizerRankOptions", "contextFeatures");
    }

    private static void customizeToUseBinaryData(PackageCustomization models, String className, String fieldName) {
        models.getClass(className).customizeAst(ast -> {
            ast.addImport("com.azure.core.util.BinaryData");
            ast.getClassByName(className).ifPresent(clazz -> {
                clazz.getFieldByName(fieldName).ifPresent(field -> field.getVariable(0).setType("List<BinaryData>"));
                clazz.getMethodsByName("get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1))
                    .forEach(method -> method.setType("List<BinaryData>"));
                clazz.getMethodsByName("set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1))
                    .forEach(method -> method.getParameter(0).setType("List<BinaryData>"));

                clazz.getMethodsByName("toJson").forEach(toJson -> toJson.getBody().ifPresent(body ->
                    toJson.setBody(StaticJavaParser.parseBlock(body.toString()
                    .replace("(writer, element) -> writer.writeUntyped(element)",
                        "(writer, element) -> element.writeTo(writer)")))));

                clazz.getMethodsByName("fromJson").forEach(fromJson -> {
                    fromJson.getBody().ifPresent(body -> {
                        String fromJsonToReplace = "List<Object> " + fieldName + " = reader.readArray(reader1 -> reader1.readUntyped());";
                        String fromJsonReplacement = "List<BinaryData> " + fieldName + " = reader.readArray(reader1 -> "
                            + "(reader1.currentToken() == JsonToken.NULL) ? null : BinaryData.fromObject(reader1.readUntyped()));";
                        fromJson.setBody(StaticJavaParser.parseBlock(body.toString().replace(fromJsonToReplace, fromJsonReplacement)));
                    });
                });
            });
        });
    }

    private void hideMethods(PackageCustomization adminModels, PackageCustomization models) {
        makeMethodPrivate(adminModels, "PersonalizerPolicyResultSummary", "setNonZeroProbability");
        makeMethodPrivate(adminModels, "PersonalizerEvaluation", "setEvaluationType", "getJobId", "setPolicyResults",
            "setFeatureImportance", "setOptimalPolicy", "setCreationTime");

        makeMethodPrivate(models, "PersonalizerSlotResult", "setId");
        makeMethodPrivate(models, "PersonalizerError", "setCode", "setMessage", "setTarget", "setDetails");
    }

    private void makeMethodPrivate(PackageCustomization customization, String className, String... methodNames) {
        customization.getClass(className).customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
            for (String methodName : methodNames) {
                clazz.getMethodsByName(methodName).forEach(NodeWithModifiers::setModifiers);
            }
        }));
    }

    private void returnBaseClassTypesForMethodReturnValues(PackageCustomization adminModels) {
        adminModels.getClass("PersonalizerPolicyResult")
            .customizeAst(ast -> ast.getClassByName("PersonalizerPolicyResult")
                .ifPresent(clazz -> clazz.getMethodsByName("getTotalSummary")
                    .forEach(method -> method.setType("PersonalizerPolicyResultSummary"))));

        adminModels.getClass("PersonalizerLogProperties")
            .customizeAst(ast -> ast.getClassByName("PersonalizerLogProperties")
                .ifPresent(clazz -> clazz.getMethodsByName("getDateRange")
                    .forEach(method -> method.setType("PersonalizerDateRange"))));
    }

    private void hideClasses(PackageCustomization adminModels, PackageCustomization models) {
        makeClassAndConstructorPackagePrivate(adminModels, "PersonalizerPolicyResultTotalSummary");
        makeClassAndConstructorPackagePrivate(adminModels, "PersonalizerLogPropertiesDateRange");

        makeClassAndConstructorPackagePrivate(models, "ServiceStatus");
    }

    private static void makeClassAndConstructorPackagePrivate(PackageCustomization customization, String className) {
        customization.getClass(className).customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
            clazz.setModifiers();
            clazz.getDefaultConstructor().ifPresent(ConstructorDeclaration::setModifiers);
        }));
    }
}
