// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Objects;

/**
 * Customization class for Administration client for Service Bus
 */
public class AdministrationClientCustomization extends Customization {
    private static final String NAMESPACE_PROPERTIES_CLASS = "NamespaceProperties";
    private static final String IMPLEMENTATION_MODELS_PACKAGE = "com.azure.messaging.servicebus.administration.implementation.models";

    private static final HashSet<String> CLASSES_TO_SKIP = new HashSet<>();

    static {
        // Don't modify the exception classes.
        CLASSES_TO_SKIP.add("ServiceBusManagementError");
        CLASSES_TO_SKIP.add("ServiceBusManagementErrorException");
    }

    /**
     * Customisations to the generated code.
     * <p>
     * 1. Rename implementation models to include Impl. (Avoids having to fully write out the namespace due to naming
     * conflicts.)
     */
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // Implementation models customizations
        final PackageCustomization implementationModels = customization.getPackage(IMPLEMENTATION_MODELS_PACKAGE);

        for (ClassCustomization classCustomization : implementationModels.listClasses()) {
            final String className = classCustomization.getClassName();

            if (CLASSES_TO_SKIP.contains(className)) {
                logger.info("Skipping '{}'", className);
                continue;
            }

            classCustomization = addImplementationToClassName(classCustomization, logger);
            replaceOffsetDateTimeParse(classCustomization);
        }

        // Change getCreatedTime modifier on NamespaceProperties.
        changeNamespaceModifier(customization, logger);
    }

    /**
     * Suffix classes with {@code Impl} to avoid naming collisions where we have to explicitly reference the package
     * name and class.
     */
    private ClassCustomization addImplementationToClassName(ClassCustomization classCustomization, Logger logger) {
        String current = classCustomization.getClassName();
        String renamed = current + "Impl";

        logger.info("Rename: '{}' -> '{}'", current, renamed);

        return classCustomization.rename(renamed);
    }

    private void replaceOffsetDateTimeParse(ClassCustomization classCustomization) {
        Editor editor = classCustomization.getEditor();
        String classFileName = classCustomization.getFileName();

        String originalContent = editor.getFileContent(classFileName);
        String updatedContent = originalContent.replaceAll("OffsetDateTime::parse",
            "EntityHelper::parseOffsetDateTimeBest");

        if (!Objects.equals(originalContent, updatedContent)) {
            editor.replaceFile(classFileName, updatedContent);
            classCustomization.addImports("com.azure.messaging.servicebus.administration.implementation.EntityHelper");
        }
    }

    private void changeNamespaceModifier(LibraryCustomization libraryCustomization, Logger logger) {
        final ClassCustomization classCustomization = libraryCustomization.getClass(
            "com.azure.messaging.servicebus.administration.models", NAMESPACE_PROPERTIES_CLASS);
        final String methodName = "setCreatedTime";

        logger.info("{}: Removing modifier on method '{}'", classCustomization.getClassName(), methodName);

        final MethodCustomization setCreatedTime = classCustomization.getMethod(methodName);
        setCreatedTime.setModifier(0);
    }
}
