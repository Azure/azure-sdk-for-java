// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Model Class Method:
 *  Fluent Methods: All methods that return an instance of the class, and that have one parameter.
 *  The method name should not start with {@code avoidStartWords}.
 */
public class FluentMethodNameCheck extends AbstractCheck {

    private static final String FLUENT_METHOD_ERR = "\"%s\" fluent method name should not start with keyword \"%s\".";

    private Set<String> avoidStartWords = new HashSet<>();
    private String className;

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
            case TokenTypes.CLASS_DEF:
                className = ast.findFirstToken(TokenTypes.IDENT).getText();
                break;
            case TokenTypes.METHOD_DEF:
                checkMethodNameStartWith(ast);
                break;
        }
    }

    /**
     * Log the error if the method name is not start with {@code avoidStartWord}
     * @param ast METHOD_DEF AST node
     */
    private void checkMethodNameStartWith(DetailAST ast) {
        // 1, parameter count should be 1
        Optional<DetailAST> parametersASTOption = TokenUtil.findFirstTokenByPredicate(ast, c -> c.getType() == TokenTypes.PARAMETERS && c.getChildCount() == 1);
        if (parametersASTOption.isPresent()) { // one param method
            // 2, method type should be matched with class name
            Optional<DetailAST> typeASTOption = TokenUtil.findFirstTokenByPredicate(ast, c -> c.getType() == TokenTypes.TYPE);
            if (typeASTOption.isPresent()) {
                Optional<DetailAST> identASTOption = TokenUtil.findFirstTokenByPredicate(typeASTOption.get(), c -> c.getType() == TokenTypes.IDENT && c.getText().equals(className));
                if (identASTOption.isPresent()) { // return type is self class type
                    Optional<DetailAST> methodIdentASTOption = TokenUtil.findFirstTokenByPredicate(ast, c -> c.getType() == TokenTypes.IDENT);
                    if (methodIdentASTOption.isPresent()) {
                        String methodName = methodIdentASTOption.get().getText();
                        // 3, log if the method name start with avoid substring
                        for (String avoidStartWord : avoidStartWords) {
                            if (methodName.length() >= avoidStartWord.length() && methodName.startsWith(avoidStartWord)) {
                                log(ast.getLineNo(), String.format(FLUENT_METHOD_ERR, methodName, avoidStartWord));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Setter to specifies valid identifiers
     * @param avoidStartWords the starting strings that should not start with in fluent method
     */
    public final void setAvoidStartWords(String... avoidStartWords) {
        Collections.addAll(this.avoidStartWords, avoidStartWords);
    }
}
