# Core Multi-Release JAR Performance

The multi-release JAR implementations in `azure-core` and `clientcore` use newer JDK APIs without raising the
libraries' Java 8 baseline. The largest measured gains remain in stream materialization and buffer-backed transfers.
Absolute `ByteBuffer` copies show no material allocation benefit on JDK 25.

## Runtime Layers and Added Features

[Multi-release JAR selection][mr-spec] happens per class: the runtime selects the highest applicable version of each
class, falling back to an earlier layer or the unversioned implementation when necessary. For example, Java 17 uses
the Java 17 hexadecimal formatter alongside the Java 11 stream implementation and Java 13 buffer-copy helper.

| Layer | Libraries | Added implementation | Performance effect | Evidence |
|---|---|---|---|---|
| Java 11 | Both | Specialized stream operations through [core][core-streams] and [clientcore][client-streams] `InputStreamUtils`. | Uses bulk JDK reads for byte-array, file, and SDK buffer streams. Retains portable generic-read buffering where JDK defaults allocated more. | Measured for byte-array and generic streams; file streams were not measured. |
| Java 11 | Both | Buffer-backed `readAllBytes()` and `transferTo()` in [core][core-buffer-stream] and [clientcore][client-buffer-stream]. | Materializes buffers into one result array. Transfers sufficiently large array-backed buffers directly to the output, avoiding a scratch copy. | Measured for heap and direct buffers. |
| Java 13 | Both | Absolute bulk `ByteBuffer.get(...)` in [core][core-buffer-copy] and [clientcore][client-buffer-copy]. | Copies without modifying the source position or allocating a duplicate view in source code. | Measured; little allocation benefit on JDK 25. |
| Java 17 | Both; span IDs in clientcore | `HexFormat` in [core][core-hex] and [clientcore][client-hex], plus fixed-width [span ID formatting][client-span]. | Enables compact-string-friendly formatting without an intermediate character array. Clientcore authentication also shares the portable formatter instead of creating a string for every byte. | Not benchmarked. |
| Java 11 | Both; semantic versions in azure-core | Range-based decimal parsing in [core][core-long-parser], [clientcore][client-long-parser], and the [semantic-version integer parser][core-integer-parser]. | Avoids temporary numeric substrings while preserving existing trimming, overflow, and invalid-input behavior. | Not benchmarked. |
| Java 11 | azure-core | Direct `Cleaner.create(...)` and `Cleaner.register(...)` through [package-private Cleaner access][core-cleaner]. | Removes reflective invocation and argument packing from registration. Cleaner-based cleanup already existed; this does not change GC or cleanup scheduling. | Not benchmarked. |

Some APIs used by the Java 11 layer were introduced in Java 9. They are placed in the Java 11 layer to align with
the existing modular baseline. The Java 8 implementations remain available, and existing supported public APIs are
retained. The hexadecimal, numeric-parser, span-formatter, and Cleaner helpers are package-private.

Clientcore's pre-existing Java 12 JDK HTTP transport layer is unchanged and is not one of the optimizations measured
here.

### Behavior and Performance Guardrails

- Bulk reads consume the remaining content; a supplied content-length hint does not truncate the stream.
- Bulk reads use JDK byte-array and file specializations only for exact `ByteArrayInputStream` and `FileInputStream`
  instances. Subclasses retain portable reads so overridden `read` methods are honored.
- Specialized transfers are restricted to exact `FileInputStream` instances and the SDK's final buffer-backed stream.
  `FileOutputStream` subclasses and all other inputs, including `BufferedInputStream`, `PushbackInputStream`, and
  `SequenceInputStream`, use bounded portable copies. This prevents nested bulk operations from bypassing read/write
  overrides or issuing large buffered writes.
- Caching, replayability, stream ownership, and source-buffer positions and marks are preserved.
- `ByteArrayInputStream.transferTo()` is deliberately avoided during output writes because it holds the input
  monitor while writing to the destination. The implementation uses bounded buffering instead.
