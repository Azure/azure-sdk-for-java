// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Objects;

/**
 * Customization class for Administration client for Service Bus
 */
public class AdministrationClientCustomization extends Customization {
    private static final String AUTHORIZATION_RULE_CLASS_NAME = "AuthorizationRule";
    private static final String NAMESPACE_KEY = "namespace";

    private static final HashSet<String> CLASSES_TO_SKIP = new HashSet<>();

    static {
        // This uses an Open API 3.0 directive that autorest for java does not understand.
        // Consequently, it throws and error if we try to rename it.
        CLASSES_TO_SKIP.add("EmptyRuleAction");
        // This is a publicly exposed type, we don't want to change the name.
        CLASSES_TO_SKIP.add("NamespaceType");

        // Don't modify the exception classes.
        CLASSES_TO_SKIP.add("ServiceBusManagementError");
        CLASSES_TO_SKIP.add("ServiceBusManagementErrorException");
    }

    /**
     * Customisations to the generated code.
     *
     * 1. Rename implementation models to include Impl. (Avoids having to fully write out the namespace due to naming
     * conflicts.) 2. Adds import for JacksonXmlRootElement
     */
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {

        // Implementation models customizations
        final PackageCustomization implementationModels = customization.getPackage(
            "com.azure.messaging.servicebus.administration.implementation.models");

        for (ClassCustomization classCustomization : implementationModels.listClasses()) {
            final String className = classCustomization.getClassName();

            if (CLASSES_TO_SKIP.contains(className)) {
                logger.info("Skipping '{}'", className);
                continue;
            }

            if (AUTHORIZATION_RULE_CLASS_NAME.equals(className)) {
                classCustomization = addAuthorizationRuleXmlNamepsace(classCustomization, logger);
            }

            classCustomization = addJacksonXmlRootElementImport(classCustomization, logger);
            classCustomization = addImplementationToClassName(classCustomization, logger);
        }
    }

    private ClassCustomization addImplementationToClassName(ClassCustomization classCustomization, Logger logger) {
        String current = classCustomization.getClassName();
        String renamed = current + "Impl";

        logger.info("Rename: '{}' -> '{}'", current, renamed);

        return classCustomization.rename(renamed);
    }

    private ClassCustomization addJacksonXmlRootElementImport(ClassCustomization classCustomization, Logger logger) {
        final String className = classCustomization.getClassName();

        return classCustomization.customizeAst(ast -> {
            logger.debug("{}: Add Import - Getting declaration.", className);
            final ClassOrInterfaceDeclaration declaration = ast.getClassByName(className)
                .orElse(null);

            if (Objects.isNull(declaration)) {
                logger.warn("{}: Add Import - Could not find classByName.", className);
                return;
            }

            logger.info("{}: Add Import - Checking for JacksonXmlRootElement attribute.", className);
            final AnnotationExpr jackson = declaration.getAnnotationByName("JacksonXmlRootElement")
                .orElse(null);

            // Doesn't have the XML attribute.
            if (jackson == null) {
                return;
            }

            logger.debug("{}: Add Import - Looking for correct import.", className);
            final boolean hasImport = ast.getImports().stream().anyMatch(
                importDeclaration -> importDeclaration.getName().getIdentifier().equals("JacksonXmlRootElement"));

            if (hasImport) {
                return;
            }

            logger.info("{}: Add Import - Adding JacksonXmlRootElement import.", className);
            ast.addImport("com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement");
        });
    }


    /**
     * Modifying AuthorizationRule class to include the namespace for authorization type. The XML namespace is not added
     * to the generated code. We want it to look like this:
     *
     * {@code @JacksonXmlProperty(localName = "type", namespace = "http://www.w3.org/2001/XMLSchema-instance", isAttribute = true)}
     */
    private ClassCustomization addAuthorizationRuleXmlNamepsace(ClassCustomization classCustomization, Logger logger) {
        return classCustomization.customizeAst(ast -> {

            logger.info("{}: AddXmlNamespace - Getting class information.", AUTHORIZATION_RULE_CLASS_NAME);
            final ClassOrInterfaceDeclaration declaration = ast.getClassByName(AUTHORIZATION_RULE_CLASS_NAME)
                .orElse(null);

            if (Objects.isNull(declaration)) {
                logger.warn("{}: AddXmlNamespace - Could not find class.", AUTHORIZATION_RULE_CLASS_NAME);
                return;
            }

            final FieldDeclaration typeField = declaration.getFieldByName("type").orElse(null);
            if (Objects.isNull(typeField)) {
                logger.warn("{}: AddXmlNamespace - Could not find authorization 'type' field.",
                    AUTHORIZATION_RULE_CLASS_NAME);
                return;
            }

            final AnnotationExpr annotation = typeField.getAnnotationByName("JacksonXmlProperty")
                .orElse(null);
            if (Objects.isNull(annotation)) {
                logger.warn("{}: AddXmlNamespace - Could not get 'JacksonXmlProperty' annotation.",
                    AUTHORIZATION_RULE_CLASS_NAME);
                return;
            }

            final NormalAnnotationExpr annotationExpression = annotation.asNormalAnnotationExpr();
            final boolean hasNamespace = annotationExpression.getPairs().stream()
                .anyMatch(e -> NAMESPACE_KEY.equals(e.getName().asString()) && e.getValue().asStringLiteralExpr() != null);

            if (hasNamespace) {
                logger.info("{}: AddXmlNamespace - {} node already exists.", AUTHORIZATION_RULE_CLASS_NAME,
                    NAMESPACE_KEY);
                return;
            }

            annotationExpression.addPair(NAMESPACE_KEY, new StringLiteralExpr("http://www.w3.org/2001/XMLSchema-instance"));
        });
    }
}
