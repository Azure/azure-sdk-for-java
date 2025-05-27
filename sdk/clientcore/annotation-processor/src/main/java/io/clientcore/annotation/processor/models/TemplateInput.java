// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.models;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.type.Type;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Represents the input required for generating a template.
 */
public class TemplateInput {

    /**
     * Initializes a new instance of the {@link TemplateInput} class.
     */
    public TemplateInput() {
    }

    // A map of fully qualified class names to their short names
    private final Map<String, String> imports = new TreeMap<>();

    private String packageName;
    private String serviceInterfaceFQN;
    private String serviceInterfaceShortName;
    private String serviceInterfaceImplShortName;
    private String host;
    private List<HttpRequestContext> httpRequestContexts;
    private List<UnexpectedResponseExceptionDetail> unexpectedResponseExceptionDetails;

    /**
     * Gets the host.
     *
     * @return the host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host.
     *
     * @param host the host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the imports map.
     *
     * @return the imports map.
     */
    public Map<String, String> getImports() {
        return imports;
    }

    /**
     * Gets the package name.
     *
     * @return the package name.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the package name.
     *
     * @param packageName the package name to set.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Gets the short name of the service interface.
     *
     * @return the short name of the service interface.
     */
    public String getServiceInterfaceShortName() {
        return serviceInterfaceShortName;
    }

    /**
     * Sets the short name of the service interface.
     *
     * @param serviceInterfaceShortName the short name of the service interface to set.
     */
    public void setServiceInterfaceShortName(String serviceInterfaceShortName) {
        this.serviceInterfaceShortName = serviceInterfaceShortName;
    }

    /**
     * Gets the short name of the service interface implementation.
     *
     * @return the short name of the service interface implementation.
     */
    public String getServiceInterfaceImplShortName() {
        return serviceInterfaceImplShortName;
    }

    /**
     * Sets the short name of the service interface implementation.
     *
     * @param serviceInterfaceImplShortName the short name of the service interface implementation to set.
     */
    public void setServiceInterfaceImplShortName(String serviceInterfaceImplShortName) {
        this.serviceInterfaceImplShortName = serviceInterfaceImplShortName;
    }

    /**
     * Converts a fully qualified class name to its short name.
     *
     * @param fqcn the fully qualified class name.
     * @return the short name of the class.
     */
    private static String toShortName(String fqcn) {
        int lastDot = fqcn.lastIndexOf('.');
        if (lastDot > 0) {
            return fqcn.substring(lastDot + 1);
        }
        return fqcn;
    }

    /**
     * Adds an import to the imports map.
     *
     * @param importFQN the fully qualified name of the import.
     * @return the short name of the class.
     */
    public String addImport(String importFQN) {
        if (importFQN != null && !importFQN.isEmpty()) {
            String shortName = toShortName(importFQN);
            imports.put(importFQN, shortName);
            return shortName;
        }
        return null;
    }

    /**
     * Adds an import to the imports map based on the type mirror.
     *
     * @param type the type mirror.
     * @return the short name of the class.
     */
    public String addImport(TypeMirror type) {
        String longName = type.toString();
        String shortName = null;
        // Handle array types properly
        if (type.getKind() == TypeKind.ARRAY) {
            ArrayType arrayType = (ArrayType) type;
            String componentType = addImport(arrayType.getComponentType()); // Recursively get the short name
            return componentType + "[]"; // Append array brackets
        }
        if (type.getKind().isPrimitive()) {
            return longName;
        } else if (imports.containsKey(type.toString())) {
            shortName = imports.get(longName);
        } else if (type.getKind() == TypeKind.DECLARED) {
            // Check if this type is a generic type, and if it is, recursively check the type arguments
            DeclaredType declaredType = (DeclaredType) type;
            TypeElement typeElement = (TypeElement) declaredType.asElement();

            String rawType = typeElement.getQualifiedName().toString();
            String shortRawType = toShortName(rawType); // Convert raw type to short name
            imports.put(rawType, shortRawType); // Store the mapping for future reference

            // Process generics recursively
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if (typeArguments != null && !typeArguments.isEmpty()) {
                List<Type> genericTypes = typeArguments.stream()
                    .map(arg -> StaticJavaParser.parseType(addImport(arg))) // Recursively get short names for generic types
                    .collect(Collectors.toList());
                Type parsedRawType = StaticJavaParser.parseType(shortRawType);
                com.github.javaparser.ast.type.ClassOrInterfaceType classOrInterfaceType
                    = new com.github.javaparser.ast.type.ClassOrInterfaceType(null, parsedRawType.toString());
                classOrInterfaceType.setTypeArguments(genericTypes.toArray(new Type[0]));
                shortName = classOrInterfaceType.toString();
            } else {
                shortName = shortRawType;
            }
        }

        return shortName;
    }

    /**
     * Sets the HTTP request contexts.
     *
     * @param httpRequestContexts the list of HTTP request contexts to set.
     */
    public void setHttpRequestContexts(List<HttpRequestContext> httpRequestContexts) {
        this.httpRequestContexts = httpRequestContexts;
    }

    /**
     * Gets the list of HTTP request contexts.
     *
     * @return the list of HTTP request contexts.
     */
    public List<HttpRequestContext> getHttpRequestContexts() {
        return httpRequestContexts;
    }

    /**
     * Sets the fully qualified name of the service interface.
     *
     * @param serviceInterfaceFQN the fully qualified name of the service interface to set.
     */
    public void setServiceInterfaceFQN(String serviceInterfaceFQN) {
        this.serviceInterfaceFQN = serviceInterfaceFQN;
    }

    /**
     * Gets the fully qualified name of the service interface.
     *
     * @return the fully qualified name of the service interface.
     */
    public String getServiceInterfaceFQN() {
        return serviceInterfaceFQN;
    }

    /**
     * Gets the list of unexpected response exception details.
     *
     * @return the list of unexpected response exception details.
     */
    public List<UnexpectedResponseExceptionDetail> getUnexpectedResponseExceptionDetails() {
        return unexpectedResponseExceptionDetails;
    }

    /**
     * Sets the list of unexpected response exception details.
     *
     * @param unexpectedResponseExceptionDetails the list of unexpected response exception details to set.
     */
    public void setUnexpectedResponseExceptionDetails(
        List<UnexpectedResponseExceptionDetail> unexpectedResponseExceptionDetails) {
        this.unexpectedResponseExceptionDetails = unexpectedResponseExceptionDetails;
    }
}
