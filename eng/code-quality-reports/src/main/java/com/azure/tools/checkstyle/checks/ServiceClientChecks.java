// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Verifies that subclasses of ServiceClient meet a set of guidelines.
 * <ol>
 *  <li>No public or protected constructors</li>
 *  <li>Implements a public static method named builder</li>
 * </ol>
 */
public class ServiceClientChecks extends AbstractCheck {
    private static final String BUILDER_METHOD_NAME = "builder";
    private static final String SERVICE_CLIENT_CLASS_NAME = "com.azure.common.ServiceClient";
    private static final String CLIENT = "Client";
    private static final String ASYNC_CLIENT = "AsyncClient";

    private static final String FAILED_TO_LOAD_MESSAGE = "%s class failed to load, ServiceClientChecks will be ignored.";
    private static final String CONSTRUCTOR_ERROR_MESSAGE = "Descendants of ServiceClient cannot have public or protected constructors.";
    private static final String BUILDER_ERROR_MESSAGE = "Descendants of ServiceClient must have a static method named builder.";
    private static final String SERVICE_INSTANCE_NAMING_ERR = "ServiceClient instance should be named either <SerivceName>Client or <ServiceName>AsyncClient";

    private static final int[] TOKENS = new int[] {
        TokenTypes.PACKAGE_DEF,
        TokenTypes.CTOR_DEF,
        TokenTypes.METHOD_DEF,
        TokenTypes.IMPORT,
        TokenTypes.LITERAL_NEW
    };

    private Class<?> serviceClientClass;
    private boolean extendsServiceClient;
    private boolean hasStaticBuilder;

    private static Set<String> castableSet = new HashSet<>();

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    /**
     * Array of tokens that trigger visitToken when the TreeWalker is traversing the AST.
     * @return The list of tokens that trigger visitToken.
     */
    @Override
    public int[] getRequiredTokens() {
        return TOKENS;
    }

    @Override
    public void init() {
        try {
            this.serviceClientClass = Class.forName(SERVICE_CLIENT_CLASS_NAME);
        } catch (ClassNotFoundException ex) {
            log(0, String.format(FAILED_TO_LOAD_MESSAGE, "ServiceClient"));
        }
    }