- Buffer-backed transfers write consecutive leading array-backed ranges directly in chunks of at most 8 KiB when each
  remaining range is at least 8 KiB. This bounds per-write destination allocations without a scratch copy. On
  encountering a smaller, direct, or read-only buffer, the remainder of the stream uses the JDK's bounded-copy path,
  which also coalesces small buffers.
- Buffer-backed transfers release the SDK stream monitor before calling the destination's potentially blocking
  `write` method.

These choices matter: blindly replacing every loop with a newer JDK API increased allocation or produced many small
writes in initial experiments. The dated results below distinguish the original IO-only artifacts from the corrected
full-feature builds.

## Revalidation (2026-11-10)

The [new 42-case dataset][revalidation-results] compares the original pre-MR baselines with packaged libraries
containing the stream-dispatch safeguards and bounded heap-buffer writes. It reruns all 40 original pairs and adds
a 1 MiB single-heap-buffer transfer pair for each library. The [metadata][revalidation-metadata] records artifact
hashes, the source commit plus uncommitted changes, and hashes of the measured stream sources. The retained JMH
harnesses were reused only after verifying that all six benchmark classes per library were byte-identical to the
current compilation.

The main run uses the same Microsoft OpenJDK 25.0.5.0.2 runtime and JMH 1.37 settings as the original capture:
two forks, three warmup and five measurement iterations of 500 ms, one worker, a fixed 256 MiB G1 heap with 4 MiB
regions, a 64 MiB code cache, and CPU affinity `0x5` with `ActiveProcessorCount=2`. Baseline and corrected variants
are adjacent, alternating which runs first. This remains a shared-workstation, memory-Blackhole experiment, not
network or file throughput.

### Representative 1 MiB Results

Buffer-backed and list-copy rows below use 128 heap buffers. Allocation is bytes per operation; ratios compare
the corrected implementation with the pre-MR baseline.

| Library | Scenario | Baseline ops/s | Corrected ops/s | Ratio | Baseline B/op | Corrected B/op |
|---|---|---:|---:|---:|---:|---:|
| azure-core | Byte-array stream to bytes | 3,406 | 13,318 | 3.91x | 3,146,025 | 1,048,673 |
| azure-core | Byte-array stream to output | 20,899 | 74,851 | 3.58x | 24,958 | 8,221 |
| azure-core | Buffer-backed `readAllBytes()` | 4,677 | 9,367 | 2.00x | 2,106,363 | 1,057,865 |
| azure-core | Buffer-backed `transferTo()` | 58,232 | 482,810 | 8.29x | 23,624 | 7,224 |
| azure-core | List of buffers to bytes | 9,457 | 9,012 | 0.95x | 1,048,897 | 1,048,898 |
| clientcore | Byte-array stream to bytes | 4,637 | 12,309 | 2.65x | 2,097,451 | 1,048,673 |
| clientcore | Byte-array stream to output | 59,876 | 63,760 | 1.06x | 8,225 | 8,217 |
| clientcore | Buffer-backed `readAllBytes()` | 4,429 | 6,128 | 1.38x | 2,106,363 | 1,057,866 |
| clientcore | Buffer-backed `transferTo()` | 58,598 | 496,527 | 8.47x | 23,624 | 7,224 |
| clientcore | List of buffers to bytes | 6,164 | 5,291 | 0.86x | 1,048,842 | 1,048,843 |

The substantial materialization and heap-transfer gains remain, including the approximately 50-67% reduction in
byte-array materialization allocation and 69% reduction in 128-heap-buffer transfer allocation. The CSV preserves
all cases, including ratios below 1.0, and the 99.9% throughput confidence-interval half-widths. Several small or
list-copy differences have wide, overlapping intervals; their point estimates do not establish a general regression
or improvement. Allocation differences near one byte per operation are not meaningful memory changes.

### Cost of Bounded Single-Buffer Writes

