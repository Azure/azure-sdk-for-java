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
cd sdk/{service}/azure-{service}
pytest --azure-test-mode=record
```

### .NET (dotnet)

```bash
cd sdk/{service}/{module}
npm test -- --test-mode=record
```

## 📦 Test Proxy Commands

### Restore Existing Recordings

```bash
# After RECORD mode, push new recordings
test-proxy push -a assets.json
```

## ⚠️ Important Notes

### Recording Requirements

- API keys and tokens
- Subscription IDs
- Client secrets

### Session Records Location

```bash
# Check required variables
echo $CONTENT_UNDERSTANDING_ENDPOINT
echo $AZURE_CLIENT_ID
```

### Test Proxy Not Running

Tests will overwrite existing recordings. Use `test-proxy restore` first if you want to preserve them.

## 🌐 Cross-Language Test Mode

| Language | Environment Variable | Command Flag |
|----------|---------------------|--------------|
| Java | `AZURE_TEST_MODE=RECORD` | `-DAZURE_TEST_MODE=RECORD` |
| Python | `AZURE_TEST_MODE=RECORD` | `--azure-test-mode=record` |
| .NET | `AZURE_TEST_MODE=Record` | `/p:TestMode=Record` |
| JavaScript | `AZURE_TEST_MODE=record` | `--test-mode=record` |
