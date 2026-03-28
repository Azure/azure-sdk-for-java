# azure-search-documents — Customization Guide (Java)

This document covers how to apply, update, and remove customizations on top of the TypeSpec-generated code in `azure-search-documents` for Java. It is intended as the primary reference when generated code must be modified to meet the SDK's public API contract.

**Related docs:**
- [architecture.md](./architecture.md) — full source layout and code generation workflow

---

## When to Customize vs. When to Fix Upstream

Before adding a Java customization, consider whether the change belongs upstream:

| Change | Where it belongs |
|---|---|
| Rename a type or property for all languages | TypeSpec `client.tsp` using `@@clientName` |
| Change access (public → internal) for all languages | TypeSpec `client.tsp` using `@@access` |
| Hide a generated method or change its visibility | `SearchCustomizations.java` (Java-specific) |
| Add older service versions to the generated enum | `SearchCustomizations.java` |
| Add semantic helpers (e.g., `FieldBuilder`, `SearchUtils`) | Hand-written source file (not generated) |
| Wrap or rewire generated `WithResponse` methods | `SearchCustomizations.java` |
| Remove generated methods that should not be public | `SearchCustomizations.java` |
| Fix a wire-contract issue (endpoint, payload, model shape) | TypeSpec/spec upstream, then regenerate |

Use Java customizations when TypeSpec cannot express the desired behavior, or when the behavior is Java-specific.

For TypeSpec-level customizations (preferred when possible), see [TypeSpec Client Customizations Reference](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/knowledge/customizing-client-tsp.md).

---

## Customization Mechanics

### How Java customizations work


