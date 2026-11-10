package com.azure.tools.apiview.processor.diagnostics.rules;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getClasses;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedMethods;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.WARNING;

/**
 * Not all builders require all methods, but we should warn regardless so it can be considered.
 */
public class RequiredBuilderMethodsDiagnosticRule implements DiagnosticRule {
    private static final String BUILDER_ANNOTATION = "ServiceClientBuilder";

    // maps from the expected method name to the type of the arguments passed into that method.
    // Normally this would be a single argument, but we allow here for multiple arguments, just in case.
    private final Map<String, Function<MethodDeclaration, Optional<Diagnostic>>> builderMethods;

    private final String builderProtocol;

    private final List<String> missingMethods = new ArrayList<>();

    public RequiredBuilderMethodsDiagnosticRule(String builderProtocol) {
        this.builderMethods = new HashMap<>();
        this.builderProtocol = builderProtocol;
    }

    public RequiredBuilderMethodsDiagnosticRule addMethod(String methodName, Function<MethodDeclaration, Optional<Diagnostic>> func) {
        builderMethods.put(methodName, func);
        missingMethods.add(methodName);
        return this;
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        // check if the class has the @ServiceClientBuilder annotation, if not, do nothing
        getClasses(cu).forEach(typeDeclaration -> {
            if (typeDeclaration.isAnnotationPresent(BUILDER_ANNOTATION)) {
                // if it does, we try to read from it the 'protocol' so that we can determine if this
                // builder is an HTTP or AMQP builder, and then we can apply the appropriate rules.
                // If the protocol specified in the builder is not present, we will use the previous logic
                // of this diagnostic rule and just assume we are looking at an http protocol.
                // This is unless the builderProtocol is null, in which case the set of rules we have
                // will all be applied.
                final String protocolName = typeDeclaration.getAnnotationByName(BUILDER_ANNOTATION).get()
                    .getChildNodes()
                    .stream()
                    .filter(n -> n instanceof MemberValuePair)
                    .map(n -> (MemberValuePair) n)
                    .filter(p -> "protocol".equals(p.getNameAsString()))
                    .map(MemberValuePair::getValue)
                    .map(Node::toString)
                    .findFirst()
                    .orElse("http");  // TODO for now we assume that no specified protocol in the builder means we will be http

                if (builderProtocol != null) {
                    if (!builderProtocol.equals(protocolName) || protocolName.isEmpty()) {
                        return;
                    }
                }

                getPublicOrProtectedMethods(typeDeclaration).forEach(methodDeclaration -> {
                    final String methodName = methodDeclaration.getNameAsString();
                    if (builderMethods.containsKey(methodName)) {
                        builderMethods.get(methodName).apply(methodDeclaration).ifPresent(listing::addDiagnostic);
                    }
                    missingMethods.remove(methodName);
                });

                if (!missingMethods.isEmpty()) {
                    listing.addDiagnostic(new Diagnostic(
                        WARNING,
                        makeId(typeDeclaration),
                        "This builder is missing common APIs. These are not required, but please consider if the following methods should exist: " + missingMethods,
                        "https://azure.github.io/azure-sdk/java_introduction.html#java-service-client-builder-consistency"));
                }
            }
        });
    }

    public static class ExactTypeNameCheckFunction implements Function<MethodDeclaration, Optional<Diagnostic>> {
        private final ParameterAllowedTypes[] expectedTypes;

        // For each parameter that we check for, we allow for there to be multiple types allowed for it,
        // e.g. credential(TokenCredential) or credential(AzureKeyCredential) or credential(AzureSasCredential)
        public ExactTypeNameCheckFunction(ParameterAllowedTypes... expectedTypes) {
            this.expectedTypes = expectedTypes;
        }

        public ExactTypeNameCheckFunction(String... expectedTypes) {
            this(Arrays.stream(expectedTypes).map(ParameterAllowedTypes::new).toArray(ParameterAllowedTypes[]::new));
        }

        @Override
        public Optional<Diagnostic> apply(final MethodDeclaration methodDeclaration) {
            for (int i = 0; i < expectedTypes.length; i++) {
                ParameterAllowedTypes expectedType = expectedTypes[i];
                Type actualType = methodDeclaration.getParameter(i).getType();

                String actualTypeName = ((ClassOrInterfaceType) actualType).getNameAsString();

                if (!expectedType.supports(actualTypeName)) {
                    return Optional.of(
                        new Diagnostic(
                            WARNING,
                            makeId(methodDeclaration),
                            "Incorrect type being supplied to this builder method. Expected " + expectedType +
                                ", but was " + actualTypeName + "."));
                }
            }

            return Optional.empty();
        }
    }

    public static class DirectSubclassCheckFunction implements Function<MethodDeclaration, Optional<Diagnostic>> {
        private final String parentTypeName;

        public DirectSubclassCheckFunction(String parentTypeName) {
            this.parentTypeName = parentTypeName;
        }

        @Override
        public Optional<Diagnostic> apply(final MethodDeclaration methodDeclaration) {
            Type parameterType = methodDeclaration.getParameter(0).getType();
            ClassOrInterfaceType classOrInterfaceType = parameterType.asClassOrInterfaceType();

            // TODO

            return Optional.empty();
        }
    }

    public static class ParameterAllowedTypes {
        private final String[] allowedTypes;

        public ParameterAllowedTypes(String... allowedTypes) {
            this.allowedTypes = allowedTypes;
        }

        public boolean supports(String type) {
            for (int i = 0; i < allowedTypes.length; i++) {
                if (type.equals(allowedTypes[i])) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "[" + Arrays.toString(allowedTypes) + "]";
        }
    }
}
