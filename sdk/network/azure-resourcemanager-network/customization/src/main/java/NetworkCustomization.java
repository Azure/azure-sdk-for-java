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
        // change base class to "Resource" for DdosProtectionPlan and RouteFilter
        customizeResourceBaseClass(fluentModelsPackage.getClass("DdosProtectionPlanInner"));
        customizeResourceBaseClass(fluentModelsPackage.getClass("RouteFilterInner"));

        // make SubResourceModel extend SubResource for backward compatibility
        PackageCustomization modelsPackage
            = customization.getPackage("com.azure.resourcemanager.network.models");
        customizeSubResourceModelBaseClass(modelsPackage.getClass("SubResourceModel"));
    }

    private static void customizeResourceBaseClass(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
                ast.addImport("com.azure.core.management.Resource");
                clazz.getExtendedTypes().clear();
                clazz.addExtendedType(new ClassOrInterfaceType(null, "Resource"));
                // remove withId/withName methods that call super methods not on Resource
                clazz.getMethodsByName("withId").forEach(m -> m.remove());
                clazz.getMethodsByName("withName").forEach(m -> m.remove());
            });
        });
    }

    private static void customizeSubResourceModelBaseClass(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
                ast.addImport("com.azure.core.management.SubResource");
                clazz.addExtendedType(new ClassOrInterfaceType(null, "SubResource"));
            });
        });
    }
}
