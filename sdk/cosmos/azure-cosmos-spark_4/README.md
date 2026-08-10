## Azure Cosmos DB OLTP Spark 4 connector shared source

This is a POM-only parent module for Spark 4.x leaf modules (`azure-cosmos-spark_4-0_2-13`,
`azure-cosmos-spark_4-1_2-13`). It is **not published** to Maven Central.

### Source aggregation layering

Spark connector Scala sources are aggregated from multiple directories in a 3-tier hierarchy.
Each leaf module's `build-helper-maven-plugin` configuration specifies which layers to include:

```
Layer 1: azure-cosmos-spark_3/src/{main,test}/scala         — shared across all Spark versions
Layer 2: azure-cosmos-spark_4/src/{main,test}/scala          — shared Spark 4.x overrides (this module)
Layer 3: <leaf>/src/{main,test}/scala                        — leaf-specific overrides
```

**Why Spark 4.1 needs override files:** Three files import `HDFSMetadataLog`, which was relocated
in SPARK-52787 (Spark 4.1). Spark 4.0 and earlier use the original package
(`o.a.s.sql.execution.streaming`), so they include the Layer 1 versions directly.
Spark 4.1+ excludes those files from Layer 1 (via `maven-resources-plugin` excludes) and provides
its own overrides in Layer 3 with the updated import path
(`o.a.s.sql.execution.streaming.checkpointing`).

Leaf modules use `combine.self="override"` on build-helper plugin `<configuration>` elements to
fully replace (not merge with) the template source lists defined in this parent POM.
