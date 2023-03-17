// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.slf4j.Logger;

/**
 * Customization class for Administration client for Service Bus
 */
public class AdministrationClientCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {

        // Implementation models customizations
        PackageCustomization implementationModels = customization.getPackage("com.azure.messaging.servicebus.administration.implementation.models");

        for (ClassCustomization classCustomization : implementationModels.listClasses()) {
            System.out.println("Class: " + classCustomization.getClassName());
            // String className = classCustomization.getClassName();
            // String renamed = className + "Impl";
            //
            // classCustomization.rename(renamed);
        }
    }

    /*
     * Uses ClassCustomization.customizeAst to replace the 'localName' value of the JacksonXmlRootElement instead of
     * the previous implementation which removed the JacksonXmlRootElement then added it back with the updated
     * 'localName'. The previous implementation would occasionally run into an issue where the JacksonXmlRootElement
     * import wouldn't be added back, causing a failure in CI when validating that code generation was up-to-date.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void changeJacksonXmlRootElementName(ClassCustomization classCustomization, String rootElementName) {
        classCustomization.customizeAst(ast -> ast.getClassByName(classCustomization.getClassName()).get()
            .getAnnotationByName("JacksonXmlRootElement").get()
            .asNormalAnnotationExpr()
            .setPairs(new NodeList<>(new MemberValuePair("localName", new StringLiteralExpr(rootElementName)))));
    }
}
