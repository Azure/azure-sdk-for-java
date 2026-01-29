---
name: review-cu-sample-quality
description: Reviews Content Understanding SDK samples for code quality, correctness, and output accuracy. Enumerates sync/async sample pairs, runs samples to generate output, reviews code line-by-line for correctness and clarity, verifies output matches code behavior, checks comments for accuracy, and ensures async samples match sync functionality. Use when reviewing sample code quality, verifying sample correctness, or ensuring sync/async parity.
---

# Review CU Sample Quality

This skill performs comprehensive quality review of Content Understanding SDK samples, ensuring code correctness, clarity, output accuracy, and sync/async parity.

## When to Use

- Reviewing sample code quality and correctness
- Verifying sample outputs match code behavior
- Ensuring sync and async samples have functional parity
- Checking comment accuracy and clarity
- Validating sample documentation matches implementation

## Prerequisites

1. **Clean working directory**: The skill requires an empty git change list before starting
2. **Sample output files**: Either existing output files in `target/sample_result_out_txt/` or ability to run samples
3. **CU SDK compiled**: Samples require the SDK to be compiled (handled automatically)

## Workflow

### Step 1: Verify Clean Working Directory

**CRITICAL**: The skill must stop if there are uncommitted changes.

```bash
git status --porcelain
```

If output is non-empty, **STOP** and inform the user that changes must be committed or stashed first.

### Step 2: Enumerate Samples

Discover all samples in `src/samples/java/com/azure/ai/contentunderstanding/samples/` and group them by sync/async pairs.

**Sample naming pattern:**
- Sync: `Sample##_Name.java`
- Async: `Sample##_NameAsync.java`

**Grouping logic:**
- Extract base name by removing `Async` suffix
- Group samples with same base number and name
- Example: `Sample00_UpdateDefaults` + `Sample00_UpdateDefaultsAsync` = one group

**Output format:**
```
Found N sample groups:
  - Sample00_UpdateDefaults [sync: Sample00_UpdateDefaults.java] [async: Sample00_UpdateDefaultsAsync.java]
  - Sample01_AnalyzeBinary [sync: Sample01_AnalyzeBinary.java] [async: Sample01_AnalyzeBinaryAsync.java]
  ...
```

### Step 3: Ensure Sample Outputs Exist

For each sample group, check if output files exist in `target/sample_result_out_txt/`:
- Sync: `target/sample_result_out_txt/Sample##_Name.out.txt`
- Async: `target/sample_result_out_txt/Sample##_NameAsync.out.txt`

**If outputs missing:**
- Use the `run-all-samples` skill to generate outputs
- The skill will skip samples that already have outputs

**If outputs exist:**
- Proceed to review (no need to re-run)

### Step 4: Review Sync Samples

For each sync sample, perform line-by-line review:

#### 4.1 Understand Sample Purpose
- Read the class-level JavaDoc comment
- Understand what the sample demonstrates
- Identify key operations and expected behavior

#### 4.2 Review Code Correctness
Check for:
- **Logic errors**: Incorrect API usage, wrong method calls, missing error handling
- **Type safety**: Correct types, proper casting, null handling
- **Resource management**: Proper cleanup, try-with-resources where needed
- **Best practices**: Following Java and Azure SDK conventions

#### 4.3 Review Code Clarity
Check for:
- **Readability**: Clear variable names, logical flow, appropriate abstractions
- **Structure**: Well-organized code, appropriate method extraction
- **Comments**: Code is self-documenting or has helpful comments

#### 4.4 Verify Output Matches Code
Compare the sample code with its output file:

1. **Trace execution flow**: Follow the code path and identify what should be printed
2. **Check for missing output**: Verify all expected print statements appear in output
3. **Check for incorrect output**: Verify output values match code logic
4. **Check for unexpected output**: Look for errors, exceptions, or warnings not explained in code

**Common issues:**
- Missing `System.out.println()` output
- Incorrect variable values in output
- Exception messages not matching code
- Missing error handling output

#### 4.5 Make Code Changes
After identifying issues:
- Fix correctness problems
- Improve clarity where needed
- Ensure output matches code behavior
- Add missing error handling or output statements

#### 4.6 Review Comments
Check both class-level JavaDoc and inline comments:

**Class-level JavaDoc:**
- Accurately describes sample purpose
- Lists all demonstrated features
- Prerequisites are correct and complete
- Examples match actual code

**Inline comments:**
- Code region markers (BEGIN/END) are correct
- Comments explain non-obvious logic
- Comments match actual code behavior
- No outdated or incorrect comments

**Make changes as needed** to ensure comments are accurate and helpful.

#### 4.7 Generate Review Summary

For each sync sample that had changes, create a markdown file:

**File naming**: `SampleReview_YYYYMMDD_HHMMSS.md` (e.g., `SampleReview_20260126_143022.md`)

**File location**: `target/sample_reviews/` (create directory if needed)

