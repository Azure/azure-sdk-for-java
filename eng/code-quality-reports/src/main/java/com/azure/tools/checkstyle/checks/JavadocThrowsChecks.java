package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.DetailNodeTreeStringPrinter;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class JavadocThrowsChecks extends AbstractCheck {
    private static final String MISSING_DESCRIPTION_MESSAGE = "Expected @throws tag to have a description explaining when the error is thrown.";
    private static final String MISSING_THROWS_TAG_MESSAGE = "Expected Javadoc @throws tag explaining when the error is thrown.";
    private static final int[] TOKENS = new int[] { TokenTypes.METHOD_DEF };

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
    public void visitToken(DetailAST methodToken) {
        DetailAST modifierToken = methodToken.findFirstToken(TokenTypes.MODIFIERS);

        // Only check throws documentation when the method is part of the public API and is implemented.
        if (!visibilityIsPublicOrProtected(modifierToken) || modifierToken.findFirstToken(TokenTypes.ABSTRACT) != null) {
            return;
        }

        verifyDocumentedMethodThrows(methodToken, findJavadocThrows(modifierToken));
    }

    private boolean visibilityIsPublicOrProtected(DetailAST modifierToken) {
        if (modifierToken == null) {
            return false;
        }

        return TokenUtil.findFirstTokenByPredicate(modifierToken,
            node -> node.getType() == TokenTypes.LITERAL_PUBLIC || node.getType() == TokenTypes.LITERAL_PROTECTED)
            .isPresent();
    }

    /**
     * Attempts to find and parse the Javadoc for the method then finds all the documented @throws statements.
     * @param modifierToken Modifier token of the method that contains the Javadoc.
     * @return Set of throws that are documented for the method.
     */
    private Set<String> findJavadocThrows(DetailAST modifierToken) {
        HashSet<String> javadocThrows = new HashSet<>();

        // Check for the block comment begin, this contains the Javadoc for the method.
        DetailAST blockCommentToken = modifierToken.findFirstToken(TokenTypes.BLOCK_COMMENT_BEGIN);
        if (blockCommentToken == null) {
            return javadocThrows;
        }

        // Turn the DetailAST into a Javadoc DetailNode.
        DetailNode javadocNode = DetailNodeTreeStringPrinter.parseJavadocAsDetailNode(blockCommentToken);
        if (javadocNode == null) {
            return  javadocThrows;
        }

        // Iterate through all the top level nodes in the Javadoc, looking for the @throws statements.
        for (DetailNode node : javadocNode.getChildren()) {
            if (node.getType() != JavadocTokenTypes.JAVADOC_TAG || JavadocUtil.findFirstToken(node, JavadocTokenTypes.THROWS_LITERAL) == null) {
                continue;
            }

            // Add the class being thrown to the set of documented throws.
            javadocThrows.add(JavadocUtil.findFirstToken(node, JavadocTokenTypes.CLASS_NAME).getText());

            if (JavadocUtil.findFirstToken(node, JavadocTokenTypes.DESCRIPTION) == null) {
                log(node.getLineNumber(), MISSING_DESCRIPTION_MESSAGE);
            }
        }

        return javadocThrows;
    }

    /**
     * Traverses the method looking for all throw instances and verifies that the Javadoc has the throw type documented.
     * @param methodToken Method definition node.
     * @param javadocThrows Set of throws documented in the Javadoc.
     */
    private void verifyDocumentedMethodThrows(DetailAST methodToken, Set<String> javadocThrows) {
        HashMap<String, String> exceptionInstantiationTypes = new HashMap<>();

        DetailAST methodBodyToken = methodToken.findFirstToken(TokenTypes.SLIST);
        TokenUtil.forEachChild(methodBodyToken, TokenTypes.LITERAL_THROW, (throwToken) -> {
            DetailAST throwingToken = throwToken.findFirstToken(TokenTypes.EXPR).getFirstChild();
            if (throwingToken.getType() == TokenTypes.LITERAL_NEW) {
                if (!javadocThrows.contains(throwingToken.getFirstChild().getText())) {
                    log(throwingToken, MISSING_THROWS_TAG_MESSAGE);
                }
            } else {
                log(throwToken, String.format("I'm throwing %s", throwingToken.getText()));
            }
        });

        // The structure of the throw is LITERAL_THROW then EXPR
        // If the EXPR has a LITERAL_NEW the first child of the new is the exception type.
        // If the EXPR doesn't have a LITERAL_NEW the first child is the IDENT which is the name of the variable.
        // - This is the trickier situation as the AST needs to get traversed upwards to find all instantiations of the exception.

    }

    private void verifyDocumentMethodThrowsHelper(DetailAST node, Set<String> javadocThrows) {

    }
}
