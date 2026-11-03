// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks public API signatures for implementation-package types and unapproved external dependencies.
 * Implementation packages are excluded. Java and Javax dependencies are always allowed.
 * All violations are reported under this rule's name.
 */
public class PublicApiTypeCheck extends ImplementationExcludingCheck {
    private static final String ALTERNATIVE_MOVE_TO_PUBLIC_API = "Alternatively, it can be removed from the "
        + "implementation package and made public API, after appropriate API review.";
    static final String TYPE_PARAM_TYPE_ERROR = "\"%s\" is in an implementation package and should not be used as a "
        + "type parameter type in public API. " + ALTERNATIVE_MOVE_TO_PUBLIC_API;
    static final String IMPLEMENTS_TYPE_ERROR = "\"%s\" is in an implementation package and should not be implemented "
        + "by a type in public API. " + ALTERNATIVE_MOVE_TO_PUBLIC_API;
    static final String EXTENDS_TYPE_ERROR = "\"%s\" is in an implementation package and should not be extended by a "
        + "type in public API. " + ALTERNATIVE_MOVE_TO_PUBLIC_API;
    static final String PARAM_TYPE_ERROR = "\"%s\" is in an implementation package and should not be used as a "
        + "parameter type in public API. " + ALTERNATIVE_MOVE_TO_PUBLIC_API;
    static final String RETURN_TYPE_ERROR = "\"%s\" is in an implementation package and should not be a return type "
        + "for public API. " + ALTERNATIVE_MOVE_TO_PUBLIC_API;
    static final String EXTERNAL_DEPENDENCY_ERROR
        = "Class ''%s'', is a class from external dependency. You should not use it as a %s type.";

    private static final Pattern IMPLEMENTATION_CLASS = Pattern.compile(".*?\\.implementation.*?\\.(\\w+)");
    private static final int[] TOKENS = {
        TokenTypes.IMPORT,
        TokenTypes.CLASS_DEF,
        TokenTypes.INTERFACE_DEF,
        TokenTypes.ENUM_DEF,
        TokenTypes.METHOD_DEF };

    private final Set<String> implementationTypes = new HashSet<>();
    private final Set<String> externalTypes = new HashSet<>();
    private String[] allowedDependencyPrefixes = new String[0];
    private boolean checkImplementationTypes = true;
    private boolean checkExternalDependencies = true;

    /** Creates a check with both public API policies enabled. */
    public PublicApiTypeCheck() {
    }

    /**
     * Sets whether implementation-package types are rejected. Enabled by default.
     *
     * @param checkImplementationTypes whether to check implementation types.
     */
    public final void setCheckImplementationTypes(boolean checkImplementationTypes) {
        this.checkImplementationTypes = checkImplementationTypes;
    }

    /**
     * Sets whether unapproved external dependencies are rejected. Enabled by default.
     *
     * @param checkExternalDependencies whether to check external dependencies.
     */
    public final void setCheckExternalDependencies(boolean checkExternalDependencies) {
        this.checkExternalDependencies = checkExternalDependencies;
    }

    /**
     * Sets allowed dependency prefixes, normally ending in a dot. Java and Javax are always allowed.
     *
     * @param allowedDependencyPrefixes the allowed fully qualified dependency prefixes.
     */
    public final void setAllowedDependencyPrefixes(String... allowedDependencyPrefixes) {
        if (allowedDependencyPrefixes != null) {
            this.allowedDependencyPrefixes = Arrays.copyOf(allowedDependencyPrefixes, allowedDependencyPrefixes.length);
        }
    }

    @Override
    public int[] getTokensForCheck() {
        return TOKENS;
    }

    @Override
    public void beforeTree(DetailAST rootAST) {
        implementationTypes.clear();
        externalTypes.clear();
    }

