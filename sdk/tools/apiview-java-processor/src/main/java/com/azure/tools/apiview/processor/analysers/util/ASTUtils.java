package com.azure.tools.apiview.processor.analysers.util;

import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.ReviewLine;
import com.azure.tools.apiview.processor.model.traits.Parent;
import com.github.javaparser.Range;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Abstract syntax tree (AST) utility methods.
 */
public final class ASTUtils {

    /**
     * Attempts to get the package name for a compilation unit.
     *
     * @param cu The compilation unit.
     * @return An optional that may contain the package name for the compilation unit.
     */
    public static Optional<String> getPackageName(CompilationUnit cu) {
        return cu.getPackageDeclaration().map(PackageDeclaration::getNameAsString);
    }

    /**
     * Attempts to get the package name for a type declaration.
     * <p>
     * If the type declaration doesn't have a package name an empty string will be returned.
     *
     * @param typeDeclaration The type declaration.
     * @return The package name for the type declaration if it exists or an empty string if it doesn't.
     */
    public static String getPackageName(TypeDeclaration<?> typeDeclaration) {
        return typeDeclaration.getFullyQualifiedName()
            .map(str -> str.substring(0, str.lastIndexOf(".")))
            .orElse("");
    }

    /**
     * Gets the imports of a compilation unit.
     *
     * @param cu The compilation unit.
     * @return The imports of the compilation unit.
     */
    public static Stream<String> getImports(CompilationUnit cu) {
        return cu.getImports().stream().map(ImportDeclaration::getNameAsString);
    }

    /**
     * Attempts to get the primary type declaration name for the compilation unit.
     * <p>
     * The class name returned is based on the file name, using the expectation that the top-level type declaration
     * matches the file name.
     *
     * @param cu The compilation unit.
     * @return The primary type declaration name for the compilation unit.
     */
    public static Optional<String> getClassName(CompilationUnit cu) {
        return cu.getPrimaryTypeName();
    }

    /**
     * Gets all type declarations contained in the compilation unit.
     * <p>
     * Type declarations are classes, enums, and interfaces.
     *
     * @param cu The compilation unit.
     * @return All type declarations contained in the compilation unit.
     */
    public static Stream<TypeDeclaration<?>> getClasses(CompilationUnit cu) {
        // previously we simply return 'cu.getTypes().stream()', but this had the effect of not returning all inner
        // types, as was expected. This is because the 'getTypes()' method only returns the top-level types, and not
        // member types. To fix this, we now return a stream of all types, including member types.
        return Stream.concat(
                cu.getTypes().stream(), // top-level types
                cu.getTypes().stream()  // member types
                     .flatMap(type -> type.getMembers().stream()
                          .filter(member -> member instanceof TypeDeclaration<?>)
                          .map(member -> (TypeDeclaration<?>) member)));
    }

    /**
     * Gets all public API constructors contained in a compilation unit.
     *
     * @param cu The compilation unit.
     * @return All public API constructors contained in the compilation unit.
     */
    public static Stream<ConstructorDeclaration> getPublicOrProtectedConstructors(CompilationUnit cu) {
        return getClasses(cu).flatMap(ASTUtils::getPublicOrProtectedConstructors);
    }

    /**
     * Gets all public API constructors contained in a type declaration.
     *
     * @param typeDeclaration The type declaration.
     * @return All public API constructors contained in the type declaration.
     */
    public static Stream<ConstructorDeclaration> getPublicOrProtectedConstructors(TypeDeclaration<?> typeDeclaration) {
        return typeDeclaration.getMembers().stream()
            .filter(member -> member instanceof ConstructorDeclaration)
            .map(member -> (ConstructorDeclaration) member)
            .filter(type -> isPublicOrProtected(type.getAccessSpecifier()));
    }

    /**
     * Gets all public API methods contained in a compilation unit.
     *
     * @param cu The compilation unit.
     * @return All public API methods contained in the compilation unit.
     */
    public static Stream<MethodDeclaration> getPublicOrProtectedMethods(CompilationUnit cu) {
        return getClasses(cu).flatMap(ASTUtils::getPublicOrProtectedMethods);
    }

