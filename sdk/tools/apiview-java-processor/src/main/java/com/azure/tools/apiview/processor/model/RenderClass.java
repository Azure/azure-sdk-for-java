package com.azure.tools.apiview.processor.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum RenderClass {
    ASSEMBLY("assembly", "java-assembly"),

    CLASS("java-class", "class"),
    INTERFACE("java-interface", "interface"),
    ENUM("java-enum", "enum"),
    ANNOTATION("java-annotation", "annotation"),
    MODULE_INFO("java-module", "moduleInfo"),

    MODULE_REFERENCE("moduleReference"),

    PUNCTUATION("punctuation"),
    TYPE_NAME("typeName"),
    KEYWORD("keyword"),
    STRING_LITERAL("stringLiteral"),
    NUMBER("number"),
    PACKAGE_NAME("java-package", "package", "packageName"),
    MODULE_NAME("java-module", "moduleName"),
    ENUM_TYPE("enumType"),
    ENUM_CONSTANT("enumConstant"),
    ANNOTATION_NAME("annotationName"),
    ANNOTATION_PARAMETER_NAME("annotationParameterName"),
    ANNOTATION_PARAMETER_VALUE("annotationParameterValue"),
    RETURN_TYPE("returnType"),
    PARAMETER_TYPE("parameterType"),
    PARAMETER_NAME("parameterName"),
    EXTENDS_TYPE("extendsType"),
    IMPLEMENTS_TYPE("implementsType"),
    MEMBER_NAME("memberName"),
    METHOD_NAME("methodName"),
    FIELD_NAME("fieldName"),
    MAVEN("java-maven", "maven"),
    MAVEN_KEY("mavenKey"),
    MAVEN_VALUE("mavenValue"),
    MAVEN_DEPENDENCY("dependency"),
    COMMENT("comment"),
    JAVADOC("javadoc"),
    DEPRECATED("deprecated");

    private final List<String> values;

    RenderClass(String... values) {
        this.values = Collections.unmodifiableList(Arrays.asList(values));
    }

    public List<String> getValues() {
        return values;
    }
}