Java uses **`SearchCustomizations.java`** in `customizations/src/main/java/`. This file:
1. Extends `com.azure.autorest.customization.Customization` (package name is a legacy artifact — the tool works with **TypeSpec**, not AutoRest)
2. Overrides `customize(LibraryCustomization, Logger)`
3. Uses the `LibraryCustomization` → `PackageCustomization` → `ClassCustomization` API
4. Manipulates the generated Java AST using [JavaParser](https://javaparser.org/) at code-generation time
5. Modifications are applied **in-place** to the generated `.java` files — there is no separate custom file

### Key APIs

```
// Get a package
PackageCustomization pkg = libraryCustomization.getPackage("com.azure.search.documents");

// Get a class in that package
ClassCustomization cls = pkg.getClass("SearchClient");

// Manipulate the AST
cls.customizeAst(ast -> {
    ast.getClassByName("SearchClient").ifPresent(clazz -> {
        // Use JavaParser API to modify the class
    });
});
```

### Available JavaParser operations

| Operation | JavaParser API |
|---|---|
| Find methods by name | `clazz.getMethodsByName("methodName")` |
| Check for annotation | `method.isAnnotationPresent("Generated")` |
| Remove modifiers (make package-private) | `method.setModifiers()` |
| Change method name | `method.setName("newName")` |
| Remove a method | `method.remove()` |
| Add a field | `clazz.addMember(new FieldDeclaration().setModifiers(...).addVariable(...))` |
| Add an enum constant | `enumDeclaration.getEntries().add(new EnumConstantDeclaration(...))` |
| Replace method body | `method.setBody(StaticJavaParser.parseBlock(newBody))` |
| Filter by visibility | `method.isPublic()`, `method.isPrivate()` |
| Check return/parameter types | `method.getType().toString()`, `param.getType().toString()` |

---

## Current Customizations (Detailed)

### 1. Hide generated search POST APIs (`hideGeneratedSearchApis`)

**Problem**: The Java generator infers `SearchOptions` from the `searchPost` TypeSpec operation parameters. To generate `SearchOptions`, the generator must make the `searchPost` API public, exposing `searchWithResponse`, `autocompleteWithResponse`, and `suggestWithResponse` methods that should not be public.

**Solution**: Strip all access modifiers from these `@Generated` methods, making them package-private.

```java
private static void hideGeneratedSearchApis(PackageCustomization documents) {
    for (String className : Arrays.asList("SearchClient", "SearchAsyncClient")) {
        documents.getClass(className).customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
            clazz.getMethodsByName("searchWithResponse")
                .stream()
                .filter(method -> method.isAnnotationPresent("Generated"))
                .forEach(MethodDeclaration::setModifiers);  // removes all modifiers → package-private
            // Same for autocompleteWithResponse, suggestWithResponse
        }));
    }
}
```

**When to update**: If the generator changes how `SearchOptions` is inferred, or if the TypeSpec spec changes these operation names.

---

### 2. Add SearchAudience scope handling (`addSearchAudienceScopeHandling`)

**Problem**: The generated builders use a static `DEFAULT_SCOPES` array for `BearerTokenAuthenticationPolicy`. `SearchAudience` support requires a mutable `scopes` field so callers can override the token scope. TypeSpec doesn't support this yet ([typespec#9458](https://github.com/microsoft/typespec/issues/9458)).

**Solution**: Add a `private String[] scopes = DEFAULT_SCOPES` field to each builder, and replace `DEFAULT_SCOPES` with `scopes` in the `createHttpPipeline` method body.

```java
private static void addSearchAudienceScopeHandling(ClassCustomization customization, Logger logger) {
    customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
        // Guard: only proceed if DEFAULT_SCOPES exists
        // Add: private String[] scopes = DEFAULT_SCOPES;
        // Modify: createHttpPipeline body to use 'scopes' instead of 'DEFAULT_SCOPES'
    }));
}
```

**Applied to**: `SearchClientBuilder`, `SearchIndexClientBuilder`, `SearchIndexerClientBuilder`, `KnowledgeBaseRetrievalClientBuilder`

**When to update**: When [typespec#9458](https://github.com/microsoft/typespec/issues/9458) is resolved and the generator natively supports audience scoping.

---

### 3. Include old API versions (`includeOldApiVersions`)

**Problem**: The TypeSpec generator only produces the latest API version enum constant. Older versions must be preserved for backward compatibility.

**Solution**: Prepend older version constants to the `SearchServiceVersion` enum.

```java
private static void includeOldApiVersions(ClassCustomization customization) {
    customization.customizeAst(ast -> ast.getEnumByName(customization.getClassName()).ifPresent(enumDeclaration -> {
        NodeList<EnumConstantDeclaration> entries = enumDeclaration.getEntries();
        for (String version : Arrays.asList("2025-09-01", "2024-07-01", "2023-11-01", "2020-06-30")) {
            String enumName = "V" + version.replace("-", "_");
            entries.add(0, new EnumConstantDeclaration(enumName)
                .addArgument(new StringLiteralExpr(version))
                .setJavadocComment("Enum value " + version + "."));
        }
        enumDeclaration.setEntries(entries);
    }));
}
```

**When to update**: When a new GA API version is released — add the previous latest version to the list.

---

### 4. Remove GET equivalents of POST APIs (`removeGetApis`)

**Problem**: The TypeSpec spec defines both GET and POST variants for search, suggest, and autocomplete. The Java SDK only exposes the POST variants.

**Solution**: Remove all methods with prefixes `searchGet`, `suggestGet`, `autocompleteGet`.

```java
private static void removeGetApis(ClassCustomization customization) {
    List<String> methodPrefixesToRemove = Arrays.asList("searchGet", "suggestGet", "autocompleteGet");
    customization.customizeAst(ast -> ast.getClassByName(customization.getClassName())
        .ifPresent(clazz -> clazz.getMethods().forEach(method -> {
            String methodName = method.getNameAsString();
            if (methodPrefixesToRemove.stream().anyMatch(methodName::startsWith)) {
                method.remove();
            }
        })));
}
```

**Applied to**: `SearchClient`, `SearchAsyncClient`

---

### 5. Hide WithResponse BinaryData APIs (`hideWithResponseBinaryDataApis`)

**Problem**: The Java TypeSpec generator produces `WithResponse` methods that use `BinaryData` for request/response bodies. The SDK should expose typed `<T>` APIs instead.

**Solution**: For each public `@Generated` method that uses `BinaryData` in its return type or parameters:
1. Rename the method to `hiddenGenerated<OriginalName>` and make it package-private
2. Update the convenience method (non-`WithResponse` version) to call the renamed method

```java
private static void hideWithResponseBinaryDataApis(ClassCustomization customization) {
    customization.customizeAst(ast -> ast.getClassByName(customization.getClassName())
        .ifPresent(clazz -> clazz.getMethods().forEach(method -> {
            if (!method.isPublic() || !method.isAnnotationPresent("Generated")) {
                return;
            }
            if (hasBinaryDataInType(method.getType())
                || method.getParameters().stream().anyMatch(param -> hasBinaryDataInType(param.getType()))) {
                String methodName = method.getNameAsString();
                String newMethodName = "hiddenGenerated" + Character.toUpperCase(methodName.charAt(0))
                    + methodName.substring(1);
                method.setModifiers().setName(newMethodName);
                // Rewire convenience methods to call the renamed method
            }
        })));
}
```

**Applied to**: All 8 client classes (sync + async for search, index, indexer, and knowledge base)

**When to update**: When the Java TypeSpec generator natively supports typed `WithResponse<T>` APIs.

---

## Common Customization Patterns

All patterns below use `ClassCustomization.customizeAst()` which provides the JavaParser `CompilationUnit` (`ast`).
Each example shows the before → customization → after.

For full reference on all available customization APIs, see the [TypeSpec Client Customizations Reference](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/knowledge/customizing-client-tsp.md).

### Pattern A: Change class modifier (make package-private)

Before: `public class Foo {}`

```
customization.getClass("com.azure.myservice.models", "Foo").customizeAst(ast ->
    ast.getClassByName("Foo").ifPresent(ClassOrInterfaceDeclaration::setModifiers));
```

After: `class Foo {}` (package-private)

### Pattern B: Change method modifier

Before: `public Bar getBar() { ... }`

```
ast.getClassByName("Foo").ifPresent(clazz ->
    clazz.getMethodsByName("getBar")
        .forEach(method -> method.setModifiers(Modifier.Keyword.PRIVATE)));
```

After: `private Bar getBar() { ... }`

To make package-private (strip all modifiers):

```
clazz.getMethodsByName("methodName")
    .stream()
    .filter(method -> method.isAnnotationPresent("Generated"))
    .forEach(MethodDeclaration::setModifiers);  // no args = package-private
```

### Pattern C: Remove a method entirely

```
clazz.getMethods().forEach(method -> {
    if (method.getNameAsString().startsWith("unwantedPrefix")) {
        method.remove();
    }
});
```

### Pattern D: Rename a method

```
method.setName("newMethodName");
```

### Pattern E: Change method return type

Before: `public String getId() { return this.id; }`

```
ast.addImport(UUID.class);
ast.getClassByName("Foo").ifPresent(clazz ->
    clazz.getMethodsByName("getId").forEach(method -> {
        method.setType("UUID");
        method.setBody(StaticJavaParser.parseBlock("{ return UUID.fromString(this.id); }"));
    }));
```

After: `public UUID getId() { return UUID.fromString(this.id); }` (import added automatically)

### Pattern F: Change class super type

Before: `public class Foo extends Bar {}`

```
ast.getClassByName("Foo").ifPresent(clazz -> {
    ast.addImport("com.azure.myservice.models.Bar1");
    clazz.getExtendedTypes().clear();
    clazz.addExtendedType(new ClassOrInterfaceType(null, "Bar1"));
});
```

After: `public class Foo extends Bar1 {}`

### Pattern G: Add an annotation to a class

```
ast.getClassByName("Foo").ifPresent(clazz ->
    clazz.addMarkerAnnotation("Deprecated"));
```

### Pattern H: Add an annotation to a method

```
ast.getClassByName("Foo").ifPresent(clazz ->
    clazz.getMethodsByName("getBar")
        .forEach(method -> method.addMarkerAnnotation("Deprecated")));
```

### Pattern I: Remove an annotation from a class

```
ast.getClassByName("Foo")
    .flatMap(clazz -> clazz.getAnnotationByName("Deprecated"))
    .ifPresent(Node::remove);
```

### Pattern J: Add a field with default value

Before: `private String bar;`

```
ast.getClassByName("Foo")
    .flatMap(clazz -> clazz.getFieldByName("bar"))
    .ifPresent(barField -> barField.getVariables().forEach(var -> {
        if (var.getNameAsString().equals("bar")) {
            var.setInitializer("\"bar\"");
        }
    }));
```

After: `private String bar = "bar";`

### Pattern K: Add a new field to a class

```
clazz.addMember(new FieldDeclaration()
    .setModifiers(Modifier.Keyword.PRIVATE)
    .addMarkerAnnotation("Generated")
    .addVariable(new VariableDeclarator()
        .setName("fieldName")
        .setType("String[]")
        .setInitializer("DEFAULT_VALUE")));
```

### Pattern L: Generate getter and setter methods

```
ast.getClassByName("Foo").ifPresent(clazz -> {
    clazz.addMethod("isActive", Modifier.Keyword.PUBLIC)
        .setType("boolean")
        .setBody(StaticJavaParser.parseBlock("{ return this.active; }"));
    clazz.addMethod("setActive", Modifier.Keyword.PUBLIC)
        .setType("Foo")
        .addParameter("boolean", "active")
        .setBody(StaticJavaParser.parseBlock("{ this.active = active; return this; }"));
});
```

### Pattern M: Rename an enum member

Before: `JPG("jpg")`

```
ast.getEnumByName("ImageFileType").ifPresent(clazz ->
    clazz.getEntries().stream()
        .filter(entry -> "JPG".equals(entry.getName().getIdentifier()))
        .forEach(entry -> entry.setName("JPEG")));
```

After: `JPEG("jpg")` (wire value unchanged)

### Pattern N: Add enum constants to a generated enum

```
NodeList<EnumConstantDeclaration> entries = enumDeclaration.getEntries();
entries.add(0, new EnumConstantDeclaration("CONSTANT_NAME")
    .addArgument(new StringLiteralExpr("value"))
    .setJavadocComment("Description."));
enumDeclaration.setEntries(entries);
```

### Pattern O: Modify a method body (text replacement)

```
clazz.getMethodsByName("methodName").forEach(method -> method.getBody().ifPresent(body ->
    method.setBody(StaticJavaParser.parseBlock(
        body.toString().replace("oldText", "newText")))));
```

### Pattern P: Rewire a convenience method to call a renamed method

```
clazz.getMethodsByName(methodName.replace("WithResponse", "")).forEach(nonWithResponse -> {
    String body = nonWithResponse.getBody().map(BlockStmt::toString).get();
    body = body.replace(originalMethodName, renamedMethodName);
    nonWithResponse.setBody(StaticJavaParser.parseBlock(body));
});
```

### Pattern Q: Set Javadoc description on a class or method

```
ast.getClassByName("Foo").ifPresent(clazz -> {
    clazz.setJavadocComment("A Foo object stored in Azure.");
    clazz.getMethodsByName("setActive")
        .forEach(method -> method.setJavadocComment("Set the active value."));
});
```

### Pattern R: Add @param Javadoc to a method

```
clazz.getMethodsByName("setActive").forEach(method -> method.getJavadoc()
    .ifPresent(javadoc -> method.setJavadocComment(
        javadoc.addBlockTag("param", "active", "if the foo object is in active state"))));
```

### Pattern S: Add @return Javadoc to a method

```
clazz.getMethodsByName("setActive").forEach(method -> method.getJavadoc()
    .ifPresent(javadoc -> method.setJavadocComment(
        javadoc.addBlockTag("return", "the current foo object"))));
```

### Pattern T: Add @throws Javadoc to a method

```
clazz.getMethodsByName("createFoo").forEach(method -> method.getJavadoc()
    .ifPresent(javadoc -> method.setJavadocComment(
        javadoc.addBlockTag("throws", "RuntimeException", "An unsuccessful response is received"))));
```

---

## Adding a New Customization

### Step-by-step

1. **Identify the problem**: Run `mvn clean compile` and note the compile errors or unwanted public API.

2. **Decide if it belongs in customizations**: See the "When to Customize" table above.

3. **Write the customization method** in `SearchCustomizations.java`:
   ```java
   private static void myCustomization(ClassCustomization customization) {
       customization.customizeAst(ast -> ast.getClassByName(customization.getClassName())
           .ifPresent(clazz -> {
               // JavaParser AST manipulation
           }));
   }
   ```

4. **Call it from `customize()`**:
   ```java
   @Override
   public void customize(LibraryCustomization libraryCustomization, Logger logger) {
       PackageCustomization documents = libraryCustomization.getPackage("com.azure.search.documents");
       // ...existing customizations...
       myCustomization(documents.getClass("TargetClass"));
   }
   ```

5. **Regenerate** to apply the customization:
   ```powershell
   tsp-client update
   ```

6. **Verify**: Run `mvn clean compile` to confirm the customization works.

---

## Removing a Customization

When a generator issue is fixed upstream and a customization is no longer needed:

1. **Remove the customization method** from `SearchCustomizations.java`.
2. **Remove the call** from `customize()`.
3. **Regenerate** (`tsp-client update`).
4. **Verify** the generated code now has the correct shape without the customization.
5. **Run `mvn clean compile`** to confirm no regressions.

---

## Identifying What Needs Updating After Regeneration

After running `tsp-client update`, a regeneration may:

| Generator action | What to check |
|---|---|
| Changed a method signature on a generated client | Customization methods that reference the old method name — update AST queries |
| Renamed a generated member | Customization methods that filter by old name — update string literals |
| Added new public methods that should be hidden | Add new AST manipulation to hide them |
| Changed the generated builder structure | `addSearchAudienceScopeHandling` may need updating if `DEFAULT_SCOPES` or `createHttpPipeline` changed |
| Added a new client class | May need to add `hideWithResponseBinaryDataApis` call for the new client |
| Changed the `SearchServiceVersion` enum | `includeOldApiVersions` may need a new version added to the list |

### Workflow for finding all impacted areas

```powershell
# 1. Regenerate
tsp-client update

# 2. Build to surface all compile errors
cd sdk/search/azure-search-documents
mvn clean compile

# 3. Group errors by category:
#    - "cannot find symbol" → a renamed/removed generated member
#    - "incompatible types" → a type change in generated code
#    - "method does not exist" → a removed or renamed generated method

# 4. For each error, check if it's in:
#    - A generated file → likely a spec/generator change, may need customization update
#    - A hand-written file (e.g., SearchUtils.java, batching/) → update the hand-written code
#    - A test file → update the test

# 5. Check for new unwanted public APIs
#    Look for new public methods with @Generated that shouldn't be exposed
```

### Finding stale customization references

```powershell
# Check all customization method references against current generated code
# If a customization references a method name that no longer exists, it silently does nothing
# Review SearchCustomizations.java and verify each method name string literal still matches generated code
```

---

## Troubleshooting Customizations

### Build fails after applying a customization

The customized Java code likely has a syntax error. Steps:

1. Check if the error is in a generated file or `SearchCustomizations.java`.
2. If in a generated file, the customization AST manipulation is producing invalid Java. Common causes:
   - `StaticJavaParser.parseBlock()` called with a string that isn't a valid block (missing braces, unbalanced parens)
   - Method body replacement via string `.replace()` hit an unintended match
   - Added a field/method with a type that hasn't been imported (`ast.addImport(...)` needed)
3. If in `SearchCustomizations.java` itself, fix the compilation error there.

### Customization silently does nothing

If a customization method references a name that no longer exists in generated code (e.g., a method was renamed upstream), the `getMethodsByName()` / `getClassByName()` call returns empty and nothing happens. This is silent — no error, no warning.

**Fix**: After regeneration, verify that all string literals in `SearchCustomizations.java` still match the generated code. Search for the method/class name in the generated files to confirm.

### Customization runs but produces wrong output

Debug by inspecting the generated file after customization is applied. The customized file is written to `src/main/java/` — open it and check the modified method/field/class.

---

## Differences from .NET Customizations

If you are familiar with the .NET SDK's customization approach, here are the key differences:

| .NET approach | Java approach |
|---|---|
| `partial class` — split type across multiple files | Not available in Java; single file per class |
| `[CodeGenType("Old")]` — rename a type | TypeSpec `@@clientName` or AST rename in `SearchCustomizations.java` |
| `[CodeGenMember("Old")]` — rename a property | TypeSpec `@@clientName` or AST manipulation |
| `[CodeGenSuppress("Member")]` — suppress a member | AST manipulation to remove or hide the member |
| `[CodeGenSerialization]` — custom JSON key | Not available; use TypeSpec `@@encodedName` or `@JsonProperty` |
| `[EditorBrowsable(Never)]` — hide from IntelliSense | Not available in Java; use `@Deprecated` or make package-private |
| `[ForwardsClientCalls]` — forwarding overloads | No equivalent; manually add convenience methods |
| `SearchModelFactory` — test/mock factory | Not used in Java SDK |
| `ApiCompatBaseline.txt` — API compat suppressions | Not used in Java SDK |
| `api/*.cs` — public API snapshots | Not used in Java SDK |
| `dotnet build` | `mvn clean compile` |
| `dotnet test` | `mvn test` |
| `Export-API.ps1` | No equivalent |

---

## Quick-Reference Checklist: After a Regeneration

```
[ ] mvn clean compile — resolve all compile errors
[ ] Check if any customization methods in SearchCustomizations.java reference
    method/field names that no longer exist in generated code
[ ] If a new API version was added:
    [ ] Add the previous version to the includeOldApiVersions() list
    [ ] Verify SearchServiceVersion.getLatest() returns the new version
[ ] If new client classes were generated:
    [ ] Add hideWithResponseBinaryDataApis() call for the new client(s)
    [ ] Add addSearchAudienceScopeHandling() call for the new builder(s)
[ ] If new public methods should be hidden:
    [ ] Add appropriate AST manipulation in SearchCustomizations.java
[ ] Run mvn test to verify tests pass
[ ] Review CHANGELOG.md entry
```