**Summary format:**
```markdown
# Sample Review: Sample##_Name

**Date**: YYYY-MM-DD HH:MM:SS
**Sample Type**: Sync
**File**: `src/samples/java/com/azure/ai/contentunderstanding/samples/Sample##_Name.java`

## Sample Purpose
[Brief description of what the sample demonstrates]

## Changes Made

### Code Correctness
- [ ] Issue: [Description]
  - Fix: [What was changed]
  
### Code Clarity
- [ ] Issue: [Description]
  - Fix: [What was changed]

### Output Verification
- [ ] Issue: [Description]
  - Fix: [What was changed]

### Comments
- [ ] Issue: [Description]
  - Fix: [What was changed]

## Summary
[Overall summary of changes and improvements]
```

### Step 5: Review Async Samples

For each async sample, perform the same review as sync samples, PLUS:

#### 5.1 Verify Functional Parity with Sync Sample

Compare async sample with its sync counterpart:

**Must match:**
- Same operations performed (in same order)
- Same error handling logic
- Same output messages (except file name differences)
- Same business logic and flow

**Expected differences (acceptable):**
- File name: `Sample##_NameAsync.java` vs `Sample##_Name.java`
- Client type: `ContentUnderstandingAsyncClient` vs `ContentUnderstandingClient`
- Reactive patterns: `Mono`/`Flux`, `block()`, `subscribe()`, etc.
- Output file name in output: `Sample##_NameAsync.out.txt` vs `Sample##_Name.out.txt`

**Must NOT differ:**
- Core functionality
- Error handling approach
- Output content (except file names)
- Business logic

#### 5.2 Verify Output Matches Sync Output

Compare async output with sync output:

1. **Same operations**: Verify same operations are performed
2. **Same results**: Output values should match (except file names)
3. **Same error handling**: Error messages should match
4. **Reactive patterns**: Verify proper use of reactive operators

**Common async-specific issues:**
- Missing `block()` calls causing incomplete execution
- Incorrect reactive operator usage
- Missing error handling in reactive chains
- Output order differences (acceptable if due to async nature)

#### 5.3 Make Changes

After identifying issues:
- Fix functional parity problems
- Ensure output matches sync sample
- Fix reactive pattern issues
- Update comments to match sync sample

#### 5.4 Update Review Summary

If async sample had changes, update the corresponding review summary file (same file as sync sample, or create new one if sync had no changes):

```markdown
## Async Sample Review: Sample##_NameAsync

**File**: `src/samples/java/com/azure/ai/contentunderstanding/samples/Sample##_NameAsync.java`

### Functional Parity Issues
- [ ] Issue: [Description]
  - Fix: [What was changed]

### Output Verification
- [ ] Issue: [Description]
  - Fix: [What was changed]

### Reactive Pattern Issues
- [ ] Issue: [Description]
  - Fix: [What was changed]
```

## Review Checklist

For each sample group, verify:

### Sync Sample
- [ ] Code is correct and handles edge cases
- [ ] Code is clear and readable
- [ ] Output file matches code behavior
- [ ] All expected output is present
- [ ] No unexpected errors or warnings
- [ ] Class-level JavaDoc is accurate
- [ ] Inline comments are accurate and helpful

### Async Sample
- [ ] Functional parity with sync sample (100% match)
- [ ] Output matches sync sample output (except file names)
- [ ] Reactive patterns are correct
- [ ] Error handling matches sync sample
- [ ] Comments match sync sample

## Output Files

All review summaries are saved to:
- **Directory**: `target/sample_reviews/`
- **Format**: `SampleReview_YYYYMMDD_HHMMSS.md`
- **Content**: Detailed review findings and changes made

## Related Skills

- **`run-all-samples`**: Generates sample output files. Automatically invoked if outputs are missing.
- **`compile-cu-sdk-in-place`**: Compiles CU SDK. Required before running samples.
- **`create-cu-async-sample`**: Creates async samples. Useful reference for async patterns.

## Example Workflow

```
1. Check git status → Clean ✓
2. Enumerate samples → Found 17 groups
3. Check outputs → 12 missing, 5 exist
4. Run run-all-samples → Generated 12 outputs
5. Review Sample00_UpdateDefaults (sync)
   - Code review ✓
   - Output verification ✓
   - Made 2 fixes
   - Created SampleReview_20260126_143022.md
6. Review Sample00_UpdateDefaultsAsync (async)
   - Functional parity check ✓
   - Output comparison ✓
   - Made 1 fix
   - Updated SampleReview_20260126_143022.md
7. Continue with remaining samples...
```

## Notes

- **Stop on errors**: If git status shows changes, stop immediately
- **Incremental reviews**: Review summaries are created per sample, allowing incremental progress
- **Output preservation**: Existing outputs are preserved unless explicitly regenerated
- **Change tracking**: All changes are documented in review summary files
