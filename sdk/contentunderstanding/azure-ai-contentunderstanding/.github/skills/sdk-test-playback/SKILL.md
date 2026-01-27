---
name: sdk-test-playback
description: |
  Run Azure SDK tests in PLAYBACK mode using recorded API responses.
  
  This skill helps you:
  - Run tests offline without Azure credentials
  - Verify SDK behavior against recorded responses
  - CI/CD testing without live service access
  
  No Azure credentials required - uses previously recorded sessions.
  
  Trigger phrases: "playback tests", "PLAYBACK mode", "run offline tests"
---

# SDK Test Playback

This skill runs Azure SDK tests in PLAYBACK mode using previously recorded API responses.

## 🎯 What This Skill Does

1. Restores session records from Azure SDK Assets repo
2. Starts test proxy in PLAYBACK mode
3. Runs tests using recorded HTTP responses
4. Reports test results

## 📋 Pre-requisites

- [ ] `assets.json` file present in module directory
- [ ] Test proxy installed (`test-proxy` command available)
- [ ] Session records available (run RECORD mode first if missing)

## 🔧 Usage

### Step 1: Restore Recordings
```bash
# Navigate to SDK module
cd sdk/{service}/{module}

# Restore session records
test-proxy restore -a assets.json
```

### Step 2: Run Tests

#### Java (Maven)
```bash
# Run all tests in PLAYBACK mode
mvn test -DAZURE_TEST_MODE=PLAYBACK

# Run specific test class
mvn test -DAZURE_TEST_MODE=PLAYBACK -Dtest=Sample01*
```

#### Python (pytest)
```bash
pytest --azure-test-mode=playback
```

#### .NET (dotnet)
```bash
dotnet test /p:TestMode=Playback
```

#### JavaScript (npm)
```bash
npm test -- --test-mode=playback
```

## 📦 Session Records

### Restore from Assets Repo
```bash
# Restore recordings to local .assets directory
test-proxy restore -a assets.json
```

### Check Assets Tag
```bash
# View current assets tag
cat assets.json | jq '.Tag'
```

## ⚠️ Common Issues

### "Unable to find a record for the request"
This means the test is making a request that wasn't recorded:

1. **Run RECORD mode** to capture the missing request:
   ```bash
   mvn test -DAZURE_TEST_MODE=RECORD -Dtest=FailingTestClass
   ```

2. **Check test data**: Ensure test uses same input as recorded session

3. **Restore assets**: Run `test-proxy restore -a assets.json`

### Test Proxy Not Running
```bash
# The SDK framework usually starts it automatically
# If needed, start manually:
test-proxy start &
```

### Stale Recordings
If recordings are outdated:
```bash
# Record fresh session
mvn test -DAZURE_TEST_MODE=RECORD

# Push new recordings
test-proxy push -a assets.json
```

## 🔍 Debugging Tips

### Verbose Test Proxy Output
```bash
export PROXY_MANUAL_START=true
test-proxy start --storage-location .assets

# In another terminal
mvn test -DAZURE_TEST_MODE=PLAYBACK
```

### Check Recording Files
```bash
# List recording files
ls -la .assets/*/
```

## 🌐 Cross-Language Test Mode

| Language | Environment Variable | Command Flag |
|----------|---------------------|--------------|
| Java | `AZURE_TEST_MODE=PLAYBACK` | `-DAZURE_TEST_MODE=PLAYBACK` |
| Python | `AZURE_TEST_MODE=PLAYBACK` | `--azure-test-mode=playback` |
| .NET | `AZURE_TEST_MODE=Playback` | `/p:TestMode=Playback` |
| JavaScript | `AZURE_TEST_MODE=playback` | `--test-mode=playback` |

## ✅ Benefits of PLAYBACK Mode

1. **No Azure credentials needed** - Tests run offline
2. **Fast execution** - No network latency
3. **Deterministic** - Same results every time
4. **CI/CD friendly** - No service dependencies
5. **Cost-free** - No Azure resource consumption
