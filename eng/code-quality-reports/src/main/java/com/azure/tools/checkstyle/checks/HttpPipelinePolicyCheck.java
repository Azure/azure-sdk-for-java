// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Set;

/**
 * Http Pipeline Policy Checks
 * Any class that implements the HttpPipelinePolicy interface should:
 *  <ol>
 *    <li>must be a public class</li>
 *    <li>not in an implementation package or sub-package</li>
 *  </ol>
 */
public class HttpPipelinePolicyCheck extends AbstractCheck {
    private static final String HTTP_PIPELINE_POLICY = "HttpPipelinePolicy";
    private boolean isImplePackage;

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
    public void visitToken(DetailAST ast) {

        switch (ast.getType()) {
            case TokenTypes.PACKAGE_DEF:
                String packageName = FullIdent.createFullIdentBelow(ast).getText();
                isImplePackage = packageName.contains("implementation");
                break;
            case TokenTypes.CLASS_DEF:
                String className = ast.findFirstToken(TokenTypes.IDENT).getText();
                // Get all interfaces name
                DetailAST impleClauseToken = ast.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE);
                // Skip check if the class doesn't implement any interface
                if (impleClauseToken == null) {
                    return;
                }
                Set<String> interfaces = new HashSet<>();
                for (DetailAST node = impleClauseToken.getFirstChild(); node != null; node = node.getNextSibling()) {
                    if (node.getType() == TokenTypes.IDENT) {
                        interfaces.add(node.getText());
                    }
                }
                // Skip check if the class doesn't implement HTTP_PIPELINE_POLICY
                if (!interfaces.contains(HTTP_PIPELINE_POLICY)) {
                    return;
                }

                DetailAST modifiersToken = ast.findFirstToken(TokenTypes.MODIFIERS);
                // Public class check
                if (!modifiersToken.branchContains(TokenTypes.LITERAL_PUBLIC)) {
                    log(modifiersToken, String.format("Class ''%s'' implements ''%s'' should be a public class", className, HTTP_PIPELINE_POLICY));
                }
                // Implementation and sub-package check
                if (isImplePackage) {
                    log(ast, String.format("Class ''%s'' implements ''%s'' should not be a implementation package or sub-package of it", className, HTTP_PIPELINE_POLICY));
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }
}