The [single-buffer reference data][revalidation-reference] freshly measures the original IO-only MR artifacts
against the corrected implementation. A 1 MiB heap buffer now produces 128 writes of at most 8 KiB instead of one
unbounded write.

| Library | Pre-MR ops/s | Unbounded MR reference ops/s | Corrected ops/s | Corrected / reference | Corrected / pre-MR |
|---|---:|---:|---:|---:|---:|
| azure-core | 58,056 | 31,644,729 | 5,247,713 | 0.17x | 90.39x |
| clientcore | 59,028 | 28,791,415 | 5,222,965 | 0.18x | 88.48x |

The bounded path is approximately 82-83% slower than the unbounded fast path in this case, while remaining much
faster than the portable pre-MR implementation. Allocation stays near 112 B/op for both MR variants, versus
16,512 B/op for the pre-MR baseline. The Blackhole sink makes per-write overhead particularly visible; these ratios
are not predictions for real destinations that process or transmit the payload. The bound prevents large
destination scratch allocations and is retained despite its measurable call overhead.

### Variability Check

The short run showed a 0.75x point estimate for clientcore's 4 KiB transfer over 128 heap buffers. A
[separate confirmation][revalidation-confirmation] used four forks and one-second iterations, preserving the original
short result rather than replacing it. The confirmation measured 389,240 +/- 40,594 ops/s for pre-MR,
425,953 +/- 47,021 for the unbounded MR reference, and 434,521 +/- 33,828 for the corrected implementation.
The intervals overlap: the corrected point estimate is 1.12x pre-MR and 1.02x the MR reference, so the apparent
short-run regression was not reproduced.

The main run contains 86 variant results and 860 measured iteration samples, including two extra pre-fix reference
results for the single-buffer cases. The confirmation adds three results and 60 samples, for 89 results and
920 measured samples overall. Comparison CSVs reuse corrected measurements where appropriate rather than
representing every row as a new independent measurement. The October snapshot below is unchanged.

## Measured IO Performance

The CSV and tables are an intentionally retained historical snapshot from October 30, 2026, not a periodically
refreshed benchmark dashboard. They describe the recorded artifacts and predate the stream-dispatch safeguards and
8 KiB limit for direct heap-buffer writes. New comparisons should record their own source revisions and environment
rather than overwrite this snapshot.

The following tables show **1 MiB payloads**. Buffer-backed and list-copy cases use **128 heap buffers**. Values are
rounded from the [complete 40-case CSV][results]; throughput is operations per second, allocation is bytes per
operation, and the ratio is updated throughput divided by baseline throughput.

**These are CPU/allocation microbenchmarks with a Blackhole output sink, not network or file throughput.** In
particular, the large transfer ratios measure the removal of SDK copying and bookkeeping; they do not imply
equivalent improvements in service-call throughput.

### azure-core

| Scenario | Baseline ops/s | MR ops/s | Ratio | Baseline B/op | MR B/op |
|---|---:|---:|---:|---:|---:|
| Byte-array stream to bytes | 3,412 | 13,794 | 4.04x | 3,146,025 | 1,048,673 |
| Byte-array stream to output | 20,272 | 66,831 | 3.30x | 24,940 | 8,223 |
| Buffer-backed `readAllBytes()` | 4,691 | 9,443 | 2.01x | 2,106,363 | 1,057,865 |
| Buffer-backed `transferTo()` | 57,334 | 483,658 | 8.44x | 23,624 | 7,224 |
| List of buffers to bytes | 9,899 | 9,897 | 1.00x | 1,048,897 | 1,048,897 |

### clientcore

