// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;

/**
 * Code customization after code generation.
 */
public class ServiceBusCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization fluentModelsPackage = customization.getPackage("com.azure.resourcemanager.servicebus.fluent.models");
        // change base class from `ProxyResource` to `Resource`, to avoid breaking changes and compilation errors
        customizeResourceBaseClass(fluentModelsPackage.getClass("SBTopicInner"));
        customizeResourceBaseClass(fluentModelsPackage.getClass("SBSubscriptionInner"));
        customizeResourceBaseClass(fluentModelsPackage.getClass("SBQueueInner"));
        customizeResourceBaseClass(fluentModelsPackage.getClass("SBAuthorizationRuleInner"));
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
}