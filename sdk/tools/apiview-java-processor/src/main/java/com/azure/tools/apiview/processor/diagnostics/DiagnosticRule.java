package com.azure.tools.apiview.processor.diagnostics;

import com.azure.tools.apiview.processor.model.APIListing;
import com.github.javaparser.ast.CompilationUnit;

/**
 * Diagnostic rules are used to scan a compilation unit and report any issues found. These rules can be implemented to
 * run either individually (as they are encountered in the source code) or as a final scan (after all compilation units).
 */
public interface DiagnosticRule {

    /**
     * Scans the compilation unit for issues and reports them to the API listing. Note that the state of the API listing
     * is representative of the current progress of the scan, so it is possible that the listing may not contain all
     * contextual information. If you want to report issues that require a full scan of the API listing, you should
     * implement the {@link #scanFinal(APIListing)} method instead.
     *
     * @param cu the compilation unit to scan.
     * @param listing the API listing to report issues to.
     */
    void scanIndividual(CompilationUnit cu, APIListing listing);

    /**
     * Scans the API listing for issues that require a full scan of the listing. This method is called after all
     * compilation units have been scanned.
     *
     * @param listing the API listing to report issues to.
     */
    default void scanFinal(APIListing listing) {
        // no-op (not all diagnostics care about doing a final scan)
    }
}
