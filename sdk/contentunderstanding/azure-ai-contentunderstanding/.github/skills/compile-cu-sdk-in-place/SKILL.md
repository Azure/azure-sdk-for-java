---
name: compile-cu-sdk-in-place
description: Compile Content Understanding SDK main code in place for SDK development or debugging. Compiles CU SDK from source in the local enlistment without installing to Maven repository. Use when developing or debugging the CU SDK, or when you need to compile the SDK before running samples. For consuming CU SDK directly, see sdk/contentunderstanding/azure-ai-contentunderstanding/README.md
---

# Compile CU SDK In Place

This skill compiles the Content Understanding SDK main code (`src/main/java`) in place within the local enlistment. This is for SDK development or debugging SDK issues, not for consuming the SDK in applications.

## When to Use

- Developing or modifying the CU SDK source code
- Debugging SDK issues
- Preparing the SDK for in-place sample execution
- Testing SDK changes without installing to Maven repository

## Quick Start

```bash
# Navigate to CU SDK directory
cd sdk/contentunderstanding/azure-ai-contentunderstanding

# Compile CU SDK
./scripts/compile-cu-sdk.sh
```

## What It Does

Compiles the CU SDK main source code (`src/main/java`) to `target/classes` using Maven:

```bash
mvn compile -DskipTests
```

This creates compiled `.class` files in `target/classes/` that can be used by:
- Sample compilation (see `run-cu-sample` skill)
- Direct Java execution with classpath
- IDE development workflows

## Prerequisites

1. **Maven**: Must be installed and available in PATH
2. **Java**: Java 8+ installed
3. **Project Structure**: Must be run from `sdk/contentunderstanding/azure-ai-contentunderstanding/` directory

## Output

- **Location**: `target/classes/`
- **Content**: Compiled CU SDK classes in package structure
- **Format**: Standard Java `.class` files

## Verification

After compilation, verify success:

```bash
# Check that target/classes exists and has content
ls -la target/classes/

# Should show compiled classes like:
# com/azure/ai/contentunderstanding/...
```

## Related Skills

- **`run-cu-sample`**: Compiles and runs CU SDK samples (requires CU SDK to be compiled first)

## Troubleshooting

**Compilation errors**: Check Maven and Java versions:
```bash
mvn --version
java -version
```

**Missing dependencies**: Ensure Maven can resolve dependencies:
```bash
mvn dependency:resolve
```

**Empty target/classes**: Check for compilation errors in Maven output.
