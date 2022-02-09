// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.naming.AccessModifierOption;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.Optional;

/**
 * Http Pipeline Policy Checks
 * Any class that implements the HttpPipelinePolicy interface should:
 *  <ol>
 *    <li>Be a public class.</li>
 *    <li>Not live in the implementation package or any of its sub-packages.</li>
 *  </ol>
 */
public class HttpPipelinePolicyCheck extends AbstractCheck {
    private static final String HTTP_PIPELINE_POLICY = "HttpPipelinePolicy";
    private boolean isImplementationPackage;

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
        return new int[] {
            TokenTypes.PACKAGE_DEF,
            TokenTypes.CLASS_DEF
        };
    }

    @Override
    public void visitToken(DetailAST token) {

        switch (token.getType()) {
            case TokenTypes.PACKAGE_DEF:
                final String packageName = FullIdent.createFullIdent(token.findFirstToken(TokenTypes.DOT)).getText();
                isImplementationPackage = packageName.contains("implementation");
                break;
            case TokenTypes.CLASS_DEF:
                checkPublicNonImplementationPolicyClass(token);
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
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

        final Optional<DetailAST> policy = TokenUtil.findFirstTokenByPredicate(implementsClauseToken,
            node -> node.getType() == TokenTypes.IDENT && HTTP_PIPELINE_POLICY.equals(node.getText()));

        // Skip check if the class doesn't implement HTTP_PIPELINE_POLICY
        if (!policy.isPresent()) {
            return;
        }

        final AccessModifierOption accessModifier = CheckUtil.getAccessModifierFromModifiersToken(classDefToken);
        final String className = classDefToken.findFirstToken(TokenTypes.IDENT).getText();
        // Public class check
        if (!accessModifier.equals(AccessModifierOption.PUBLIC)) {
            log(classDefToken, String.format("Class ''%s'' implementing ''%s'' and should be a public class",
                className, HTTP_PIPELINE_POLICY));
        }

        // Implementation and sub-package check
        if (isImplementationPackage) {
            log(classDefToken, String.format("Class ''%s'' implementing ''%s'' and should not be a implementation "
                + "package or sub-package of it", className, HTTP_PIPELINE_POLICY));
        }
    }
}
