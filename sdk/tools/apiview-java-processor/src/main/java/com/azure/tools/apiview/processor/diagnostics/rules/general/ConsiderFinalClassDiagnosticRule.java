package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.INFO;

public class ConsiderFinalClassDiagnosticRule implements DiagnosticRule {
    private final List<TypeDeclaration<?>> nonFinalTypes = new ArrayList<>();
    private final Set<String> knownParentClasses = new TreeSet<>(String::compareTo);

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        cu.getTypes().forEach(type -> {
            if (type.isEnumDeclaration()) return;
            if (type.hasModifier(Modifier.Keyword.ABSTRACT)) return;
            if (type.isClassOrInterfaceDeclaration() && type.asClassOrInterfaceDeclaration().isInterface()) return;
            if (type.isAnnotationDeclaration()) return;

            // if we are here we are looking at a type we are interested in tracking.
            // for the type we are looking at, we check to see if it is a final type...
            if (!type.hasModifier(Modifier.Keyword.FINAL)) {
                nonFinalTypes.add(type);
            }

            // and we also care whether this type extends any other type, because for each of those types we extend,
            // they obvious cannot be final and therefore we shouldn't include an error message about that type.
            type.asClassOrInterfaceDeclaration().getExtendedTypes().forEach(parentType -> {
                knownParentClasses.add(parentType.getNameAsString());
            });
        });
    }

    @Override
    public void scanFinal(final APIListing listing) {
        for (final TypeDeclaration<?> type : nonFinalTypes) {
            final String name = type.getNameAsString();
            if (!knownParentClasses.contains(name)) {
                listing.addDiagnostic(new Diagnostic(INFO, makeId(type),
                    "Consider making all classes final by default - only make non-final if subclassing is supported."));
            }
        }
    }
}
