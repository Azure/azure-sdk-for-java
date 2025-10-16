// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.naming.AccessModifierOption;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

/**
 * Checks that any class that implements {@code HttpPipelinePolicy} should follow the following rules:
 * <ol>
 *   <li>Be a public class.</li>
 *   <li>Not live in the implementation package or any of its sub-packages.</li>
 * </ol>
 */
public class HttpPipelinePolicyCheck extends AbstractCheck {
    private static final String HTTP_PIPELINE_POLICY = "HttpPipelinePolicy";
    private boolean isImplementationPackage;

    /**
     * Creates a new instance of {@link HttpPipelinePolicyCheck}.
     */
    public HttpPipelinePolicyCheck() {
    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] { TokenTypes.PACKAGE_DEF, TokenTypes.CLASS_DEF };
    }

    @Override
    public void visitToken(DetailAST token) {
        if (token.getType() == TokenTypes.PACKAGE_DEF) {
            final String packageName = FullIdent.createFullIdent(token.findFirstToken(TokenTypes.DOT)).getText();
            isImplementationPackage = packageName.contains("implementation");
        } else if (token.getType() == TokenTypes.CLASS_DEF) {
            checkPublicNonImplementationPolicyClass(token);
        }
    }

    /**
     * Any class implements HttpPipelinePolicy, should be a public class and not live in the implementation package or
     * any of its sub-packages
     *
     * @param classDefToken CLASS_DEF token
     */
    private void checkPublicNonImplementationPolicyClass(DetailAST classDefToken) {
        // Get all interfaces name
        final DetailAST implementsClauseToken = classDefToken.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE);
        // Skip check if the class doesn't implement any interface
        if (implementsClauseToken == null) {
            return;
        }

        TokenUtil
            .findFirstTokenByPredicate(implementsClauseToken,
                node -> node.getType() == TokenTypes.IDENT && HTTP_PIPELINE_POLICY.equals(node.getText()))
            .ifPresent(ignored -> {
                final String className = classDefToken.findFirstToken(TokenTypes.IDENT).getText();
                // Public class check
                if (CheckUtil.getAccessModifierFromModifiersToken(classDefToken) != AccessModifierOption.PUBLIC) {
                    log(classDefToken, String.format("Class ''%s'' implementing ''%s'' and should be a public class",
                        className, HTTP_PIPELINE_POLICY));
                }

                // Implementation and sub-package check
                if (isImplementationPackage) {
                    log(classDefToken, String.format("Class ''%s'' implementing ''%s'' and should not be a "
                        + "implementation package or sub-package of it", className, HTTP_PIPELINE_POLICY));
                }
            });
    }
}
