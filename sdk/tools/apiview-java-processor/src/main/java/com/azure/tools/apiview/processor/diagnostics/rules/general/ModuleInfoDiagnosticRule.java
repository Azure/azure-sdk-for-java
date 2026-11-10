package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.analysers.JavaASTAnalyser;
import com.azure.tools.apiview.processor.analysers.models.Constants;
import com.azure.tools.apiview.processor.analysers.util.ASTUtils;
import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.*;
import com.azure.tools.apiview.processor.model.traits.Parent;
import com.github.javaparser.ast.CompilationUnit;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.azure.tools.apiview.processor.analysers.JavaASTAnalyser.MODULE_INFO_KEY;
import static com.azure.tools.apiview.processor.analysers.models.Constants.*;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;


/**
 * This diagnostic rule checks that the module has `module-info.java` and also validates that the
 * name of the module matches the base package name.
 */
public class ModuleInfoDiagnosticRule implements DiagnosticRule {
    private final Set<String> packages = new HashSet<>();

    @Override
    public void scanIndividual(CompilationUnit cu, APIListing listing) {
        packages.add(cu.getPackageDeclaration().get().getNameAsString());
    }

    @Override
    public void scanFinal(APIListing listing) {
        // In this method, we first look for the presence of module-info.java.
        // If not present, add a warning message at the base package level
        // If present, validate that the module name is the same as the base package name

        // Base package name is the package that has the shortest name in a module
        String basePackageName = packages
                .stream()
                .min(Comparator.comparingInt(String::length))
                .orElse("");

        Set<String> exportsPackages = new HashSet<>();

        Optional<ReviewLine> moduleInfoLineOptional = ASTUtils.findReviewLine(listing, line -> line.hasProperty(JavaASTAnalyser.MODULE_INFO_KEY));
        if (!moduleInfoLineOptional.isPresent()) {
            listing.addDiagnostic(new Diagnostic(DiagnosticKind.WARNING, makeId(basePackageName),
                    "This module is missing module-info.java"));
            return;
        }

        ReviewLine moduleInfoLine = moduleInfoLineOptional.get();
        moduleInfoLine.getChildren().stream()
            .filter(childLine -> childLine.hasProperty(Constants.PROPERTY_MODULE_EXPORTS))
            .forEach(childLine -> exportsPackages.add(childLine.getProperty(Constants.PROPERTY_MODULE_EXPORTS)));

        String moduleName = moduleInfoLine.getProperty(PROPERTY_MODULE_NAME);
        if (moduleName != null) {
            // special casing azure-core as the base package doesn't have any classes and hence not included in the
            // list of packages
            if (!moduleName.equals(basePackageName) && !moduleName.equals("com.azure.core")) {
                // add warning message if the module name does not match the base package name
                listing.addDiagnostic(new Diagnostic(DiagnosticKind.WARNING,
                        makeId(MODULE_INFO_KEY), "Module name should be the same as base package " +
                        "name: " + basePackageName));
            }

            // Validate that all public packages are exported in module-info
            packages.stream()
                    .filter(publicPackage -> !exportsPackages.contains(publicPackage))
                    .forEach(missingExport -> {
                        listing.addDiagnostic(new Diagnostic(DiagnosticKind.ERROR,
                                makeId(MODULE_INFO_KEY), "Public package not exported: " + missingExport));
                    });

            exportsPackages.stream()
                    .filter(exportedPackage -> exportedPackage.contains(".implementation"))
                    .forEach(implementationPackage -> {
                        listing.addDiagnostic(new Diagnostic(DiagnosticKind.ERROR,
                                makeId(MODULE_INFO_KEY + "-exports-" + implementationPackage), "Implementation package should not be exported - " + implementationPackage));
                    });
        }
    }
}
