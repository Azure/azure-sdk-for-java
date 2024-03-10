// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Customization class for Administration client for Service Bus
 */
public class AdministrationClientCustomization extends Customization {
    private static final String AUTHORIZATION_RULE_CLASS = "AuthorizationRule";
    private static final String NAMESPACE_KEY = "namespace";
    private static final String NAMESPACE_PROPERTIES_CLASS = "NamespaceProperties";
    private static final String IMPLEMENTATION_MODELS_PACKAGE = "com.azure.messaging.servicebus.administration.implementation.models";

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
        final PackageCustomization implementationModels = customization.getPackage(IMPLEMENTATION_MODELS_PACKAGE);

        ClassCustomization queueDescription = implementationModels.getClass("QueueDescription");

        queueDescription = addAuthorizationRulesConstructor(queueDescription, logger);

        for (ClassCustomization classCustomization : implementationModels.listClasses()) {
            final String className = classCustomization.getClassName();

            if (CLASSES_TO_SKIP.contains(className)) {
                logger.info("Skipping '{}'", className);
                continue;
            }

            if (AUTHORIZATION_RULE_CLASS.equals(className)) {
                classCustomization = addAuthorizationRuleXmlNamespace(classCustomization, logger);
            }

            classCustomization = addJacksonXmlRootElementImport(classCustomization, logger);
            classCustomization = addImplementationToClassName(classCustomization, logger);
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

    /**
     * When JacksonXmlRootElement annotation is added to the class, sometimes the import is not added. Consequently, it
     * breaks the build.
     */
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
     * {@code @JacksonXmlProperty(localName = "type", namespace = "http://www.w3.org/2001/XMLSchema-instance",
     * isAttribute = true)}
     */
    private ClassCustomization addAuthorizationRuleXmlNamespace(ClassCustomization classCustomization, Logger logger) {
        return classCustomization.customizeAst(ast -> {

            logger.info("{}: AddXmlNamespace - Getting class information.", AUTHORIZATION_RULE_CLASS);
            final ClassOrInterfaceDeclaration declaration = ast.getClassByName(AUTHORIZATION_RULE_CLASS)
                .orElse(null);

            if (Objects.isNull(declaration)) {
                logger.warn("{}: AddXmlNamespace - Could not find class.", AUTHORIZATION_RULE_CLASS);
                return;
            }

            final FieldDeclaration typeField = declaration.getFieldByName("type").orElse(null);
            if (Objects.isNull(typeField)) {
                logger.warn("{}: AddXmlNamespace - Could not find authorization 'type' field.",
                    AUTHORIZATION_RULE_CLASS);
                return;
            }

            final AnnotationExpr annotation = typeField.getAnnotationByName("JacksonXmlProperty")
                .orElse(null);
            if (Objects.isNull(annotation)) {
                logger.warn("{}: AddXmlNamespace - Could not get 'JacksonXmlProperty' annotation.",
                    AUTHORIZATION_RULE_CLASS);
                return;
            }

            final NormalAnnotationExpr annotationExpression = annotation.asNormalAnnotationExpr();
            final boolean hasNamespace = annotationExpression.getPairs().stream()
                .anyMatch(e -> NAMESPACE_KEY.equals(e.getName().asString()) && e.getValue().asStringLiteralExpr() != null);

            if (hasNamespace) {
                logger.info("{}: AddXmlNamespace - {} node already exists.", AUTHORIZATION_RULE_CLASS,
                    NAMESPACE_KEY);
                return;
            }

            annotationExpression.addPair(NAMESPACE_KEY, new StringLiteralExpr("http://www.w3.org/2001/XMLSchema-instance"));
        });
    }

    /**
     * Adds the no argument AuthorizationRulesWrapper constructor.
     */
    private ClassCustomization addAuthorizationRulesConstructor(ClassCustomization classCustomization, Logger logger) {
        final String authorizationWrapper = "AuthorizationRulesWrapper";

        logger.info("{}: Adding constructor to: {}", classCustomization.getClassName(), authorizationWrapper);

        return classCustomization.customizeAst(compilationUnit -> {
            final List<ClassOrInterfaceDeclaration> allClasses =
                compilationUnit.getLocalDeclarationFromClassname(authorizationWrapper);

            if (allClasses.isEmpty()) {
                logger.warn("{}: Unable to find any classes with '{}'.", classCustomization.getClassName(),
                    authorizationWrapper);
                return;
            } else if (allClasses.size() != 1) {
                final String matches = allClasses.stream().map(NodeWithSimpleName::getNameAsString)
                    .collect(Collectors.joining(", "));

                logger.warn("{}: Found more than one class ending with '{}'. [{}]", classCustomization.getClassName(),
                    authorizationWrapper, matches);
                return;
            }

            final NormalAnnotationExpr jsonCreatorAnnotation =
                new NormalAnnotationExpr(new Name("JsonCreator"), new NodeList<>());

            final ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration(authorizationWrapper);
            constructorDeclaration.addAnnotation(jsonCreatorAnnotation);
            constructorDeclaration.setModifiers(Modifier.Keyword.PRIVATE);
            constructorDeclaration.getBody().addStatement("this.items = java.util.Collections.emptyList();");

            final ClassOrInterfaceDeclaration declaration = allClasses.get(0);
            declaration.getMembers().add(constructorDeclaration);
        });
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
