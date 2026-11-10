package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.analysers.util.ASTUtils;
import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;

import java.util.Arrays;
import java.util.List;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getClassName;

import static com.azure.tools.apiview.processor.model.DiagnosticKind.*;

public class ImportsDiagnosticRule implements DiagnosticRule {

    private final List<String> illegalPackages;

    public ImportsDiagnosticRule(String... packageNames) {
        if (packageNames == null || packageNames.length == 0) {
            throw new IllegalArgumentException("ImportsDiagnosticRule created with no illegal package names");
        }
        this.illegalPackages = Arrays.asList(packageNames);
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        // we need to map the issue to the class id, because import text isn't printed in the APIView output
        getClassName(cu).map(listing.getKnownTypes()::get).ifPresent(typeId -> ASTUtils.getImports(cu)
            .forEach(importStr -> {
                for (String illegalPackage : illegalPackages) {
                    if (importStr.contains(illegalPackage)) {
                        listing.addDiagnostic(new Diagnostic(WARNING, typeId,
                            "Do not add dependencies to classes in the '" + illegalPackage + "' package."));
                    }
                }
            }));
    }
}
