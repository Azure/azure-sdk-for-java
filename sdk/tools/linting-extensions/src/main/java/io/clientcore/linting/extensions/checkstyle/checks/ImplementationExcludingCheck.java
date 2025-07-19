// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checkstyle check that skips running validation on {@code *.implementation.*} classes.
 */
public abstract class ImplementationExcludingCheck extends AbstractCheck {
    private boolean addedPackageDef = false;
    private boolean implementationPackage = false;

    /**
     * Creates a new instance of {@link ImplementationExcludingCheck}.
     */
    public ImplementationExcludingCheck() {
    }

    @Override
    public final int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public final int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public final int[] getRequiredTokens() {
        int[] tokensForCheck = getTokensForCheck();
        for (int tokenForCheck : tokensForCheck) {
            if (tokenForCheck == TokenTypes.PACKAGE_DEF) {
                // If the tokens used in the check already contains PACKAGE_DEF return it without modification.
                return tokensForCheck;
            }
        }

        // Tokens used in the check didn't contain PACKAGE_DEF.
        // Create a new array, add PACKAGE_DEF, and copy the tokens used in the check to the new array.
        int[] requiredTokens = new int[tokensForCheck.length + 1];
        requiredTokens[0] = TokenTypes.PACKAGE_DEF;
        System.arraycopy(tokensForCheck, 0, requiredTokens, 1, tokensForCheck.length);
        addedPackageDef = true;

        return requiredTokens;
    }

    /**
     * The tokens used by the implementing class.
     *
     * @return The tokens used by the implementing class.
     */
    public abstract int[] getTokensForCheck();

    @Override
    public final void beginTree(DetailAST rootAST) {
        // Before starting the tree reset the flag for implementation package.
        // This is done both before and after the tree just to make sure.
        implementationPackage = false;
        beforeTree(rootAST);
    }

    /**
     * Method to prepare a check before processing the AST tree.
     *
     * @param rootAst The root node of the tree.
     */
    public void beforeTree(DetailAST rootAst) {
        // No-op by default.
    }

    @Override
    public final void finishTree(DetailAST rootAST) {
        // After completing the tree reset the flag for implementation package.
        // This is done both before and after the tree just to make sure.
        implementationPackage = false;
        afterTree(rootAST);
    }

    /**
     * Method to clean up a check after processing the AST tree.
     *
     * @param rootAst The root node of the tree.
     */
    public void afterTree(DetailAST rootAst) {
        // No-op by default.
    }

    @Override
    public final void visitToken(DetailAST token) {
        if (implementationPackage) {
            return;
        }

        if (token.getType() == TokenTypes.PACKAGE_DEF) {
            // Check if we're in an implementation package.
            final String packageName = FullIdent.createFullIdent(token.findFirstToken(TokenTypes.DOT)).getText();
            implementationPackage = packageName.contains("implementation");

            // If PACKAGE_DEF was added by this class don't propagate the token to the processing method in the
            // subclass. It shouldn't need to handle it and probably won't know how to handle it.
            if (addedPackageDef) {
                return;
            }
        }

        processToken(token);
    }

    /**
     * Method to process a token in the tree that matches one of the token types defined in
     * {@link #getRequiredTokens()}.
     *
     * @param token A node in the tree to process.
     */
    public abstract void processToken(DetailAST token);
}
