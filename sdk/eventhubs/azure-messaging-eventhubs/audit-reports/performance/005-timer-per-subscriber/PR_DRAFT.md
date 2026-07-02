# PR Title: [performance] Share Java synchronous receive timeout scheduling

## Summary

This change keeps synchronous receive timeout handling on a shared scheduler instead of allocating timeout infrastructure per subscriber. The goal is to reduce thread churn and timeout-management overhead during repeated synchronous receive calls while preserving the current timeout semantics.

## Changes

- `SynchronousEventSubscriber.java`: keep timeout scheduling on the shared scheduler used by the local implementation
- `SynchronousEventSubscriber.java`: cancel scheduled timeout work when the subscriber completes, errors, or receives a value
- `SynchronousEventSubscriberTest.java`: add focused coverage for timeout scheduling and cancellation behavior

## Validation

- Validation for this item is currently unit-test based through `SynchronousEventSubscriberTest`
- The branch also includes focused unit suites for the other reconciled performance changes and emulator-backed benchmark coverage for the producer-side hot paths

## Benchmark Note

The current public benchmark harness does not have a dedicated section for `SynchronousEventSubscriber`, so this item does not yet have a standalone emulator-backed latency number. The change is isolated in commit `df8856a` and validated through source inspection plus focused tests.

## Risk

- No public API changes
- Timeout behavior remains internal and test-covered
- Main behavioral change is lower timeout scheduling overhead under repeated synchronous receive usage

## Related

- Performance report: [REPORT.md](./REPORT.md)