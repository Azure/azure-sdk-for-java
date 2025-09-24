// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Ensures that test code doesn't use {@code StepVerifier.setDefaultTimeout}.
 * <p>
 * This configures a default timeout used by all {@code StepVerifier} calls, which can lead to flaky tests as this may
 * affect other tests.
 */
public class StepVerifierCheck extends AbstractCheck {
    private static final String SET_DEFAULT_TIMEOUT = "setDefaultTimeout";
    private static final String FULLY_QUALIFIED = "reactor.test.StepVerifier.setDefaultTimeout";
    private static final String METHOD_CALL = "StepVerifier.setDefaultTimeout";

    static final String ERROR_MESSAGE = "Do not use StepVerifier.setDefaultTimeout as it can affect other tests. "
        + "Instead use expect* methods on StepVerifier and use verify(Duration) to set timeouts on a test-by-test basis.";

    private boolean hasStaticImport;

    /**
     * Creates a new instance of {@link StepVerifierCheck}.
     */
    public StepVerifierCheck() {
    }

    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.METHOD_CALL, TokenTypes.STATIC_IMPORT };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    @Override
    public void init() {
        super.init();
        hasStaticImport = false;
    }

    @Override
    public void destroy() {
        super.destroy();
        hasStaticImport = false;
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.STATIC_IMPORT) {
            // Compare if the static import is for StepVerifier.setDefaultTimeout
            hasStaticImport
                = FULLY_QUALIFIED.equals(FullIdent.createFullIdent(ast.getFirstChild().getNextSibling()).getText());
        } else {
            // Compare the method call against StepVerifier.setDefaultTimeout or setDefaultTimeout if there is a static
            // import for StepVerifier.setDefaultTimeout
            FullIdent fullIdent = FullIdent.createFullIdentBelow(ast);
            if (hasStaticImport && SET_DEFAULT_TIMEOUT.equals(fullIdent.getText())) {
                log(ast.getLineNo(), fullIdent.getColumnNo(), ERROR_MESSAGE);
            } else if (METHOD_CALL.equals(fullIdent.getText())) {
                log(ast.getLineNo(), fullIdent.getColumnNo(), ERROR_MESSAGE);
            } else if (FULLY_QUALIFIED.equals(fullIdent.getText())) {
                log(ast.getLineNo(), fullIdent.getColumnNo(), ERROR_MESSAGE);
            }
        }
    }
}
