---
name: multi-tenancy-benchmark-heapdump
description: Analyze and compare heap dump (.hprof) files from multi-tenancy benchmark runs. Use when the user wants to compare PRE_CLOSE vs POST_CLOSE heap dumps, identify retained objects after client close, find memory leaks, or understand what objects are consuming heap. Triggers on "analyze heap dump", "compare hprof", "memory leak objects", "what is retained", "heap dump diff".
---

# Analyze Heap Dumps

Compare two .hprof files (typically PRE_CLOSE and POST_CLOSE) to identify retained objects causing memory leaks.

## Workflow

### 1. Locate the heap dump files

Look in the benchmark results directory:
```
results/<run-dir>/heap-dumps/heap-PRE_CLOSE-*.hprof
results/<run-dir>/heap-dumps/heap-POST_CLOSE-*.hprof
```

If on a remote VM, use `scp` to download or analyze in-place.

### 2. Generate class histograms

Since `jhat` is removed in modern JDKs, use the **HeapDumpAnalyzer** utility included in the benchmark module:

```bash
# On the VM (requires Java 11+):
java -cp azure-cosmos-benchmark-*-jar-with-dependencies.jar \
  com.azure.cosmos.benchmark.HeapDumpAnalyzer \
  <pre-close.hprof> <post-close.hprof>
```

If the analyzer class is not available, fall back to Eclipse MAT headless:

```bash
# Install MAT CLI (one-time):
wget https://www.eclipse.org/downloads/download.php?file=/mat/1.15.0/rcp/MemoryAnalyzer-1.15.0.20231206-linux.gtk.x86_64.zip
unzip MemoryAnalyzer-*.zip

# Parse heap dump to generate histogram:
./mat/ParseHeapDump.sh <file.hprof> org.eclipse.mat.api:suspects org.eclipse.mat.api:overview
```

### 3. Alternative: Python hprof parser

For a quick histogram without external tools, use the Python script at `references/parse_hprof.py`:

```bash
python3 references/parse_hprof.py <pre-close.hprof> --top 30
python3 references/parse_hprof.py <post-close.hprof> --top 30
```

### 4. Compare histograms

Diff the two histograms to find classes with significantly more instances or bytes after close:

```
python3 references/parse_hprof.py --diff <pre-close.hprof> <post-close.hprof> --top 20
```

### 5. Interpret results

Use `references/heap-objects.md` to map retained object classes to root causes.

## Output Format

```
Heap Dump Comparison: PRE_CLOSE vs POST_CLOSE

PRE_CLOSE:  <size> MB  (<N> objects)
POST_CLOSE: <size> MB  (<N> objects)
Delta:      <size> MB  (<N> objects)

Top retained classes after close (sorted by byte delta):

  Class                                          PRE count  POST count  Delta     Bytes delta
  -----------------------------------------------  ---------  ----------  --------  -----------
  c.a.c.impl.clienttelemetry.ConcurrentDoubleHist  50         50          0         +12.5 MB
  io.netty.buffer.PooledByteBuf                    1200       200         -1000     -8.2 MB
  c.a.c.impl.SessionContainer$SessionTokenEntry    5000       5000        0         +2.1 MB
  ...

Suspect: <class> -- see references/heap-objects.md for root cause
```

## References

- **Object-to-root-cause mapping**: `references/heap-objects.md`
- **Python hprof parser**: `references/parse_hprof.py`