| Scenario | Baseline ops/s | MR ops/s | Ratio | Baseline B/op | MR B/op |
|---|---:|---:|---:|---:|---:|
| Byte-array stream to bytes | 5,311 | 14,060 | 2.65x | 2,097,451 | 1,048,681 |
| Byte-array stream to output | 73,479 | 72,246 | 0.98x | 8,214 | 8,216 |
| Buffer-backed `readAllBytes()` | 4,709 | 6,273 | 1.33x | 2,106,363 | 1,057,866 |
| Buffer-backed `transferTo()` | 55,919 | 480,497 | 8.59x | 23,624 | 7,224 |
| List of buffers to bytes | 6,301 | 7,083 | 1.12x | 1,048,842 | 1,048,842 |

### Interpretation

- **Specialized reads:** the 1 MiB byte-array materialization case reduces allocation by approximately 67% in
  azure-core and 50% in clientcore. The baseline allocation differs because clientcore could already return its
  backing array without a final copy when that array was exactly filled.
- **Buffer-backed reads:** allocation is approximately halved for the measured 1 MiB heap and direct-buffer cases.
  The output array is still required; views and collection overhead account for the remainder.
- **Buffer-backed transfers:** the 1 MiB heap-buffer case reduces allocation by approximately 69%. Direct-buffer
  transfers retain bounded copying and show little allocation change.
- **Stream-to-output differences:** azure-core benefits from removing channel adaptation on applicable paths.
  Clientcore already used a similar portable copy loop, so its byte-array and generic output results remain near
  baseline.
- **Absolute buffer copies:** avoiding `duplicate()` in source code did not materially reduce measured allocation.
  Baseline allocation is consistent with the JDK 25 JIT optimizing away those temporary views. The required result
  array dominates allocation, and near-1.0x throughput differences should not be interpreted as a general performance
  win.

The CSV also includes 4 KiB payloads, generic streams, and direct buffers, including cases with ratios below 1.0.
It preserves the JMH 99.9% confidence-interval half-widths in `baseline_error_99_9` and `updated_error_99_9`.
Ratios are point estimates, not significance tests; overlapping intervals make small differences inconclusive.
Tiny fractional differences in allocation near zero reduction are not meaningful memory savings.

## Measurement Method

| Setting | Recorded value |
|---|---|
| Capture date | 2026-10-30 |
| Runtime | Microsoft OpenJDK 25.0.5.0.2, build 25.0.5.0.2+1-LTS |
| Machine | Intel Core i7-13800H, 14 cores / 20 logical processors |
| OS | Windows 11 Enterprise, version 10.0.26300 |
| JMH | 1.37, throughput mode, one worker thread |
| Forks | 2 per variant and case |
| Warmup | 3 iterations of 500 ms per fork |
| Measurement | 5 iterations of 500 ms per fork |
| Heap / GC | Fixed 256 MiB heap, G1, 4 MiB regions |
| CPU control | Affinity mask `0x5` (logical processors 0 and 2), `ActiveProcessorCount=2` |
| Allocation measurement | JMH GC profiler, `gc.alloc.rate.norm` in B/op |
| Ordering | Baseline and updated artifacts adjacent for each case, alternating which ran first |

The machine was a shared workstation, not an isolated performance host. Both variants used the same JDK and JVM
settings, so the comparison does not conflate an SDK change with upgrading from Java 8 to Java 25.

The measured matrix contains 20 paired cases per library:

| Benchmark group | Methods | Parameters |
|---|---|---|
| Streams | `streamToBytes`, `streamToOutput` | 4 KiB / 1 MiB; byte-array / generic input |
| Buffers | `bufferedReadAllBytes`, `bufferedTransferTo`, `listToBytes` | 4 KiB / 1 MiB; heap / direct; 128 buffers |

This gives 40 paired cases, 80 variant results, and 800 measured iteration samples. The benchmark suite also supports
other shapes, including read-only buffers and a single buffer, but those were not part of this recorded matrix.

`streamToBytes` and `listToBytes` create fresh `BinaryData` for each operation to avoid measuring a cached `toBytes()`
result. `bufferedReadAllBytes` and `bufferedTransferTo` reuse the `BinaryData` wrapper but create a fresh stream over
the same source buffers for each operation. Clientcore's benchmark accesses its package-private buffer factory from
the same package; these are implementation-level measurements, not a new public API.

