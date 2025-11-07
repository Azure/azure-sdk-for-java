// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
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

        CustomizeToJsonDateSerialization(fluentModelsPackage.getClass("SBTopicProperties"));
        CustomizeToJsonDateSerialization(fluentModelsPackage.getClass("SBQueueProperties"));
        CustomizeToJsonDateSerialization(fluentModelsPackage.getClass("SBSubscriptionProperties"));
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
     * Customize the "toJson" method implementation, to replace `CoreUtils.durationToStringWithDays` with `DurationSerializer.serialize`.
     *
     * @param customization the customization for class
     */
    private static void CustomizeToJsonDateSerialization(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.addImport("com.azure.resourcemanager.servicebus.implementation.DurationSerializer");
            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
                clazz.getMethodsByName("toJson")
                    .forEach(method -> {
                        method.findAll(MethodCallExpr.class)
                            .forEach(mc -> {
                                boolean qualifiedMatch =
                                    mc.getNameAsString().equals("durationToStringWithDays")
                                        && mc.getScope().isPresent()
                                        && mc.getScope().get().isNameExpr()
                                        && mc.getScope().get().asNameExpr().getNameAsString().equals("CoreUtils");

                                if (qualifiedMatch) {
                                    MethodCallExpr replacement =
                                        new MethodCallExpr(new NameExpr("DurationSerializer"), "serialize");
                                    for (Expression arg : mc.getArguments()) {
                                        replacement.addArgument(arg.clone());
                                    }
                                    mc.replace(replacement);
                                }
                            });
                    });
            });
        });
    }
}