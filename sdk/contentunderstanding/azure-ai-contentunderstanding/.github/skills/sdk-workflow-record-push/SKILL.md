---
name: sdk-workflow-record-push
description: |
  Complete workflow to record tests and push recordings to Azure SDK Assets repo.
  
  This workflow executes: setup-env → compile → test-record → push-recordings → test-playback
  
  Use when you need to:
  - Record new test sessions with live Azure services
  - Update existing recordings after API changes
  - Complete the full RECORD and PUSH cycle
  
  Trigger phrases: "record and push", "complete recording workflow", "full test recording cycle"
---

# SDK Workflow: Record and Push

This workflow orchestrates the complete process of recording SDK tests and pushing them to the Azure SDK Assets repository.

## 🎯 What This Workflow Does

1. Loads environment variables
2. Compiles the SDK
3. Runs tests in RECORD mode (live Azure services)
4. Pushes recordings to assets repo
5. Verifies with PLAYBACK mode

## 📋 Pre-requisites

- [ ] `.env` file with Azure credentials
- [ ] `assets.json` file in module directory
- [ ] Network access to Azure services
- [ ] Git credentials for Azure SDK Assets repo

## 🔄 Workflow Steps

Execute these steps in order. Stop if any step fails.

### Step 1: Load Environment ➡️ `sdk-setup-env`

```bash
# Load credentials from .env file
source .github/skills/sdk-setup-env/scripts/load-env.sh
```

**Checkpoint:** Verify `CONTENT_UNDERSTANDING_ENDPOINT` and credentials are set.

---

### Step 2: Compile SDK ➡️ `sdk-compile`

```bash
# Compile the SDK module
mvn compile -f pom.xml -DskipTests
```

**Checkpoint:** Build should succeed with no errors.

---

### Step 3: Run RECORD Mode Tests ➡️ `sdk-test-record`

```bash
# Run tests against live Azure services
mvn test -DAZURE_TEST_MODE=RECORD
```

**Checkpoint:** All tests should pass. Note any skipped tests.

---

### Step 4: Push Recordings ➡️ `sdk-push-recordings`

```bash
# Push session recordings to Azure SDK Assets repo
test-proxy push -a assets.json
```

**Checkpoint:** Note the new tag in `assets.json`. Example:

```
java/contentunderstanding/azure-ai-contentunderstanding_abc123
```

---

### Step 5: Verify with PLAYBACK ➡️ `sdk-test-playback`

```bash
# Restore and run with recorded responses
test-proxy restore -a assets.json
mvn test -DAZURE_TEST_MODE=PLAYBACK
```

**Checkpoint:** All tests should pass using recorded responses.

---

## ✅ Completion Checklist

After workflow completes successfully:

- [ ] All tests passed in RECORD mode
- [ ] Recordings pushed (new tag in `assets.json`)
- [ ] All tests passed in PLAYBACK mode
- [ ] `assets.json` ready to commit

## ⚠️ Error Recovery

| Step | Common Error | Resolution |
|------|--------------|------------|
| Step 1 | Missing .env | Create .env with credentials |
| Step 2 | Compile error | Fix code issues |
| Step 3 | Auth failure | Check Azure credentials |
| Step 3 | Test failure | Debug failing test |
| Step 4 | Push failed | Check Git credentials |
| Step 5 | Recording mismatch | Re-run RECORD mode |

## 🔗 Related Skills

| Skill | Role in Workflow |
|-------|------------------|
| `sdk-setup-env` | Step 1 - Environment |
| `sdk-compile` | Step 2 - Build |
| `sdk-test-record` | Step 3 - Record |
| `sdk-push-recordings` | Step 4 - Push |
| `sdk-test-playback` | Step 5 - Verify |
