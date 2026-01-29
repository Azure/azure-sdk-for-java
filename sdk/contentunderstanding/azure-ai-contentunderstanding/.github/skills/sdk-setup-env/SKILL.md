---
name: sdk-setup-env
description: |
  Load environment variables from .env file for Azure SDK development.
  
  This skill helps you:
  - Find .env files in your project
  - Load environment variables into your shell session
  - Validate required variables for testing
  
  Trigger phrases: "load env", "setup environment", "configure SDK"
---

# SDK Environment Setup

This skill loads environment variables from `.env` files for Azure SDK development and testing.

## 🎯 What This Skill Does

1. Locates `.env` files in your workspace
2. Loads environment variables into the current shell session
3. Validates required variables for SDK testing

## 📋 Pre-requisites

- [ ] `.env` file exists in the SDK module directory
- [ ] Required Azure credentials are configured

## 🔧 Usage

### Quick Start (Bash/Zsh)

```powershell
# Navigate to SDK module directory
cd sdk\{service}\{module}

# Load environment variables  
. .github\skills\sdk-setup-env\scripts\load-env.ps1
```

## 📦 Required Environment Variables

### Common Variables (All Services)

| Variable | Description |
|----------|-------------|
| `CONTENT_UNDERSTANDING_ENDPOINT` | Service endpoint URL |
| `CONTENT_UNDERSTANDING_KEY` | Service key (optional if using AAD) |

## ⚠️ Security Notes

- Never commit `.env` files to version control
- Ensure `.gitignore` includes `.env`
- Use Azure Key Vault for production secrets

## 🌐 Cross-Language Support

| Language | Script | Notes |
|----------|--------|-------|
| Java | `load-env.sh` | Export vars before Maven |
| Python | `load-env.sh` | python-dotenv also works |
| .NET | `load-env.ps1` | launchSettings.json alternative |
| JavaScript | `load-env.sh` | dotenv package alternative |
