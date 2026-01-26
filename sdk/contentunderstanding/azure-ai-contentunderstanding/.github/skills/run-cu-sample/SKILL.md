---
name: run-cu-sample
description: Build and run Content Understanding SDK samples using in-place compilation in the local enlistment. Compiles and executes CU SDK samples without installing the package. Requires CU SDK to be compiled first (use compile-cu-sdk-in-place skill). Use when the user wants to compile and execute CU SDK samples without installing the package, or when working with local SDK development in the enlistment.
---

# Run Content Understanding SDK Samples

This skill helps you build and run Content Understanding SDK samples using in-place compilation. This approach compiles samples locally and runs them without requiring a Maven install or package installation.

**Prerequisite**: The CU SDK must be compiled first. Use the `compile-cu-sdk-in-place` skill to compile the SDK before running samples.

## When to Use

- Running samples during SDK development
- Testing changes to the SDK without installing
- Quick iteration on sample code
- Debugging sample execution issues

## Quick Start

**First, compile the CU SDK** (if not already compiled):
- Use the `compile-cu-sdk-in-place` skill, or
- Run: `./scripts/compile-cu-sdk.sh` from the `compile-cu-sdk-in-place` skill

Then run a specific sample:

```bash
# Build and run a sample (assumes CU SDK is already compiled)
./scripts/build-and-run.sh Sample02_AnalyzeUrlAsync
```

Or use the individual steps:

```bash
# Navigate to CU SDK directory
cd sdk/contentunderstanding/azure-ai-contentunderstanding

# 1. Build classpath
mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.txt -q

# 2. Compile all samples
./scripts/compile-all-samples.sh

# 3. Run a sample
./scripts/run-sample.sh Sample02_AnalyzeUrlAsync
```

## Available Samples

All samples are in `src/samples/java/com/azure/ai/contentunderstanding/samples/`:

- `Sample00_UpdateDefaults` / `Sample00_UpdateDefaultsAsync`
- `Sample01_AnalyzeBinary` / `Sample01_AnalyzeBinaryAsync`
- `Sample02_AnalyzeUrl` / `Sample02_AnalyzeUrlAsync`
- `Sample03_AnalyzeInvoice` / `Sample03_AnalyzeInvoiceAsync`
- `Sample04_CreateAnalyzer` / `Sample04_CreateAnalyzerAsync`
- `Sample05_CreateClassifier` / `Sample05_CreateClassifierAsync`
- And more...

## Prerequisites

1. **CU SDK Compiled**: The CU SDK must be compiled first. Use the `compile-cu-sdk-in-place` skill or ensure `target/classes/` contains compiled SDK classes.

2. **Environment Variables**: Ensure `.env` file is loaded or set:
   - `CONTENTUNDERSTANDING_ENDPOINT` (required)
   - `CONTENTUNDERSTANDING_KEY` (optional, for key auth)
   - Other configuration variables as needed

3. **Maven**: Must be installed and available in PATH

4. **Java**: Java 8+ installed (samples compile with `--release 8`)

## Build Process

The in-place build process (assumes CU SDK is already compiled):

1. **Build Classpath**: Extracts all dependency JAR paths to `target/classpath.txt`
2. **Compile Samples**: Compiles sample code using the classpath
3. **Run Sample**: Executes the sample with proper classpath

**Note**: CU SDK compilation is handled by the `compile-cu-sdk-in-place` skill. Ensure the SDK is compiled before using this skill.

For detailed background, see [references/in-place-build.md](references/in-place-build.md).

## Related Skills

- **`compile-cu-sdk-in-place`**: Compiles the CU SDK main code in place. **Required before using this skill** if the SDK is not already compiled. This skill handles SDK compilation for development and debugging purposes.

## Scripts

The skill includes helper scripts in `scripts/`:

- `build-and-run.sh` - Complete build and run workflow (uses worker scripts, assumes CU SDK is compiled)
- `build-classpath.sh` - Generate classpath file
- `compile-all-samples.sh` - Compile all sample code (checks if CU SDK is compiled)
- `run-sample.sh` - Run a compiled sample

## Troubleshooting

**Classpath issues**: Ensure `target/classpath.txt` exists. Rebuild with:
```bash
mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.txt -q
```

**Compilation errors**: Check that CU SDK is compiled. Use the `compile-cu-sdk-in-place` skill to compile the SDK first.

**Missing environment variables**: Load `.env` file:
```bash
set -a
source .env
set +a
```

**Sample not found**: Verify the sample class name matches exactly (case-sensitive).
