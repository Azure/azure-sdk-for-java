# Release History

## 1.0.0-beta.6 (Unreleased)


## 1.0.0-beta.5 (2021-08-17)

### Features added
- Caching schemas based on schemaId.

### Breaking changes
- 4xx responses return their respective `HttpResponseException` rather than `IllegalStateException`
- Removed `Response<T>` overloads for getSchema and getSchemaId because response could be cached.
- `SchemaRegistryClientBuilder.maxCacheSize` is package-private.

## 1.0.0-beta.4 (2020-09-21)
- Minor code cleanup and refactoring

## 1.0.0-beta.3 (2020-09-10)
- Removed `SchemaRegistryCodec` and `SchemaRegistrySerializer` 
- Updated Schema Registry client APIs to use `SchemaProperties`

## 1.0.0-beta.2 (2020-06-19)
- Fix 4xx HTTP response handling

## 1.0.0-beta.1 (2020-06-04)
- Initial add