    /**
     * Gets all public API methods contained in a type declaration.
     *
     * @param typeDeclaration The type declaration.
     * @return All public API methods contained in the type declaration.
     */
    public static Stream<MethodDeclaration> getPublicOrProtectedMethods(TypeDeclaration<?> typeDeclaration) {
        return typeDeclaration.getMembers().stream()
            .filter(member -> member instanceof MethodDeclaration)
            .map(member -> (MethodDeclaration) member)
            .filter(type -> isPublicOrProtected(type.getAccessSpecifier()));
    }

    /**
     * Gets all public API fields contained in a compilation unit.
     *
     * @param cu The compilation unit.
     * @return All public API fields contained in the compilation unit.
     */
    public static Stream<FieldDeclaration> getPublicOrProtectedFields(CompilationUnit cu) {
        return getClasses(cu).flatMap(ASTUtils::getPublicOrProtectedFields);
    }

    /**
     * Gets all public API fields contained in a type declaration.
     *
     * @param typeDeclaration The type declaration.
     * @return All public API fields contained in the type declaration.
     */
    public static Stream<FieldDeclaration> getPublicOrProtectedFields(TypeDeclaration<?> typeDeclaration) {
        return typeDeclaration.getMembers().stream()
            .filter(member -> member instanceof FieldDeclaration)
            .map(member -> (FieldDeclaration) member)
            .filter(type -> isPublicOrProtected(type.getAccessSpecifier()));
    }

    /**
     * Determines whether the access specifier is public or protected.
     *
     * @param accessSpecifier The access specifier.
     * @return Whether the access specifier is public or protected.
     */
    public static boolean isPublicOrProtected(AccessSpecifier accessSpecifier) {
        return (accessSpecifier == AccessSpecifier.PUBLIC) || (accessSpecifier == AccessSpecifier.PROTECTED);
    }

    /**
     * Determines whether the access specifier is package-private or private.
     *
     * @param accessSpecifier The access specifier.
     * @return Whether the access specifier is package-private or private.
     */
    public static boolean isPrivateOrPackagePrivate(AccessSpecifier accessSpecifier) {
        return (accessSpecifier == AccessSpecifier.PRIVATE) || (accessSpecifier == AccessSpecifier.NONE);
    }

    public static String makeId(CompilationUnit cu) {
        return makeId(cu.getPrimaryType().get());
    }

    public static String makeId(Node node) {
        // switch based on known subtypes
        if (node instanceof TypeDeclaration<?>) {
            return makeId((TypeDeclaration<?>) node);
        } else if (node instanceof AnnotationMemberDeclaration) {
            return makeId((AnnotationMemberDeclaration) node);
        } else if (node instanceof CallableDeclaration<?>) {
            return makeId((CallableDeclaration<?>) node);
        } else if (node instanceof FieldDeclaration) {
            return makeId((FieldDeclaration) node);
        } else if (node instanceof EnumConstantDeclaration) {
            return makeId((EnumConstantDeclaration) node);
        } else {
            return makeId(node.toString());
        }
    }

    public static String makeId(TypeDeclaration<?> typeDeclaration) {
        return makeId(typeDeclaration.getFullyQualifiedName().get());
    }

    public static String makeId(AnnotationMemberDeclaration annotationMemberDeclaration) {
        return makeId(getNodeFullyQualifiedName(annotationMemberDeclaration.getParentNode()) + "." + annotationMemberDeclaration.getName());
    }

    public static String makeId(VariableDeclarator variableDeclarator) {
        return makeId(getNodeFullyQualifiedName(variableDeclarator.getParentNode()) + "." + variableDeclarator.getNameAsString());
    }

    public static String makeId(CallableDeclaration<?> callableDeclaration) {
        return makeId(getNodeFullyQualifiedName(callableDeclaration.getParentNode()) + "." + callableDeclaration.getDeclarationAsString());
    }