    /**
     * Start of the TreeWalker traversal.
     * @param rootAST Root of the AST.
     */
    @Override
    public void beginTree(DetailAST rootAST) {
        castableSet.clear();
        this.extendsServiceClient = false;
        this.hasStaticBuilder = false;
    }
    /**
     * Processes a token from the required tokens list when the TreeWalker visits it.
     * @param token Node in the AST.
     */
    @Override
    public void visitToken(DetailAST token) {
        // Failed to load ServiceClient's class, don't validate anything.
        if (this.serviceClientClass == null) {
            return;
        }

        switch (token.getType()) {
            case TokenTypes.PACKAGE_DEF:
                this.extendsServiceClient = extendsServiceClient(token);
                break;
            case TokenTypes.CTOR_DEF:
                if (this.extendsServiceClient && visibilityIsPublicOrProtected(token)) {
                    log(token, CONSTRUCTOR_ERROR_MESSAGE);
                }
                break;
            case TokenTypes.METHOD_DEF:
                if (this.extendsServiceClient && !this.hasStaticBuilder && methodIsStaticBuilder(token)) {
                    this.hasStaticBuilder = true;
                }
                break;
            case TokenTypes.IMPORT:
                collectAllServiceClientSubclass(token);
                break;
            case TokenTypes.LITERAL_NEW:
                isInstanceNamingCorrect(token);
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * End of the TreeWalker traversal.
     * @param rootAST Root of the AST.
     */
    @Override
    public void finishTree(DetailAST rootAST) {
        if (this.extendsServiceClient && !this.hasStaticBuilder) {
            log(0, BUILDER_ERROR_MESSAGE);
        }
    }

    /**
     * Determines if the class extends ServiceClient.
     * @param packageDefinitionToken Package definition token.
     * @return True if the package is not in "com.microsoft", the file is a class definition, and the class extends ServiceClient.
     */
    private boolean extendsServiceClient(DetailAST packageDefinitionToken) {
        String packageName = FullIdent.createFullIdent(packageDefinitionToken.findFirstToken(TokenTypes.DOT)).getText();
        if (packageName.startsWith("com.microsoft")) {
            return false;
        }

        DetailAST classDefinitionToken = packageDefinitionToken.findFirstToken(TokenTypes.CLASS_DEF);
        if (classDefinitionToken == null) {
            return false;
        }

        String className = classDefinitionToken.findFirstToken(TokenTypes.IDENT).getText();
        try {
            Class<?> clazz = Class.forName(packageName + "." + className);

            return this.serviceClientClass.isAssignableFrom(clazz);
        } catch (ClassNotFoundException ex) {
            log(classDefinitionToken, String.format(FAILED_TO_LOAD_MESSAGE, className));
            return false;
        }
    }

    /**
     * Checks if the constructor is using the public or protected scope.
     * @param constructorToken Construction token.
     * @return True if the constructor has a public or protected modifier token.
     */
    private boolean visibilityIsPublicOrProtected(DetailAST constructorToken) {
        DetailAST modifierToken = constructorToken.findFirstToken(TokenTypes.MODIFIERS);

        // No modifiers means package private.
        if (modifierToken == null) {
            return false;
        }

        return TokenUtil.findFirstTokenByPredicate(modifierToken,
            node -> node.getType() == TokenTypes.LITERAL_PUBLIC || node.getType() == TokenTypes.LITERAL_PROTECTED)
            .isPresent();
    }

    /**
     * Checks if the method node is public static and named builder.
     * @param methodToken Method node
     * @return True if the method is public static and is named builder
     */
    private boolean methodIsStaticBuilder(DetailAST methodToken) {
        DetailAST modifierToken = methodToken.findFirstToken(TokenTypes.MODIFIERS);
        if (modifierToken == null) {
            return false;
        }

        if (modifierToken.findFirstToken(TokenTypes.LITERAL_STATIC) == null
            || modifierToken.findFirstToken(TokenTypes.LITERAL_PUBLIC) == null) {
            return false;
        }

        return methodToken.findFirstToken(TokenTypes.IDENT).getText().equals(BUILDER_METHOD_NAME);
    }

    /**
     * All new Service Client instance should be named either as (ServiceName)Client
     * or (ServiceName)AsyncClient
     * @param literalNewAST the LITERAL_NEW AST node
     */
    private void isInstanceNamingCorrect(DetailAST literalNewAST) {
        String varName = literalNewAST.getParent().findFirstToken(TokenTypes.IDENT).getText();
        String varClassName = literalNewAST.findFirstToken(TokenTypes.IDENT).getText();
        if (varClassName.length() > CLIENT.length() && castableSet.contains(varClassName)) {
            int endingIdx = varClassName.indexOf(CLIENT);
            if (endingIdx == -1) {
                return;
            }
            String serviceName = varClassName.substring(0, endingIdx);
            if (!varName.startsWith(serviceName)
                ||!(varName.endsWith(CLIENT) || varName.endsWith(ASYNC_CLIENT))) {
                log(literalNewAST, SERVICE_INSTANCE_NAMING_ERR);
            }
        }
    }

    /**
     * Collect all implementation or subclass of ServiceClient interface
     * @param importAST The import AST node
     */
    private void collectAllServiceClientSubclass(DetailAST importAST) {
        FullIdent classNameFI = FullIdent.createFullIdentBelow(importAST);
        String classFullPath = classNameFI.getText();
        try {
            Class<?> importClass = Class.forName(classFullPath);
            if (this.serviceClientClass.isInstance(importClass)) {
                String className = lowerFirstLetter(importClass.getSimpleName());
                if (!className.isEmpty()) {
                    castableSet.add(className);
                }
            }
        }  catch (ClassNotFoundException ex) {
            log(importAST, String.format(FAILED_TO_LOAD_MESSAGE, classFullPath));
        }
    }

    private String lowerFirstLetter(String str) {
        StringBuilder sb = new StringBuilder();
        if (str != null && str.length() > 0) {
            sb.append(Character.toLowerCase(str.charAt(0)));
            sb.append(str.substring(1));
        }
        return sb.toString();
    }
}
