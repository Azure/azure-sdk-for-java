# Chunked Content Validation Memory Analysis

## Problem

Chunked uploads with structured message validation use ~650–1200 MB peak heap vs ~550 MB without validation. Single-part uploads with validation stay at ~547 MB.

## Data Flow Comparison

### Chunked No Validation (~550 MB)

1. **uploadInChunks** uses `BufferStagingArea` to accumulate 10 MB blocks from the 500 MB source.
2. Each block is passed to `stageBlockWithResponse(Flux)`.
3. **BlockBlobAsyncClient** calls `BinaryData.fromFlux(data, length, false)` → buffers the full block into a single byte[].
4. `stageBlockWithResponse(BinaryData)` sends the buffered body.
5. **Memory**: 500 MB (source) + 8 × 10 MB (BinaryData per concurrent block) ≈ 580 MB.

### Chunked Structured Message (650–1200 MB, variable)

1. Same flow through `BufferStagingArea`.
2. **BlockBlobAsyncClient** calls `BinaryData.fromFlux` (same as no-validation).
3. **StorageContentValidationPolicy** replaces the body with `Flux.from(body).limitRate(1).concatMap(encoder::encode)`.
4. **Memory**: ~900–1200 MB (observed); root cause not fully identified.

## Findings

- **Flux vs BinaryData in BlockBlobAsyncClient**: Reverting to always use `BinaryData.fromFlux` did *not* reduce memory; chunked structured message remained ~966 MB.
- **Concurrency**: Reducing `maxConcurrency` from 8 to 1 did *not* reduce memory; chunked structured message remained ~910 MB with concurrency=1.
- **Conclusion**: The extra ~400 MB in the structured message path is not from BlockBlobAsyncClient buffering or from concurrent block staging. The source is likely in the policy + HTTP pipeline (e.g., how the encoded Flux is consumed, Netty buffering, or RestProxy behavior). Further investigation would require profiling (e.g., heap dump or allocation tracing).

## Current State

- **BlockBlobAsyncClient**: Always uses `BinaryData.fromFlux` for both validation and non-validation (reverted Flux-direct optimization).
- **StorageContentValidationPolicy**: Passes body directly to `StructuredMessageEncoder` without `BufferStagingArea` re-buffering.