The [metadata file][metadata] records the JVM arguments and SHA-256 hashes of the measured artifacts. The updated
artifacts were intermediate IO-only builds, not a published Maven release or a byte-identical build of the final
feature revision. The metadata distinguishes that artifact scope from the full source change. This guide includes
aggregated results and artifact identities; the captured JAR binaries and per-iteration JSON are not bundled here.

## Repeating a Measurement

Use the [azure-core benchmark][core-benchmark] or [clientcore benchmark][client-benchmark]. Run against a packaged
SDK JAR: putting `target\classes` on the production classpath bypasses MR selection. The harness checks that
`BinaryData` is loaded from a JAR.

Build and retain a baseline JAR outside `target` before building the feature version. A fresh comparison can use the
pre-change revision and the full feature revision recorded in the metadata, but that is not a bit-for-bit
reproduction of the intermediate artifacts used for the historical tables.

The following PowerShell example runs the 1 MiB heap-buffer transfer case. Set `JAVA_HOME` to JDK 25 and replace
the two artifact paths. Build prerequisites are described in
[Building](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/building.md#prerequisites).

| Library | `$Package` | `$Benchmark` |
|---|---|---|
| azure-core | `sdk\core\azure-core` | `com.azure.core.util.BinaryDataIOBenchmark.bufferedTransferTo` |
| clientcore | `sdk\clientcore\core` | `io.clientcore.core.models.binarydata.BinaryDataIOBenchmark.bufferedTransferTo` |

```powershell
$ErrorActionPreference = 'Stop'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$Package = 'sdk\core\azure-core'
$Benchmark = 'com.azure.core.util.BinaryDataIOBenchmark.bufferedTransferTo'
$BaselineJar = '<absolute-path-to-baseline-jar>'
$UpdatedJar = '<absolute-path-to-updated-jar>'
$Artifacts = Join-Path (Get-Location) 'artifacts\core-multi-release'
New-Item -ItemType Directory -Force -Path $Artifacts | Out-Null

mvn -f "$Package\pom.xml" '-Djmh-benchmark' '-DskipTests' `
    '-Dcodesnippet.skip=true' '-Dmaven.javadoc.skip=true' '-Dgpg.skip=true' clean package
if ($LASTEXITCODE -ne 0) { throw 'Benchmark build failed.' }

$DependenciesFile = Join-Path $Artifacts 'dependencies.txt'
mvn -f "$Package\pom.xml" dependency:build-classpath '-Dmdep.includeScope=test' `
    "-Dmdep.outputFile=$DependenciesFile"
if ($LASTEXITCODE -ne 0) { throw 'Dependency classpath resolution failed.' }
$Dependencies = (Get-Content -LiteralPath $DependenciesFile -Raw).Trim()

$Harness = Join-Path $Artifacts 'benchmarks.jar'
& "$env:JAVA_HOME\bin\jar.exe" --create --file $Harness -C "$Package\target\test-classes" .
if ($LASTEXITCODE -ne 0) { throw 'Benchmark harness packaging failed.' }

$JmhArgs = @(
    '-p', 'size=1048576', '-p', 'bufferCount=128', '-p', 'bufferType=heap',
    '-f', '2', '-wi', '3', '-i', '5', '-w', '500ms', '-r', '500ms',
    '-t', '1', '-prof', 'gc', '-foe', 'true', '-rf', 'json'
)
$Runs = @(
    @{ Name = 'baseline'; Jar = $BaselineJar },
    @{ Name = 'updated'; Jar = $UpdatedJar }
)
foreach ($Run in $Runs) {
    $Classpath = "$Harness;$($Run.Jar);$Dependencies"
    $Result = Join-Path $Artifacts "$($Run.Name).json"
    & "$env:JAVA_HOME\bin\java.exe" -Xms32m -Xmx128m -XX:ReservedCodeCacheSize=32m `
        -XX:ActiveProcessorCount=2 -cp $Classpath org.openjdk.jmh.Main $Benchmark @JmhArgs -rff $Result
    if ($LASTEXITCODE -ne 0) { throw "Benchmark failed: $($Run.Name)" }
}
```

The forked JVM memory/GC settings are specified by the benchmark's `@Fork` annotation. The recorded 500 ms iteration
durations override its longer defaults. CPU affinity is not set by this example: to match the recorded setup, apply
mask `0x5` to the runner and its children on a machine where those logical processors are available, and restore the
original mask afterwards. Record any differences in hardware, affinity, JVM flags, or runtime.

For the full matrix, repeat the methods and parameter combinations above, alternating artifact order for each
case. Only one SDK JAR should be on the production classpath at a time. Generate the harness with `-Djmh-benchmark`
and save it outside `target`, then start normal quality checks from a clean build without that profile so stale
generated JMH padding classes are not mistaken for handwritten SDK code.

For the November single-buffer case, change `bufferCount` to `1` while keeping the 1 MiB heap-buffer transfer.
The targeted 4 KiB confirmation uses `bufferCount=128`, `bufferType=heap`, four forks, and one-second iterations.
Its baseline, corrected, and MR-reference artifacts run in that order; the metadata records the distinct protocol.

## Unmeasured Features

There are no throughput or allocation claims yet for the Java 17 hexadecimal/span formatting, Java 11 range-based
numeric parsing, or direct Cleaner registration. Functional and MR-layer selection coverage is not performance
evidence. Dedicated measurements should isolate formatting/parsing work and Cleaner registration from referent
collection and cleanup latency.

The available data also does not establish performance on Java 8, 11, or 17, other JVM vendors, virtual-thread
workloads, real HTTP transports, cold startup, or end-to-end service operations.

[mr-spec]: https://openjdk.org/jeps/238#Description
[results]: https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/performance-data/core-multi-release-jdk25.csv
[metadata]: https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/performance-data/core-multi-release-jdk25.metadata.json
[revalidation-results]: https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/performance-data/core-multi-release-jdk25-20261110.csv
[revalidation-metadata]: https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/performance-data/core-multi-release-jdk25-20261110.metadata.json
[revalidation-reference]: https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/performance-data/core-multi-release-jdk25-20261110-single-buffer-reference.csv
[revalidation-confirmation]: https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/performance-data/core-multi-release-jdk25-20261110-small-heap-confirmation.csv
[core-streams]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java11/com/azure/core/implementation/util/InputStreamUtils.java
[client-streams]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/core/src/main/java11/io/clientcore/core/implementation/utils/InputStreamUtils.java
[core-buffer-stream]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java11/com/azure/core/implementation/util/ByteBufferInputStream.java
[client-buffer-stream]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/core/src/main/java11/io/clientcore/core/implementation/utils/ByteBufferInputStream.java
[core-buffer-copy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java13/com/azure/core/implementation/util/ByteBufferUtils.java
[client-buffer-copy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/core/src/main/java13/io/clientcore/core/implementation/utils/ByteBufferUtils.java
[core-hex]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java17/com/azure/core/util/HexEncoding.java
[client-hex]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/core/src/main/java17/io/clientcore/core/utils/HexEncoding.java
[client-span]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/core/src/main/java17/io/clientcore/core/implementation/instrumentation/fallback/SpanIdFormatter.java
[core-long-parser]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java11/com/azure/core/util/LongParser.java
[client-long-parser]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/core/src/main/java11/io/clientcore/core/utils/LongParser.java
[core-integer-parser]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java11/com/azure/core/implementation/IntegerParser.java
[core-cleaner]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java11/com/azure/core/implementation/util/CleanerAccess.java
[core-benchmark]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/test/java/com/azure/core/util/BinaryDataIOBenchmark.java
[client-benchmark]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/core/src/test/java/io/clientcore/core/models/binarydata/BinaryDataIOBenchmark.java
