package com.azure.customcheckstyles;

import com.azure.common.ServiceClient;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

/**
 *
 */
public class ServiceClientSubclassCheck extends AbstractCheck {
    private static final String BUILDER_METHOD_NAME = "builder";

    private static final String CONSTRUCTOR_ERROR_MESSAGE = "Descendants of ServiceClient cannot have public or protected constructors.";
    private static final String BUILDER_ERROR_MESSAGE = "Descendants of ServiceClient must have a static method named builder.";

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
        return new int[] { TokenTypes.PACKAGE_DEF };
    }

    /**
     * Runs the check logic on the tokens we marked as needing to be checked in getRequiredTokens.
     * @param ast Abstract syntax tree node that will be checked.
     */
    @Override
    public void visitToken(DetailAST ast) {
        DetailAST classDefNode = CustomCheckUtils.findNextSiblingOfType(ast, (node) -> node.getType() == TokenTypes.CLASS_DEF);
        if (classDefNode == null) {
            return;
        }

        DetailAST objBlockNode = classDefNode.findFirstToken(TokenTypes.OBJBLOCK);
        String fullClassName = getFullClassName(ast, classDefNode);

        // Attempt to load the class and run the checks if it is a descendant of ServiceClient.
        try {
            Class<?> classToCheck = this.getClassLoader().loadClass(fullClassName);
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
     * @return Full class name, if the file isn't a class an empty string
     */
    private String getFullClassName(DetailAST packageDefNode, DetailAST classDefNode) {
        String packageName = FullIdent.createFullIdent(packageDefNode.findFirstToken(TokenTypes.DOT)).getText();
        DetailAST classIdentifierNode = classDefNode.findFirstToken(TokenTypes.LITERAL_CLASS);
        DetailAST classNameNode = CustomCheckUtils.findNextSiblingOfType(classIdentifierNode, (node) -> node.getType() == TokenTypes.IDENT);
        if (classNameNode == null) {
            classNameNode = CustomCheckUtils.findPreviousSiblingOfType(classIdentifierNode, (node) -> node.getType() == TokenTypes.IDENT);
        }

        if (classNameNode == null) {
            return "";
        }

        return packageName + "." + classNameNode.getText();
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

        if (CustomCheckUtils.hasAnyModifier(modifierNode, TokenTypes.LITERAL_PUBLIC, TokenTypes.LITERAL_PROTECTED)) {
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

        if (CustomCheckUtils.hasAllModifiers(modifierNode, TokenTypes.LITERAL_STATIC, TokenTypes.LITERAL_PUBLIC)) {
            return false;
        }

        return node.findFirstToken(TokenTypes.LPAREN).getPreviousSibling().getText().equals(BUILDER_METHOD_NAME);
    }
}
