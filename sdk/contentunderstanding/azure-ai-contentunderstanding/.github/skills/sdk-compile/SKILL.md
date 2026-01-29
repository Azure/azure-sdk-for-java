---
name: sdk-compile
description: |
  Compile Azure SDK source code.
  
  This skill helps you:
  - Build SDK modules locally
  - Verify compilation before testing
  - Resolve dependency issues
  
  Supported build systems: Maven (Java), pip (Python), dotnet (C#), npm (JavaScript)
  
  Trigger phrases: "compile sdk", "build project", "maven compile"
---

# SDK Compile

This skill compiles Azure SDK source code for local development and testing.

## 🎯 What This Skill Does

1. Detects the SDK language and build system
2. Compiles source code with appropriate flags
3. Reports compilation errors with context

## 📋 Pre-requisites

- [ ] SDK source code checked out
- [ ] Build tools installed (Maven/pip/dotnet/npm)
- [ ] JDK 8+ (for Java)

## 🔧 Usage

### Java (Maven)

```bash
# Navigate to SDK module
cd sdk/{service}/azure-{service}

# Install in editable mode
pip install -e .
```

### .NET (dotnet)

```bash
# Navigate to SDK module
cd sdk/{service}/{module}

# Build
npm run build
```

## 📦 Java-Specific Notes

### Compile Single Module (Recommended)

```bash
# From repo root
mvn compile -pl sdk/contentunderstanding/azure-ai-contentunderstanding -am
```

### Common Maven Flags

```bash
# Install to local repo first
mvn install -DskipTests -pl sdk/core/azure-core
```

### Checkstyle Errors

Fix code style issues instead of disabling Checkstyle rules.

### SpotBugs Warnings  

Address warnings instead of suppressing them.

## 🌐 Cross-Language Commands

| Language | Compile Command | Notes |
|----------|----------------|-------|
| Java | `mvn compile` | Requires JDK 8+ |
| Python | `pip install -e .` | Creates editable install |
| .NET | `dotnet build` | Requires .NET SDK |
| JavaScript | `npm run build` | Check package.json for script |
