---
name: cu-setup-env
description: Load environment variables from .env file into the current shell session. Parses .env file, skips comments and empty lines, handles quoted values, and exports variables. Use when setting up the Content Understanding SDK development environment or when environment variables need to be loaded from a .env file.
---

# CU Setup Environment

This skill loads environment variables from a `.env` file into the current shell session.

## When to Use

- Setting up the Content Understanding SDK development environment
- Loading environment variables from a `.env` file before running samples or tests
- Configuring Azure credentials and endpoint settings for local development

## Quick Start

```bash
# Source the script to load environment variables into current shell
source .cursor/skills/cu-setup-env/scripts/load-env.sh

# Or use the script directly
.cursor/skills/cu-setup-env/scripts/load-env.sh
```

## What It Does

The script:
1. Checks if `.env` file exists in the current directory
2. Parses each line for `KEY=value` format
3. Skips comments (lines starting with `#`) and empty lines
4. Removes surrounding quotes from values if present
5. Exports variables to the current shell session

## Prerequisites

1. **`.env` file**: Must exist in the current working directory
2. **Bash shell**: Requires bash (standard on Linux/macOS, available via WSL/Git Bash on Windows)

## Usage

### Source the script (recommended)

```bash
# This loads variables into the current shell
source .cursor/skills/cu-setup-env/scripts/load-env.sh
```

### Execute the script

```bash
# This also works but requires the script to export variables
.cursor/skills/cu-setup-env/scripts/load-env.sh
```

## .env File Format

The `.env` file should follow this format:

```bash
# Comments start with #
AZURE_ENDPOINT=https://your-endpoint.cognitiveservices.azure.com/
AZURE_KEY=your-api-key-here

# Values can be quoted
AZURE_REGION="eastus"

# Empty lines are ignored
ANOTHER_VAR=value
```

## Example Output

When successful, the script outputs:
```
Exported: AZURE_ENDPOINT
Exported: AZURE_KEY
Exported: AZURE_REGION
Environment variables loaded from .env file
```

## Error Handling

If the `.env` file is not found:
```
Error: .env file not found at .env
```

## Related Skills

- **`compile-cu-sdk-in-place`**: Compile the CU SDK (may require environment variables)
- **`run-cu-sample`**: Run CU SDK samples (requires Azure credentials from environment)
