---
name: run-all-samples
description: Compiles and runs all Content Understanding SDK samples (both sync and async variants), ensuring SDK and samples are compiled first. Saves all output to files in target/sample_result_out_txt/. Use when you need to test all samples, verify SDK functionality, or generate sample output files for documentation or debugging.
---

# Run All CU SDK Samples

This skill compiles and runs all Content Understanding SDK samples, ensuring prerequisites are met and capturing all output to files.

## When to Use

- Testing all samples after SDK changes
- Verifying SDK functionality across all sample scenarios
- Generating sample output files for documentation
- Debugging sample execution issues
- Running comprehensive sample validation

## Quick Start

```bash
# Navigate to CU SDK directory
cd sdk/contentunderstanding/azure-ai-contentunderstanding

# Run only samples that don't have output files yet (saves time)
./.github/skills/run-all-samples/scripts/run-all-samples.sh

# Reset and run all samples (deletes output directory and re-runs everything)
./.github/skills/run-all-samples/scripts/run-all-samples.sh --reset
```

## What It Does

The script performs these steps:

1. **Loads environment variables** from `.env` file (if present)
2. **Ensures CU SDK is compiled** - Uses `compile-cu-sdk-in-place` skill if needed
3. **Ensures samples are compiled** - Uses `run-cu-sample` skill if needed
4. **Enumerates all samples** - Discovers sync/async pairs and lists them
5. **Checks for existing output** - Skips samples that already have output files (unless `--reset` is used)
6. **Runs each sample** - Executes both sync and async variants (only those without output)
7. **Saves output** - Redirects stdout and stderr to `target/sample_result_out_txt/<SampleName>.out.txt`

### Smart Skipping

By default, the script **skips samples that already have output files** to save time. This is useful when:
- Running the script multiple times
- Only some samples failed previously
- You want to resume from where you left off

### Reset Mode

Use the `--reset` flag to:
- **Delete the entire output directory** (`target/sample_result_out_txt/`) to ensure a clean start
- Force re-running all samples, even if output files exist
- Useful when you want to regenerate all output files from scratch

## Prerequisites

1. **Maven**: Must be installed and available in PATH
2. **Java**: Java 8+ installed
3. **Environment**: `.env` file should contain required variables:
   - `CONTENTUNDERSTANDING_ENDPOINT` (required)
   - `CONTENTUNDERSTANDING_KEY` (optional, for key auth)
   - Other configuration variables as needed

## Output

All sample output is saved to:
- **Directory**: `target/sample_result_out_txt/`
- **Format**: `<SampleName>.out.txt` (e.g., `Sample01_AnalyzeBinary.out.txt`, `Sample01_AnalyzeBinaryAsync.out.txt`)
- **Content**: Complete stdout and stderr from each sample execution

## Sample Discovery

The script automatically discovers samples by:
- Scanning `src/samples/java/com/azure/ai/contentunderstanding/samples/`
- Grouping sync/async pairs by base name
- Running both variants when available

**Example sample groups:**
- `Sample00_UpdateDefaults` (sync) + `Sample00_UpdateDefaultsAsync` (async)
- `Sample01_AnalyzeBinary` (sync) + `Sample01_AnalyzeBinaryAsync` (async)
- And so on...

## Execution Flow

```
1. Check CU SDK compilation
   └─> If not compiled: Run compile-cu-sdk.sh

2. Check sample compilation
   └─> If not compiled: Run compile-all-samples.sh (builds classpath if needed)

3. Enumerate samples
   └─> List all sync/async pairs found

4. Run each sample
   ├─> If --reset: Delete output directory first
   ├─> Check if output file exists (skip if exists and not reset)
   ├─> Load .env variables
   ├─> Execute sample with proper classpath
   └─> Save output to target/sample_result_out_txt/<SampleName>.out.txt

5. Print summary
   └─> Total run, successful, failed, skipped counts
```

## Related Skills

- **`compile-cu-sdk-in-place`**: Compiles the CU SDK main code. Automatically invoked if SDK is not compiled.
- **`run-cu-sample`**: Compiles and runs individual samples. This skill uses its compilation logic.

## Troubleshooting

**Samples fail to compile**: Ensure CU SDK is compiled first. The script will attempt to compile it automatically.

**Missing .env file**: Samples may fail without proper configuration. Create a `.env` file with required variables.

**Output files not created**: Check that `target/sample_result_out_txt/` directory is writable.

**Authentication errors**: Verify `.env` contains valid `CONTENTUNDERSTANDING_ENDPOINT` and authentication credentials.

**Classpath issues**: The script automatically builds classpath if missing. If issues persist, manually run:
```bash
mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.txt -q
```

## Example Output

After running, you'll see:
```
==========================================
Step 1: Checking CU SDK compilation...
==========================================
CU SDK already compiled.

==========================================
Step 2: Checking sample compilation...
==========================================
Samples already compiled.

==========================================
Step 3: Enumerating samples...
==========================================
Found 17 sample groups:
  - Sample00_UpdateDefaults [sync] [async]
  - Sample01_AnalyzeBinary [sync] [async]
  ...

==========================================
Step 4: Running all samples...
==========================================
Running: Sample00_UpdateDefaults (sync) -> target/sample_result_out_txt/Sample00_UpdateDefaults.out.txt
  ✓ Success
...

==========================================
Summary
==========================================
Total samples run: 34
Successful: 34
Failed: 0
Output directory: target/sample_result_out_txt
```
