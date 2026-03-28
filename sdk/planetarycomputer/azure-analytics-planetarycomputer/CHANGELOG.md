# Release History

## 1.0.0-beta.1 (Unreleased)

- Azure PlanetaryComputer client library for Java. This package contains the Microsoft Azure PlanetaryComputer client library.

### Features Added

- Initial beta release of `azure-analytics-planetarycomputer`.
- `StacClient` and `StacAsyncClient` for managing STAC collections, items, mosaics, render options, queryables, and search.
- `DataClient` and `DataAsyncClient` for tiler operations including map tiles, previews, statistics, static images, and legends.
- `IngestionClient` and `IngestionAsyncClient` for managing data ingestion definitions, runs, sources, and operations.
- `SharedAccessSignatureClient` and `SharedAccessSignatureAsyncClient` for generating and managing SAS tokens for Azure Blob Storage access.
- Support for Azure AD authentication via `TokenCredential`.
- Long-running operation (LRO) support for collection, item, and ingestion operations.
