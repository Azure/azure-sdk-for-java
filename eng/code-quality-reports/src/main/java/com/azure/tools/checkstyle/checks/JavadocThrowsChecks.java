// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.DetailNodeTreeStringPrinter;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.BlockCommentPosition;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class JavadocThrowsChecks extends AbstractCheck {
    private static final String MISSING_DESCRIPTION_MESSAGE =
        "@throws tag requires a description explaining when the error is thrown.";
    private static final String MISSING_THROWS_TAG_MESSAGE = "Javadoc @throws tag required for unchecked throw.";
    private static final int[] TOKENS = new int[] {
        TokenTypes.CTOR_DEF,
        TokenTypes.METHOD_DEF,
        TokenTypes.BLOCK_COMMENT_BEGIN,
        TokenTypes.LITERAL_THROWS,
        TokenTypes.LITERAL_THROW,
        TokenTypes.PARAMETER_DEF,
        TokenTypes.VARIABLE_DEF,
    };
    private static final String THIS_TOKEN = "this";
    private static final String CLASS_TOKEN = "class";

    private Map<String, HashSet<String>> javadocThrowsMapping;
    private Map<String, HashSet<String>> exceptionMapping;
    private String currentScopeIdentifier;
    private boolean currentScopeNeedsChecking;

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
        return TOKENS;
    }

    @Override
    public boolean isCommentNodesRequired() {
        return true;
    }

    @Override
    public void beginTree(DetailAST rootToken) {
        javadocThrowsMapping = new HashMap<>();
        exceptionMapping = new HashMap<>();
        currentScopeNeedsChecking = false;
        currentScopeIdentifier = "";
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CTOR_DEF:
            case TokenTypes.METHOD_DEF:
                setIdentifierAndCheckStatus(token);
                break;

            case TokenTypes.BLOCK_COMMENT_BEGIN:
                findJavadocThrows(token);
                break;

            case TokenTypes.LITERAL_THROWS:
                if (currentScopeNeedsChecking) {
                    verifyCheckedThrowJavadoc(token);
                }
                break;

            case TokenTypes.LITERAL_THROW:
                if (currentScopeNeedsChecking) {
                    verifyUncheckedThrowJavadoc(token);
                }
                break;

            case TokenTypes.PARAMETER_DEF:
            case TokenTypes.VARIABLE_DEF:
                if (currentScopeNeedsChecking || token.getParent().getType() == TokenTypes.OBJBLOCK) {
                    addExceptionMapping(token);
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /*
     * Gets the current method identifier and determines if it needs to be checked.
     * @param scopeDefToken Method definition token.
     */
    private void setIdentifierAndCheckStatus(DetailAST scopeDefToken) {
        currentScopeIdentifier = scopeDefToken.findFirstToken(TokenTypes.IDENT).getText() + scopeDefToken.getLineNo();
        currentScopeNeedsChecking =
            visibilityIsPublicOrProtectedAndNotAbstractOrOverride(scopeDefToken.findFirstToken(TokenTypes.MODIFIERS));
    }

    /*
     * Determines if the modifiers contains either public or protected and isn't abstract or an override.
     * @param modifiersToken Modifiers token.
     * @return True if the method if public or protected and isn't abstract.
     */
    private boolean visibilityIsPublicOrProtectedAndNotAbstractOrOverride(DetailAST modifiersToken) {
        if (modifiersToken == null) {
            return false;
        }

        // Don't need to check abstract methods as they won't have implementation.
        if (modifiersToken.findFirstToken(TokenTypes.ABSTRACT) != null) {
            return false;
        }

        // Don't need to check override methods that don't have JavaDocs.
        if (modifiersToken.findFirstToken(TokenTypes.BLOCK_COMMENT_BEGIN) == null) {
            if (TokenUtil.findFirstTokenByPredicate(modifiersToken, this::isOverrideAnnotation).isPresent()) {
                return false;
            }
        }

        // Check public or protect methods.
        return modifiersToken.findFirstToken(TokenTypes.LITERAL_PUBLIC) != null
            || modifiersToken.findFirstToken(TokenTypes.LITERAL_PROTECTED) != null;
    }

    private boolean isOverrideAnnotation(DetailAST modifierToken) {
        if (modifierToken.getType() != TokenTypes.ANNOTATION) {
            return false;
        }

        // Possible for an identifier not to exist if it is a nested class (ie. @Parameterized.Parameters(String)).
        final DetailAST identifier = modifierToken.findFirstToken(TokenTypes.IDENT);

        return identifier != null && "Override".equals(identifier.getText());
    }

    /*
     * Checks if the comment is on a method, if so it searches for the documented Javadoc @throws statements.
     * @param blockCommentToken Block comment token.
     */
    private void findJavadocThrows(DetailAST blockCommentToken) {
        if (!BlockCommentPosition.isOnMethod(blockCommentToken)
            && !BlockCommentPosition.isOnConstructor(blockCommentToken)) {
            return;
        }

        // Turn the DetailAST into a Javadoc DetailNode.
        DetailNode javadocNode = null;
        try {
            javadocNode = DetailNodeTreeStringPrinter.parseJavadocAsDetailNode(blockCommentToken);
        } catch (IllegalArgumentException ex) {
            // Exceptions are thrown if the JavaDoc has invalid formatting.
        }

        if (javadocNode == null) {
            return;
        }

        // Append the line number to differentiate overloads.
        HashSet<String> javadocThrows = javadocThrowsMapping.getOrDefault(currentScopeIdentifier, new HashSet<>());

        // Iterate through all the top level nodes in the Javadoc, looking for the @throws statements.
        for (DetailNode node : javadocNode.getChildren()) {
            if (node.getType() != JavadocTokenTypes.JAVADOC_TAG
                || JavadocUtil.findFirstToken(node, JavadocTokenTypes.THROWS_LITERAL) == null) {
                continue;
            }

            // Add the class being thrown to the set of documented throws.
            javadocThrows.add(JavadocUtil.findFirstToken(node, JavadocTokenTypes.CLASS_NAME).getText());

            if (JavadocUtil.findFirstToken(node, JavadocTokenTypes.DESCRIPTION) == null) {
                log(node.getLineNumber(), MISSING_DESCRIPTION_MESSAGE);
            }
        }

        javadocThrowsMapping.put(currentScopeIdentifier, javadocThrows);
    }

    /*
     * Checks if parameter and variable definitions are exception definitions, if so adds them to the mapping.
     * @param definitionToken Definition token.
     */
    private void addExceptionMapping(DetailAST definitionToken) {
        DetailAST typeToken = definitionToken.findFirstToken(TokenTypes.TYPE).getFirstChild();

        // Lambdas don't list a type, quit out.
        if (typeToken == null) {
            return;
        }

        String scope = currentScopeIdentifier;
        if (currentScopeIdentifier == null || currentScopeIdentifier.isEmpty()) {
            if (definitionToken.branchContains(TokenTypes.LITERAL_STATIC)) {
                scope = CLASS_TOKEN;
            } else {
                scope = THIS_TOKEN;
            }
        }
        String identifier = scope + definitionToken.findFirstToken(TokenTypes.IDENT).getText();
        HashSet<String> types = exceptionMapping.getOrDefault(identifier, new HashSet<>());

        if (typeToken.getType() == TokenTypes.BOR) {
            TokenUtil.forEachChild(typeToken, TokenTypes.IDENT, (identityToken) -> tryToAddType(identityToken, types));
        } else {
            tryToAddType(typeToken, types);
        }

        exceptionMapping.put(identifier, types);
    }

    private void tryToAddType(DetailAST typeToken, HashSet<String> types) {
        String type = typeToken.getText();
        if (type.endsWith("Exception") || type.endsWith("Error")) {
            types.add(type);
        }
    }

    /*
     * Verifies that the checked exceptions, those in the throws statement, are documented.
     * @param throwsToken Throws token.
     */
    private void verifyCheckedThrowJavadoc(DetailAST throwsToken) {
        HashSet<String> methodJavadocThrows = javadocThrowsMapping.get(currentScopeIdentifier);
        if (methodJavadocThrows == null) {
            log(throwsToken, MISSING_THROWS_TAG_MESSAGE);
            return;
        }

        TokenUtil.forEachChild(throwsToken, TokenTypes.IDENT, (throwTypeToken) -> {
            if (!methodJavadocThrows.contains(throwTypeToken.getText())) {
                log(throwTypeToken, MISSING_THROWS_TAG_MESSAGE);
            }
        });
    }

    /*
     * Checks if the throw statement has documentation in the Javadoc.
     * @param throwToken Throw statement token.
     */
    private void verifyUncheckedThrowJavadoc(DetailAST throwToken) {
        // Early out check for method that don't have Javadocs, they cannot have @throws documented.
        HashSet<String> methodJavadocThrows = javadocThrowsMapping.get(currentScopeIdentifier);
        if (methodJavadocThrows == null) {
            log(throwToken, MISSING_THROWS_TAG_MESSAGE);
            return;
        }

        DetailAST throwExprToken = throwToken.findFirstToken(TokenTypes.EXPR);

        // Check if the throw is constructing the exception, method call, or throwing an instantiated exception.
        DetailAST literalNewToken = throwExprToken.findFirstToken(TokenTypes.LITERAL_NEW);
        DetailAST methodCallToken = throwExprToken.findFirstToken(TokenTypes.METHOD_CALL);
        DetailAST typecastToken = throwExprToken.findFirstToken(TokenTypes.TYPECAST);

        if (typecastToken != null) {
            // Throwing a casted variable.
            String throwType = typecastToken.findFirstToken(TokenTypes.TYPE).findFirstToken(TokenTypes.IDENT).getText();
            if (!methodJavadocThrows.contains(throwType)) {
                log(throwExprToken, MISSING_THROWS_TAG_MESSAGE);
            }
        } else if (literalNewToken != null) {
            // Throwing a constructed exception.
            if (!methodJavadocThrows.contains(literalNewToken.findFirstToken(TokenTypes.IDENT).getText())) {
                log(throwToken, MISSING_THROWS_TAG_MESSAGE);
            }
        } else if (methodCallToken != null) {
            // Throwing a method call.
            // Checkstyle complains about empty blocks.
            // TODO: Should we ignore this checkstyle error?
            return;
        } else {
            // Throwing an un-casted variable.
            DetailAST lastIdentifier = null;
            DetailAST current = throwExprToken;
            while (current != null) {
                if (current.getType() == TokenTypes.IDENT) {
                    lastIdentifier = current;
                }
                if (current.getFirstChild() != null) {
                    current = current.getFirstChild();
                } else {
                    current = current.getNextSibling();
                }
            }
            if (lastIdentifier == null) {
                return;
            }

            String throwIdentName = lastIdentifier.getText();
            HashSet<String> types = findMatchingExceptionType(currentScopeIdentifier, throwIdentName);

            if (types == null) {
                return;
            }

            for (String type : types) {
                if (!methodJavadocThrows.contains(type)) {
                    log(throwExprToken, MISSING_THROWS_TAG_MESSAGE);
                }
            }
        }
    }

    private HashSet<String> findMatchingExceptionType(String scope, String throwIdent) {
        // check current scope
        HashSet<String> types = exceptionMapping.get(scope + throwIdent);
        if (types == null) {
            // if a matching type is not found in current method scope, search object scope
            types = exceptionMapping.get(THIS_TOKEN + throwIdent);
        }
        if (types == null) {
            // if a matching type is not found in method or instance scope, search class scope
            types = exceptionMapping.get(CLASS_TOKEN + throwIdent);
        }
        return types;
    }
}
