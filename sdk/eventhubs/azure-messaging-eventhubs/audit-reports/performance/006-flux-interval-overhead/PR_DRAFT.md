# PR Title: [performance] Reset Java buffered publish timeout with a single delay stream

## Summary

This change keeps `EventDataAggregator` on a single sink-driven `switchMap(... Mono.delay(...))` timeout reset stream instead of the draft per-event interval approach described in the original audit notes. That preserves the existing batching behavior while reducing scheduler churn in the buffered publish path.

## Changes

- `EventDataAggregator.java`: drive batch timeout resets from a single sink-backed delayed stream
- Keep the existing lock-based batch publication flow intact while resetting only the active timeout window
- Refresh the audit text to match the current implementation rather than the original draft design notes

## Validation

Emulator-backed buffered producer benchmark runs on commit `94f2e15`, 5 samples:

- enqueue: samples `[156, 152, 159, 175, 152]`, median `156 ms`
- flush: samples `[16, 16, 16, 15, 16]`, median `16 ms`
- successful batches: samples `[4, 4, 4, 4, 4]`, median `4`
- failed batches: samples `[0, 0, 0, 0, 0]`, median `0`
- throughput: samples `[5759, 5939, 5653, 5247, 5904]`, median `5759 msgs/sec`

For comparison, the immediately preceding PERF-004 commit `7ab2c7b` measured:

- enqueue: median `168 ms`
- flush: median `17 ms`
- throughput: median `5367 msgs/sec`
- failed batches: median `0`

These numbers are directional, but they show the timeout-stream change did not regress buffered publishing and slightly improved the measured harness path.

## Risk

- No public API changes
- Internal batching logic remains Reactor-based and test-covered
- Main risk is timeout semantics drift, which is why the draft stays tightly aligned to the current code instead of the earlier speculative alternatives

## Related

- Performance report: [REPORT.md](./REPORT.md)