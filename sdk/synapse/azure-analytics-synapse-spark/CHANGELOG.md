## 1.0.0-beta.3 (Unreleased)


## 1.0.0-beta.2 (2021-02-09)

- Support specifying the service API version. (AutoRest update)
- Send missing "Accept" request headers

**Breaking changes:**
- `getMsg()` renamed to `getMessage()` on `SparkStatementCancellationResult`
- `setMsg()` renamed to `setMessage()` on `SparkStatementCancellationResult`
- `PYSPARK` renamed to `PY_SPARK` on `SparkStatementLanguageType`
- `DOTNETSPARK` renamed to `DOT_NET_SPARK` on `SparkStatementLanguageType`

## 1.0.0-beta.1 (2020-12-08)

Version 1.0.0-beta.1 is a beta of our efforts in creating an Azure Synapse Spark client library that is developer-friendly, idiomatic to
the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the
[Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

### New Features

- It uses Azure Synapse 2019-11-01-preview API
- Two client design:
    - `SparkBatchClient` to manage Spark batch jobs
    - `SparkSessionClient` to manage Spark sessions and statements
- Reactive streams support using [Project Reactor](https://projectreactor.io/)
