# STG105 Data Locality — implementation notes

## What was built

This branch (`stg105/dataLocality`, based on `feature/storage/stg105base`)
implements the full **Data Locality** feature across `azure-storage-common`,
`azure-storage-blob`, and `azure-storage-file-datalake`, parity-ported from
[Azure/azure-sdk-for-net#57554](https://github.com/Azure/azure-sdk-for-net/pull/57554)
("Data Locality Support (origin branch)").

Concretely:

- A new paged REST operation, `GET /{container}/{blob}?comp=layout`, returns
  a blob's on-disk **layout** (a set of byte ranges, each pointing at the
  storage-cluster endpoint that physically holds that range) alongside the
  blob's standard properties in a single call. Surfaced as
  `BlobClient`/`BlobAsyncClient.getLayout(...)` (paged, `PagedIterable`/
  `PagedFlux<BlobLayoutInfo>`), and mirrored for Data Lake as
  `DataLakeFileClient`/`DataLakeFileAsyncClient.getLayout(...)`.
- A new `x-ms-download-hint` response header on the existing `Download`
  operation (`BlobDownloadHeaders.getDownloadHint()` / `DownloadHint`). The
  service uses this header to hint that a client performing a large download
  should call `getLayout` and route subsequent range requests to the
  endpoints it names.
- A generic, reusable **`AutoRefreshingCache<T>`** (`azure-storage-common`)
  and a pipeline policy **`DataLocalityPolicy`** (`azure-storage-common`)
  that rewrites an outgoing request's host/port when a per-call `Context`
  property (`DataLocalityPolicy.LAYOUT_ENDPOINT_KEY`) is set.
- Wiring of the above into `BlobAsyncClientBase.downloadToFileImpl` and
  `BlobClientBase.openInputStream`/`BlobInputStream`, gated behind a new
  `enableDataLocality` opt-in flag on `BlobDownloadToFileOptions` and
  `BlobInputStreamOptions` (default `false`). When enabled and the initial
  response's `DownloadHint` is `LAYOUT`, a per-download layout cache is built
  and each chunk/read beyond the first is routed to its resolved endpoint.
- The equivalent opt-in flag and `getLayout` mirror on the Data Lake side,
  which required no independent chunking/routing logic since
  `DataLakeFileClient`/`DataLakeFileAsyncClient` already delegate their
  download/stream implementations to the wrapped `BlockBlobClient`/
  `BlockBlobAsyncClient`.

## How it works

### Wire protocol (generated layer — commit `1d0324b1192`)

`sdk/storage/azure-storage-blob/swagger/README.md`'s `input-file` was
pointed at the same swagger spec commit the .NET PR uses
(`nickliu-msft/azure-rest-api-specs@5c678e4`, `2026-10-06/blob.json` — **an
unmerged fork/preview spec**, see "Outstanding items"), and `autorest` was
re-run. That regeneration produced `BlobsImpl.getLayout(...)`, `BlobLayout`/
`BlobLayoutRanges`/`BlobLayoutEndpoints` XML models, `BlobsGetLayoutHeaders`,
and `DownloadHint` (`implementation.models`) — none hand-edited.

### Hand-written Blob layer (commit `f621d73b3b3`)

- **`com.azure.storage.blob.models.DownloadHint`** — public
  `ExpandableStringEnum` wrapper over the generated type.
- **`BlobDownloadHeaders.getDownloadHint()`** — passthrough to the generated
  header.
- **`BlobGetLayoutOptions`** — request options bag (`range`,
  `requestConditions`, `maxResultsPerPage`), shaped like
  `ListPageRangesOptions`.
- **`BlobLayoutInfo`** — public per-page result model (properties +
  `getRanges()`).
- **`BlobLayoutRange`** — immutable `(HttpRange, endpoint)` pair. Unlike
  .NET's generated `BlobLayoutRangesRangeItem` (which only carries an
  `endpointIndex`), the endpoint is resolved eagerly by
  `ModelHelper.transformBlobLayoutInfo` so callers never see the raw index —
  a deliberate simplification of the public surface (one list instead of two
  + an index contract).
