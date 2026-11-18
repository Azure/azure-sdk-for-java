// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;

/**
 * Code customization after code generation.
 */
public class PolicyCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizePolicyClient(
            customization.getPackage("com.azure.resourcemanager.resources.fluent").getClass("PolicyClient"));
        customizePolicyClientImpl(
            customization.getPackage("com.azure.resourcemanager.resources.implementation")
                .getClass("PolicyClientImpl"));
    }

    private static void customizePolicyClient(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getInterfaceByName(customization.getClassName()).ifPresent(clazz -> {
            if (clazz.getMethodsByName("getPolicyExemptions").isEmpty()) {
                MethodDeclaration method = clazz.addMethod("getPolicyExemptions");
                method.setType("PolicyExemptionsClient");
                method.removeBody();
                method.setJavadocComment("Gets the PolicyExemptionsClient object to access its operations.\n\n"
                    + "@return the PolicyExemptionsClient object.");
            }
        }));
    }

    private static void customizePolicyClientImpl(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            if (clazz.getMethodsByName("getPolicyExemptions").isEmpty()) {
                ast.addImport("com.azure.resourcemanager.resources.fluent.PolicyExemptionsClient");
                clazz.addField("PolicyExemptionsClient", "policyExemptions", Modifier.Keyword.PRIVATE,
                    Modifier.Keyword.FINAL);

                MethodDeclaration method = clazz.addMethod("getPolicyExemptions", Modifier.Keyword.PUBLIC);
                method.addMarkerAnnotation("Override");
                method.setType("PolicyExemptionsClient");
                method.setBody(StaticJavaParser.parseBlock("{ return this.policyExemptions; }"));
                method.setJavadocComment("Gets the PolicyExemptionsClient object to access its operations.\n\n"
                    + "@return the PolicyExemptionsClient object.");

                clazz.getConstructors()
                    .forEach(constructor -> constructor.getBody()
                        .addStatement("this.policyExemptions = new PolicyExemptionsClientImpl(this);"));
            }
        }));
    }
}
