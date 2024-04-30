// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import org.slf4j.Logger;

import java.util.Arrays;

/**
 * This class contains the customization code to customize the AutoRest generated code for Event Grid.
 */
public class EventGridCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeEventGridClientImplImports(customization, logger);
    }


    public void customizeEventGridClientImplImports(LibraryCustomization customization, Logger logger) {

        Arrays.asList("com.azure.messaging.eventgrid.namespaces", "com.azure.messaging.eventgrid.namespaces.models").forEach(p -> {
            logger.info("Working on " + p);
            PackageCustomization packageCustomization = customization.getPackage(p);
            packageCustomization.listClasses().forEach(c -> {
                c.customizeAst(comp -> {
                    if (comp.getImports().removeIf(i -> i.getNameAsString().equals("com.azure.messaging.eventgrid.namespaces.models.CloudEvent"))) {
                        logger.info("Removed CloudEvent import from " + c.getClassName());
                        comp.addImport("com.azure.core.models.CloudEvent");
                    }
                });
            });
        });
        customization.getRawEditor().removeFile("src/main/java/com/azure/messaging/eventgrid/namespaces/models/PublishResult.java");
        customization.getRawEditor().removeFile("src/main/java/com/azure/messaging/eventgrid/namespaces/models/CloudEvent.java");

        // add preview ServiceVersion
        PackageCustomization packageCustomization = customization.getPackage("com.azure.messaging.eventgrid.namespaces");
        ClassCustomization classCustomization = packageCustomization.getClass("EventGridServiceVersion");
        classCustomization.customizeAst(compilationUnit -> {
            EnumDeclaration clazz = compilationUnit.getEnumByName("EventGridServiceVersion").get();
            clazz.getEntries().add(0, new EnumConstantDeclaration("/**\n" +
                " * Enum value 2023-10-01-preview.\n" +
                " */\n" +
                "V2023_10_01_PREVIEW(\"2023-10-01-preview\")"));
            clazz.getEntries().add(0, new EnumConstantDeclaration("/**\n" +
                " * Enum value 2023-06-01-preview.\n" +
                " */\n" +
                "V2023_06_01_PREVIEW(\"2023-06-01-preview\")"));
        });
    }
}
