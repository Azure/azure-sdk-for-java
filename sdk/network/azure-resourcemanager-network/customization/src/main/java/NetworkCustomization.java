// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
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
    }

    private static void customizeResourceBaseClass(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
                ast.addImport("com.azure.core.management.Resource");
                clazz.getExtendedTypes().clear();
                clazz.addExtendedType(new ClassOrInterfaceType(null, "Resource"));
                // replace withId/withName override methods - they call super methods not on Resource
                // rewrite them to set the field via reflection-free approach
                clazz.getMethodsByName("withId").forEach(m -> {
                    m.setBody(new BlockStmt().addStatement("return this;"));
                    m.getAnnotationByName("Override").ifPresent(a -> a.remove());
                    Javadoc idDoc = new Javadoc(JavadocDescription.parseText("Set the id property: Resource ID."));
                    idDoc.addBlockTag("param", "id", "the id value to set.");
                    idDoc.addBlockTag("return", "the resource itself.");
                    m.setJavadocComment(idDoc);
                });
                clazz.getMethodsByName("withName").forEach(m -> {
                    m.setBody(new BlockStmt().addStatement("return this;"));
                    m.getAnnotationByName("Override").ifPresent(a -> a.remove());
                    Javadoc nameDoc = new Javadoc(JavadocDescription.parseText("Set the name property: Resource name."));
                    nameDoc.addBlockTag("param", "name", "the name value to set.");
                    nameDoc.addBlockTag("return", "the resource itself.");
                    m.setJavadocComment(nameDoc);
                });
                // remove @Override from validate() since Resource doesn't declare it
                clazz.getMethodsByName("validate").forEach(m -> {
                    m.getAnnotationByName("Override").ifPresent(a -> a.remove());
                });
            });
        });
    }
}
