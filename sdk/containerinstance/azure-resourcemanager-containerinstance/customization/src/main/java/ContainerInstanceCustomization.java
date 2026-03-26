// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;

/**
 * Code customization after code generation.
 */
public class ContainerInstanceCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization fluentModelsPackage
            = customization.getPackage("com.azure.resourcemanager.containerinstance.fluent.models");
        // change base class from `ProxyResource` to `Resource`, to avoid breaking changes and compilation errors
        customizeResourceBaseClass(fluentModelsPackage.getClass("ContainerGroupInner"));

        // Replace ListResultContainerGroupInner with ContainerGroupInner in list operation types.
        // The list API returns ListResultContainerGroup which has the same JSON wire format as ContainerGroup.
        // Deserializing as ContainerGroupInner avoids a type mismatch with the hand-written SDK code that
        // expects InnerSupportsListing<ContainerGroupInner>.
        PackageCustomization implModelsPackage
            = customization.getPackage("com.azure.resourcemanager.containerinstance.implementation.models");
        replaceListResultWithContainerGroupInner(implModelsPackage.getClass("ContainerGroupListResult"));

        PackageCustomization fluentPackage
            = customization.getPackage("com.azure.resourcemanager.containerinstance.fluent");
        replaceListResultWithContainerGroupInner(fluentPackage.getClass("ContainerGroupsClient"));

        PackageCustomization implPackage
            = customization.getPackage("com.azure.resourcemanager.containerinstance.implementation");
        replaceListResultWithContainerGroupInner(implPackage.getClass("ContainerGroupsClientImpl"));
    }

    /**
     * Customize the base class to be "com.azure.core.management.Resource".
     *
     * @param customization the customization for class
     */
    private static void customizeResourceBaseClass(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
                String resourceClassName = "com.azure.core.management.Resource";
                ast.addImport(resourceClassName);
                clazz.getExtendedTypes().clear();
                clazz.addExtendedType(new ClassOrInterfaceType(null, "Resource"));
            });
        });
    }

    /**
     * Replace all references to ListResultContainerGroupInner with ContainerGroupInner.
     *
     * @param customization the customization for class
     */
    private static void replaceListResultWithContainerGroupInner(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            // Replace all type references
            ast.findAll(ClassOrInterfaceType.class).forEach(type -> {
                if (type.getNameAsString().equals("ListResultContainerGroupInner")) {
                    type.setName("ContainerGroupInner");
                }
            });

            // Replace in expressions (e.g., method call scopes like ListResultContainerGroupInner.fromJson())
            ast.findAll(NameExpr.class).forEach(expr -> {
                if (expr.getNameAsString().equals("ListResultContainerGroupInner")) {
                    expr.setName("ContainerGroupInner");
                }
            });

            // Fix imports
            ast.getImports().removeIf(i -> i.getNameAsString().endsWith("ListResultContainerGroupInner"));
            String importName = "com.azure.resourcemanager.containerinstance.fluent.models.ContainerGroupInner";
            if (ast.getImports().stream().noneMatch(i -> i.getNameAsString().equals(importName))) {
                ast.addImport(importName);
            }
        });
    }
}
