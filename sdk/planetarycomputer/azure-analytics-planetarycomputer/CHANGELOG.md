# Release History

## 1.0.0 (Unreleased)

### Features Added

- GA release of the Azure Planetary Computer client library for Java targeting API version `2026-04-15`.
- Added models `AssetStatisticsResponse`, `BandStatisticsMap`, `ClassMapLegendResponse`, `QueryableDefinitionsResponse`, `TilerAssetGeoJson`, `TilerInfoMapResponse`.
- Added search-based mosaic endpoints (`getSearchTileJson`, `getSearchTile`, `getSearchWmtsCapabilities`, `getSearchInfo`, etc.) under `/data/mosaic/searches/` path.
- Added collection-based mosaic endpoints (`getCollectionTile`, `getCollectionTileJson`, `getCollectionWmtsCapabilities`, etc.).
- Added `getItemAssetStatistics` for per-asset statistics on STAC items.
- Added `getItemInfo` and `getItemInfoGeoJson` for item tiler metadata.
- `getClassMapLegend` now returns `ClassMapLegendResponse`.
- `assetBandIndices` parameter changed from `String` to `List<String>` to support multiple per-asset band selections.

### Breaking Changes

- Removed `createStaticImage`, `getStaticImage`, `ImageParameters`, and `ImageResponse` (static image rendering API removed from GA spec).
- Removed options bag classes: `GetMosaicTileJsonOptions`, `GetMosaicTileOptions`, `GetWmtsCapabilitiesOptions`, `GetMosaicWmtsCapabilitiesOptions`, `GetPartOptions`, `GetPreviewOptions`. Parameters are now passed directly to methods.
- Renamed item-level tiler methods with `Item` prefix: `getBounds` → `getItemBounds`, `getPreview` → `getItemPreview`, `getPoint` → `getItemPoint`, `getStatistics` → `getItemStatistics`, `getTileJson` → `getItemTileJson`, `getWmtsCapabilities` → `getItemWmtsCapabilities`, `getInfoGeoJson` → `getItemInfoGeoJson`, `getAssetStatistics` → `getItemAssetStatistics`.
- Renamed mosaic methods: `getMosaicsSearchInfo` → `getSearchInfo`, `getMosaicsTileJson` → `getSearchTileJson`, `getMosaicsTile` → `getSearchTile`, `getMosaicsWmtsCapabilities` → `getSearchWmtsCapabilities`.
- Renamed conformance: `getConformanceClass` → `getConformanceClasses`.
- Renamed listing methods: `listRenderOptions` → `getRenderOptions`, `listMosaics` → `getMosaics`, `listTileMatrices` → `getTileMatrices`, `listAvailableAssets` → `getItemAvailableAssets`.
- `StacSearchParameters.filter` type changed from `Map<String, Object>` to `Map<String, BinaryData>`.
- `StacCollectionTemporalExtent` constructor now takes `List<List<OffsetDateTime>>` instead of `List<Map<String, Object>>`.
- `StacItemProperties.getGsd()` return type changed from `Number` to `Double`.
- `getItemCollection` method signature expanded with additional parameters.

## 1.0.0-beta.1 (2026-06-08)

- Azure PlanetaryComputer client library for Java. This package contains Microsoft Azure PlanetaryComputer client library.

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes
