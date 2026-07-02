# PR Title: [test] Add regression coverage for bounded Java receive buffering

## Summary

The original PERF-002 report was stale. `AmqpReceiveLinkProcessor` already bounds upstream receive buffering with `onBackpressureBuffer(maxQueueSize, BufferOverflowStrategy.ERROR)` and derives `maxQueueSize` from `prefetch * 2`. This change adds focused regression coverage and updates the audit artifacts to reflect the current implementation.

## Changes

- Add regression tests in `AmqpReceiveLinkProcessorTest`
- Verify the processor bounds buffered messages to `prefetch * 2`
- Verify credit calculation accounts for queued messages before requesting more work
- Update PERF-002 audit text to describe the current verified behavior

## Validation

- Focused Maven run: `mvn -Dtest=AmqpReceiveLinkProcessorTest test`
- Result: `BUILD SUCCESS`
- Test result: `AmqpReceiveLinkProcessorTest ran 18 tests with 0 failures, 0 errors, 0 skipped`

## Notes

- No product-code refactor was needed because the bounded-buffer behavior is already present locally.
- The remaining work for this item was explicit regression coverage and audit cleanup.

## Related

- Performance report: [REPORT.md](./REPORT.md)