- **`BlobAsyncClientBase.getLayout(BlobGetLayoutOptions)`** /
  **`BlobClientBase.getLayout(BlobGetLayoutOptions, Context)`** — public
  paged entry points, following the exact existing
  `PageBlobAsyncClient.listPageRanges` pattern (page function ->
  `azureBlobStorage.getBlobs().getLayoutWithResponseAsync(...)` ->
  `PagedResponseBase`, threading the service's `NextMarker` as the
  continuation token).

### Data locality infrastructure (`azure-storage-common`, commits `1c5ccb28247`/`f5a85c5489d`)

- **`AutoRefreshingCache<T>`** — genericized from an existing, more complete
  `StorageSessionCredentialCache` (from PR
  [#49471](https://github.com/Azure/azure-sdk-for-java/pull/49471), branch
  `session-private-drop-with-env`), per explicit direction to reuse that
  cache design rather than build a new one from scratch, since it is also
  needed by an unrelated, separately-tracked "sessions" feature. Constructed
  with `Supplier<T> syncCreator`, `Supplier<Mono<T>> asyncCreator`, and a
  `Function<T, OffsetDateTime> expirationExtractor` (rather than requiring
  `T` to implement a new interface, to avoid forcing value types into a
  shape they don't otherwise need). Public API: `getValidSync()`,
  `getValidAsync()`, `invalidate(T)`, `refreshInBackground()`,
  `forceRefreshInBackground()`. Thread-safe, single-flight (de-duplicated)
  creation, jittered proactive background refresh.
- **`DataLocalityPolicy`** — an `HttpPipelinePolicy` (`PER_RETRY`, so the URL
  rewrite reapplies on every retry attempt, not just the first). No-op
  unless `Context` data under `LAYOUT_ENDPOINT_KEY` is a non-empty `String`;
  when present, rewrites the outgoing request's host/port to that endpoint
  while preserving the original `host:port` on the `Host` header (required
  for server-side TLS-SNI/virtual-hosting). Malformed endpoints are logged
  and skipped, never thrown. Registered unconditionally in
  `BuilderHelper.java`'s policy list (safe no-op for the vast majority of
  requests that never set the context key).

### Blob download/stream wiring (commit `86b51ef2bbe`)

- **`BlobLayoutCacheValue`** (`azure-storage-blob`) — the cache value type:
  `List<BlobLayoutRange> ranges` (non-null non-empty = routing applies;
  non-null empty = service returned no layout; `null` = `getLayout` failed,
  soft-fail) + `OffsetDateTime expiresOn`.
- **`BlobLayoutRangeResolver.resolveEndpoint(long chunkRangeStart, List<BlobLayoutRange> ranges)`**
  — binary search for the endpoint of the segment covering an offset,
  mirroring .NET's `BlobExtensions.GetLayoutEndpoint`.
- **`BlobAsyncClientBase.fetchLayoutCacheValueAsync(...)`** and a
  context-propagating internal `getLayout(options, context)` overload —
  fetch and flatten all pages of a `getLayout` call into one
  `BlobLayoutCacheValue`, soft-failing (caching a `null`-ranges value rather
  than propagating the exception) on `BlobStorageException`.
- **`downloadToFileImpl`**: after the first chunk resolves, if
  `enableDataLocality` and the initial response's `DownloadHint` is
  `LAYOUT` and there are bytes remaining beyond the first chunk, builds one
  `AutoRefreshingCache<BlobLayoutCacheValue>` per download (etag-locked,
  scoped to the range beyond the first chunk) and wraps the chunk-download
  function used for chunks 1+ (chunk 0 already completed before the cache
  exists — this mirrors .NET's `PartitionedDownloader` exactly) to resolve
  and set `LAYOUT_ENDPOINT_KEY` per chunk.
- **`openInputStream`/`BlobInputStream.dispatchRead`**: same pattern, but
  scoped to the full requested stream range (rather than "remaining after
  the first chunk") since a stream can seek to arbitrary offsets, not just
  monotonically increasing chunks.

### Data Lake mirror (commit `96ad0aa4fd1`)

`DataLakeFileClient`/`DataLakeFileAsyncClient` already delegate
`readToFileWithResponse`/`openInputStream` to a wrapped `BlockBlobClient`/
`BlockBlobAsyncClient`. This made the Data Lake port almost entirely
additive:

- New `DataLakeFileLayoutInfo`/`DataLakeFileLayoutRange` public models and
  `DataLakeFileGetLayoutOptions` (Data Lake-domain shapes, field-mapping
  conventions matched to the existing `Transforms.toPathProperties`).
- `DataLakeFileClient`/`DataLakeFileAsyncClient.getLayout(...)` **proxies**
  the already-implemented `BlockBlobClient`/`BlockBlobAsyncClient.getLayout`
  and maps `BlobLayoutInfo` -> `DataLakeFileLayoutInfo`. This is an explicit,
  documented scope decision (see the `@implNote`-style comment at the call
  site): Data Lake does not yet have its own generated layout REST
  operation, so rather than duplicate a whole swagger-regeneration cycle for
  an operation that (as far as this repo's evidence shows) returns the same
  shape either way, the existing Blob-layer implementation is reused
  directly. Revisit if/when a Data Lake-native `getLayout` swagger operation
  is added.
- `enableDataLocality` added to `ReadToFileOptions`/
  `DataLakeFileInputStreamOptions`, forwarded to the underlying
  `BlobDownloadToFileOptions`/`BlobInputStreamOptions` via `Transforms`. No
  independent routing/chunking logic was needed on the Data Lake side.

### Tests

- `BlobClientBaseGetLayoutApiTests`/`BlobClientBaseGetLayoutAsyncApiTests`
  (commit `d75a76d0146`) — `getLayout` API surface: success, empty blob,
  ranged request, page-size limiting, continuation tokens, request
  conditions (success + failure), not-found error.
- `AutoRefreshingCacheTests` (commit `ac33c76c802`), `DataLocalityPolicyTest`
  — unit tests for the shared infrastructure.
- `BlobLayoutRangeResolverTests`/`BlobLayoutCacheValueTests` (commit
  `ca147a91fd8`) — binary-search resolution edge cases (single/multiple/
  unaligned/many ranges, out-of-range offsets) and cache-value state
  (populated/empty/failed, defensive immutability).
- `BlobDataLocalityDownloadApiTests` (commit `bdc049f4109`) — end-to-end
  `downloadToFileWithResponse` (single-chunk, multi-chunk, disabled/default)
  and `openInputStream` (full and partial range) with `enableDataLocality`,
  recorded against a real (preprod) storage account. **Finding**: that
  account does not set `x-ms-download-hint: Layout` on plain download
  responses, so these recordings validate the wiring's regression-safety
  (the opt-in flag never changes downloaded bytes) end-to-end against a real
  service, rather than the true endpoint-routing behavior — the
  routing-selection logic itself (`DownloadHint` gating, per-chunk endpoint
  resolution, `DataLocalityPolicy`'s URL rewrite) is covered by the unit
  tests above plus code review, not by a live "actually got redirected to a
  different endpoint" observation. This is a known, documented test-coverage
  gap; see "Outstanding items".

## Why key decisions were made

- **`BlobLayoutRange` resolves the endpoint eagerly** rather than exposing
  the raw `endpointIndex` + a separate endpoint list, unlike .NET's
  `BlobLayoutSegment`/private index-resolution helper. Simpler public
  surface, matches Java's general preference for ready-to-use result types
  (e.g. `PageRangeItem` vs. raw `PageRange`), and nothing in the public
  `getLayout` surface needs the unresolved index.
- **`AutoRefreshingCache<T>` is parameterized via suppliers/a
  `Function<T, OffsetDateTime>` rather than an interface** (unlike .NET's
  `IExpiringValue`), to avoid forcing cached value types (here,
  `BlobLayoutCacheValue`; elsewhere, session credentials) to implement a new
  contract.
- **The unmerged swagger spec commit was used as-is** (matching the .NET
  PR, which is also still open) rather than guessing at the eventual merged
  shape — flagged clearly as needing revisit.
- **Data Lake proxies Blob's `getLayout` rather than generating its own REST
  client** for the same reason described above — a deliberate, bounded scope
  decision given no Data Lake-native layout operation exists yet in this
  repo's swagger, not an oversight.
- **Plan-then-delegate-then-review build process** (per the
  `azure-storage-stg-parity` skill): mechanical implementation was planned
  in detail against the actual .NET source (fetched via `gh`/raw GitHub URLs
  for the specific PR), delegated to a GPT-5-class model at low/medium
  reasoning effort, then independently reviewed and re-verified (compile,
  checkstyle, spotbugs, and the full relevant existing test suites re-run by
  the reviewer, not just trusting the builder's self-report) at full
  capability before committing. No build attempt in this feature required
  the loop's retry path (all passed review on the first attempt); one
  earlier delegation (`getLayout`'s hand-written layer) needed one direct
  fix for a missing `objectReplicationSourcePolicies` field.

## Outstanding items

1. **Unmerged spec dependency.** The swagger spec pointed at
   (`nickliu-msft/azure-rest-api-specs@5c678e4`) is not yet merged into
   `Azure/azure-rest-api-specs`, and the sourcing .NET PR
   (Azure/azure-sdk-for-net#57554) is still open. Re-run `autorest` against
   the final merged commit once available and diff against what's here.
2. **True endpoint-routing behavior is not covered by a live/recorded
   test.** No available test account sets `x-ms-download-hint: Layout` on
   download responses (this is a fictional STG105 exercise; the feature
   isn't implemented server-side anywhere accessible here), so the
   `enableDataLocality=true` recordings only prove the no-op/regression path
   end-to-end. The actual routing decision logic (resolving an endpoint and
   rewriting the request URL) is proven correct by unit tests
   (`BlobLayoutRangeResolverTests`, `DataLocalityPolicyTest`) and code
   review, not by an observed live redirect. If/when a real service starts
   returning this header, re-verify with a fresh recording.
3. **Data Lake's `getLayout` is a proxy, not a native REST operation** — see
   "Why key decisions were made" above. If a Data Lake-native `comp=layout`
   swagger operation is ever added, this should be revisited (the current
   proxy approach is functionally complete but bypasses the
   generated-vs-hand-written classification the rest of this feature went
   through).

## Step 12 — Final reconciliation against the .NET PR

Performed a fresh, whole-feature comparison against .NET PR
Azure/azure-sdk-for-net#57554's full changed-file list (69 files,
re-fetched via `gh pr diff 57554 --repo Azure/azure-sdk-for-net --name-only`
independent of earlier planning notes). Outcome, by category:

**Covered (Java equivalent confirmed present and wired):**
- Core wiring: `BlobBaseClient.cs`/`BlobClientOptions.cs` (adds
  `DataLocalityPolicy` to the pipeline) → `BlobClientBase.java` /
  `BlobAsyncClientBase.java` / `BuilderHelper.java`
  (`policies.add(new DataLocalityPolicy())`), proven end-to-end by
  `BlobDataLocalityWiringTests.java`.
- `Shared/AutoRefreshingCache.cs` → `AutoRefreshingCache.java`, with its own
  unit test class `AutoRefreshingCacheTest.java` (mirrors .NET's
  `AutoRefreshingCacheTests.cs`).
- `Shared/DataLocalityPolicy.cs` → `DataLocalityPolicy.java` (see
  architectural note below on the split with the cache).
- All `Models/BlobLayout*`/`BlobGetLayoutOptions`/`BlobLayoutInfo`
  generated + hand-written models → `BlobLayoutInfo.java`,
  `BlobLayoutRange.java`, `BlobGetLayoutOptions.java`, `DownloadHint.java`
  (both generated and hand-written variants).
- `Models/BlobDownloadOptions.cs`/`BlobDownloadToOptions.cs`/
  `BlobOpenReadOptions.cs` (`EnableDataLocality`) →
  `BlobDownloadToFileOptions.java`/`BlobInputStreamOptions.java`
  (`enableDataLocality`, confirmed wired).
- DataLake side: `DataLakeFileClient.cs`,
  `Models/DataLakeFileGetLayoutOptions.cs`,
  `Models/DataLakeFileLayoutEndpoints*`/`DataLakeFileLayoutRanges*` →
  `DataLakeFileClient.java`/`DataLakeFileAsyncClient.java`,
  `DataLakeFileGetLayoutOptions.java`,
  `DataLakeFileLayoutInfo.java`/`DataLakeFileLayoutRange.java` (consolidated,
  matching the Blob-side pattern). `Models/DataLakeFileReadToOptions.cs`/
  `DataLakeOpenReadOptions.cs` → `ReadToFileOptions.java`/
  `DataLakeFileInputStreamOptions.java` (`enableDataLocality`, confirmed
  wired).
- `DataLakeExtensions.cs` → `Transforms.java`.
- Tests: `tests/BlobBaseClientTests.cs`, `tests/OpenReadDataLocalityTests.cs`
  → `BlobApiTests.java`/`BlobAsyncApiTests.java`.
  `tests/PartitionedDownloaderTests.cs` (Moq wiring tests) →
  `BlobDataLocalityWiringTests.java`. `tests/FileClientTests.cs` →
  `FileApiTest.java`/`FileAsyncApiTests.java`.

**Legitimate architectural/idiomatic differences (not gaps):**
- `Shared/IExpiringValue.cs` has no direct Java interface counterpart:
  Java's `AutoRefreshingCache<T>` takes a
  `Function<T, OffsetDateTime> expirationExtractor` constructor argument
  instead of requiring `T` to implement an interface — an idiomatic
  functional-parameter substitute for the same contract.
- `Models/BlobLayoutSegment.cs`/`BlobLayoutSegmentCacheValue.cs` have no
  standalone Java class: the per-blob layout cache and TTL/refresh logic
  live inline in `BlobClientBase`/`BlobAsyncClientBase` via the generic
  `AutoRefreshingCache<T>`, rather than a dedicated cache-value struct.
- `Models/GetLayoutAsyncCollection.cs` has no Java counterpart: Java uses
  `azure-core`'s built-in `PagedFlux`/`PagedIterable` for `getLayout`'s
  paging instead of a bespoke per-feature async collection type.
- `Models/BlobsModelFactory.cs`/`DataLakeModelFactory.cs` additions have no
  Java counterpart: the Java Storage SDK has no "public model factory for
  test mocking" pattern anywhere in the module (a pre-existing, module-wide
  ecosystem difference from .NET, not something introduced or missed by
  this feature).
- `DataLakeClientOptions.cs` explicitly adds `DataLocalityPolicy` to its own
  pipeline in .NET; Java's DataLake builders do **not** separately register
  the policy. Confirmed **not** a gap: every DataLake operation that needs
  locality-aware routing (`getLayout`, `readToFile`, `openInputStream`)
  proxies to an internal `BlockBlobClient`/`BlockBlobAsyncClient` (see
  `DataLakeFileClient.getLayout`/`readToFileWithResponse`/`openInputStream`),
  whose pipeline already has `DataLocalityPolicy` registered via
  `BuilderHelper`. This mirrors .NET's own documented behavior ("This method
  currently proxies the Blob service getLayout API").
- `api/*.cs` (API-surface snapshot files), `*.csproj` (project files),
  `autorest.md` (swagger codegen config), and each module's `assets.json`
  are .NET/tooling-specific and have no meaningful Java counterpart to
  reconcile against individually (Java's own versioning, checkstyle/revapi,
  and `assets.json` files serve the equivalent purposes and are tracked
  separately).

**Confirmed gap, intentionally deferred (not silently missed):**
- `Models/BlobDownloadOptions.cs`/`DataLakeFileReadOptions.cs` add a manual
  `LayoutEndpoint` escape-hatch property (letting a caller bypass automatic
  caching/resolution and specify their own layout endpoint for a single,
  non-chunked download). Java does not expose an equivalent property on a
  download-options object, because Java has no `BlobDownloadOptions`-style
  bundle for its plain (non-parallel) download methods in the first place.
  The equivalent capability **does exist** in Java, just via the public
  `DataLocalityPolicy.LAYOUT_ENDPOINT_KEY` `Context` key (documented in
  `DataLocalityPolicy.java`'s Javadoc: `context.addData(DataLocalityPolicy
  .LAYOUT_ENDPOINT_KEY, endpoint)`), which any Java storage call already
  accepts via its `Context` parameter. This is functionally equivalent but
  less discoverable than a dedicated options-object property — flagged here
  for manual review, not silently treated as full parity.

No other gaps were found in this pass. This reconciliation confirms the
findings already listed under "Outstanding items" above (unmerged spec
dependency, environment's non-implementation of real Layout partitioning,
Data Lake's proxy-based `getLayout`) remain the complete list of open items;
no additional missed files or behaviors were discovered.

**Autorest regeneration verification.** Re-ran `autorest` against
`sdk/storage/azure-storage-blob/swagger/README.md`
(`npx autorest --version=3.9.7 --use=@autorest/java@4.1.63 README.md`,
161 files generated) to check for accidental hand-edits to generated code
or generated files that were deleted/restructured:
- The fully-generated `implementation/` package (raw REST client + internal
  models, e.g. `BlobsGetLayoutHeaders.java`, `BlobLayout.java`,
  `BlobLayoutEndpoints*.java`, `BlobLayoutRanges*.java`) came back
  byte-identical to what's already committed (verified with
  `git diff --ignore-space-at-eol -w`, which showed zero real diff beyond
  CRLF/LF line-ending noise from the generator's LF output vs this repo's
  CRLF line endings) — confirms nothing in the generated layer was
  accidentally hand-modified this session, and no generated file was
  deleted or restructured by this feature's changes.
- The public `models` package (the `custom-types`-promoted classes) also
  regenerated identically, **except** for `BlobDownloadHeaders.java` and
  `DownloadHint.java`, which retained a stale, broken `{@link ...getLayout
  (BlobGetLayoutOptions)}` Javadoc reference (missing the `Context`
  parameter, which previously broke `mvn javadoc:javadoc`). This confirms
  these two files are promoted-once-then-hand-maintained (autorest does not
  overwrite them on subsequent regenerations), so the Javadoc fix applied
  in this pass (adding the `Context` parameter to the `@link`) is a
  legitimate, permanent hand-edit — not something that will be silently
  reverted by a future regeneration.
- Data Lake has no `swagger/` folder at all, consistent with the
  already-documented finding that its `getLayout` is a hand-written proxy
  over the Blob client rather than a generated REST operation — there is
  nothing to regenerate/verify there.

## Commits on this branch

```
104e52bf186 Add Storage service version 2027-03-07 for STG 105
1d0324b1192 Regenerate azure-storage-blob from updated swagger spec
f621d73b3b3 Implement Data Locality getLayout API
d75a76d0146 Add tests for getLayout API
5fc7ad896de Add implementation notes
ec7526c025d Fix BlobLayoutInfo missing blob-content fields + async test bug
f1723e88049 Fix BlobTestBase afterTest cleanup client ignoring service version
1f98d73cb5e Update azure-storage-blob assets tag for STG105 recordings
1c5ccb28247 Add generic AutoRefreshingCache to azure-storage-common
ac33c76c802 Add tests for AutoRefreshingCache
f5a85c5489d Add DataLocalityPolicy to azure-storage-common
8ec4109d9e1 Add tests for DataLocalityPolicy
f506a073c64 Add enableDataLocality option to BlobDownloadToFileOptions and BlobInputStreamOptions
ecc2e05a191 Add BlobLayoutCacheValue and BlobLayoutRangeResolver for data locality routing
ca147a91fd8 Add tests for BlobLayoutCacheValue and BlobLayoutRangeResolver
86b51ef2bbe Wire DataLocalityPolicy and layout cache into Blob download paths
96ad0aa4fd1 Add getLayout and data locality wiring for DataLake file client
bdc049f4109 Add end-to-end tests for data locality download/stream wiring
db17b6c68c3 Add CHANGELOG entries for STG105 data locality feature
```
