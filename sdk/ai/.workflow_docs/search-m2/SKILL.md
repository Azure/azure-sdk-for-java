---
name: search-m2
description: Search for Java classes inside Maven dependencies in ~/.m2. Use when the user asks to locate classes or inspect JARs. Cross-reference pom.xml files in the current directory to resolve dependency names/versions.
---

# Search Maven Local Repository (search-m2)

Use this skill to find which dependency JAR contains a class and to inspect JAR contents in `~/.m2/repository`.

## Preconditions
- Work from the user’s current directory.
- Look for `pom.xml` files in the current directory tree and use them to resolve dependency names and versions.

## Key paths
- Maven local repo: `~/.m2/repository`
- Dependency JAR path pattern:
  `~/.m2/repository/<groupId path>/<artifactId>/<version>/<artifactId>-<version>.jar`

## Steps
1. **Discover pom files**: run `find . -name pom.xml` from the current directory.
   - If multiple poms exist, prefer the closest one to the current directory or ask the user which project/module to use.
2. **Extract dependency coordinates**:
   - If the user provides a group/artifact, search the poms for that dependency and read its version.
   - If versions are defined via properties (e.g., `${foo.version}`), resolve the property from the same pom (or parent if obvious).
3. **Resolve the JAR path** using the groupId/artifactId/version mapping above.
   - If the version is not found, list available versions in `~/.m2/repository/<groupId path>/<artifactId>/` and ask the user which to inspect.
4. **Search for classes**:
   - List classes: `jar tf <jar> | rg '\.class$'`
   - Find a specific class: `jar tf <jar> | rg '<ClassName>(\.class)?$'`
5. **Inspect class details** (if requested):
   - `javap -classpath <jar> <fully.qualified.ClassName>`

## Useful commands

### Find a class across all jars (fallback)
```bash
rg -g '*.jar' --files ~/.m2/repository | while read -r jar; do
  jar tf "$jar" | rg -q 'com/example/MyClass.class' && echo "$jar"
done
```

### Resolve versions for a dependency
```bash
ls ~/.m2/repository/<groupId path>/<artifactId>/
```

## Notes
- If the user is vague, start by identifying the relevant pom.xml and extracting dependency coordinates.
- If the dependency is transitive and not in the pom, check `mvn -q -DskipTests dependency:tree` (only if needed and the user agrees).
