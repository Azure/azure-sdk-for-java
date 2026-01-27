---
name: sdk-run-sample
description: |
  Run a single Azure SDK sample.
  
  This skill helps you:
  - Execute individual SDK samples
  - Test sample code functionality
  - Debug sample implementations
  
  Trigger phrases: "run sample", "execute sample", "test sample code"
---

# SDK Run Sample

This skill runs individual Azure SDK samples for testing and demonstration.

## 🎯 What This Skill Does

1. Identifies sample files in the SDK module
2. Compiles the sample if needed
3. Executes the sample with proper configuration
4. Reports execution results

## 📋 Pre-requisites

- [ ] SDK module compiled successfully
- [ ] Environment variables configured (for live samples)
- [ ] Sample file exists in module

## 🔧 Usage

### Java (Maven)
```bash
# Navigate to SDK module
cd sdk/{service}/{module}

# Run sample as test
mvn test -Dtest=Sample01BasicOperations -DAZURE_TEST_MODE=PLAYBACK

# Run with live service
mvn test -Dtest=Sample01BasicOperations -DAZURE_TEST_MODE=RECORD
```

### Python
```bash
cd sdk/{service}/azure-{service}/samples
python sample_basic_operations.py
```

### .NET
```bash
cd sdk/{service}/Azure.{Service}/samples
dotnet run --project Sample01BasicOperations.csproj
```

### JavaScript
```bash
cd sdk/{service}/{module}/samples
npx ts-node sample_basic_operations.ts
```

## 📦 Sample Locations

| Language | Location | Pattern |
|----------|----------|---------|
| Java | `src/samples/java/` | `Sample*.java` |
| Python | `samples/` | `sample_*.py` |
| .NET | `samples/` | `Sample*.cs` |
| JavaScript | `samples/` | `*.ts` or `*.js` |

## ⚠️ Sample Types

### Live Samples (Require Credentials)
- Connect to real Azure services
- Require environment variables
- May incur Azure costs

### Recorded Samples (PLAYBACK mode)
- Use pre-recorded responses
- No credentials needed
- Fast and repeatable

## 🔍 Finding Samples

### Java
```bash
# List all sample files
find src/samples -name "Sample*.java" | head -20
```

### Python
```bash
ls samples/sample_*.py
```

## 🌐 Cross-Language Commands

| Language | Command | Notes |
|----------|---------|-------|
| Java | `mvn test -Dtest={SampleClass}` | Samples are test classes |
| Python | `python samples/{sample}.py` | Direct execution |
| .NET | `dotnet run --project samples/{sample}` | Project-based |
| JavaScript | `npx ts-node samples/{sample}.ts` | TypeScript |
