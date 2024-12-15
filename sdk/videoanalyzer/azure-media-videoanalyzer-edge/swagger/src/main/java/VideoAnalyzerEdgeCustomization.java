// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Customization class for Video Analyzer Edge.
 */
public class VideoAnalyzerEdgeCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        customizeModelsPackage(libraryCustomization.getPackage("com.azure.media.videoanalyzer.edge.models"));
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        customizeMethodRequest(packageCustomization.getClass("MethodRequest"));
        customizePipelineSetRequest(packageCustomization.getClass("PipelineTopologySetRequest"));
        customizeLivePipelineSetRequest(packageCustomization.getClass("LivePipelineSetRequest"));
        customizeRemoteDeviceAdapterSetRequest(packageCustomization.getClass("RemoteDeviceAdapterSetRequest"));

        Map<String, ClassCustomization> classNameToCustomization = packageCustomization.listClasses()
            .stream()
            .collect(Collectors.toMap(ClassCustomization::getClassName, Function.identity()));

        // Remove "setApiVersion" from "MethodRequest" subtypes.
        for (ClassCustomization customization : classNameToCustomization.values()) {
            if ("MethodRequest".equals(customization.getClassName())) {
                continue; // Don't need to modify MethodRequest itself.
            }

            if (extendsMethodRequest(customization, classNameToCustomization)) {
                customization.customizeAst(ast -> ast.getClassByName(customization.getClassName())
                    .ifPresent(clazz -> clazz.getMethodsByName("setApiVersion").forEach(Node::remove)));
            }
        }
    }

    private void customizePipelineSetRequest(ClassCustomization classCustomization) {
        addGetPayloadAsJsonShared(classCustomization, "PipelineTopologySetRequestBody", "pipelineTopology");
    }

    private void customizeRemoteDeviceAdapterSetRequest(ClassCustomization classCustomization) {
        addGetPayloadAsJsonShared(classCustomization, "RemoteDeviceAdapterSetRequestBody", "remoteDeviceAdapter");
    }

    private void customizeLivePipelineSetRequest(ClassCustomization classCustomization) {
        addGetPayloadAsJsonShared(classCustomization, "LivePipelineSetRequestBody", "livePipeline");
    }

    private static void addGetPayloadAsJsonShared(ClassCustomization customization, String classToCreate,
        String fieldToUse) {
        customization.customizeAst(ast -> {
            ast.addImport("java.io.UncheckedIOException");

            ast.getClassByName(customization.getClassName())
                .ifPresent(clazz -> clazz.addMethod("getPayloadAsJson", Modifier.Keyword.PUBLIC)
                    .setType("String")
                    .setBody(StaticJavaParser.parseBlock(String.format("{return new %1$s(this.%2$s.getName())"
                            + ".setSystemData(this.%2$s.getSystemData()).setProperties(this.%2$s.getProperties())"
                            + ".getPayloadAsJson();}", classToCreate, fieldToUse)))
                    .setJavadocComment(createGetPayloadAsJsonJavadoc()));
        });
    }

    private void customizeMethodRequest(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport("com.azure.core.util.logging.ClientLogger");
            ast.addImport("java.io.UncheckedIOException");

            ast.getClassByName(classCustomization.getClassName())
                .ifPresent(clazz -> {
                    clazz.addFieldWithInitializer("ClientLogger", "LOGGER",
                        StaticJavaParser.parseExpression("new ClientLogger(" + classCustomization.getClassName() + ".class)"),
                        Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);

                    clazz.addMethod("getPayloadAsJson", Modifier.Keyword.PUBLIC)
                        .setType("String")
                        .setBody(StaticJavaParser.parseBlock("{try { return this.toJsonString(); } "
                            + "catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); }}"))
                        .setJavadocComment(createGetPayloadAsJsonJavadoc());

                    clazz.getMethodsByName("setApiVersion").forEach(MethodDeclaration::setModifiers);
                });
        });
    }

    private static Javadoc createGetPayloadAsJsonJavadoc() {
        return new Javadoc(JavadocDescription.parseText(
            "Get the payload as JSON: the serialized form of the request body"))
            .addBlockTag("throws", "UncheckedIOException", "UncheckedIOException")
            .addBlockTag("return", "the payload as JSON");
    }

    private static boolean extendsMethodRequest(ClassCustomization customization,
        Map<String, ClassCustomization> classNameToCustomization) {
        CompilationUnit ast = getAst(customization);
        for (ClassOrInterfaceType extended : ast.getClassByName(customization.getClassName()).get().getExtendedTypes()) {
            String extendedName = extended.asString();
            if ("MethodRequest".equals(extendedName)) {
                return true;
            }

            ClassCustomization extendedCustomization = classNameToCustomization.get(extendedName);
            if (extendedCustomization == null) {
                continue;
            }

            if (extendsMethodRequest(extendedCustomization, classNameToCustomization)) {
                return true;
            }
        }

        return false;
    }

    private static CompilationUnit getAst(ClassCustomization customization) {
        return StaticJavaParser.parse(customization.getEditor().getFileContent(customization.getFileName()));
    }
}
