# TypeSpec Java Customization

The `azure-autorest-customization` package provides APIs to safely customize generated Java code for cases that
TypeSpec Java cannot represent directly. Customizations use JavaParser ASTs through `ClassCustomization.customizeAst`.

## Before you customize

First consider whether the change belongs in TypeSpec (`client.tsp`). TypeSpec customizations are cleaner and persist
through regeneration. See the [TypeSpec Client Customizations Reference](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/knowledge/customizing-client-tsp.md) for decorators such as `@@clientName` and `@@access`.

Use Java code customizations only when TypeSpec cannot express the required behavior.

## Set up a customization project

Create a Maven project that depends on `azure-autorest-customization`:

```xml
<dependency>
  <groupId>com.azure.tools</groupId>
  <artifactId>azure-autorest-customization</artifactId>
  <version>1.0.0-beta.11</version>
</dependency>
```

Create a class that extends `com.azure.autorest.customization.Customization` and overrides
`void customize(LibraryCustomization, Logger)`. The `LibraryCustomization` parameter is the entry point for changing
generated Java code before it is written to disk.

```java
@Override
public void customize(LibraryCustomization customization, Logger logger) {
    customization.getClass("com.azure.myservice.models", "Foo")
        .customizeAst(ast -> ast.getClassByName("Foo")
            .ifPresent(clazz -> clazz.addMarkerAnnotation("Deprecated")));
}
```

Configure the generator to use the class:

```diff
  "@azure-tools/typespec-java":
    emitter-output-dir: "{output-dir}/{service-dir}/azure-contoso-widgetmanager"
    namespace: com.azure.contoso.widgetmanager
+    customization-class: customization/src/main/java/MyCustomization.java
```

## Navigate generated code

Start with `LibraryCustomization`, then navigate to a package or class with `getClass(packageName, className)`.
`LibraryCustomization`, `PackageCustomization`, and `ClassCustomization` provide the primary navigation APIs.
Use `customizeAst` to change the JavaParser AST for the selected source file. Add imports with
`ast.addImport(...)` whenever the new code references a type not already imported.

## Supported customizations

The following common changes can be made through the JavaParser AST:

| Customization | Typical AST operation |
|---|---|
| Change a class modifier | `ClassOrInterfaceDeclaration.setModifiers(...)` |
| Change a method modifier | `MethodDeclaration.setModifiers(...)` |
| Change a method return type or body | `MethodDeclaration.setType(...)` and `setBody(...)` |
| Change a class supertype | Clear and add `ClassOrInterfaceType` extended types |
| Add or remove a class or method annotation | `addMarkerAnnotation(...)` or remove the matching annotation node |
| Add a field default value | `VariableDeclarator.setInitializer(...)` |
| Add getter and setter methods | `ClassOrInterfaceDeclaration.addMethod(...)` |
| Rename an enum member | `EnumConstantDeclaration.setName(...)` |
| Update class or method Javadoc | `setJavadocComment(...)` |
| Add or remove Javadoc tags | Update the `Javadoc` returned by `getJavadoc()` |

### Change a method modifier

```java
@Override
public void customize(LibraryCustomization customization, Logger logger) {
    customization.getClass("com.azure.myservice.models", "Foo").customizeAst(ast -> ast.getClassByName("Foo")
        .ifPresent(clazz -> clazz.getMethodsByName("getBar")
            .forEach(method -> method.setModifiers(Modifier.Keyword.PRIVATE))));
}
```

### Change a method return type

When changing a return type, update both the declared type and method body. Add an import for the replacement type.

```java
@Override
public void customize(LibraryCustomization customization, Logger logger) {
    customization.getClass("com.azure.myservice.models", "Foo").customizeAst(ast -> {
        ast.addImport(UUID.class);
        ast.getClassByName("Foo").ifPresent(clazz -> clazz.getMethodsByName("getId").forEach(method -> {
            method.setType("UUID");
            method.setBody(StaticJavaParser.parseBlock("{ return UUID.fromString(this.id); }"));
        }));
    });
}
```

For a method originally returning `void`, provide the complete return expression. To change a method to return
`void`, set its body appropriately and do not add a return value.

### Change a supertype

```java
@Override
public void customize(LibraryCustomization customization, Logger logger) {
    customization.getClass("com.azure.myservice.models", "Foo").customizeAst(ast -> ast.getClassByName("Foo")
        .ifPresent(clazz -> {
            ast.addImport("com.azure.myservice.models.Bar1");
            clazz.getExtendedTypes().clear();
            clazz.addExtendedType(new ClassOrInterfaceType(null, "Bar1"));
        }));
}
```

### Add a field default value

```java
@Override
public void customize(LibraryCustomization customization, Logger logger) {
    customization.getClass("com.azure.myservice.models", "Foo").customizeAst(ast -> ast.getClassByName("Foo")
        .flatMap(clazz -> clazz.getFieldByName("bar"))
        .ifPresent(field -> field.getVariables().forEach(variable -> {
            if ("bar".equals(variable.getNameAsString())) {
                variable.setInitializer("\"bar\"");
            }
        })));
}
```

### Generate accessor methods

```java
@Override
public void customize(LibraryCustomization customization, Logger logger) {
    customization.getClass("com.azure.myservice.models", "Foo").customizeAst(ast -> ast.getClassByName("Foo")
        .ifPresent(clazz -> {
            clazz.addMethod("isActive", Modifier.Keyword.PUBLIC).setType("boolean")
                .setBody(StaticJavaParser.parseBlock("{ return this.active; }"));
            clazz.addMethod("setActive", Modifier.Keyword.PUBLIC).setType("Foo")
                .addParameter("boolean", "active")
                .setBody(StaticJavaParser.parseBlock("{ this.active = active; return this; }"));
        }));
}
```

### Update Javadoc

Set a complete description with `setJavadocComment`. For parameter, return, and exception tags, get the existing
Javadoc and add a block tag:

```java
method.getJavadoc().ifPresent(javadoc -> method.setJavadocComment(
    javadoc.addBlockTag("param", "active", "whether the Foo is active")));
```

Use `return` for a return-value tag and `throws` with the exception type and description for an exception tag.

## Troubleshooting

### TypeSpec Java reports “Failed to format file: `<path>`. File content: `<file-content>`.”

Customized Java code likely contains a syntax error.

Inspect the source shown after `File content:` to locate the malformed code, then fix the customization and regenerate SDK.
