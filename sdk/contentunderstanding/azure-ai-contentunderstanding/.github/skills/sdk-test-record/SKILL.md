---
name: sdk-test-record
description: |
  Run Azure SDK tests in RECORD mode to capture live API responses.
  
  This skill helps you:
  - Record new test sessions with live Azure services
  - Update existing recordings when APIs change
  - Generate session record files for playback testing
  
  IMPORTANT: Requires Azure credentials and live service access.
  
  Trigger phrases: "record tests", "RECORD mode", "capture test recordings"
---

# SDK Test Record

This skill runs Azure SDK tests in RECORD mode to capture live API responses for playback testing.

## 🎯 What This Skill Does

1. Starts the test proxy in RECORD mode
2. Runs tests against live Azure services
3. Captures HTTP request/response pairs
4. Saves session records to `.assets` directory

## 📋 Pre-requisites

- [ ] Azure credentials configured (via `.env` or environment)
- [ ] Test proxy installed (`test-proxy` command available)
- [ ] `assets.json` file present in module directory
- [ ] Live Azure service endpoint accessible

## 🔧 Usage

### Java (Maven)
```bash
# Navigate to SDK module
cd sdk/{service}/{module}

# Run tests in RECORD mode
mvn test -DAZURE_TEST_MODE=RECORD

# Run specific test class
mvn test -DAZURE_TEST_MODE=RECORD -Dtest=Sample01*

# Run specific test method
mvn test -DAZURE_TEST_MODE=RECORD -Dtest=Sample01BasicOperations#testAnalyzeDocument
```

### Python (pytest)
```bash
cd sdk/{service}/azure-{service}
pytest --azure-test-mode=record
```

### .NET (dotnet)
```bash
cd sdk/{service}/Azure.{Service}
dotnet test /p:TestMode=Record
```

### JavaScript (npm)
```bash
cd sdk/{service}/{module}
npm test -- --test-mode=record
```

## 📦 Test Proxy Commands

### Restore Existing Recordings
```bash
# Before RECORD mode, restore existing assets
test-proxy restore -a assets.json
```

### Push After Recording
```bash
# After RECORD mode, push new recordings
test-proxy push -a assets.json
```

## ⚠️ Important Notes

### Recording Requirements
1. **Live credentials**: Tests connect to real Azure services
2. **Network access**: Ensure firewall allows Azure endpoints
3. **Cost awareness**: Recording creates real Azure resources (may incur costs)

### Sanitization
Recordings are automatically sanitized to remove:
- API keys and tokens
- Subscription IDs
- Client secrets

### Session Records Location
| Language | Location |
|----------|----------|
| Java | `.assets/{tag}/` (managed by test-proxy) |
| Python | `recordings/` folder |
| .NET | `SessionRecords/` folder |
| JavaScript | `recordings/` folder |

## 🔍 Troubleshooting

### Missing Environment Variables
```bash
# Check required variables
echo $CONTENT_UNDERSTANDING_ENDPOINT
echo $AZURE_CLIENT_ID
```

### Test Proxy Not Running
```bash
# Start test proxy manually
test-proxy start &

# Or let SDK framework handle it automatically
```

### Recording Already Exists
Tests will overwrite existing recordings. Use `test-proxy restore` first if you want to preserve them.

## 🌐 Cross-Language Test Mode

| Language | Environment Variable | Command Flag |
|----------|---------------------|--------------|
| Java | `AZURE_TEST_MODE=RECORD` | `-DAZURE_TEST_MODE=RECORD` |
| Python | `AZURE_TEST_MODE=RECORD` | `--azure-test-mode=record` |
| .NET | `AZURE_TEST_MODE=Record` | `/p:TestMode=Record` |
| JavaScript | `AZURE_TEST_MODE=record` | `--test-mode=record` |