    public static String makeId(FieldDeclaration fieldDeclaration) {
        return makeId(fieldDeclaration.getVariables().get(0));
    }

    public static String makeId(EnumConstantDeclaration enumDeclaration) {
        return makeId(getNodeFullyQualifiedName(enumDeclaration.getParentNode()) + "." + enumDeclaration.getNameAsString());
    }

    public static String makeId(String fullPath) {
        // Previously, this used a regex to replace '"' and ' ' with '-', that wasn't necessary. The replacement pattern
        // is simple and can be replaced with a simple loop. This is a performance optimization.
        //
        // The logic is that we iterate over the string, if no replacements are needed we return the original string.
        // Otherwise, we create a new StringBuilder the size of the string being replaced, as the replacement size is
        // the same as the search size, and we append the parts of the string that don't need to be replaced, and the
        // replacement character for the parts that do. At the end of the loop, we append the last part of the string
        // that doesn't need to be replaced and return the final string.
        if (fullPath == null || fullPath.isEmpty()) {
            return fullPath;
        }

        StringBuilder sb = null;
        int prevStart = 0;

        int length = fullPath.length();
        for (int i = 0; i < length; i++) {
            char c = fullPath.charAt(i);
            if (c == '"' || c == ' ') {
                if (sb == null) {
                    sb = new StringBuilder(length);
                }

                if (prevStart != i) {
                    sb.append(fullPath, prevStart, i);
                }

                sb.append('-');
                prevStart = i + 1;
            }
        }

        if (sb == null) {
            return fullPath;
        }

        sb.append(fullPath, prevStart, length);
        return sb.toString();
    }

    public static String makeId(AnnotationExpr annotation, NodeWithAnnotations<?> nodeWithAnnotations) {
        String annotationContext = getAnnotationContext(nodeWithAnnotations);

        String idSuffix = "";
        if (annotationContext != null && !annotationContext.isEmpty()) {
            idSuffix = "-" + annotationContext;
        }
        return makeId(getNodeFullyQualifiedName(annotation.getParentNode()) + "." + annotation.getNameAsString() + idSuffix);
    }

