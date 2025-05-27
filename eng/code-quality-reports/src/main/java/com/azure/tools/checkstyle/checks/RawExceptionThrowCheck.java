// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.List;

import static com.puppycrawl.tools.checkstyle.utils.TokenUtil.findFirstTokenByPredicate;

/**
 * Check that verifies that exceptions are logged when they are created.
 * The check is skipped if:
 * - the exception is created in a generated method or service interface
 * - the exception is a NullPointerException, IllegalArgumentException or UnsupportedOperationException that are used for immediate input validation
 */
public class RawExceptionThrowCheck extends AbstractCheck {
    static final String ERROR_MESSAGE = "Directly throwing a new exception is disallowed. Use the \"io.clientcore.core.instrumentation.logging.ClientLogger\" API instead, "
        + "such as \"logger.throwableAtError()\" or \"logger.throwableAtWarning()\"."
        + "See https://github.com/Azure/azure-sdk-for-java/wiki/Client-core:-logging-exceptions-best-practices for more details.";

    private static final List<String> IGNORED_EXCEPTIONS = Arrays.asList("NullPointerException",
        "IllegalArgumentException",
        "IllegalStateException",
        "UnsupportedOperationException");

    private static final String CORE_EXCEPTION_FACTORY_NAME = "CoreException.from";

    private boolean insideGeneratedMethod = false;
    private boolean insideServiceInterface = false;

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
            TokenTypes.METHOD_DEF,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.LITERAL_THROW
        };
    }

    @Override
    public void leaveToken(DetailAST token) {
        if (token.getType() == TokenTypes.METHOD_DEF) {
            insideGeneratedMethod = false;
        } else if (token.getType() == TokenTypes.INTERFACE_DEF) {
            insideServiceInterface = false;
        }
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.METHOD_DEF:
                insideGeneratedMethod = hasGeneratedAnnotation(token);
                break;
            case TokenTypes.INTERFACE_DEF:
                insideServiceInterface = hasServiceInterfaceAnnotation(token);
                break;
            case TokenTypes.LITERAL_THROW:
                if (insideGeneratedMethod || insideServiceInterface) {
                    return;
                }
                DetailAST expr = token.findFirstToken(TokenTypes.EXPR);
                if (expr == null) {
                    return;
                }

                DetailAST newToken = expr.findFirstToken(TokenTypes.LITERAL_NEW);
                if (newToken != null) {
                    String name = FullIdent.createFullIdentBelow(newToken).getText();
                    if (IGNORED_EXCEPTIONS.stream().noneMatch(name::endsWith)) {
                        log(newToken, ERROR_MESSAGE);
                    }

                    break;
                }

                DetailAST methodCallToken = expr.findFirstToken(TokenTypes.METHOD_CALL);
                if (methodCallToken != null) {
                    if (CORE_EXCEPTION_FACTORY_NAME.equals(FullIdent.createFullIdentBelow(methodCallToken).getText()))
                    {
                        log(methodCallToken, ERROR_MESSAGE);
                    }
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    private boolean hasGeneratedAnnotation(DetailAST methodDefAst) {
        DetailAST modifiers = methodDefAst.findFirstToken(TokenTypes.MODIFIERS);
        DetailAST annotation = modifiers.findFirstToken(TokenTypes.ANNOTATION);
        if (annotation == null) {
            return false;
        }

        String annotationName = annotation.findFirstToken(TokenTypes.IDENT).getText();
        if (!annotationName.endsWith("Metadata")) {
            return false;
        }

        DetailAST memberValuePairs = annotation.findFirstToken(TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR);
        if (memberValuePairs == null) {
            return false;
        }

        DetailAST props = findFirstTokenByPredicate(memberValuePairs, p -> p.getType() == TokenTypes.IDENT && "properties".equals(p.getText())).orElse(null);
        if (props == null) {
            return false;
        }

        DetailAST argValue = memberValuePairs.findFirstToken(TokenTypes.ANNOTATION_ARRAY_INIT);
        return findFirstTokenByPredicate(argValue == null ? memberValuePairs : argValue, expr -> {
            if (expr.getType() != TokenTypes.EXPR) {
                return false;
            }

            return FullIdent.createFullIdentBelow(expr).getText().endsWith("GENERATED");
        }).isPresent();
    }

    private boolean hasServiceInterfaceAnnotation(DetailAST interfaceDefAst) {
        DetailAST modifiers = interfaceDefAst.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) {
            return false;
        }
        DetailAST annotation = modifiers.findFirstToken(TokenTypes.ANNOTATION);
        if (annotation == null) {
            return false;
        }

        String annotationName = annotation.findFirstToken(TokenTypes.IDENT).getText();
        return annotationName.endsWith("ServiceInterface");
    }
}
