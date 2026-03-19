// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;

/**
 * Code customization after code generation for Network.
 */
public class NetworkCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization fluentModelsPackage
            = customization.getPackage("com.azure.resourcemanager.network.fluent.models");
        // change base class to "Resource", to avoid breaking changes and compilation errors
        customizeResourceBaseClass(fluentModelsPackage.getClass("DdosProtectionPlanInner"));
        customizeResourceBaseClass(fluentModelsPackage.getClass("RouteFilterInner"));

        // change SubnetInner base class to SubResource for backward compatibility
        customizeSubResourceBaseClass(fluentModelsPackage.getClass("SubnetInner"));
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
                // remove withId method that references super.withId() which doesn't exist on Resource
                clazz.getMethodsByName("withId").forEach(method -> method.remove());
            });
        });
    }

    /**
     * Customize the base class to be "com.azure.core.management.SubResource".
     *
     * @param customization the customization for class
     */
    private static void customizeSubResourceBaseClass(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
                String subResourceClassName = "com.azure.core.management.SubResource";
                ast.addImport(subResourceClassName);
                clazz.getExtendedTypes().clear();
                clazz.addExtendedType(new ClassOrInterfaceType(null, "SubResource"));
            });
        });
    }
}
