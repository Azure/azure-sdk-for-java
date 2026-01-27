---
name: sdk-run-all-samples
description: |
  Run all Azure SDK samples in sequence.
  
  This skill helps you:
  - Execute all samples for validation
  - Verify sample code works correctly
  - Batch test sample implementations
  
  Trigger phrases: "run all samples", "execute all samples", "test all samples"
---

# SDK Run All Samples

This skill runs all Azure SDK samples in a module for comprehensive validation.

## 🎯 What This Skill Does

1. Discovers all sample files in the SDK module
2. Compiles samples if needed
3. Executes each sample in sequence
4. Reports overall results

## 📋 Pre-requisites

- [ ] SDK module compiled successfully
- [ ] Environment variables configured (for live samples)
- [ ] Session recordings restored (for PLAYBACK mode)

## 🔧 Usage

### Java (Maven) - PLAYBACK Mode
```bash
# Navigate to SDK module
cd sdk/{service}/{module}

# Restore recordings first
test-proxy restore -a assets.json

# Run all samples in PLAYBACK mode
mvn test -Dtest="Sample*" -DAZURE_TEST_MODE=PLAYBACK
```

### Java (Maven) - RECORD Mode
```bash
# Run all samples with live service
mvn test -Dtest="Sample*" -DAZURE_TEST_MODE=RECORD
```

### Python
```bash
cd sdk/{service}/azure-{service}/samples

# Run all samples
for f in sample_*.py; do
    echo "Running $f..."
    python "$f"
done
```

### .NET
```bash
cd sdk/{service}/Azure.{Service}/samples
dotnet test
```

### JavaScript
```bash
cd sdk/{service}/{module}/samples
npm run samples
```

## 📦 Sample Discovery

### Java Pattern
```bash
# Find all sample test classes
find src/samples -name "Sample*.java" -exec basename {} .java \;
```

### Expected Output
```
Sample01BasicOperations
Sample02AnalyzeDocument
Sample03ExtractFields
...
```

## ⚠️ Important Notes

### Execution Order
Samples may have dependencies. If one fails, others might also fail.

### Resource Cleanup
Live samples may create Azure resources. Ensure cleanup:
- Check sample for cleanup code
- Manually delete test resources if needed

### Timeout Handling
Long-running samples may timeout:
```bash
# Increase Maven timeout
mvn test -Dtest="Sample*" -Dsurefire.timeout=600
```

## 🔍 Troubleshooting

### Some Samples Skipped
Check `@Disabled` annotations or conditional execution.

### Environment Variables Missing
```bash
# Load from .env file
source .github/skills/sdk-setup-env/scripts/load-env.sh
```

## 🌐 Cross-Language Commands

| Language | Command | Notes |
|----------|---------|-------|
| Java | `mvn test -Dtest="Sample*"` | Wildcard pattern |
| Python | `pytest samples/` | pytest discovers tests |
| .NET | `dotnet test samples/` | Test all sample projects |
| JavaScript | `npm run samples` | Check package.json |
