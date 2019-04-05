// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.azure.common.ServiceClient;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.Optional;

/**
 * Verifies that subclasses of {@link ServiceClient} meet a set of guidelines.
 * <ol>
 *  <li>They cannot have public or protected constructors</li>
 *  <li>They must implement a public static method named builder</li>
 * </ol>
 */
public class ServiceClientChecks extends AbstractCheck {
    private static final String BUILDER_METHOD_NAME = "builder";

    private static final String CONSTRUCTOR_ERROR_MESSAGE = "Descendants of ServiceClient cannot have public or protected constructors.";
    private static final String BUILDER_ERROR_MESSAGE = "Descendants of ServiceClient must have a static method named builder.";

    private static final int[] TOKENS = new int[] { TokenTypes.PACKAGE_DEF };
    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    /**
     * Denotes that the PACKAGE_DEF token type is the only one that should trigger visitToken to be called.
     * This is done to prevent inner classes from accidentally firing checks.
     * @return AST token types that will trigger visitToken to be called.
     */
    @Override
    public int[] getRequiredTokens() {
        return TOKENS;
    }

    /**
     * Runs the check logic on the tokens we marked as needing to be checked in getRequiredTokens.
     * @param ast Abstract syntax tree node that will be checked.
     */
    @Override
    public void visitToken(DetailAST ast) {
        DetailAST classDefNode = CustomCheckUtils.findNextSiblingOfType(ast, node -> node.getType() == TokenTypes.CLASS_DEF);
        if (classDefNode == null) {
            return;
        }

        DetailAST objBlockNode = classDefNode.findFirstToken(TokenTypes.OBJBLOCK);
        Optional<String> fullClassName = getFullClassName(ast, classDefNode);
        if (!fullClassName.isPresent()) {
            return;
        }

        // Attempt to load the class and run the checks if it is a descendant of ServiceClient.
        try {
            Class<?> classToCheck = this.getClassLoader().loadClass(fullClassName.get());
            Class<?> serviceClientClass = this.getClassLoader().loadClass(ServiceClient.class.getName());

            if (!serviceClientClass.isAssignableFrom(classToCheck)) {
                return;
            }
        } catch (ClassNotFoundException ex) {
            return;
        }

        TokenUtil.forEachChild(objBlockNode, TokenTypes.CTOR_DEF, this::checkConstructorIsHidden);
        if (!TokenUtil.findFirstTokenByPredicate(objBlockNode, this::isMethodStaticBuilder).isPresent()) {
            log(classDefNode, BUILDER_ERROR_MESSAGE);
        }
    }

    /**
     * Given the package node and the class definition node construct the full class name.
     * @param packageDefNode Node containing the package name
     * @param classDefNode Node containing the class definition
     * @return Full class name, if the file isn't a class an empty optional
     */
    private Optional<String> getFullClassName(DetailAST packageDefNode, DetailAST classDefNode) {
        String packageName = FullIdent.createFullIdent(packageDefNode.findFirstToken(TokenTypes.DOT)).getText();

        // If the package begins with com.microsoft.* it is still in track one and should skip this check.
        if (packageName.startsWith("com.microsoft")) {
            return Optional.empty();
        }

        DetailAST classIdentifierNode = classDefNode.findFirstToken(TokenTypes.LITERAL_CLASS);
        DetailAST classNameNode = CustomCheckUtils.findNextSiblingOfType(classIdentifierNode, node -> node.getType() == TokenTypes.IDENT);
        if (classNameNode == null) {
            classNameNode = CustomCheckUtils.findPreviousSiblingOfType(classIdentifierNode, node -> node.getType() == TokenTypes.IDENT);
        }

        // If the class name cannot be found we shouldn't attempt to run the check.
        if (classNameNode == null) {
            return Optional.empty();
        }

        return Optional.of(packageName + "." + classNameNode.getText());
    }

    /**
     * Checks if the constructor node has public or protected modifiers.
     * @param constructorNode Constructor node
     */
    private void checkConstructorIsHidden(DetailAST constructorNode) {
        DetailAST modifierNode = constructorNode.findFirstToken(TokenTypes.MODIFIERS);
        if (modifierNode == null) {
            return;
        }

        Optional<DetailAST> disallowedModifierNode = TokenUtil.findFirstTokenByPredicate(modifierNode, node -> {
            return node.getType() == TokenTypes.LITERAL_PUBLIC || node.getType() == TokenTypes.LITERAL_PROTECTED;
        });

        if (disallowedModifierNode.isPresent()) {
            log(constructorNode, CONSTRUCTOR_ERROR_MESSAGE);
        }
    }

    /**
     * Checks if the node is a method node, is public static, and is named builder.
     * @param node Child node of objBlock
     * @return True if the node is a method that is public static and is named builder
     */
    private boolean isMethodStaticBuilder(DetailAST node) {
        if (node.getType() != TokenTypes.METHOD_DEF) {
            return false;
        }

        DetailAST modifierNode = node.findFirstToken(TokenTypes.MODIFIERS);
        if (modifierNode == null) {
            return false;
        }

        if (modifierNode.findFirstToken(TokenTypes.LITERAL_STATIC) == null
            || modifierNode.findFirstToken(TokenTypes.LITERAL_PUBLIC) == null) {
            return false;
        }

        return node.findFirstToken(TokenTypes.LPAREN).getPreviousSibling().getText().equals(BUILDER_METHOD_NAME);
    }
}
