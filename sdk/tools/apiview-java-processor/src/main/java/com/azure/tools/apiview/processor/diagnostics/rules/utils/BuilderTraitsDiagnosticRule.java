package com.azure.tools.apiview.processor.diagnostics.rules.utils;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.*;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.isTypeImplementingInterface;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.ERROR;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.WARNING;

public abstract class BuilderTraitsDiagnosticRule implements DiagnosticRule {
    private static final String ANNOTATION_SERVICE_CLIENT_BUILDER = "ServiceClientBuilder";

    private final Map<String, TraitClass> traits;

    protected BuilderTraitsDiagnosticRule(Map<String, TraitClass> traits) {
        this.traits = traits;
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        // If the CU has a @ServiceClientBuilder annotation:
        //   Look for methods with names of methods we know should belong to the trait:
        //     Ensure the matching trait is stated as being implemented. If the method exists but the trait interface is
        //     not defined as being implemented, add a warning diagnostic that the trait should be implemented.
        //     If the method name exists, but it has different parameters that what we expect, add a warning diagnostic
        //     suggesting to use the trait.

        cu.getTypes().forEach(type -> {
            Optional<AnnotationExpr> serviceClientBuilderAnnotation = type.getAnnotationByName(ANNOTATION_SERVICE_CLIENT_BUILDER);

            if (!serviceClientBuilderAnnotation.isPresent()) return;

            // iterate through trait-by-trait
            for (Map.Entry<String, TraitClass> trait : traits.entrySet()) {
                String traitName = trait.getKey();
                TraitClass traitClass = trait.getValue();
                List<TraitMethod> traitMethods = traitClass.methods;

                // check if the ServiceClientBuilder 'protocol' annotation property matches the TraitClass builderProtocol
                // value. We only do this for TraitClass instances whose builderProtocol != N/A (which means the trait
                // is protocol-agnostic
                if (!traitClass.builderProtocol.equals(TraitClass.BUILDER_PROTOCOL_NOT_APPLICABLE)) {
                    AnnotationExpr annotationExpr = serviceClientBuilderAnnotation.get();
                    String builderProtocol = TraitClass.BUILDER_PROTOCOL_NOT_APPLICABLE;
                    if (annotationExpr.isNormalAnnotationExpr()) {
                        builderProtocol = annotationExpr.asNormalAnnotationExpr().getPairs()
                                .stream()
                                .filter(mvp -> mvp.getNameAsString().equals("protocol"))
                                .map(mvp -> mvp.getValue().toString())
                                .findFirst()
                                .orElse(TraitClass.BUILDER_PROTOCOL_NOT_APPLICABLE);
                    }

                    if (!builderProtocol.equals(traitClass.builderProtocol)) {
                        continue;
                    }
                }

                for (TraitMethod traitMethod : traitMethods) {
                    String methodName = traitMethod.methodName;

                    // firstly check if the exact method (name + params) is present
                    List<MethodDeclaration> matchingMethods = type.getMethodsBySignature(methodName, traitMethod.methodParamTypes);
                    if (!matchingMethods.isEmpty()) {
                        // ensure we implement the trait
                        if (!isTypeImplementingInterface(type, traitName)) {
                            // we have an exact match to a trait method, but we do not implement the trait!
                            for (MethodDeclaration m : matchingMethods) {
                                listing.addDiagnostic(new Diagnostic(
                                        ERROR,
                                        makeId(m),
                                        "This builder has methods that exactly match a method in the " + traitName
                                                + " trait, but the builder does not implement this trait. Consider " +
                                                "implementing the trait to ensure greater consistency."));
                            }
                        }
                    }

                    // now check for other methods with the trait method name (and some / all of the expected params),
                    // but with additional non-standard params as well
                    List<MethodDeclaration> matchingNameMethods = type.getMethodsByName(methodName);
                    for (MethodDeclaration matchingNameMethod : matchingNameMethods) {
                        int matchingParams = 0;
                        for (Parameter parameter : matchingNameMethod.getParameters()) {
                            for (String traitParamType : traitMethod.methodParamTypes) {
                                if (parameter.getTypeAsString().equals(traitParamType)) {
                                    matchingParams++;
                                }
                            }
                        }

                        if (matchingParams > 0 && matchingParams < matchingNameMethod.getParameters().size()) {
                            // the method has a matching name, and some of the params match, but there are extra ones
                            // that mean this method is non-standard and not part of the trait.
                            String message = isTypeImplementingInterface(type, traitName) ?
                                    "This builder implements the " + traitName + " trait, but offers duplicate " +
                                            "functionality. Consider whether this impacts consistency with other builders." :
                                    "This builder does not implement the " + traitName + " trait, but offers " +
                                            "similar functionality. Consider implementing the trait and aligning the parameters " +
                                            "in this builder method.";
                            listing.addDiagnostic(new Diagnostic(WARNING, makeId(matchingNameMethod), message));
                        }
                    }
                }
            }
        });
    }

    public static class TraitClass {
        public static final String BUILDER_PROTOCOL_NOT_APPLICABLE = "N/A";
        public static final String BUILDER_PROTOCOL_HTTP = "ServiceClientProtocol.HTTP";
        public static final String BUILDER_PROTOCOL_AMQP = "ServiceClientProtocol.AMQP";

        private final String builderProtocol;
        private final List<TraitMethod> methods;

        public TraitClass(TraitMethod... methods) {
            this(BUILDER_PROTOCOL_NOT_APPLICABLE, methods);
        }

        public TraitClass(String builderProtocol, TraitMethod... methods) {
            this.builderProtocol = builderProtocol;
            this.methods = Arrays.asList(methods);
        }
    }

    public static class TraitMethod {
        private final String methodName;
        private final String[] methodParamTypes;

        public TraitMethod(String methodName, String... methodParamTypes) {
            this.methodName = methodName;
            this.methodParamTypes = methodParamTypes;
        }
    }
}