    @Override
    public void processToken(DetailAST token) {
        if (token.getType() == TokenTypes.IMPORT) {
            classifyImport(token);
        } else if (token.getType() == TokenTypes.METHOD_DEF) {
            if (isPublicApi(token) && !isInStaticInitializer(token)) {
                checkSignatureType(token.findFirstToken(TokenTypes.TYPE), false);
                TokenUtil.forEachChild(token.findFirstToken(TokenTypes.PARAMETERS), TokenTypes.PARAMETER_DEF,
                    parameter -> checkSignatureType(parameter.findFirstToken(TokenTypes.TYPE), true));
            }
        } else if (checkImplementationTypes && isPublicApi(token)) {
            CheckUtil.getTypeParameters(token).forEach(this::checkTypeParameter);
            checkInheritance(token.findFirstToken(TokenTypes.EXTENDS_CLAUSE), EXTENDS_TYPE_ERROR);
            checkInheritance(token.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE), IMPLEMENTS_TYPE_ERROR);
        }
    }

    private void classifyImport(DetailAST token) {
        String qualifiedName = FullIdent.createFullIdentBelow(token).getText();
        String simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
        if (checkImplementationTypes) {
            Matcher matcher = IMPLEMENTATION_CLASS.matcher(qualifiedName);
            if (matcher.matches()) {
                implementationTypes.add(matcher.group(1));
            }
        }
        if (checkExternalDependencies) {
            if (isAllowedDependency(qualifiedName)) {
                externalTypes.remove(simpleName);
            } else {
                externalTypes.add(simpleName);
            }
        }
    }

    private boolean isAllowedDependency(String qualifiedName) {
        if (qualifiedName.startsWith("java.") || qualifiedName.startsWith("javax.")) {
            return true;
        }
        for (String prefix : allowedDependencyPrefixes) {
            if (qualifiedName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPublicApi(DetailAST token) {
        Scope scope = ScopeUtil.getScope(token);
        if (scope != Scope.PUBLIC && scope != Scope.PROTECTED) {
            return false;
        }
        Scope surrounding = ScopeUtil.getSurroundingScope(token);
        return surrounding == null || surrounding == Scope.PUBLIC || surrounding == Scope.PROTECTED;
    }

    private static boolean isInStaticInitializer(DetailAST token) {
        for (DetailAST parent = token.getParent(); parent != null; parent = parent.getParent()) {
            if (parent.getType() == TokenTypes.STATIC_INIT) {
                return true;
            }
        }
        return false;
    }

    private void checkSignatureType(DetailAST type, boolean parameter) {
        String name = FullIdent.createFullIdentBelow(type).getText();
        if (checkImplementationTypes && isImplementationType(name)) {
            log(type, String.format(parameter ? PARAM_TYPE_ERROR : RETURN_TYPE_ERROR, name));
        }
        if (checkExternalDependencies) {
            checkExternalTypes(type, parameter ? "method argument" : "return", !parameter);
        }
    }

    // Walk type nodes directly and report immediately; no per-method result maps or repeated prefix scans.
    private void checkExternalTypes(DetailAST type, String position, boolean returnRoot) {
        if (returnRoot && type.findFirstToken(TokenTypes.IDENT) == null) {
            return;
        }
        for (DetailAST child = type.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == TokenTypes.IDENT && externalTypes.contains(child.getText())) {
                log(returnRoot ? type : child, String.format(EXTERNAL_DEPENDENCY_ERROR, child.getText(), position));
            } else if (child.getType() == TokenTypes.TYPE_ARGUMENT || child.getType() == TokenTypes.TYPE_ARGUMENTS) {
                checkExternalTypes(child, position, false);
            }
        }
    }

    private void checkTypeParameter(DetailAST parameter) {
        DetailAST upper = parameter.findFirstToken(TokenTypes.TYPE_UPPER_BOUNDS);
        DetailAST lower = parameter.findFirstToken(TokenTypes.TYPE_LOWER_BOUNDS);
        if (upper != null) {
            checkTypeParameterName(parameter, upper);
        }
        if (lower != null) {
            checkTypeParameterName(parameter, lower);
        }
        checkTypeParameterName(parameter, parameter);
    }

    private void checkTypeParameterName(DetailAST parameter, DetailAST type) {
        String name = FullIdent.createFullIdentBelow(type).getText();
        if (isImplementationType(name)) {
            log(parameter, String.format(TYPE_PARAM_TYPE_ERROR, name));
        }
    }

    private void checkInheritance(DetailAST clause, String message) {
        if (clause != null) {
            TokenUtil.forEachChild(clause, TokenTypes.IDENT, type -> {
                String name = FullIdent.createFullIdent(type).getText();
                if (isImplementationType(name)) {
                    log(type, String.format(message, name));
                }
            });
        }
    }

    private boolean isImplementationType(String name) {
        return implementationTypes.contains(name)
            || (name.indexOf('.') >= 0 && IMPLEMENTATION_CLASS.matcher(name).matches());
    }
}