    private static String getAnnotationContext(NodeWithAnnotations<?> nodeWithAnnotations) {
        if (nodeWithAnnotations instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) nodeWithAnnotations;
            return methodDeclaration.getDeclarationAsString(true, true, true);
        } else if (nodeWithAnnotations instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) nodeWithAnnotations;
            return classOrInterfaceDeclaration.getNameAsString();
        } else if (nodeWithAnnotations instanceof EnumDeclaration) {
            EnumDeclaration enumDeclaration = (EnumDeclaration) nodeWithAnnotations;
            return enumDeclaration.getNameAsString();
        } else if (nodeWithAnnotations instanceof EnumConstantDeclaration) {
            EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) nodeWithAnnotations;
            return enumConstantDeclaration.getNameAsString();
        } else if (nodeWithAnnotations instanceof ConstructorDeclaration) {
            ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) nodeWithAnnotations;
            return constructorDeclaration.getDeclarationAsString(true, true, true);
        } else {
            return "";
        }
    }

    /**
     * Determines whether the type declaration is a public API (public or protected).
     * <p>
     * This handles inner classes and interfaces by also checking the surrounding type for being a public API as well.
     *
     * @param type The type declaration.
     * @return Whether the type declaration is a public API.
     */
    public static boolean isTypeAPublicAPI(TypeDeclaration<?> type) {
        Node parentNode = type.getParentNode().orElse(null);
        final boolean isTypePrivate = isPrivateOrPackagePrivate(type.getAccessSpecifier());

        // When we have no parent use the access modifier on the given type.
        if (parentNode == null) {
            return !isTypePrivate;
        }

        // otherwise there are more rules we want to consider...
        final boolean isInterfaceType = isInterfaceType(type);

        if (parentNode instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration parentClass = (ClassOrInterfaceDeclaration) parentNode;
            boolean isInPublicParent = isPublicOrProtected(parentClass.getAccessSpecifier());
            boolean isParentAnInterface = isInterfaceType(parentClass);

            /*
             * 1) If the parent type is a non-public non-interface type this type is not part of the public API.
             */
            if (!isInPublicParent && !isParentAnInterface) {
                return false;
            }

            /*
             * 2) If the parent type is a public interface then this type is part of the public API.
             */
            if (isInPublicParent && isParentAnInterface) {
                return true;
            }

            // 3) If the type is an non-public interface, but all parent types are either public or also interfaces,
            // then this type is public EXCEPT if the root type is a private interface.
            if (isTypePrivate && isInterfaceType) {
                // work way up parent chain, checking along the way
                while (parentClass != null) {
                    if (isInPublicParent && !isParentAnInterface) {
                        // we are looking at a public parent *class*. This means we have a private interface in a public
                        // class, so we know we have non-public API here
                        return false;
                    }

                    if (isParentAnInterface || isInPublicParent) {
                        parentNode = parentClass.getParentNode().orElse(null);
                        if (parentNode == null || parentNode instanceof CompilationUnit) {
                            // we have reached the top of the hierarchy, lets now determine if the previous type
                            // was public - if so we return true
                            return isInPublicParent;
                        }
                        parentClass = (ClassOrInterfaceDeclaration) parentNode;
                        isInPublicParent = isPublicOrProtected(parentClass.getAccessSpecifier());
                        isParentAnInterface = parentClass.isInterface();
                    } else {
                        return false;
                    }
                }
            }
        }

        // 3) otherwise, we return based on whether the type itself is public or not
        return !isTypePrivate;
    }

    /**
     * Returns true if the type is a public interface.
     */
    public static boolean isInterfaceType(TypeDeclaration<?> type) {
        if (type.isClassOrInterfaceDeclaration()) {
            return type.asClassOrInterfaceDeclaration().isInterface();
        }
        return false;
    }

    public static boolean isTypeImplementingInterface(TypeDeclaration<?> type, String interfaceName) {
        return type.asClassOrInterfaceDeclaration().getImplementedTypes().stream()
                .anyMatch(_interface -> _interface.getNameAsString().equals(interfaceName));
    }

    public static String getNodeFullyQualifiedName(Node node) {
        if (node == null) {
            throw new NullPointerException("node cannot be null");
        }

        if (node instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit) node;
            String packageName = cu.getPackageDeclaration()
                    .map(PackageDeclaration::getNameAsString)
                    .orElse("");
            String typeName = cu.getPrimaryType()
                    .map(NodeWithSimpleName::getNameAsString)
                    .orElse("");
            return packageName.isEmpty() ? typeName : packageName + "." + typeName;
        }

        if (node instanceof TypeDeclaration<?>) {
            return ((TypeDeclaration<?>)node).getFullyQualifiedName().orElse("");
        }

        if (node instanceof CallableDeclaration<?>) {
            CallableDeclaration<?> callableDeclaration = (CallableDeclaration<?>) node;
            return getNodeFullyQualifiedName(callableDeclaration.getParentNode().orElse(null)) + "." + callableDeclaration.getSignature();
        }

        if (node instanceof FieldDeclaration) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
            return getNodeFullyQualifiedName(fieldDeclaration.getParentNode().orElse(null)) + "." + fieldDeclaration.getVariables().get(0).getNameAsString();
        }

        if (node instanceof EnumConstantDeclaration) {
            EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) node;
            return getNodeFullyQualifiedName(enumConstantDeclaration.getParentNode().orElse(null)) + "." + enumConstantDeclaration.getNameAsString();
        }

        if (node instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) node;
            if (classOrInterfaceType.getScope().isPresent()) {
                return getNodeFullyQualifiedName(classOrInterfaceType.getScope().get()) + "." + classOrInterfaceType.getNameAsString();
            } else {
                return classOrInterfaceType.getNameAsString();
            }
        }

        if (node instanceof NodeWithSimpleName<?>) {
            NodeWithSimpleName<?> nodeWithSimpleName = (NodeWithSimpleName<?>) node;
            return getNodeFullyQualifiedName(node.getParentNode().orElse(null)) + "." + nodeWithSimpleName.getNameAsString();
        }

        if (node instanceof SimpleName) {
            return ((SimpleName)node).getIdentifier();
        }

        if (node instanceof PrimitiveType) {
            return ((PrimitiveType)node).toString();
        }

        throw new IllegalArgumentException("Unsupported node type: " + node.getClass().getName());
    }

    private static String getNodeFullyQualifiedName(Optional<Node> nodeOptional) {
        return nodeOptional.map(ASTUtils::getNodeFullyQualifiedName).orElse("");
    }

    /**
     * Attempts to retrieve the {@link JavadocComment} for a given {@link BodyDeclaration}.
     * <p>
     * If the {@link BodyDeclaration} is an instance of {@link NodeWithJavadoc}, it will be checked for having an
     * existing {@link NodeWithJavadoc#getJavadocComment()}.
     * <p>
     * If the {@link BodyDeclaration} isn't an instance of {@link NodeWithJavadoc} or doesn't have an existing {@link
     * JavadocComment} the {@link BodyDeclaration} will be inspected for having a "detached Javadoc". Detached Javadoc
     * comments can occur when there is comment lines between the Javadoc and the body. This is resolved by walking up
     * the comments (reading earlier line numbers) until the previous line is no longer a comment line. Then the
     * orphaned Javadoc comments for the file are checked for containing the current line number, if so that Javadoc
     * comment is used as the Javadoc comment for the body declaration.
     *
     * @param bodyDeclaration The {@link BodyDeclaration} whose Javadoc comment is being found.
     * @return An {@link Optional} either containing the Javadoc comment for the body declaration or {@link
     * Optional#empty()}.
     */
    public static Optional<JavadocComment> attemptToFindJavadocComment(BodyDeclaration<?> bodyDeclaration) {
        if (!(bodyDeclaration instanceof NodeWithJavadoc<?>)) {
            return Optional.empty();
        }
        NodeWithJavadoc<?> nodeWithJavadoc = (NodeWithJavadoc<?>) bodyDeclaration;

        // BodyDeclaration has a Javadoc.
        if (nodeWithJavadoc.getJavadocComment().isPresent()) {
            return nodeWithJavadoc.getJavadocComment();
        }

        // Get the orphaned comments.
        List<Comment> orphanedComments = bodyDeclaration.getParentNode()
            .map(Node::getOrphanComments)
            .orElse(Collections.emptyList());

        // Traverse up the comments between the Javadoc and the body declaration.
        Optional<Comment> commentTraversalNode = bodyDeclaration.getComment();
        Comment comment = null;
        while (commentTraversalNode.isPresent()) {
            comment = commentTraversalNode.get();
            commentTraversalNode = comment.getComment();
        }

        if (comment == null) {
            return Optional.empty();
        }

        // Check if there are any orphaned Javadoc comments before the additional code comments.
        Range expectedJavadocRangeOverlap = comment.getRange().get()
            .withBeginLine(comment.getRange().get().begin.line - 1);

        return orphanedComments.stream()
            .filter(c -> c instanceof JavadocComment)
            .map(c -> (JavadocComment) c)
            .filter(c -> c.getRange().map(range -> range.overlapsWith(expectedJavadocRangeOverlap)).orElse(false))
            .findFirst();
    }

    public static Optional<ReviewLine> findReviewLine(APIListing listing, Function<ReviewLine, Boolean> f) {
        return findReviewLine((Parent)listing, f);
    }

    private static Optional<ReviewLine> findReviewLine(Parent parent, Function<ReviewLine, Boolean> f) {
        for (ReviewLine line : parent.getChildren()) {
            if (f.apply(line)) {
                return Optional.of(line);
            }
        }

        // check each child
        for (ReviewLine line : parent.getChildren()) {
            Optional<ReviewLine> result = findReviewLine(line, f);
            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    private ASTUtils() {
    }
}
