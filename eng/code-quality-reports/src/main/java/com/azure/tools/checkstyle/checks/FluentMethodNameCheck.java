// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Model Class Method:
 *  Fluent Methods: All methods that return an instance of the class, and with one parameter.
 *  The method name should not start with {@code avoidStartWord}.
 */
public class FluentMethodNameCheck extends AbstractCheck {

    private static final String FLUENT_METHOD_ERR = "Fluent Method name should not start with keyword %s.";

    // Specifies valid identifier: default start word is 'with'
    private String[] avoidStartWords = new String[] {"with"}; // by default
    private static final String MODEL = ".model";
    private String className;
    private static boolean isModelClass;

    @Override
    public void beginTree(DetailAST ast) {
        this.isModelClass = false;
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
        return new int[] {
            TokenTypes.CLASS_DEF,
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.PACKAGE_DEF:
                FullIdent packageNameFI = FullIdent.createFullIdentBelow(ast);
                this.isModelClass = packageNameFI.getText().endsWith(MODEL);
                break;
            case TokenTypes.CLASS_DEF:
                if (isModelClass) {
                    className = ast.findFirstToken(TokenTypes.IDENT).getText();
                }
                break;
            case TokenTypes.METHOD_DEF:
                if (isModelClass) {
                    isMethodNameStartWith(ast);
                }
                break;
        }
    }

    /**
     * Log the error if the method name is not start with {@code avoidStartWord}
     * @param ast METHOD_DEF AST node
     */
    private void isMethodNameStartWith(DetailAST ast) {
        String methodType = ast.findFirstToken(TokenTypes.TYPE).getFirstChild().getText();
        if (methodType.equals(className)) {
            String methodName = ast.findFirstToken(TokenTypes.IDENT).getText();
            int paramtersCount = ast.findFirstToken(TokenTypes.PARAMETERS).getChildCount();
            if (paramtersCount != 1) {
                return;
            }
            for (String avoidStartWord : avoidStartWords) {
                if (methodName.length() >= avoidStartWord.length()
                    && methodName.substring(0, avoidStartWord.length()).equals(avoidStartWord)) {
                    log(ast.getLineNo(), String.format(FLUENT_METHOD_ERR, avoidStartWord));
                }
            }

        }
    }

    /**
     * Setter to specifies valid identifiers
     * @param avoidStartWords the starting strings that should not start with in fluent method
     */
    public void setAvoidStartWord(String[] avoidStartWords) {
        if (avoidStartWords == null || avoidStartWords.length == 0) {
            return;
        }
        this.avoidStartWords = avoidStartWords;
    }
}

