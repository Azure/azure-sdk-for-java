
package com.azure.tools.apiview.processor.model;

import java.util.EnumSet;
import java.util.Set;

public enum TokenKind {
    TEXT(StructuredTokenKind.TEXT),
//    NEW_LINE(LINE_BREAK),
//    WHITESPACE(NONE_BREAKING_SPACE),
//    TAB_SPACE(StructuredTokenKind.TAB_SPACE),
//    PARAMETER_SEPARATOR(StructuredTokenKind.PARAMETER_SEPARATOR),
    PUNCTUATION(StructuredTokenKind.PUNCTUATION, RenderClass.PUNCTUATION),
    KEYWORD(StructuredTokenKind.KEYWORD, RenderClass.KEYWORD),

    CLASS(StructuredTokenKind.TYPE_NAME, "class", /*RenderClass.TYPE_NAME,*/ RenderClass.CLASS),
    INTERFACE(StructuredTokenKind.TYPE_NAME, "interface", /*RenderClass.TYPE_NAME,*/ RenderClass.INTERFACE),
    ENUM(StructuredTokenKind.TYPE_NAME, "enum", /*RenderClass.TYPE_NAME,*/ RenderClass.ENUM),
    ANNOTATION(StructuredTokenKind.TYPE_NAME, "@annotation", /*RenderClass.TYPE_NAME,*/ RenderClass.ANNOTATION),
    MODULE_INFO(StructuredTokenKind.TYPE_NAME, /*RenderClass.TYPE_NAME,*/ RenderClass.MODULE_INFO),

    TYPE_NAME(StructuredTokenKind.TYPE_NAME, RenderClass.TYPE_NAME),
    STRING_LITERAL(StructuredTokenKind.STRING_LITERAL, RenderClass.STRING_LITERAL),
    NUMBER(StructuredTokenKind.TEXT, RenderClass.NUMBER),

    MODULE_REFERENCE(StructuredTokenKind.TYPE_NAME, RenderClass.MODULE_REFERENCE),

    PACKAGE_NAME(StructuredTokenKind.TYPE_NAME, /*RenderClass.TYPE_NAME,*/ RenderClass.PACKAGE_NAME),
    MODULE_NAME(StructuredTokenKind.TYPE_NAME, /*RenderClass.TYPE_NAME,*/ RenderClass.MODULE_NAME),

    ENUM_TYPE(StructuredTokenKind.TYPE_NAME, RenderClass.TYPE_NAME, RenderClass.ENUM_TYPE),
    ENUM_CONSTANT(StructuredTokenKind.LITERAL, RenderClass.TYPE_NAME, RenderClass.ENUM_CONSTANT),

    ANNOTATION_NAME(StructuredTokenKind.TYPE_NAME, RenderClass.TYPE_NAME, RenderClass.ANNOTATION_NAME),
    ANNOTATION_PARAMETER_NAME(StructuredTokenKind.TYPE_NAME, RenderClass.TYPE_NAME, RenderClass.ANNOTATION_PARAMETER_NAME),
    ANNOTATION_PARAMETER_VALUE(StructuredTokenKind.TYPE_NAME, RenderClass.TYPE_NAME, RenderClass.ANNOTATION_PARAMETER_VALUE),

    RETURN_TYPE(StructuredTokenKind.TYPE_NAME, RenderClass.TYPE_NAME, RenderClass.RETURN_TYPE),
    PARAMETER_TYPE(StructuredTokenKind.TYPE_NAME, RenderClass.TYPE_NAME, RenderClass.PARAMETER_TYPE),
    PARAMETER_NAME(StructuredTokenKind.TYPE_NAME, RenderClass.PARAMETER_NAME),
    EXTENDS_TYPE(StructuredTokenKind.TYPE_NAME, RenderClass.TYPE_NAME, RenderClass.EXTENDS_TYPE),
    IMPLEMENTS_TYPE(StructuredTokenKind.TYPE_NAME, RenderClass.TYPE_NAME, RenderClass.IMPLEMENTS_TYPE),

    METHOD_NAME(StructuredTokenKind.MEMBER_NAME, RenderClass.MEMBER_NAME, RenderClass.METHOD_NAME),
    FIELD_NAME(StructuredTokenKind.MEMBER_NAME, RenderClass.MEMBER_NAME, RenderClass.FIELD_NAME),

    MAVEN(StructuredTokenKind.KEYWORD, RenderClass.MAVEN),
    MAVEN_KEY(StructuredTokenKind.KEYWORD, RenderClass.KEYWORD, RenderClass.MAVEN_KEY),
    MAVEN_VALUE(StructuredTokenKind.TEXT, RenderClass.MAVEN_VALUE),
    MAVEN_DEPENDENCY(StructuredTokenKind.TEXT, RenderClass.MAVEN_VALUE, RenderClass.MAVEN_DEPENDENCY),

    JAVADOC(StructuredTokenKind.COMMENT, RenderClass.COMMENT, RenderClass.JAVADOC),

    // comment is a single line comment
    COMMENT(StructuredTokenKind.COMMENT, RenderClass.COMMENT);

    private final StructuredTokenKind structuredTokenKind;
    private final Set<RenderClass> renderClasses;
    private final String typeDeclarationString;

    TokenKind(StructuredTokenKind structuredTokenKind) {
        this.structuredTokenKind = structuredTokenKind;
        this.renderClasses = EnumSet.noneOf(RenderClass.class);
        this.typeDeclarationString = null;
    }

    TokenKind(StructuredTokenKind structuredTokenKind, RenderClass renderClass) {
        this.structuredTokenKind = structuredTokenKind;
        this.renderClasses = EnumSet.of(renderClass);
        this.typeDeclarationString = null;
    }

    TokenKind(StructuredTokenKind structuredTokenKind, RenderClass renderClass1, RenderClass renderClass2) {
        this.structuredTokenKind = structuredTokenKind;
        this.renderClasses = EnumSet.of(renderClass1, renderClass2);
        this.typeDeclarationString = null;
    }

    TokenKind(StructuredTokenKind structuredTokenKind, String typeDeclarationString, RenderClass renderClass) {
        this.structuredTokenKind = structuredTokenKind;
        this.typeDeclarationString = typeDeclarationString;
        this.renderClasses = EnumSet.of(renderClass);
    }

    public StructuredTokenKind getStructuredTokenKind() {
        return structuredTokenKind;
    }

    public int getTokenKindId() {
        return structuredTokenKind.getId();
    }

    public Set<RenderClass> getRenderClasses() {
        return renderClasses;
    }

    public String getTypeDeclarationString() {
        return typeDeclarationString;
    }

    private enum StructuredTokenKind {
        /**
         * Text: Token kind should be set as Text for any plan text token.
         * for e.g documentation, namespace value, or attribute or decorator tokens.
         **/
        TEXT(0),

        /** Punctuation **/
        PUNCTUATION(1),

        /** Keyword **/
        KEYWORD(2),

        /** TypeName: Kind should be set as TypeName for class definitions, base class token, parameter types etc **/
        TYPE_NAME(3),

        /** MemberName: Kind should be set as MemberName for method name tokens, member variable tokens **/
        MEMBER_NAME(4),

        /** StringLiteral: Token kind for any metadata or string literals to show in API view **/
        STRING_LITERAL(5),

        /** Literal: Token kind for any literals, for e.g. enum value or numerical constant literal or default value **/
        LITERAL(6),

        /** Comment: Comment text within the code that's really a documentation.
         *  Few languages wants to show comments within API review that's not tagged as documentation **/
        COMMENT(7);

        private final int id;

        StructuredTokenKind(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
