# Refactoring Comparison Process

When refactoring a file that has a main-branch baseline, follow this process
to avoid accidentally dropping behavioral logic.

## Steps

1. **Save the main-branch version into the workspace** so tools can read it:
   `
   git show main:<path> > sdk/cosmos/azure-cosmos-benchmark/main-baseline/<Filename>.java
   `
   Add the baseline directory to .gitignore.

2. **Use a subagent to read BOTH files completely** and produce an exhaustive
   numbered inventory. The prompt should be:
   > "For EVERY functional block in the main branch, check if it exists in
   > the current version. Do NOT skip anything because it seems minor.
   > Produce a table: feature, main line range, current status
   > (PRESENT/MISSING/MOVED), and if MISSING the exact code."

3. **Categorize every item BEFORE writing any code**:
   - MOVED to orchestrator (intentional)
   - Intentional improvement
   - MISSING (needs to be restored)
   - Trivial (comments only)

4. **Get user confirmation** on the categorization before proceeding.

5. **Restore all MISSING items in a single commit**, not incrementally.

## Why This Matters

Incremental "fix what we notice" leads to repeated rounds of:
- User spots a gap -> agent patches it -> user spots another gap
- Each round risks introducing new inconsistencies

The exhaustive inventory up front eliminates this cycle.
