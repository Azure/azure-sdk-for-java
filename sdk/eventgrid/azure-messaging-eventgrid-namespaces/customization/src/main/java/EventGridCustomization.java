// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.Node;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class contains the customization code to customize the AutoRest generated code for Event Grid.
 */
public class EventGridCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        removeExtraFiles(customization, logger);
        removeSendEvents(customization);
        customizeEventGridClientImplImports(customization, logger);
    }

    private void removeSendEvents(LibraryCustomization customization) {
        PackageCustomization packageCustomization = customization.getPackage("com.azure.messaging.eventgrid.namespaces");
        packageCustomization.getClass("EventGridSenderClient").customizeAst(ast ->
            ast.getClassByName("EventGridSenderClient").ifPresent(clazz ->
                clazz.getMethodsByName("sendEvents").forEach(Node::remove)));

        packageCustomization.getClass("EventGridSenderAsyncClient").customizeAst(ast ->
            ast.getClassByName("EventGridSenderAsyncClient").ifPresent(clazz ->
                clazz.getMethodsByName("sendEvents").forEach(Node::remove)));
    }

    public void removeExtraFiles(LibraryCustomization customization, Logger logger) {
        logger.info("removing PublishResult.java");
        customization.getRawEditor().removeFile("src/main/java/com/azure/messaging/eventgrid/namespaces/implementation/models/PublishResult.java");
        logger.info("removing CloudEvent.java");
        customization.getRawEditor().removeFile("src/main/java/com/azure/messaging/eventgrid/namespaces/models/CloudEvent.java");
    }

    public void customizeEventGridClientImplImports(LibraryCustomization customization, Logger logger) {
        for (String packageName : Arrays.asList("com.azure.messaging.eventgrid.namespaces",
            "com.azure.messaging.eventgrid.namespaces.models",
            "com.azure.messaging.eventgrid.namespaces.implementation.models")) {
            logger.info("Working on {}", packageName);
            PackageCustomization packageCustomization = customization.getPackage(packageName);

            // Manual listing of classes in the package until a bug is fixed in TypeSpec Java.
            String packagePath = "src/main/java/" + packageName.replace(".", "/") + "/";
            List<String> classNames = customization.getRawEditor().getContents().keySet().stream()
                .filter(fileName -> fileName.startsWith(packagePath))
                .map(fileName -> fileName.substring(packagePath.length(), fileName.length() - 5))
                .filter(className -> !className.contains("/") && !"package-info".equals(className))
                .collect(Collectors.toList());

            for (String className : classNames) {
                packageCustomization.getClass(className).customizeAst(comp -> {
                    if (comp.getImports().removeIf(i -> i.getNameAsString().equals("com.azure.messaging.eventgrid.namespaces.models.CloudEvent"))) {
                        logger.info("Removed CloudEvent import from {}", className);
                        comp.addImport("com.azure.core.models.CloudEvent");
                    }
                    if (comp.getImports().removeIf(i -> i.getNameAsString().equals("com.azure.messaging.eventgrid.namespaces.implementation.models.PublishResult"))) {
                        logger.info("Removed PublishResult import from {}", className);
                    }
                });
            }
        }
    }
}
