// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Set;

public class NoImplInPublicAPI extends AbstractCheck {

    private static final String COM_AZURE = "com.azure";
    private static final String DOT_IMPLEMENTATION = ".implementation";
    private static final String PARAM_TYPE_ERROR = "\"%s\" class is in an implementation package, and it should not be used as a parameter type in public API. Alternatively, it can be removed from the implementation package and made public API, after appropriate API review.";
    private static final String RETURN_TYPE_ERROR = "\"%s\" class is in an implementation package, and it should not be a return type from public API. Alternatively, it can be removed from the implementation package and made public API.";

    private Set<String> implementationClassSet = new HashSet<>();

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
            TokenTypes.IMPORT,
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void beginTree(DetailAST root) {
        this.implementationClassSet.clear();
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.IMPORT:
                String importClassPath = FullIdent.createFullIdentBelow(ast).getText();
                if (importClassPath.startsWith(COM_AZURE)) {
                    int idx = importClassPath.indexOf(DOT_IMPLEMENTATION);
                    if (idx > -1) {
                        String remainingPath = importClassPath.substring(idx + DOT_IMPLEMENTATION.length());
                        if (remainingPath != null && remainingPath.length() > 1) {
                            String className = remainingPath.substring(remainingPath.lastIndexOf(".") + 1);
                            implementationClassSet.add(className);
                        }
                    }
                }
                break;
            case TokenTypes.METHOD_DEF:
                if (implementationClassSet.isEmpty()) {
                    return;
                }
                DetailAST modifiersAST = ast.findFirstToken(TokenTypes.MODIFIERS);
                if (modifiersAST.branchContains(TokenTypes.LITERAL_PUBLIC)
                    || modifiersAST.branchContains(TokenTypes.LITERAL_PROTECTED)) {
                    DetailAST typeAST = ast.findFirstToken(TokenTypes.TYPE);
                    String returnType = typeAST.getFirstChild().getText();
                    if (implementationClassSet.contains(returnType)) {
                        log(typeAST, String.format(RETURN_TYPE_ERROR, returnType));
                    }

                    DetailAST paramAST = ast.findFirstToken(TokenTypes.PARAMETERS);

                    for (DetailAST curr = paramAST.getFirstChild(); curr != null; curr = curr.getNextSibling()) {
                        if (curr.getType() == TokenTypes.PARAMETER_DEF) {
                            DetailAST paramTypeAST = curr.findFirstToken(TokenTypes.TYPE);
                            String paramType = paramTypeAST.getFirstChild().getText();
                            if (implementationClassSet.contains(paramType)) {
                                log(paramTypeAST, String.format(PARAM_TYPE_ERROR, paramType));
                            }
                        }
                    }
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }
}
