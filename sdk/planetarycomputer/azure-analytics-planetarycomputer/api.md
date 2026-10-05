```java
maven {
    parent : com.azure:azure-client-sdk-parent:1.7.0
    properties : com.azure:azure-analytics-planetarycomputer:1.1.0-beta.1
    configuration {
        jacoco {
            min-line-coverage : 0.2
            min-branch-coverage : 0.05
        }
    }
    name : Microsoft Azure SDK for Planetary Computer
    description : This package contains Microsoft Azure Planetary Computer client library.
    dependencies {
        // compile scope
        com.azure:azure-core 1.59.0
        com.azure:azure-core-http-netty 1.16.6
    }
}
module com.azure.analytics.planetarycomputer {
    requires transitive com.azure.core;
    exports com.azure.analytics.planetarycomputer;
    exports com.azure.analytics.planetarycomputer.models;
    opens com.azure.analytics.planetarycomputer.models to com.azure.core;
    opens com.azure.analytics.planetarycomputer.implementation.models to com.azure.core;
}
package com.azure.analytics.planetarycomputer {
    @ServiceClient(builder = PlanetaryComputerProClientBuilder, isAsync = true)
    public final class DataAsyncClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        @Generated public Mono<ClassMapLegendResponse> getClassMapLegend(String classmapName)
        @Generated public Mono<ClassMapLegendResponse> getClassMapLegend(String classmapName, Integer trimStart, Integer trimEnd)
        @Generated public Mono<Response<BinaryData>> getClassMapLegendWithResponse(String classmapName, RequestOptions requestOptions)
        @Generated public Mono<List<BinaryData>> getCollectionAssetsForBbox(String collectionId, double minx, double miny, double maxx, double maxy)
        @Generated public Mono<List<BinaryData>> getCollectionAssetsForBbox(String collectionId, double minx, double miny, double maxx, double maxy, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, String coordinateReferenceSystem)
        @Generated public Mono<Response<BinaryData>> getCollectionAssetsForBboxWithResponse(String collectionId, double minx, double miny, double maxx, double maxy, RequestOptions requestOptions)
        @Generated public Mono<List<BinaryData>> getCollectionAssetsForTileNoTms(String collectionId, double z, double x, double y)
        @Generated public Mono<List<BinaryData>> getCollectionAssetsForTileNoTms(String collectionId, double z, double x, double y, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TileMatrixSetId tileMatrixSetId)
        @Generated public Mono<Response<BinaryData>> getCollectionAssetsForTileNoTmsWithResponse(String collectionId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public Mono<List<TilerAssetGeoJson>> getCollectionAssetsForTileWithTms(String collectionId, String tileMatrixSetId, double z, double x, double y)
        @Generated public Mono<List<TilerAssetGeoJson>> getCollectionAssetsForTileWithTms(String collectionId, String tileMatrixSetId, double z, double x, double y, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getCollectionAssetsForTileWithTmsWithResponse(String collectionId, String tileMatrixSetId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getCollectionBboxCrop(String collectionId, double minx, double miny, double maxx, double maxy, String format)
        @Generated public Mono<BinaryData> getCollectionBboxCrop(String collectionId, double minx, double miny, double maxx, double maxy, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, String destinationCrs, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask)
        @Generated public Mono<BinaryData> getCollectionBboxCropWithDimensions(String collectionId, double minx, double miny, double maxx, double maxy, int width, int height, String format)
        @Generated public Mono<BinaryData> getCollectionBboxCropWithDimensions(String collectionId, double minx, double miny, double maxx, double maxy, int width, int height, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, String destinationCrs, Integer maxSize, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask)
        @Generated public Mono<Response<BinaryData>> getCollectionBboxCropWithDimensionsWithResponse(String collectionId, double minx, double miny, double maxx, double maxy, int width, int height, String format, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getCollectionBboxCropWithResponse(String collectionId, double minx, double miny, double maxx, double maxy, String format, RequestOptions requestOptions)
        @Generated public Mono<TilerStacSearchRegistration> getCollectionInfo(String collectionId)
        @Generated public Mono<Response<BinaryData>> getCollectionInfoWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<TilerCoreModelsResponsesPoint> getCollectionPoint(String collectionId, double longitude, double latitude)
        @Generated public Mono<TilerCoreModelsResponsesPoint> getCollectionPoint(String collectionId, double longitude, double latitude, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, String coordinateReferenceSystem, Resampling resampling)
        @Generated public Mono<List<StacItemPointAsset>> getCollectionPointAssets(String collectionId, double longitude, double latitude)
        @Generated public Mono<List<StacItemPointAsset>> getCollectionPointAssets(String collectionId, double longitude, double latitude, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, String coordinateReferenceSystem)
        @Generated public Mono<Response<BinaryData>> getCollectionPointAssetsWithResponse(String collectionId, double longitude, double latitude, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getCollectionPointWithResponse(String collectionId, double longitude, double latitude, RequestOptions requestOptions)
        @Generated public Mono<TileJsonMetadata> getCollectionTileJson(String collectionId)
        @Generated public Mono<TileJsonMetadata> getCollectionTileJson(String collectionId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<Response<BinaryData>> getCollectionTileJsonWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<TileJsonMetadata> getCollectionTileJsonWithTms(String collectionId, String tileMatrixSetId)
        @Generated public Mono<TileJsonMetadata> getCollectionTileJsonWithTms(String collectionId, String tileMatrixSetId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<Response<BinaryData>> getCollectionTileJsonWithTmsWithResponse(String collectionId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getCollectionTileNoTms(String collectionId, double z, double x, double y)
        @Generated public Mono<BinaryData> getCollectionTileNoTms(String collectionId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<BinaryData> getCollectionTileNoTmsByFormat(String collectionId, double z, double x, double y, String format)
        @Generated public Mono<BinaryData> getCollectionTileNoTmsByFormat(String collectionId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<Response<BinaryData>> getCollectionTileNoTmsByFormatWithResponse(String collectionId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getCollectionTileNoTmsByScale(String collectionId, double z, double x, double y, double scale)
        @Generated public Mono<BinaryData> getCollectionTileNoTmsByScale(String collectionId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<BinaryData> getCollectionTileNoTmsByScaleAndFormat(String collectionId, double z, double x, double y, double scale, String format)
        @Generated public Mono<BinaryData> getCollectionTileNoTmsByScaleAndFormat(String collectionId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<Response<BinaryData>> getCollectionTileNoTmsByScaleAndFormatWithResponse(String collectionId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getCollectionTileNoTmsByScaleWithResponse(String collectionId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getCollectionTileNoTmsWithResponse(String collectionId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public Mono<TileSetMetadata> getCollectionTilesetMetadata(String collectionId, String tileMatrixSetId)
        @Generated public Mono<TileSetMetadata> getCollectionTilesetMetadata(String collectionId, String tileMatrixSetId, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getCollectionTilesetMetadataWithResponse(String collectionId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public Mono<TileSetList> getCollectionTilesets(String collectionId)
        @Generated public Mono<TileSetList> getCollectionTilesets(String collectionId, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getCollectionTilesetsWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getCollectionTileWithTms(String collectionId, String tileMatrixSetId, double z, double x, double y)
        @Generated public Mono<BinaryData> getCollectionTileWithTms(String collectionId, String tileMatrixSetId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<BinaryData> getCollectionTileWithTmsByFormat(String collectionId, String tileMatrixSetId, double z, double x, double y, String format)
        @Generated public Mono<BinaryData> getCollectionTileWithTmsByFormat(String collectionId, String tileMatrixSetId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<Response<BinaryData>> getCollectionTileWithTmsByFormatWithResponse(String collectionId, String tileMatrixSetId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getCollectionTileWithTmsByScale(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale)
        @Generated public Mono<BinaryData> getCollectionTileWithTmsByScale(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<BinaryData> getCollectionTileWithTmsByScaleAndFormat(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale, String format)
        @Generated public Mono<BinaryData> getCollectionTileWithTmsByScaleAndFormat(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<Response<BinaryData>> getCollectionTileWithTmsByScaleAndFormatWithResponse(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getCollectionTileWithTmsByScaleWithResponse(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getCollectionTileWithTmsWithResponse(String collectionId, String tileMatrixSetId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public Mono<byte[]> getCollectionWmtsCapabilities(String collectionId)
        @Generated public Mono<byte[]> getCollectionWmtsCapabilities(String collectionId, String ids, String bbox, String query, String sortby, String datetime, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject)
        @Generated public Mono<Response<BinaryData>> getCollectionWmtsCapabilitiesWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<byte[]> getCollectionWmtsCapabilitiesWithTms(String collectionId, String tileMatrixSetId)
        @Generated public Mono<byte[]> getCollectionWmtsCapabilitiesWithTms(String collectionId, String tileMatrixSetId, String ids, String bbox, String query, String sortby, String datetime, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject)
        @Generated public Mono<Response<BinaryData>> getCollectionWmtsCapabilitiesWithTmsWithResponse(String collectionId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> cropCollectionFeature(String collectionId, GeoJsonFeature body)
        @Generated public Mono<BinaryData> cropCollectionFeature(String collectionId, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs, TilerImageFormat format)
        @Generated public Mono<BinaryData> cropCollectionFeatureByFormat(String collectionId, String format, GeoJsonFeature body)
        @Generated public Mono<BinaryData> cropCollectionFeatureByFormat(String collectionId, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs)
        @Generated public Mono<Response<BinaryData>> cropCollectionFeatureByFormatWithResponse(String collectionId, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> cropCollectionFeatureWidthByHeight(String collectionId, int width, int height, String format, GeoJsonFeature body)
        @Generated public Mono<BinaryData> cropCollectionFeatureWidthByHeight(String collectionId, int width, int height, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs)
        @Generated public Mono<Response<BinaryData>> cropCollectionFeatureWidthByHeightWithResponse(String collectionId, int width, int height, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> cropCollectionFeatureWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> cropFeature(String collectionId, String itemId, GeoJsonFeature body)
        @Generated public Mono<BinaryData> cropFeature(String collectionId, String itemId, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String coordinateReferenceSystem, Resampling resampling, Integer maxSize, Integer height, Integer width, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TilerImageFormat format)
        @Generated public Mono<BinaryData> cropFeatureByFormat(String collectionId, String itemId, String format, GeoJsonFeature body)
        @Generated public Mono<BinaryData> cropFeatureByFormat(String collectionId, String itemId, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String coordinateReferenceSystem, Resampling resampling, Integer maxSize, Integer height, Integer width, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> cropFeatureByFormatWithResponse(String collectionId, String itemId, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> cropFeatureWidthByHeight(String collectionId, String itemId, int width, int height, String format, GeoJsonFeature body)
        @Generated public Mono<BinaryData> cropFeatureWidthByHeight(String collectionId, String itemId, int width, int height, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String coordinateReferenceSystem, Resampling resampling, Integer maxSize, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> cropFeatureWidthByHeightWithResponse(String collectionId, String itemId, int width, int height, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> cropFeatureWithResponse(String collectionId, String itemId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> cropSearchFeature(String searchId, GeoJsonFeature body)
        @Generated public Mono<BinaryData> cropSearchFeature(String searchId, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs, TilerImageFormat format)
        @Generated public Mono<BinaryData> cropSearchFeatureByFormat(String searchId, String format, GeoJsonFeature body)
        @Generated public Mono<BinaryData> cropSearchFeatureByFormat(String searchId, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs)
        @Generated public Mono<Response<BinaryData>> cropSearchFeatureByFormatWithResponse(String searchId, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> cropSearchFeatureWidthByHeight(String searchId, int width, int height, String format, GeoJsonFeature body)
        @Generated public Mono<BinaryData> cropSearchFeatureWidthByHeight(String searchId, int width, int height, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs)
        @Generated public Mono<Response<BinaryData>> cropSearchFeatureWidthByHeightWithResponse(String searchId, int width, int height, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> cropSearchFeatureWithResponse(String searchId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<List<List<List<Long>>>> getIntervalLegend(String classmapName)
        @Generated public Mono<List<List<List<Long>>>> getIntervalLegend(String classmapName, Integer trimStart, Integer trimEnd)
        @Generated public Mono<Response<BinaryData>> getIntervalLegendWithResponse(String classmapName, RequestOptions requestOptions)
        @Generated public Mono<AssetStatisticsResponse> getItemAssetStatistics(String collectionId, String itemId)
        @Generated public Mono<AssetStatisticsResponse> getItemAssetStatistics(String collectionId, String itemId, List<Integer> bidx, List<String> assets, List<String> assetBandIndices, String noData, Boolean unscale, WarpKernelResampling reproject, Resampling resampling, Integer maxSize, Boolean categorical, List<Integer> categoriesPixels, List<Integer> percentiles, String histogramBins, String histogramRange, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, List<String> assetExpression, Integer height, Integer width)
        @Generated public Mono<Response<BinaryData>> getItemAssetStatisticsWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Mono<List<String>> getItemAvailableAssets(String collectionId, String itemId)
        @Generated public Mono<List<String>> getItemAvailableAssets(String collectionId, String itemId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getItemAvailableAssetsWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getItemBboxCrop(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, String format)
        @Generated public Mono<BinaryData> getItemBboxCrop(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String coordinateReferenceSystem, String destinationCrs, Resampling resampling, Integer maxSize, Integer height, Integer width, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<BinaryData> getItemBboxCropWithDimensions(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, int width, int height, String format)
        @Generated public Mono<BinaryData> getItemBboxCropWithDimensions(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, int width, int height, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String coordinateReferenceSystem, String destinationCrs, Resampling resampling, Integer maxSize, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getItemBboxCropWithDimensionsWithResponse(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, int width, int height, String format, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getItemBboxCropWithResponse(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, String format, RequestOptions requestOptions)
        @Generated public Mono<StacItemBounds> getItemBounds(String collectionId, String itemId)
        @Generated public Mono<StacItemBounds> getItemBounds(String collectionId, String itemId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getItemBoundsWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Mono<StacItemStatisticsGeoJson> getItemFeatureStatistics(String collectionId, String itemId, GeoJsonFeature body)
        @Generated public Mono<StacItemStatisticsGeoJson> getItemFeatureStatistics(String collectionId, String itemId, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, String coordinateReferenceSystem, Resampling resampling, Integer maxSize, Boolean categorical, List<Integer> categoriesPixels, List<Integer> percentiles, String histogramBins, String histogramRange, String destinationCrs, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, String algorithm, String algorithmParams, Integer height, Integer width)
        @Generated public Mono<Response<BinaryData>> getItemFeatureStatisticsWithResponse(String collectionId, String itemId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<TilerInfoMapResponse> getItemInfo(String collectionId, String itemId)
        @Generated public Mono<TilerInfoMapResponse> getItemInfo(String collectionId, String itemId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, List<String> assets)
        @Generated public Mono<TilerInfoGeoJsonFeature> getItemInfoGeoJson(String collectionId, String itemId)
        @Generated public Mono<TilerInfoGeoJsonFeature> getItemInfoGeoJson(String collectionId, String itemId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, List<String> assets)
        @Generated public Mono<Response<BinaryData>> getItemInfoGeoJsonWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getItemInfoWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Mono<TilerCoreModelsResponsesPoint> getItemPoint(String collectionId, String itemId, double longitude, double latitude)
        @Generated public Mono<TilerCoreModelsResponsesPoint> getItemPoint(String collectionId, String itemId, double longitude, double latitude, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, String coordinateReferenceSystem, Resampling resampling)
        @Generated public Mono<Response<BinaryData>> getItemPointWithResponse(String collectionId, String itemId, double longitude, double latitude, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getItemPreview(String collectionId, String itemId)
        @Generated public Mono<BinaryData> getItemPreview(String collectionId, String itemId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, String colorFormula, String dstCrs, Resampling resampling, Integer maxSize, Integer height, Integer width, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<BinaryData> getItemPreviewWithFormat(String collectionId, String itemId, String format)
        @Generated public Mono<BinaryData> getItemPreviewWithFormat(String collectionId, String itemId, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String dstCrs, Resampling resampling, Integer maxSize, Integer height, Integer width, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getItemPreviewWithFormatWithResponse(String collectionId, String itemId, String format, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getItemPreviewWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Mono<TilerStacItemStatistics> getItemStatistics(String collectionId, String itemId)
        @Generated public Mono<TilerStacItemStatistics> getItemStatistics(String collectionId, String itemId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Resampling resampling, Integer maxSize, Boolean categorical, List<Integer> categoriesPixels, List<Integer> percentiles, String histogramBins, String histogramRange, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, String algorithm, String algorithmParams, Integer height, Integer width)
        @Generated public Mono<Response<BinaryData>> getItemStatisticsWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Mono<TileJsonMetadata> getItemTileJson(String collectionId, String itemId)
        @Generated public Mono<TileJsonMetadata> getItemTileJson(String collectionId, String itemId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getItemTileJsonWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Mono<TileJsonMetadata> getItemTileJsonWithTms(String collectionId, String itemId, String tileMatrixSetId)
        @Generated public Mono<TileJsonMetadata> getItemTileJsonWithTms(String collectionId, String itemId, String tileMatrixSetId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getItemTileJsonWithTmsWithResponse(String collectionId, String itemId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public Mono<byte[]> getItemWmtsCapabilities(String collectionId, String itemId)
        @Generated public Mono<byte[]> getItemWmtsCapabilities(String collectionId, String itemId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getItemWmtsCapabilitiesWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Mono<byte[]> getItemWmtsCapabilitiesWithTms(String collectionId, String itemId, String tileMatrixSetId)
        @Generated public Mono<byte[]> getItemWmtsCapabilitiesWithTms(String collectionId, String itemId, String tileMatrixSetId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getItemWmtsCapabilitiesWithTmsWithResponse(String collectionId, String itemId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getLegend(String colorMapName)
        @Generated public Mono<BinaryData> getLegend(String colorMapName, Double height, Double width, Integer trimStart, Integer trimEnd)
        @Generated public Mono<Response<BinaryData>> getLegendWithResponse(String colorMapName, RequestOptions requestOptions)
        @Generated public Mono<TilerMosaicSearchRegistrationResponse> registerMosaicsSearch(RegisterMosaicsSearchOptions options)
        @Generated public Mono<Response<BinaryData>> registerMosaicsSearchWithResponse(BinaryData registerMosaicsSearchRequest, RequestOptions requestOptions)
        @Generated public Mono<List<BinaryData>> getSearchAssetsForTileNoTms(String searchId, double z, double x, double y)
        @Generated public Mono<List<BinaryData>> getSearchAssetsForTileNoTms(String searchId, double z, double x, double y, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TileMatrixSetId tileMatrixSetId)
        @Generated public Mono<Response<BinaryData>> getSearchAssetsForTileNoTmsWithResponse(String searchId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public Mono<List<TilerAssetGeoJson>> getSearchAssetsForTileWithTms(String searchId, String tileMatrixSetId, String collectionId, double z, double x, double y)
        @Generated public Mono<List<TilerAssetGeoJson>> getSearchAssetsForTileWithTms(String searchId, String tileMatrixSetId, String collectionId, double z, double x, double y, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getSearchAssetsForTileWithTmsWithResponse(String searchId, String tileMatrixSetId, String collectionId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public Mono<List<BinaryData>> getSearchBboxAssets(String searchId, double minx, double miny, double maxx, double maxy)
        @Generated public Mono<List<BinaryData>> getSearchBboxAssets(String searchId, double minx, double miny, double maxx, double maxy, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, String coordinateReferenceSystem)
        @Generated public Mono<Response<BinaryData>> getSearchBboxAssetsWithResponse(String searchId, double minx, double miny, double maxx, double maxy, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getSearchBboxCrop(String searchId, double minx, double miny, double maxx, double maxy, String format)
        @Generated public Mono<BinaryData> getSearchBboxCrop(String searchId, double minx, double miny, double maxx, double maxy, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, String destinationCrs, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask)
        @Generated public Mono<BinaryData> getSearchBboxCropWithDimensions(String searchId, double minx, double miny, double maxx, double maxy, int width, int height, String format)
        @Generated public Mono<BinaryData> getSearchBboxCropWithDimensions(String searchId, double minx, double miny, double maxx, double maxy, int width, int height, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, String destinationCrs, Integer maxSize, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask)
        @Generated public Mono<Response<BinaryData>> getSearchBboxCropWithDimensionsWithResponse(String searchId, double minx, double miny, double maxx, double maxy, int width, int height, String format, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getSearchBboxCropWithResponse(String searchId, double minx, double miny, double maxx, double maxy, String format, RequestOptions requestOptions)
        @Generated public Mono<TilerStacSearchRegistration> getSearchInfo(String searchId)
        @Generated public Mono<Response<BinaryData>> getSearchInfoWithResponse(String searchId, RequestOptions requestOptions)
        @Generated public Mono<TilerCoreModelsResponsesPoint> getSearchPoint(String searchId, double longitude, double latitude)
        @Generated public Mono<TilerCoreModelsResponsesPoint> getSearchPoint(String searchId, double longitude, double latitude, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, String coordinateReferenceSystem, Resampling resampling)
        @Generated public Mono<List<StacItemPointAsset>> getSearchPointWithAssets(String searchId, double longitude, double latitude)
        @Generated public Mono<List<StacItemPointAsset>> getSearchPointWithAssets(String searchId, double longitude, double latitude, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, String coordinateReferenceSystem)
        @Generated public Mono<Response<BinaryData>> getSearchPointWithAssetsWithResponse(String searchId, double longitude, double latitude, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getSearchPointWithResponse(String searchId, double longitude, double latitude, RequestOptions requestOptions)
        @Generated public Mono<TileJsonMetadata> getSearchTileJson(String searchId)
        @Generated public Mono<TileJsonMetadata> getSearchTileJson(String searchId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Integer padding, Double buffer, String colorFormula, String collectionId, Resampling resampling, PixelSelection pixelSelection, TerrainAlgorithm algorithm, String algorithmParams, List<String> rescale, ColorMapNames colormapName, String colormap, Boolean returnMask)
        @Generated public Mono<Response<BinaryData>> getSearchTileJsonWithResponse(String searchId, RequestOptions requestOptions)
        @Generated public Mono<TileJsonMetadata> getSearchTileJsonWithTms(String searchId, String tileMatrixSetId)
        @Generated public Mono<TileJsonMetadata> getSearchTileJsonWithTms(String searchId, String tileMatrixSetId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, Integer minZoom, Integer maxZoom, TilerImageFormat tileFormat, Integer tileScale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<Response<BinaryData>> getSearchTileJsonWithTmsWithResponse(String searchId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getSearchTileNoTms(String searchId, double z, double x, double y)
        @Generated public Mono<BinaryData> getSearchTileNoTms(String searchId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<BinaryData> getSearchTileNoTmsByFormat(String searchId, double z, double x, double y, String format)
        @Generated public Mono<BinaryData> getSearchTileNoTmsByFormat(String searchId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<Response<BinaryData>> getSearchTileNoTmsByFormatWithResponse(String searchId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getSearchTileNoTmsByScale(String searchId, double z, double x, double y, double scale)
        @Generated public Mono<BinaryData> getSearchTileNoTmsByScale(String searchId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<BinaryData> getSearchTileNoTmsByScaleAndFormat(String searchId, double z, double x, double y, double scale, String format)
        @Generated public Mono<BinaryData> getSearchTileNoTmsByScaleAndFormat(String searchId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<Response<BinaryData>> getSearchTileNoTmsByScaleAndFormatWithResponse(String searchId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getSearchTileNoTmsByScaleWithResponse(String searchId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getSearchTileNoTmsWithResponse(String searchId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public Mono<TileSetMetadata> getSearchTilesetMetadata(String searchId, String tileMatrixSetId)
        @Generated public Mono<TileSetMetadata> getSearchTilesetMetadata(String searchId, String tileMatrixSetId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getSearchTilesetMetadataWithResponse(String searchId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public Mono<TileSetList> getSearchTilesets(String searchId)
        @Generated public Mono<TileSetList> getSearchTilesets(String searchId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getSearchTilesetsWithResponse(String searchId, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getSearchTileWithTms(String searchId, String tileMatrixSetId, double z, double x, double y)
        @Generated public Mono<BinaryData> getSearchTileWithTms(String searchId, String tileMatrixSetId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<BinaryData> getSearchTileWithTmsByFormat(String searchId, String tileMatrixSetId, double z, double x, double y, String format)
        @Generated public Mono<BinaryData> getSearchTileWithTmsByFormat(String searchId, String tileMatrixSetId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<Response<BinaryData>> getSearchTileWithTmsByFormatWithResponse(String searchId, String tileMatrixSetId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getSearchTileWithTmsByScale(String searchId, String tileMatrixSetId, double z, double x, double y, double scale)
        @Generated public Mono<BinaryData> getSearchTileWithTmsByScale(String searchId, String tileMatrixSetId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<BinaryData> getSearchTileWithTmsByScaleAndFormat(String searchId, String tileMatrixSetId, double z, double x, double y, double scale, String format)
        @Generated public Mono<BinaryData> getSearchTileWithTmsByScaleAndFormat(String searchId, String tileMatrixSetId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Mono<Response<BinaryData>> getSearchTileWithTmsByScaleAndFormatWithResponse(String searchId, String tileMatrixSetId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getSearchTileWithTmsByScaleWithResponse(String searchId, String tileMatrixSetId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getSearchTileWithTmsWithResponse(String searchId, String tileMatrixSetId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public Mono<byte[]> getSearchWmtsCapabilities(String searchId)
        @Generated public Mono<byte[]> getSearchWmtsCapabilities(String searchId, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject)
        @Generated public Mono<Response<BinaryData>> getSearchWmtsCapabilitiesWithResponse(String searchId, RequestOptions requestOptions)
        @Generated public Mono<byte[]> getSearchWmtsCapabilitiesWithTms(String searchId, String tileMatrixSetId)
        @Generated public Mono<byte[]> getSearchWmtsCapabilitiesWithTms(String searchId, String tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject)
        @Generated public Mono<Response<BinaryData>> getSearchWmtsCapabilitiesWithTmsWithResponse(String searchId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public Mono<List<String>> getTileMatrices()
        @Generated public Mono<Response<BinaryData>> getTileMatricesWithResponse(RequestOptions requestOptions)
        @Generated public Mono<TileMatrixSet> getTileMatrixDefinitions(String tileMatrixSetId)
        @Generated public Mono<Response<BinaryData>> getTileMatrixDefinitionsWithResponse(String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getTileNoTms(String collectionId, String itemId, double z, double x, double y)
        @Generated public Mono<BinaryData> getTileNoTms(String collectionId, String itemId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<BinaryData> getTileNoTmsByFormat(String collectionId, String itemId, double z, double x, double y, String format)
        @Generated public Mono<BinaryData> getTileNoTmsByFormat(String collectionId, String itemId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Integer scale, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getTileNoTmsByFormatWithResponse(String collectionId, String itemId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getTileNoTmsByScale(String collectionId, String itemId, double z, double x, double y, double scale)
        @Generated public Mono<BinaryData> getTileNoTmsByScale(String collectionId, String itemId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<BinaryData> getTileNoTmsByScaleAndFormat(String collectionId, String itemId, double z, double x, double y, double scale, String format)
        @Generated public Mono<BinaryData> getTileNoTmsByScaleAndFormat(String collectionId, String itemId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getTileNoTmsByScaleAndFormatWithResponse(String collectionId, String itemId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getTileNoTmsByScaleWithResponse(String collectionId, String itemId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getTileNoTmsWithResponse(String collectionId, String itemId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public Mono<TileSetMetadata> getTilesetMetadata(String collectionId, String itemId, String tileMatrixSetId)
        @Generated public Mono<TileSetMetadata> getTilesetMetadata(String collectionId, String itemId, String tileMatrixSetId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getTilesetMetadataWithResponse(String collectionId, String itemId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public Mono<TileSetList> getTilesets(String collectionId, String itemId)
        @Generated public Mono<TileSetList> getTilesets(String collectionId, String itemId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getTilesetsWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getTileWithTms(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y)
        @Generated public Mono<BinaryData> getTileWithTms(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<BinaryData> getTileWithTmsByFormat(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, String format)
        @Generated public Mono<BinaryData> getTileWithTmsByFormat(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, Integer scale, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getTileWithTmsByFormatWithResponse(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getTileWithTmsByScale(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale)
        @Generated public Mono<BinaryData> getTileWithTmsByScale(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<BinaryData> getTileWithTmsByScaleAndFormat(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale, String format)
        @Generated public Mono<BinaryData> getTileWithTmsByScaleAndFormat(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Mono<Response<BinaryData>> getTileWithTmsByScaleAndFormatWithResponse(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getTileWithTmsByScaleWithResponse(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getTileWithTmsWithResponse(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, RequestOptions requestOptions)
    }
    @ServiceClient(builder = PlanetaryComputerProClientBuilder)
    public final class DataClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        @Generated public ClassMapLegendResponse getClassMapLegend(String classmapName)
        @Generated public ClassMapLegendResponse getClassMapLegend(String classmapName, Integer trimStart, Integer trimEnd)
        @Generated public Response<BinaryData> getClassMapLegendWithResponse(String classmapName, RequestOptions requestOptions)
        @Generated public List<BinaryData> getCollectionAssetsForBbox(String collectionId, double minx, double miny, double maxx, double maxy)
        @Generated public List<BinaryData> getCollectionAssetsForBbox(String collectionId, double minx, double miny, double maxx, double maxy, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, String coordinateReferenceSystem)
        @Generated public Response<BinaryData> getCollectionAssetsForBboxWithResponse(String collectionId, double minx, double miny, double maxx, double maxy, RequestOptions requestOptions)
        @Generated public List<BinaryData> getCollectionAssetsForTileNoTms(String collectionId, double z, double x, double y)
        @Generated public List<BinaryData> getCollectionAssetsForTileNoTms(String collectionId, double z, double x, double y, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TileMatrixSetId tileMatrixSetId)
        @Generated public Response<BinaryData> getCollectionAssetsForTileNoTmsWithResponse(String collectionId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public List<TilerAssetGeoJson> getCollectionAssetsForTileWithTms(String collectionId, String tileMatrixSetId, double z, double x, double y)
        @Generated public List<TilerAssetGeoJson> getCollectionAssetsForTileWithTms(String collectionId, String tileMatrixSetId, double z, double x, double y, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getCollectionAssetsForTileWithTmsWithResponse(String collectionId, String tileMatrixSetId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public BinaryData getCollectionBboxCrop(String collectionId, double minx, double miny, double maxx, double maxy, String format)
        @Generated public BinaryData getCollectionBboxCrop(String collectionId, double minx, double miny, double maxx, double maxy, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, String destinationCrs, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask)
        @Generated public BinaryData getCollectionBboxCropWithDimensions(String collectionId, double minx, double miny, double maxx, double maxy, int width, int height, String format)
        @Generated public BinaryData getCollectionBboxCropWithDimensions(String collectionId, double minx, double miny, double maxx, double maxy, int width, int height, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, String destinationCrs, Integer maxSize, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask)
        @Generated public Response<BinaryData> getCollectionBboxCropWithDimensionsWithResponse(String collectionId, double minx, double miny, double maxx, double maxy, int width, int height, String format, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getCollectionBboxCropWithResponse(String collectionId, double minx, double miny, double maxx, double maxy, String format, RequestOptions requestOptions)
        @Generated public TilerStacSearchRegistration getCollectionInfo(String collectionId)
        @Generated public Response<BinaryData> getCollectionInfoWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public TilerCoreModelsResponsesPoint getCollectionPoint(String collectionId, double longitude, double latitude)
        @Generated public TilerCoreModelsResponsesPoint getCollectionPoint(String collectionId, double longitude, double latitude, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, String coordinateReferenceSystem, Resampling resampling)
        @Generated public List<StacItemPointAsset> getCollectionPointAssets(String collectionId, double longitude, double latitude)
        @Generated public List<StacItemPointAsset> getCollectionPointAssets(String collectionId, double longitude, double latitude, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, String coordinateReferenceSystem)
        @Generated public Response<BinaryData> getCollectionPointAssetsWithResponse(String collectionId, double longitude, double latitude, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getCollectionPointWithResponse(String collectionId, double longitude, double latitude, RequestOptions requestOptions)
        @Generated public TileJsonMetadata getCollectionTileJson(String collectionId)
        @Generated public TileJsonMetadata getCollectionTileJson(String collectionId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Response<BinaryData> getCollectionTileJsonWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public TileJsonMetadata getCollectionTileJsonWithTms(String collectionId, String tileMatrixSetId)
        @Generated public TileJsonMetadata getCollectionTileJsonWithTms(String collectionId, String tileMatrixSetId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Response<BinaryData> getCollectionTileJsonWithTmsWithResponse(String collectionId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public BinaryData getCollectionTileNoTms(String collectionId, double z, double x, double y)
        @Generated public BinaryData getCollectionTileNoTms(String collectionId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public BinaryData getCollectionTileNoTmsByFormat(String collectionId, double z, double x, double y, String format)
        @Generated public BinaryData getCollectionTileNoTmsByFormat(String collectionId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Response<BinaryData> getCollectionTileNoTmsByFormatWithResponse(String collectionId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public BinaryData getCollectionTileNoTmsByScale(String collectionId, double z, double x, double y, double scale)
        @Generated public BinaryData getCollectionTileNoTmsByScale(String collectionId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public BinaryData getCollectionTileNoTmsByScaleAndFormat(String collectionId, double z, double x, double y, double scale, String format)
        @Generated public BinaryData getCollectionTileNoTmsByScaleAndFormat(String collectionId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Response<BinaryData> getCollectionTileNoTmsByScaleAndFormatWithResponse(String collectionId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getCollectionTileNoTmsByScaleWithResponse(String collectionId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getCollectionTileNoTmsWithResponse(String collectionId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public TileSetMetadata getCollectionTilesetMetadata(String collectionId, String tileMatrixSetId)
        @Generated public TileSetMetadata getCollectionTilesetMetadata(String collectionId, String tileMatrixSetId, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getCollectionTilesetMetadataWithResponse(String collectionId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public TileSetList getCollectionTilesets(String collectionId)
        @Generated public TileSetList getCollectionTilesets(String collectionId, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getCollectionTilesetsWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public BinaryData getCollectionTileWithTms(String collectionId, String tileMatrixSetId, double z, double x, double y)
        @Generated public BinaryData getCollectionTileWithTms(String collectionId, String tileMatrixSetId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public BinaryData getCollectionTileWithTmsByFormat(String collectionId, String tileMatrixSetId, double z, double x, double y, String format)
        @Generated public BinaryData getCollectionTileWithTmsByFormat(String collectionId, String tileMatrixSetId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Response<BinaryData> getCollectionTileWithTmsByFormatWithResponse(String collectionId, String tileMatrixSetId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public BinaryData getCollectionTileWithTmsByScale(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale)
        @Generated public BinaryData getCollectionTileWithTmsByScale(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public BinaryData getCollectionTileWithTmsByScaleAndFormat(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale, String format)
        @Generated public BinaryData getCollectionTileWithTmsByScaleAndFormat(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Response<BinaryData> getCollectionTileWithTmsByScaleAndFormatWithResponse(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getCollectionTileWithTmsByScaleWithResponse(String collectionId, String tileMatrixSetId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getCollectionTileWithTmsWithResponse(String collectionId, String tileMatrixSetId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public byte[] getCollectionWmtsCapabilities(String collectionId)
        @Generated public byte[] getCollectionWmtsCapabilities(String collectionId, String ids, String bbox, String query, String sortby, String datetime, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject)
        @Generated public Response<BinaryData> getCollectionWmtsCapabilitiesWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public byte[] getCollectionWmtsCapabilitiesWithTms(String collectionId, String tileMatrixSetId)
        @Generated public byte[] getCollectionWmtsCapabilitiesWithTms(String collectionId, String tileMatrixSetId, String ids, String bbox, String query, String sortby, String datetime, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject)
        @Generated public Response<BinaryData> getCollectionWmtsCapabilitiesWithTmsWithResponse(String collectionId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public BinaryData cropCollectionFeature(String collectionId, GeoJsonFeature body)
        @Generated public BinaryData cropCollectionFeature(String collectionId, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs, TilerImageFormat format)
        @Generated public BinaryData cropCollectionFeatureByFormat(String collectionId, String format, GeoJsonFeature body)
        @Generated public BinaryData cropCollectionFeatureByFormat(String collectionId, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs)
        @Generated public Response<BinaryData> cropCollectionFeatureByFormatWithResponse(String collectionId, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public BinaryData cropCollectionFeatureWidthByHeight(String collectionId, int width, int height, String format, GeoJsonFeature body)
        @Generated public BinaryData cropCollectionFeatureWidthByHeight(String collectionId, int width, int height, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String ids, String bbox, String query, String sortby, String datetime, String subdatasetName, List<Integer> subdatasetBands, String crs, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs)
        @Generated public Response<BinaryData> cropCollectionFeatureWidthByHeightWithResponse(String collectionId, int width, int height, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public Response<BinaryData> cropCollectionFeatureWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public BinaryData cropFeature(String collectionId, String itemId, GeoJsonFeature body)
        @Generated public BinaryData cropFeature(String collectionId, String itemId, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String coordinateReferenceSystem, Resampling resampling, Integer maxSize, Integer height, Integer width, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TilerImageFormat format)
        @Generated public BinaryData cropFeatureByFormat(String collectionId, String itemId, String format, GeoJsonFeature body)
        @Generated public BinaryData cropFeatureByFormat(String collectionId, String itemId, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String coordinateReferenceSystem, Resampling resampling, Integer maxSize, Integer height, Integer width, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> cropFeatureByFormatWithResponse(String collectionId, String itemId, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public BinaryData cropFeatureWidthByHeight(String collectionId, String itemId, int width, int height, String format, GeoJsonFeature body)
        @Generated public BinaryData cropFeatureWidthByHeight(String collectionId, String itemId, int width, int height, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String coordinateReferenceSystem, Resampling resampling, Integer maxSize, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> cropFeatureWidthByHeightWithResponse(String collectionId, String itemId, int width, int height, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public Response<BinaryData> cropFeatureWithResponse(String collectionId, String itemId, BinaryData body, RequestOptions requestOptions)
        @Generated public BinaryData cropSearchFeature(String searchId, GeoJsonFeature body)
        @Generated public BinaryData cropSearchFeature(String searchId, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs, TilerImageFormat format)
        @Generated public BinaryData cropSearchFeatureByFormat(String searchId, String format, GeoJsonFeature body)
        @Generated public BinaryData cropSearchFeatureByFormat(String searchId, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs)
        @Generated public Response<BinaryData> cropSearchFeatureByFormatWithResponse(String searchId, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public BinaryData cropSearchFeatureWidthByHeight(String searchId, int width, int height, String format, GeoJsonFeature body)
        @Generated public BinaryData cropSearchFeatureWidthByHeight(String searchId, int width, int height, String format, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, Integer maxSize, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String destinationCrs)
        @Generated public Response<BinaryData> cropSearchFeatureWidthByHeightWithResponse(String searchId, int width, int height, String format, BinaryData body, RequestOptions requestOptions)
        @Generated public Response<BinaryData> cropSearchFeatureWithResponse(String searchId, BinaryData body, RequestOptions requestOptions)
        @Generated public List<List<List<Long>>> getIntervalLegend(String classmapName)
        @Generated public List<List<List<Long>>> getIntervalLegend(String classmapName, Integer trimStart, Integer trimEnd)
        @Generated public Response<BinaryData> getIntervalLegendWithResponse(String classmapName, RequestOptions requestOptions)
        @Generated public AssetStatisticsResponse getItemAssetStatistics(String collectionId, String itemId)
        @Generated public AssetStatisticsResponse getItemAssetStatistics(String collectionId, String itemId, List<Integer> bidx, List<String> assets, List<String> assetBandIndices, String noData, Boolean unscale, WarpKernelResampling reproject, Resampling resampling, Integer maxSize, Boolean categorical, List<Integer> categoriesPixels, List<Integer> percentiles, String histogramBins, String histogramRange, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, List<String> assetExpression, Integer height, Integer width)
        @Generated public Response<BinaryData> getItemAssetStatisticsWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public List<String> getItemAvailableAssets(String collectionId, String itemId)
        @Generated public List<String> getItemAvailableAssets(String collectionId, String itemId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getItemAvailableAssetsWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public BinaryData getItemBboxCrop(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, String format)
        @Generated public BinaryData getItemBboxCrop(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String coordinateReferenceSystem, String destinationCrs, Resampling resampling, Integer maxSize, Integer height, Integer width, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public BinaryData getItemBboxCropWithDimensions(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, int width, int height, String format)
        @Generated public BinaryData getItemBboxCropWithDimensions(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, int width, int height, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String coordinateReferenceSystem, String destinationCrs, Resampling resampling, Integer maxSize, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getItemBboxCropWithDimensionsWithResponse(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, int width, int height, String format, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getItemBboxCropWithResponse(String collectionId, String itemId, double minx, double miny, double maxx, double maxy, String format, RequestOptions requestOptions)
        @Generated public StacItemBounds getItemBounds(String collectionId, String itemId)
        @Generated public StacItemBounds getItemBounds(String collectionId, String itemId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getItemBoundsWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public StacItemStatisticsGeoJson getItemFeatureStatistics(String collectionId, String itemId, GeoJsonFeature body)
        @Generated public StacItemStatisticsGeoJson getItemFeatureStatistics(String collectionId, String itemId, GeoJsonFeature body, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, String coordinateReferenceSystem, Resampling resampling, Integer maxSize, Boolean categorical, List<Integer> categoriesPixels, List<Integer> percentiles, String histogramBins, String histogramRange, String destinationCrs, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, String algorithm, String algorithmParams, Integer height, Integer width)
        @Generated public Response<BinaryData> getItemFeatureStatisticsWithResponse(String collectionId, String itemId, BinaryData body, RequestOptions requestOptions)
        @Generated public TilerInfoMapResponse getItemInfo(String collectionId, String itemId)
        @Generated public TilerInfoMapResponse getItemInfo(String collectionId, String itemId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, List<String> assets)
        @Generated public TilerInfoGeoJsonFeature getItemInfoGeoJson(String collectionId, String itemId)
        @Generated public TilerInfoGeoJsonFeature getItemInfoGeoJson(String collectionId, String itemId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, List<String> assets)
        @Generated public Response<BinaryData> getItemInfoGeoJsonWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getItemInfoWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public TilerCoreModelsResponsesPoint getItemPoint(String collectionId, String itemId, double longitude, double latitude)
        @Generated public TilerCoreModelsResponsesPoint getItemPoint(String collectionId, String itemId, double longitude, double latitude, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, String coordinateReferenceSystem, Resampling resampling)
        @Generated public Response<BinaryData> getItemPointWithResponse(String collectionId, String itemId, double longitude, double latitude, RequestOptions requestOptions)
        @Generated public BinaryData getItemPreview(String collectionId, String itemId)
        @Generated public BinaryData getItemPreview(String collectionId, String itemId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, String colorFormula, String dstCrs, Resampling resampling, Integer maxSize, Integer height, Integer width, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public BinaryData getItemPreviewWithFormat(String collectionId, String itemId, String format)
        @Generated public BinaryData getItemPreviewWithFormat(String collectionId, String itemId, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, String colorFormula, String dstCrs, Resampling resampling, Integer maxSize, Integer height, Integer width, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getItemPreviewWithFormatWithResponse(String collectionId, String itemId, String format, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getItemPreviewWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public TilerStacItemStatistics getItemStatistics(String collectionId, String itemId)
        @Generated public TilerStacItemStatistics getItemStatistics(String collectionId, String itemId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Resampling resampling, Integer maxSize, Boolean categorical, List<Integer> categoriesPixels, List<Integer> percentiles, String histogramBins, String histogramRange, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, String algorithm, String algorithmParams, Integer height, Integer width)
        @Generated public Response<BinaryData> getItemStatisticsWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public TileJsonMetadata getItemTileJson(String collectionId, String itemId)
        @Generated public TileJsonMetadata getItemTileJson(String collectionId, String itemId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getItemTileJsonWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public TileJsonMetadata getItemTileJsonWithTms(String collectionId, String itemId, String tileMatrixSetId)
        @Generated public TileJsonMetadata getItemTileJsonWithTms(String collectionId, String itemId, String tileMatrixSetId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getItemTileJsonWithTmsWithResponse(String collectionId, String itemId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public byte[] getItemWmtsCapabilities(String collectionId, String itemId)
        @Generated public byte[] getItemWmtsCapabilities(String collectionId, String itemId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getItemWmtsCapabilitiesWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public byte[] getItemWmtsCapabilitiesWithTms(String collectionId, String itemId, String tileMatrixSetId)
        @Generated public byte[] getItemWmtsCapabilitiesWithTms(String collectionId, String itemId, String tileMatrixSetId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getItemWmtsCapabilitiesWithTmsWithResponse(String collectionId, String itemId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public BinaryData getLegend(String colorMapName)
        @Generated public BinaryData getLegend(String colorMapName, Double height, Double width, Integer trimStart, Integer trimEnd)
        @Generated public Response<BinaryData> getLegendWithResponse(String colorMapName, RequestOptions requestOptions)
        @Generated public TilerMosaicSearchRegistrationResponse registerMosaicsSearch(RegisterMosaicsSearchOptions options)
        @Generated public Response<BinaryData> registerMosaicsSearchWithResponse(BinaryData registerMosaicsSearchRequest, RequestOptions requestOptions)
        @Generated public List<BinaryData> getSearchAssetsForTileNoTms(String searchId, double z, double x, double y)
        @Generated public List<BinaryData> getSearchAssetsForTileNoTms(String searchId, double z, double x, double y, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TileMatrixSetId tileMatrixSetId)
        @Generated public Response<BinaryData> getSearchAssetsForTileNoTmsWithResponse(String searchId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public List<TilerAssetGeoJson> getSearchAssetsForTileWithTms(String searchId, String tileMatrixSetId, String collectionId, double z, double x, double y)
        @Generated public List<TilerAssetGeoJson> getSearchAssetsForTileWithTms(String searchId, String tileMatrixSetId, String collectionId, double z, double x, double y, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getSearchAssetsForTileWithTmsWithResponse(String searchId, String tileMatrixSetId, String collectionId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public List<BinaryData> getSearchBboxAssets(String searchId, double minx, double miny, double maxx, double maxy)
        @Generated public List<BinaryData> getSearchBboxAssets(String searchId, double minx, double miny, double maxx, double maxy, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, String coordinateReferenceSystem)
        @Generated public Response<BinaryData> getSearchBboxAssetsWithResponse(String searchId, double minx, double miny, double maxx, double maxy, RequestOptions requestOptions)
        @Generated public BinaryData getSearchBboxCrop(String searchId, double minx, double miny, double maxx, double maxy, String format)
        @Generated public BinaryData getSearchBboxCrop(String searchId, double minx, double miny, double maxx, double maxy, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, String destinationCrs, Integer maxSize, Integer height, Integer width, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask)
        @Generated public BinaryData getSearchBboxCropWithDimensions(String searchId, double minx, double miny, double maxx, double maxy, int width, int height, String format)
        @Generated public BinaryData getSearchBboxCropWithDimensions(String searchId, double minx, double miny, double maxx, double maxy, int width, int height, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, String coordinateReferenceSystem, String destinationCrs, Integer maxSize, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask)
        @Generated public Response<BinaryData> getSearchBboxCropWithDimensionsWithResponse(String searchId, double minx, double miny, double maxx, double maxy, int width, int height, String format, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getSearchBboxCropWithResponse(String searchId, double minx, double miny, double maxx, double maxy, String format, RequestOptions requestOptions)
        @Generated public TilerStacSearchRegistration getSearchInfo(String searchId)
        @Generated public Response<BinaryData> getSearchInfoWithResponse(String searchId, RequestOptions requestOptions)
        @Generated public TilerCoreModelsResponsesPoint getSearchPoint(String searchId, double longitude, double latitude)
        @Generated public TilerCoreModelsResponsesPoint getSearchPoint(String searchId, double longitude, double latitude, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, String coordinateReferenceSystem, Resampling resampling)
        @Generated public List<StacItemPointAsset> getSearchPointWithAssets(String searchId, double longitude, double latitude)
        @Generated public List<StacItemPointAsset> getSearchPointWithAssets(String searchId, double longitude, double latitude, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, String coordinateReferenceSystem)
        @Generated public Response<BinaryData> getSearchPointWithAssetsWithResponse(String searchId, double longitude, double latitude, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getSearchPointWithResponse(String searchId, double longitude, double latitude, RequestOptions requestOptions)
        @Generated public TileJsonMetadata getSearchTileJson(String searchId)
        @Generated public TileJsonMetadata getSearchTileJson(String searchId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, Integer padding, Double buffer, String colorFormula, String collectionId, Resampling resampling, PixelSelection pixelSelection, TerrainAlgorithm algorithm, String algorithmParams, List<String> rescale, ColorMapNames colormapName, String colormap, Boolean returnMask)
        @Generated public Response<BinaryData> getSearchTileJsonWithResponse(String searchId, RequestOptions requestOptions)
        @Generated public TileJsonMetadata getSearchTileJsonWithTms(String searchId, String tileMatrixSetId)
        @Generated public TileJsonMetadata getSearchTileJsonWithTms(String searchId, String tileMatrixSetId, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, Integer minZoom, Integer maxZoom, TilerImageFormat tileFormat, Integer tileScale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Response<BinaryData> getSearchTileJsonWithTmsWithResponse(String searchId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public BinaryData getSearchTileNoTms(String searchId, double z, double x, double y)
        @Generated public BinaryData getSearchTileNoTms(String searchId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public BinaryData getSearchTileNoTmsByFormat(String searchId, double z, double x, double y, String format)
        @Generated public BinaryData getSearchTileNoTmsByFormat(String searchId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Response<BinaryData> getSearchTileNoTmsByFormatWithResponse(String searchId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public BinaryData getSearchTileNoTmsByScale(String searchId, double z, double x, double y, double scale)
        @Generated public BinaryData getSearchTileNoTmsByScale(String searchId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public BinaryData getSearchTileNoTmsByScaleAndFormat(String searchId, double z, double x, double y, double scale, String format)
        @Generated public BinaryData getSearchTileNoTmsByScaleAndFormat(String searchId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Response<BinaryData> getSearchTileNoTmsByScaleAndFormatWithResponse(String searchId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getSearchTileNoTmsByScaleWithResponse(String searchId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getSearchTileNoTmsWithResponse(String searchId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public TileSetMetadata getSearchTilesetMetadata(String searchId, String tileMatrixSetId)
        @Generated public TileSetMetadata getSearchTilesetMetadata(String searchId, String tileMatrixSetId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getSearchTilesetMetadataWithResponse(String searchId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public TileSetList getSearchTilesets(String searchId)
        @Generated public TileSetList getSearchTilesets(String searchId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getSearchTilesetsWithResponse(String searchId, RequestOptions requestOptions)
        @Generated public BinaryData getSearchTileWithTms(String searchId, String tileMatrixSetId, double z, double x, double y)
        @Generated public BinaryData getSearchTileWithTms(String searchId, String tileMatrixSetId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public BinaryData getSearchTileWithTmsByFormat(String searchId, String tileMatrixSetId, double z, double x, double y, String format)
        @Generated public BinaryData getSearchTileWithTmsByFormat(String searchId, String tileMatrixSetId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, Integer scale, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Response<BinaryData> getSearchTileWithTmsByFormatWithResponse(String searchId, String tileMatrixSetId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public BinaryData getSearchTileWithTmsByScale(String searchId, String tileMatrixSetId, double z, double x, double y, double scale)
        @Generated public BinaryData getSearchTileWithTmsByScale(String searchId, String tileMatrixSetId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public BinaryData getSearchTileWithTmsByScaleAndFormat(String searchId, String tileMatrixSetId, double z, double x, double y, double scale, String format)
        @Generated public BinaryData getSearchTileWithTmsByScaleAndFormat(String searchId, String tileMatrixSetId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, Integer scanLimit, Integer itemsLimit, Integer timeLimit, Boolean exitWhenFull, Boolean skipCovered, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod, TerrainAlgorithm algorithm, String algorithmParams, Double buffer, String colorFormula, String collection, Resampling resampling, PixelSelection pixelSelection, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding)
        @Generated public Response<BinaryData> getSearchTileWithTmsByScaleAndFormatWithResponse(String searchId, String tileMatrixSetId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getSearchTileWithTmsByScaleWithResponse(String searchId, String tileMatrixSetId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getSearchTileWithTmsWithResponse(String searchId, String tileMatrixSetId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public byte[] getSearchWmtsCapabilities(String searchId)
        @Generated public byte[] getSearchWmtsCapabilities(String searchId, TileMatrixSetId tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject)
        @Generated public Response<BinaryData> getSearchWmtsCapabilitiesWithResponse(String searchId, RequestOptions requestOptions)
        @Generated public byte[] getSearchWmtsCapabilitiesWithTms(String searchId, String tileMatrixSetId)
        @Generated public byte[] getSearchWmtsCapabilitiesWithTms(String searchId, String tileMatrixSetId, TilerImageFormat tileFormat, Integer tileScale, Integer minZoom, Integer maxZoom, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject)
        @Generated public Response<BinaryData> getSearchWmtsCapabilitiesWithTmsWithResponse(String searchId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public List<String> getTileMatrices()
        @Generated public Response<BinaryData> getTileMatricesWithResponse(RequestOptions requestOptions)
        @Generated public TileMatrixSet getTileMatrixDefinitions(String tileMatrixSetId)
        @Generated public Response<BinaryData> getTileMatrixDefinitionsWithResponse(String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public BinaryData getTileNoTms(String collectionId, String itemId, double z, double x, double y)
        @Generated public BinaryData getTileNoTms(String collectionId, String itemId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public BinaryData getTileNoTmsByFormat(String collectionId, String itemId, double z, double x, double y, String format)
        @Generated public BinaryData getTileNoTmsByFormat(String collectionId, String itemId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Integer scale, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getTileNoTmsByFormatWithResponse(String collectionId, String itemId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public BinaryData getTileNoTmsByScale(String collectionId, String itemId, double z, double x, double y, double scale)
        @Generated public BinaryData getTileNoTmsByScale(String collectionId, String itemId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, TilerImageFormat format, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public BinaryData getTileNoTmsByScaleAndFormat(String collectionId, String itemId, double z, double x, double y, double scale, String format)
        @Generated public BinaryData getTileNoTmsByScaleAndFormat(String collectionId, String itemId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TileMatrixSetId tileMatrixSetId, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getTileNoTmsByScaleAndFormatWithResponse(String collectionId, String itemId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getTileNoTmsByScaleWithResponse(String collectionId, String itemId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getTileNoTmsWithResponse(String collectionId, String itemId, double z, double x, double y, RequestOptions requestOptions)
        @Generated public TileSetMetadata getTilesetMetadata(String collectionId, String itemId, String tileMatrixSetId)
        @Generated public TileSetMetadata getTilesetMetadata(String collectionId, String itemId, String tileMatrixSetId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getTilesetMetadataWithResponse(String collectionId, String itemId, String tileMatrixSetId, RequestOptions requestOptions)
        @Generated public TileSetList getTilesets(String collectionId, String itemId)
        @Generated public TileSetList getTilesets(String collectionId, String itemId, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getTilesetsWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public BinaryData getTileWithTms(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y)
        @Generated public BinaryData getTileWithTms(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Integer scale, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public BinaryData getTileWithTmsByFormat(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, String format)
        @Generated public BinaryData getTileWithTmsByFormat(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, Integer scale, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getTileWithTmsByFormatWithResponse(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, String format, RequestOptions requestOptions)
        @Generated public BinaryData getTileWithTmsByScale(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale)
        @Generated public BinaryData getTileWithTmsByScale(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, TilerImageFormat format, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public BinaryData getTileWithTmsByScaleAndFormat(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale, String format)
        @Generated public BinaryData getTileWithTmsByScaleAndFormat(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale, String format, List<Integer> bidx, List<String> assets, String expression, List<String> assetBandIndices, Boolean assetAsBand, String noData, Boolean unscale, WarpKernelResampling reproject, TerrainAlgorithm algorithm, String algorithmParams, Double buffer, String colorFormula, Resampling resampling, List<String> rescale, ColorMapNames colorMapName, String colorMap, Boolean returnMask, Integer padding, String subdatasetName, List<Integer> subdatasetBands, String crs, String datetime, List<String> sel, SelMethod selMethod)
        @Generated public Response<BinaryData> getTileWithTmsByScaleAndFormatWithResponse(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale, String format, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getTileWithTmsByScaleWithResponse(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, double scale, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getTileWithTmsWithResponse(String collectionId, String itemId, String tileMatrixSetId, double z, double x, double y, RequestOptions requestOptions)
    }
    @ServiceClient(builder = PlanetaryComputerProClientBuilder, isAsync = true)
    public final class IngestionAsyncClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        @Generated public Mono<IngestionDefinition> get(String collectionId, String ingestionId)
        @Generated public PollerFlux<Operation, Void> beginDelete(String collectionId, String ingestionId)
        @Generated public PollerFlux<BinaryData, Void> beginDelete(String collectionId, String ingestionId, RequestOptions requestOptions)
        @Generated public Mono<Void> cancelAllOperations()
        @Generated public Mono<Response<Void>> cancelAllOperationsWithResponse(RequestOptions requestOptions)
        @Generated public Mono<Void> cancelOperation(String operationId)
        @Generated public Mono<Response<Void>> cancelOperationWithResponse(String operationId, RequestOptions requestOptions)
        @Generated public Mono<IngestionDefinition> create(String collectionId, IngestionDefinition body)
        @Generated public Mono<IngestionRun> createRun(String collectionId, String ingestionId)
        @Generated public Mono<Response<BinaryData>> createRunWithResponse(String collectionId, String ingestionId, RequestOptions requestOptions)
        @Generated public Mono<IngestionSource> createSource(IngestionSource body)
        @Generated public Mono<Response<BinaryData>> createSourceWithResponse(BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> createWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<Void> deleteSource(String id)
        @Generated public Mono<Response<Void>> deleteSourceWithResponse(String id, RequestOptions requestOptions)
        @Generated public PagedFlux<IngestionDefinition> list(String collectionId)
        @Generated public PagedFlux<BinaryData> list(String collectionId, RequestOptions requestOptions)
        @Generated public PagedFlux<IngestionDefinition> list(String collectionId, Integer top, Integer skip)
        @Generated public PagedFlux<ManagedIdentityMetadata> listManagedIdentities()
        @Generated public PagedFlux<BinaryData> listManagedIdentities(RequestOptions requestOptions)
        @Generated public PagedFlux<Operation> listOperations()
        @Generated public PagedFlux<BinaryData> listOperations(RequestOptions requestOptions)
        @Generated public PagedFlux<Operation> listOperations(Integer top, Integer skip, String collectionId, OperationStatus status)
        @Generated public PagedFlux<IngestionRun> listRuns(String collectionId, String ingestionId)
        @Generated public PagedFlux<BinaryData> listRuns(String collectionId, String ingestionId, RequestOptions requestOptions)
        @Generated public PagedFlux<IngestionRun> listRuns(String collectionId, String ingestionId, Integer top, Integer skip)
        @Generated public PagedFlux<IngestionSourceSummary> listSources()
        @Generated public PagedFlux<BinaryData> listSources(RequestOptions requestOptions)
        @Generated public PagedFlux<IngestionSourceSummary> listSources(Integer top, Integer skip)
        @Generated public Mono<Operation> getOperation(String operationId)
        @Generated public Mono<Response<BinaryData>> getOperationWithResponse(String operationId, RequestOptions requestOptions)
        @Generated public Mono<IngestionSource> replaceSource(String id, IngestionSource body)
        @Generated public Mono<Response<BinaryData>> replaceSourceWithResponse(String id, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<IngestionRun> getRun(String collectionId, String ingestionId, String runId)
        @Generated public Mono<Response<BinaryData>> getRunWithResponse(String collectionId, String ingestionId, String runId, RequestOptions requestOptions)
        @Generated public Mono<IngestionSource> getSource(String id)
        @Generated public Mono<Response<BinaryData>> getSourceWithResponse(String id, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> updateWithResponse(String collectionId, String ingestionId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getWithResponse(String collectionId, String ingestionId, RequestOptions requestOptions)
    }
    @ServiceClient(builder = PlanetaryComputerProClientBuilder)
    public final class IngestionClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        @Generated public IngestionDefinition get(String collectionId, String ingestionId)
        @Generated public SyncPoller<Operation, Void> beginDelete(String collectionId, String ingestionId)
        @Generated public SyncPoller<BinaryData, Void> beginDelete(String collectionId, String ingestionId, RequestOptions requestOptions)
        @Generated public void cancelAllOperations()
        @Generated public Response<Void> cancelAllOperationsWithResponse(RequestOptions requestOptions)
        @Generated public void cancelOperation(String operationId)
        @Generated public Response<Void> cancelOperationWithResponse(String operationId, RequestOptions requestOptions)
        @Generated public IngestionDefinition create(String collectionId, IngestionDefinition body)
        @Generated public IngestionRun createRun(String collectionId, String ingestionId)
        @Generated public Response<BinaryData> createRunWithResponse(String collectionId, String ingestionId, RequestOptions requestOptions)
        @Generated public IngestionSource createSource(IngestionSource body)
        @Generated public Response<BinaryData> createSourceWithResponse(BinaryData body, RequestOptions requestOptions)
        @Generated public Response<BinaryData> createWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public void deleteSource(String id)
        @Generated public Response<Void> deleteSourceWithResponse(String id, RequestOptions requestOptions)
        @Generated public PagedIterable<IngestionDefinition> list(String collectionId)
        @Generated public PagedIterable<BinaryData> list(String collectionId, RequestOptions requestOptions)
        @Generated public PagedIterable<IngestionDefinition> list(String collectionId, Integer top, Integer skip)
        @Generated public PagedIterable<ManagedIdentityMetadata> listManagedIdentities()
        @Generated public PagedIterable<BinaryData> listManagedIdentities(RequestOptions requestOptions)
        @Generated public PagedIterable<Operation> listOperations()
        @Generated public PagedIterable<BinaryData> listOperations(RequestOptions requestOptions)
        @Generated public PagedIterable<Operation> listOperations(Integer top, Integer skip, String collectionId, OperationStatus status)
        @Generated public PagedIterable<IngestionRun> listRuns(String collectionId, String ingestionId)
        @Generated public PagedIterable<BinaryData> listRuns(String collectionId, String ingestionId, RequestOptions requestOptions)
        @Generated public PagedIterable<IngestionRun> listRuns(String collectionId, String ingestionId, Integer top, Integer skip)
        @Generated public PagedIterable<IngestionSourceSummary> listSources()
        @Generated public PagedIterable<BinaryData> listSources(RequestOptions requestOptions)
        @Generated public PagedIterable<IngestionSourceSummary> listSources(Integer top, Integer skip)
        @Generated public Operation getOperation(String operationId)
        @Generated public Response<BinaryData> getOperationWithResponse(String operationId, RequestOptions requestOptions)
        @Generated public IngestionSource replaceSource(String id, IngestionSource body)
        @Generated public Response<BinaryData> replaceSourceWithResponse(String id, BinaryData body, RequestOptions requestOptions)
        @Generated public IngestionRun getRun(String collectionId, String ingestionId, String runId)
        @Generated public Response<BinaryData> getRunWithResponse(String collectionId, String ingestionId, String runId, RequestOptions requestOptions)
        @Generated public IngestionSource getSource(String id)
        @Generated public Response<BinaryData> getSourceWithResponse(String id, RequestOptions requestOptions)
        @Generated public Response<BinaryData> updateWithResponse(String collectionId, String ingestionId, BinaryData body, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getWithResponse(String collectionId, String ingestionId, RequestOptions requestOptions)
    }
    @ServiceClientBuilder(serviceClients = { IngestionClient, StacClient, DataClient, SharedAccessSignatureClient, IngestionAsyncClient, StacAsyncClient, DataAsyncClient, SharedAccessSignatureAsyncClient })
    public final class PlanetaryComputerProClientBuilder implements HttpTrait<PlanetaryComputerProClientBuilder> , ConfigurationTrait<PlanetaryComputerProClientBuilder> , TokenCredentialTrait<PlanetaryComputerProClientBuilder> , EndpointTrait<PlanetaryComputerProClientBuilder> {
        @Generated public PlanetaryComputerProClientBuilder()
        @Generated @Override public PlanetaryComputerProClientBuilder addPolicy(HttpPipelinePolicy customPolicy)
        @Generated @Override public PlanetaryComputerProClientBuilder clientOptions(ClientOptions clientOptions)
        @Generated @Override public PlanetaryComputerProClientBuilder configuration(Configuration configuration)
        @Generated @Override public PlanetaryComputerProClientBuilder credential(TokenCredential tokenCredential)
        @Generated @Override public PlanetaryComputerProClientBuilder endpoint(String endpoint)
        @Generated @Override public PlanetaryComputerProClientBuilder httpClient(HttpClient httpClient)
        @Generated @Override public PlanetaryComputerProClientBuilder httpLogOptions(HttpLogOptions httpLogOptions)
        @Generated @Override public PlanetaryComputerProClientBuilder pipeline(HttpPipeline pipeline)
        @Generated @Override public PlanetaryComputerProClientBuilder retryOptions(RetryOptions retryOptions)
        @Generated public PlanetaryComputerProClientBuilder retryPolicy(RetryPolicy retryPolicy)
        @Generated public PlanetaryComputerProClientBuilder serviceVersion(PlanetaryComputerServiceVersion serviceVersion)
        @Generated public DataAsyncClient buildDataAsyncClient()
        @Generated public DataClient buildDataClient()
        @Generated public IngestionAsyncClient buildIngestionAsyncClient()
        @Generated public IngestionClient buildIngestionClient()
        @Generated public SharedAccessSignatureAsyncClient buildSharedAccessSignatureAsyncClient()
        @Generated public SharedAccessSignatureClient buildSharedAccessSignatureClient()
        @Generated public StacAsyncClient buildStacAsyncClient()
        @Generated public StacClient buildStacClient()
    }
    public enum PlanetaryComputerServiceVersion implements ServiceVersion {
        V2026_04_15("2026-04-15");
        public static PlanetaryComputerServiceVersion getLatest() // returns V2026_04_15
        @Override public String getVersion()
    }
    @ServiceClient(builder = PlanetaryComputerProClientBuilder, isAsync = true)
    public final class SharedAccessSignatureAsyncClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        @Generated public Mono<Void> revokeToken()
        @Generated public Mono<Void> revokeToken(Integer durationInMinutes)
        @Generated public Mono<Response<Void>> revokeTokenWithResponse(RequestOptions requestOptions)
        @Generated public Mono<SharedAccessSignatureToken> getToken(String collectionId)
        @Generated public Mono<SharedAccessSignatureToken> getToken(String collectionId, Integer durationInMinutes)
        @Generated public Mono<Response<BinaryData>> getTokenWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<SharedAccessSignatureSignedLink> getUrl(String href)
        @Generated public Mono<SharedAccessSignatureSignedLink> getUrl(String href, Integer durationInMinutes)
        @Generated public Mono<Response<BinaryData>> getUrlWithResponse(String href, RequestOptions requestOptions)
    }
    @ServiceClient(builder = PlanetaryComputerProClientBuilder)
    public final class SharedAccessSignatureClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        @Generated public void revokeToken()
        @Generated public void revokeToken(Integer durationInMinutes)
        @Generated public Response<Void> revokeTokenWithResponse(RequestOptions requestOptions)
        @Generated public SharedAccessSignatureToken getToken(String collectionId)
        @Generated public SharedAccessSignatureToken getToken(String collectionId, Integer durationInMinutes)
        @Generated public Response<BinaryData> getTokenWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public SharedAccessSignatureSignedLink getUrl(String href)
        @Generated public SharedAccessSignatureSignedLink getUrl(String href, Integer durationInMinutes)
        @Generated public Response<BinaryData> getUrlWithResponse(String href, RequestOptions requestOptions)
    }
    @ServiceClient(builder = PlanetaryComputerProClientBuilder, isAsync = true)
    public final class StacAsyncClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        @Generated public Mono<StacMosaic> addMosaic(String collectionId, StacMosaic body)
        @Generated public Mono<Response<BinaryData>> addMosaicWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public PollerFlux<Operation, Void> beginCreateCollection(StacCollection body)
        @Generated public PollerFlux<BinaryData, BinaryData> beginCreateCollection(BinaryData body, RequestOptions requestOptions)
        @Generated public PollerFlux<Operation, Void> beginCreateItem(String collectionId, StacItemOrStacItemCollection body)
        @Generated public PollerFlux<BinaryData, BinaryData> beginCreateItem(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public PollerFlux<Operation, Void> beginDeleteCollection(String collectionId)
        @Generated public PollerFlux<BinaryData, Void> beginDeleteCollection(String collectionId, RequestOptions requestOptions)
        @Generated public PollerFlux<Operation, Void> beginDeleteItem(String collectionId, String itemId)
        @Generated public PollerFlux<BinaryData, Void> beginDeleteItem(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public PollerFlux<Operation, Void> beginReplaceItem(String collectionId, String itemId, StacItem body)
        @Generated public PollerFlux<BinaryData, BinaryData> beginReplaceItem(String collectionId, String itemId, BinaryData body, RequestOptions requestOptions)
        @Generated public PollerFlux<BinaryData, BinaryData> beginUpdateItem(String collectionId, String itemId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<StacCollection> getCollection(String collectionId)
        @Generated public Mono<StacCollection> getCollection(String collectionId, StacAssetUrlSigningMode sign, Integer durationInMinutes)
        @Generated public Mono<UserCollectionSettings> getCollectionConfiguration(String collectionId)
        @Generated public Mono<Response<BinaryData>> getCollectionConfigurationWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<QueryableDefinitionsResponse> getCollectionQueryables(String collectionId)
        @Generated public Mono<Response<BinaryData>> getCollectionQueryablesWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<StacCatalogCollections> getCollections()
        @Generated public Mono<StacCatalogCollections> getCollections(StacAssetUrlSigningMode sign, Integer durationInMinutes)
        @Generated public Mono<Response<BinaryData>> getCollectionsWithResponse(RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getCollectionThumbnail(String collectionId)
        @Generated public Mono<Response<BinaryData>> getCollectionThumbnailWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getCollectionWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<StacConformanceClasses> getConformanceClasses()
        @Generated public Mono<Response<BinaryData>> getConformanceClassesWithResponse(RequestOptions requestOptions)
        @Generated public Mono<StacCollection> createCollectionAsset(String collectionId, StacAssetData body)
        @Generated public Mono<List<StacQueryable>> createQueryables(String collectionId, List<StacQueryable> body)
        @Generated public Mono<Response<BinaryData>> createQueryablesWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<RenderOption> createRenderOption(String collectionId, RenderOption body)
        @Generated public Mono<Response<BinaryData>> createRenderOptionWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<StacCollection> deleteCollectionAsset(String collectionId, String assetId)
        @Generated public Mono<Response<BinaryData>> deleteCollectionAssetWithResponse(String collectionId, String assetId, RequestOptions requestOptions)
        @Generated public Mono<Void> deleteMosaic(String collectionId, String mosaicId)
        @Generated public Mono<Response<Void>> deleteMosaicWithResponse(String collectionId, String mosaicId, RequestOptions requestOptions)
        @Generated public Mono<Void> deleteQueryable(String collectionId, String queryableName)
        @Generated public Mono<Response<Void>> deleteQueryableWithResponse(String collectionId, String queryableName, RequestOptions requestOptions)
        @Generated public Mono<Void> deleteRenderOption(String collectionId, String renderOptionId)
        @Generated public Mono<Response<Void>> deleteRenderOptionWithResponse(String collectionId, String renderOptionId, RequestOptions requestOptions)
        @Generated public Mono<StacItem> getItem(String collectionId, String itemId)
        @Generated public Mono<StacItem> getItem(String collectionId, String itemId, StacAssetUrlSigningMode sign, Integer durationInMinutes)
        @Generated public Mono<StacItemCollection> getItemCollection(String collectionId)
        @Generated public Mono<StacItemCollection> getItemCollection(String collectionId, Integer limit, List<String> boundingBox, String datetime, StacAssetUrlSigningMode sign, Integer durationInMinutes, String token)
        @Generated public Mono<Response<BinaryData>> getItemCollectionWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getItemWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public Mono<StacLandingPage> getLandingPage()
        @Generated public Mono<Response<BinaryData>> getLandingPageWithResponse(RequestOptions requestOptions)
        @Generated public Mono<StacMosaic> getMosaic(String collectionId, String mosaicId)
        @Generated public Mono<List<StacMosaic>> getMosaics(String collectionId)
        @Generated public Mono<Response<BinaryData>> getMosaicsWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getMosaicWithResponse(String collectionId, String mosaicId, RequestOptions requestOptions)
        @Generated public Mono<PartitionType> getPartitionType(String collectionId)
        @Generated public Mono<Response<BinaryData>> getPartitionTypeWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<QueryableDefinitionsResponse> getQueryables()
        @Generated public Mono<Response<BinaryData>> getQueryablesWithResponse(RequestOptions requestOptions)
        @Generated public Mono<RenderOption> getRenderOption(String collectionId, String renderOptionId)
        @Generated public Mono<List<RenderOption>> getRenderOptions(String collectionId)
        @Generated public Mono<Response<BinaryData>> getRenderOptionsWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getRenderOptionWithResponse(String collectionId, String renderOptionId, RequestOptions requestOptions)
        @Generated public Mono<StacCollection> replaceCollection(String collectionId, StacCollection body)
        @Generated public Mono<StacCollection> replaceCollectionAsset(String collectionId, String assetId, StacAssetData body)
        @Generated public Mono<Response<BinaryData>> replaceCollectionWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<StacMosaic> replaceMosaic(String collectionId, String mosaicId, StacMosaic body)
        @Generated public Mono<Response<BinaryData>> replaceMosaicWithResponse(String collectionId, String mosaicId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<Void> replacePartitionType(String collectionId, PartitionType body)
        @Generated public Mono<Response<Void>> replacePartitionTypeWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<StacQueryable> replaceQueryable(String collectionId, String queryableName, StacQueryable body)
        @Generated public Mono<Response<BinaryData>> replaceQueryableWithResponse(String collectionId, String queryableName, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<RenderOption> replaceRenderOption(String collectionId, String renderOptionId, RenderOption body)
        @Generated public Mono<Response<BinaryData>> replaceRenderOptionWithResponse(String collectionId, String renderOptionId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<TileSettings> replaceTileSettings(String collectionId, TileSettings body)
        @Generated public Mono<Response<BinaryData>> replaceTileSettingsWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<StacItemCollection> search(StacSearchParameters body)
        @Generated public Mono<StacItemCollection> search(StacSearchParameters body, StacAssetUrlSigningMode sign, Integer durationInMinutes)
        @Generated public Mono<Response<BinaryData>> searchWithResponse(BinaryData body, RequestOptions requestOptions)
        @Generated public Mono<TileSettings> getTileSettings(String collectionId)
        @Generated public Mono<Response<BinaryData>> getTileSettingsWithResponse(String collectionId, RequestOptions requestOptions)
    }
    @ServiceClient(builder = PlanetaryComputerProClientBuilder)
    public final class StacClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        @Generated public StacMosaic addMosaic(String collectionId, StacMosaic body)
        @Generated public Response<BinaryData> addMosaicWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public SyncPoller<Operation, Void> beginCreateCollection(StacCollection body)
        @Generated public SyncPoller<BinaryData, BinaryData> beginCreateCollection(BinaryData body, RequestOptions requestOptions)
        @Generated public SyncPoller<Operation, Void> beginCreateItem(String collectionId, StacItemOrStacItemCollection body)
        @Generated public SyncPoller<BinaryData, BinaryData> beginCreateItem(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public SyncPoller<Operation, Void> beginDeleteCollection(String collectionId)
        @Generated public SyncPoller<BinaryData, Void> beginDeleteCollection(String collectionId, RequestOptions requestOptions)
        @Generated public SyncPoller<Operation, Void> beginDeleteItem(String collectionId, String itemId)
        @Generated public SyncPoller<BinaryData, Void> beginDeleteItem(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public SyncPoller<Operation, Void> beginReplaceItem(String collectionId, String itemId, StacItem body)
        @Generated public SyncPoller<BinaryData, BinaryData> beginReplaceItem(String collectionId, String itemId, BinaryData body, RequestOptions requestOptions)
        @Generated public SyncPoller<BinaryData, BinaryData> beginUpdateItem(String collectionId, String itemId, BinaryData body, RequestOptions requestOptions)
        @Generated public StacCollection getCollection(String collectionId)
        @Generated public StacCollection getCollection(String collectionId, StacAssetUrlSigningMode sign, Integer durationInMinutes)
        @Generated public UserCollectionSettings getCollectionConfiguration(String collectionId)
        @Generated public Response<BinaryData> getCollectionConfigurationWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public QueryableDefinitionsResponse getCollectionQueryables(String collectionId)
        @Generated public Response<BinaryData> getCollectionQueryablesWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public StacCatalogCollections getCollections()
        @Generated public StacCatalogCollections getCollections(StacAssetUrlSigningMode sign, Integer durationInMinutes)
        @Generated public Response<BinaryData> getCollectionsWithResponse(RequestOptions requestOptions)
        @Generated public BinaryData getCollectionThumbnail(String collectionId)
        @Generated public Response<BinaryData> getCollectionThumbnailWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getCollectionWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public StacConformanceClasses getConformanceClasses()
        @Generated public Response<BinaryData> getConformanceClassesWithResponse(RequestOptions requestOptions)
        @Generated public StacCollection createCollectionAsset(String collectionId, StacAssetData body)
        @Generated public List<StacQueryable> createQueryables(String collectionId, List<StacQueryable> body)
        @Generated public Response<BinaryData> createQueryablesWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public RenderOption createRenderOption(String collectionId, RenderOption body)
        @Generated public Response<BinaryData> createRenderOptionWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public StacCollection deleteCollectionAsset(String collectionId, String assetId)
        @Generated public Response<BinaryData> deleteCollectionAssetWithResponse(String collectionId, String assetId, RequestOptions requestOptions)
        @Generated public void deleteMosaic(String collectionId, String mosaicId)
        @Generated public Response<Void> deleteMosaicWithResponse(String collectionId, String mosaicId, RequestOptions requestOptions)
        @Generated public void deleteQueryable(String collectionId, String queryableName)
        @Generated public Response<Void> deleteQueryableWithResponse(String collectionId, String queryableName, RequestOptions requestOptions)
        @Generated public void deleteRenderOption(String collectionId, String renderOptionId)
        @Generated public Response<Void> deleteRenderOptionWithResponse(String collectionId, String renderOptionId, RequestOptions requestOptions)
        @Generated public StacItem getItem(String collectionId, String itemId)
        @Generated public StacItem getItem(String collectionId, String itemId, StacAssetUrlSigningMode sign, Integer durationInMinutes)
        @Generated public StacItemCollection getItemCollection(String collectionId)
        @Generated public StacItemCollection getItemCollection(String collectionId, Integer limit, List<String> boundingBox, String datetime, StacAssetUrlSigningMode sign, Integer durationInMinutes, String token)
        @Generated public Response<BinaryData> getItemCollectionWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getItemWithResponse(String collectionId, String itemId, RequestOptions requestOptions)
        @Generated public StacLandingPage getLandingPage()
        @Generated public Response<BinaryData> getLandingPageWithResponse(RequestOptions requestOptions)
        @Generated public StacMosaic getMosaic(String collectionId, String mosaicId)
        @Generated public List<StacMosaic> getMosaics(String collectionId)
        @Generated public Response<BinaryData> getMosaicsWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getMosaicWithResponse(String collectionId, String mosaicId, RequestOptions requestOptions)
        @Generated public PartitionType getPartitionType(String collectionId)
        @Generated public Response<BinaryData> getPartitionTypeWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public QueryableDefinitionsResponse getQueryables()
        @Generated public Response<BinaryData> getQueryablesWithResponse(RequestOptions requestOptions)
        @Generated public RenderOption getRenderOption(String collectionId, String renderOptionId)
        @Generated public List<RenderOption> getRenderOptions(String collectionId)
        @Generated public Response<BinaryData> getRenderOptionsWithResponse(String collectionId, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getRenderOptionWithResponse(String collectionId, String renderOptionId, RequestOptions requestOptions)
        @Generated public StacCollection replaceCollection(String collectionId, StacCollection body)
        @Generated public StacCollection replaceCollectionAsset(String collectionId, String assetId, StacAssetData body)
        @Generated public Response<BinaryData> replaceCollectionWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public StacMosaic replaceMosaic(String collectionId, String mosaicId, StacMosaic body)
        @Generated public Response<BinaryData> replaceMosaicWithResponse(String collectionId, String mosaicId, BinaryData body, RequestOptions requestOptions)
        @Generated public void replacePartitionType(String collectionId, PartitionType body)
        @Generated public Response<Void> replacePartitionTypeWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public StacQueryable replaceQueryable(String collectionId, String queryableName, StacQueryable body)
        @Generated public Response<BinaryData> replaceQueryableWithResponse(String collectionId, String queryableName, BinaryData body, RequestOptions requestOptions)
        @Generated public RenderOption replaceRenderOption(String collectionId, String renderOptionId, RenderOption body)
        @Generated public Response<BinaryData> replaceRenderOptionWithResponse(String collectionId, String renderOptionId, BinaryData body, RequestOptions requestOptions)
        @Generated public TileSettings replaceTileSettings(String collectionId, TileSettings body)
        @Generated public Response<BinaryData> replaceTileSettingsWithResponse(String collectionId, BinaryData body, RequestOptions requestOptions)
        @Generated public StacItemCollection search(StacSearchParameters body)
        @Generated public StacItemCollection search(StacSearchParameters body, StacAssetUrlSigningMode sign, Integer durationInMinutes)
        @Generated public Response<BinaryData> searchWithResponse(BinaryData body, RequestOptions requestOptions)
        @Generated public TileSettings getTileSettings(String collectionId)
        @Generated public Response<BinaryData> getTileSettingsWithResponse(String collectionId, RequestOptions requestOptions)
    }
}
package com.azure.analytics.planetarycomputer.models {
    @Immutable
    public final class AssetMetadata implements JsonSerializable<AssetMetadata> {
        @Generated public AssetMetadata(String key, String type, List<String> roles, String title, String description)
        @Generated public String getDescription()
        @Generated public String getKey()
        @Generated public List<String> getRoles()
        @Generated public String getTitle()
        @Generated public String getType()
    }
    @Immutable
    public final class AssetStatisticsResponse implements JsonSerializable<AssetStatisticsResponse> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Map<String, BandStatisticsMap> getAdditionalProperties()
    }
    @Immutable
    public final class BandStatistics implements JsonSerializable<BandStatistics> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public double getCount()
        @Generated public List<List<Double>> getHistogram()
        @Generated public double getMajority()
        @Generated public double getMaskedPixels()
        @Generated public double getMaximum()
        @Generated public double getMean()
        @Generated public double getMedian()
        @Generated public double getMinimum()
        @Generated public double getMinority()
        @Generated public double getPercentile2()
        @Generated public double getPercentile98()
        @Generated public double getStd()
        @Generated public double getSum()
        @Generated public double getUnique()
        @Generated public double getValidPercent()
        @Generated public double getValidPixels()
    }
    @Immutable
    public final class BandStatisticsMap implements JsonSerializable<BandStatisticsMap> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Map<String, BandStatistics> getAdditionalProperties()
    }
    @Immutable
    public final class ClassMapLegendResponse implements JsonSerializable<ClassMapLegendResponse> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Map<String, BinaryData> getAdditionalProperties()
    }
    public final class ColorMapNames extends ExpandableStringEnum<ColorMapNames> {
        @Generated public static final ColorMapNames ACCENT = fromString("accent");
        @Generated public static final ColorMapNames ACCENT_R = fromString("accent_r");
        @Generated public static final ColorMapNames AFMHOT = fromString("afmhot");
        @Generated public static final ColorMapNames AFMHOT_R = fromString("afmhot_r");
        @Generated public static final ColorMapNames AI4G_LULC = fromString("ai4g-lulc");
        @Generated public static final ColorMapNames ALOS_FNF = fromString("alos-fnf");
        @Generated public static final ColorMapNames ALOS_PALSAR_MASK = fromString("alos-palsar-mask");
        @Generated public static final ColorMapNames AUTUMN = fromString("autumn");
        @Generated public static final ColorMapNames AUTUMN_R = fromString("autumn_r");
        @Generated public static final ColorMapNames BINARY = fromString("binary");
        @Generated public static final ColorMapNames BINARY_R = fromString("binary_r");
        @Generated public static final ColorMapNames BLUES = fromString("blues");
        @Generated public static final ColorMapNames BLUES_R = fromString("blues_r");
        @Generated public static final ColorMapNames BONE = fromString("bone");
        @Generated public static final ColorMapNames BONE_R = fromString("bone_r");
        @Generated public static final ColorMapNames BRBG = fromString("brbg");
        @Generated public static final ColorMapNames BRBG_R = fromString("brbg_r");
        @Generated public static final ColorMapNames BRG = fromString("brg");
        @Generated public static final ColorMapNames BRG_R = fromString("brg_r");
        @Generated public static final ColorMapNames BUGN = fromString("bugn");
        @Generated public static final ColorMapNames BUGN_R = fromString("bugn_r");
        @Generated public static final ColorMapNames BUPU = fromString("bupu");
        @Generated public static final ColorMapNames BUPU_R = fromString("bupu_r");
        @Generated public static final ColorMapNames BWR = fromString("bwr");
        @Generated public static final ColorMapNames BWR_R = fromString("bwr_r");
        @Generated public static final ColorMapNames C_CAP = fromString("c-cap");
        @Generated public static final ColorMapNames CFASTIE = fromString("cfastie");
        @Generated public static final ColorMapNames CHESAPEAKE_LC_13 = fromString("chesapeake-lc-13");
        @Generated public static final ColorMapNames CHESAPEAKE_LC_7 = fromString("chesapeake-lc-7");
        @Generated public static final ColorMapNames CHESAPEAKE_LU = fromString("chesapeake-lu");
        @Generated public static final ColorMapNames CHLORIS_BIOMASS = fromString("chloris-biomass");
        @Generated public static final ColorMapNames CIVIDIS = fromString("cividis");
        @Generated public static final ColorMapNames CIVIDIS_R = fromString("cividis_r");
        @Generated public static final ColorMapNames CMRMAP = fromString("cmrmap");
        @Generated public static final ColorMapNames CMRMAP_R = fromString("cmrmap_r");
        @Generated public static final ColorMapNames COOL = fromString("cool");
        @Generated public static final ColorMapNames COOL_R = fromString("cool_r");
        @Generated public static final ColorMapNames COOLWARM = fromString("coolwarm");
        @Generated public static final ColorMapNames COOLWARM_R = fromString("coolwarm_r");
        @Generated public static final ColorMapNames COPPER = fromString("copper");
        @Generated public static final ColorMapNames COPPER_R = fromString("copper_r");
        @Generated public static final ColorMapNames CUBEHELIX = fromString("cubehelix");
        @Generated public static final ColorMapNames CUBEHELIX_R = fromString("cubehelix_r");
        @Generated public static final ColorMapNames DARK2 = fromString("dark2");
        @Generated public static final ColorMapNames DARK2_R = fromString("dark2_r");
        @Generated public static final ColorMapNames DRCOG_LULC = fromString("drcog-lulc");
        @Generated public static final ColorMapNames ESA_CCI_LC = fromString("esa-cci-lc");
        @Generated public static final ColorMapNames ESA_WORLDCOVER = fromString("esa-worldcover");
        @Generated public static final ColorMapNames FLAG = fromString("flag");
        @Generated public static final ColorMapNames FLAG_R = fromString("flag_r");
        @Generated public static final ColorMapNames GAP_LULC = fromString("gap-lulc");
        @Generated public static final ColorMapNames GIST_EARTH = fromString("gist_earth");
        @Generated public static final ColorMapNames GIST_EARTH_R = fromString("gist_earth_r");
        @Generated public static final ColorMapNames GIST_GRAY = fromString("gist_gray");
        @Generated public static final ColorMapNames GIST_GRAY_R = fromString("gist_gray_r");
        @Generated public static final ColorMapNames GIST_HEAT = fromString("gist_heat");
        @Generated public static final ColorMapNames GIST_HEAT_R = fromString("gist_heat_r");
        @Generated public static final ColorMapNames GIST_NCAR = fromString("gist_ncar");
        @Generated public static final ColorMapNames GIST_NCAR_R = fromString("gist_ncar_r");
        @Generated public static final ColorMapNames GIST_RAINBOW = fromString("gist_rainbow");
        @Generated public static final ColorMapNames GIST_RAINBOW_R = fromString("gist_rainbow_r");
        @Generated public static final ColorMapNames GIST_STERN = fromString("gist_stern");
        @Generated public static final ColorMapNames GIST_STERN_R = fromString("gist_stern_r");
        @Generated public static final ColorMapNames GIST_YARG = fromString("gist_yarg");
        @Generated public static final ColorMapNames GIST_YARG_R = fromString("gist_yarg_r");
        @Generated public static final ColorMapNames GNBU = fromString("gnbu");
        @Generated public static final ColorMapNames GNBU_R = fromString("gnbu_r");
        @Generated public static final ColorMapNames GNUPLOT = fromString("gnuplot");
        @Generated public static final ColorMapNames GNUPLOT2 = fromString("gnuplot2");
        @Generated public static final ColorMapNames GNUPLOT2_R = fromString("gnuplot2_r");
        @Generated public static final ColorMapNames GNUPLOT_R = fromString("gnuplot_r");
        @Generated public static final ColorMapNames GRAY = fromString("gray");
        @Generated public static final ColorMapNames GRAY_R = fromString("gray_r");
        @Generated public static final ColorMapNames GREENS = fromString("greens");
        @Generated public static final ColorMapNames GREENS_R = fromString("greens_r");
        @Generated public static final ColorMapNames GREYS = fromString("greys");
        @Generated public static final ColorMapNames GREYS_R = fromString("greys_r");
        @Generated public static final ColorMapNames HOT = fromString("hot");
        @Generated public static final ColorMapNames HOT_R = fromString("hot_r");
        @Generated public static final ColorMapNames HSV = fromString("hsv");
        @Generated public static final ColorMapNames HSV_R = fromString("hsv_r");
        @Generated public static final ColorMapNames INFERNO = fromString("inferno");
        @Generated public static final ColorMapNames INFERNO_R = fromString("inferno_r");
        @Generated public static final ColorMapNames IO_BII = fromString("io-bii");
        @Generated public static final ColorMapNames IO_LULC = fromString("io-lulc");
        @Generated public static final ColorMapNames IO_LULC_9_CLASS = fromString("io-lulc-9-class");
        @Generated public static final ColorMapNames JET = fromString("jet");
        @Generated public static final ColorMapNames JET_R = fromString("jet_r");
        @Generated public static final ColorMapNames JRC_CHANGE = fromString("jrc-change");
        @Generated public static final ColorMapNames JRC_EXTENT = fromString("jrc-extent");
        @Generated public static final ColorMapNames JRC_OCCURRENCE = fromString("jrc-occurrence");
        @Generated public static final ColorMapNames JRC_RECURRENCE = fromString("jrc-recurrence");
        @Generated public static final ColorMapNames JRC_SEASONALITY = fromString("jrc-seasonality");
        @Generated public static final ColorMapNames JRC_TRANSITIONS = fromString("jrc-transitions");
        @Generated public static final ColorMapNames LIDAR_CLASSIFICATION = fromString("lidar-classification");
        @Generated public static final ColorMapNames LIDAR_HAG = fromString("lidar-hag");
        @Generated public static final ColorMapNames LIDAR_HAG_ALTERNATIVE = fromString("lidar-hag-alternative");
        @Generated public static final ColorMapNames LIDAR_INTENSITY = fromString("lidar-intensity");
        @Generated public static final ColorMapNames LIDAR_RETURNS = fromString("lidar-returns");
        @Generated public static final ColorMapNames MAGMA = fromString("magma");
        @Generated public static final ColorMapNames MAGMA_R = fromString("magma_r");
        @Generated public static final ColorMapNames MODIS_10A1 = fromString("modis-10A1");
        @Generated public static final ColorMapNames MODIS_10A2 = fromString("modis-10A2");
        @Generated public static final ColorMapNames MODIS_13A1_Q1 = fromString("modis-13A1|Q1");
        @Generated public static final ColorMapNames MODIS_14A1_A2 = fromString("modis-14A1|A2");
        @Generated public static final ColorMapNames MODIS_15A2H_A3H = fromString("modis-15A2H|A3H");
        @Generated public static final ColorMapNames MODIS_16A3GF_ET = fromString("modis-16A3GF-ET");
        @Generated public static final ColorMapNames MODIS_16A3GF_PET = fromString("modis-16A3GF-PET");
        @Generated public static final ColorMapNames MODIS_17A2H_A2HGF = fromString("modis-17A2H|A2HGF");
        @Generated public static final ColorMapNames MODIS_17A3HGF = fromString("modis-17A3HGF");
        @Generated public static final ColorMapNames MODIS_64A1 = fromString("modis-64A1");
        @Generated public static final ColorMapNames MTBS_SEVERITY = fromString("mtbs-severity");
        @Generated public static final ColorMapNames NIPY_SPECTRAL = fromString("nipy_spectral");
        @Generated public static final ColorMapNames NIPY_SPECTRAL_R = fromString("nipy_spectral_r");
        @Generated public static final ColorMapNames NRCAN_LULC = fromString("nrcan-lulc");
        @Generated public static final ColorMapNames OCEAN = fromString("ocean");
        @Generated public static final ColorMapNames OCEAN_R = fromString("ocean_r");
        @Generated public static final ColorMapNames ORANGES = fromString("oranges");
        @Generated public static final ColorMapNames ORANGES_R = fromString("oranges_r");
        @Generated public static final ColorMapNames ORRD = fromString("orrd");
        @Generated public static final ColorMapNames ORRD_R = fromString("orrd_r");
        @Generated public static final ColorMapNames PAIRED = fromString("paired");
        @Generated public static final ColorMapNames PAIRED_R = fromString("paired_r");
        @Generated public static final ColorMapNames PASTEL1 = fromString("pastel1");
        @Generated public static final ColorMapNames PASTEL1_R = fromString("pastel1_r");
        @Generated public static final ColorMapNames PASTEL2 = fromString("pastel2");
        @Generated public static final ColorMapNames PASTEL2_R = fromString("pastel2_r");
        @Generated public static final ColorMapNames PINK = fromString("pink");
        @Generated public static final ColorMapNames PINK_R = fromString("pink_r");
        @Generated public static final ColorMapNames PIYG = fromString("piyg");
        @Generated public static final ColorMapNames PIYG_R = fromString("piyg_r");
        @Generated public static final ColorMapNames PLASMA = fromString("plasma");
        @Generated public static final ColorMapNames PLASMA_R = fromString("plasma_r");
        @Generated public static final ColorMapNames PRGN = fromString("prgn");
        @Generated public static final ColorMapNames PRGN_R = fromString("prgn_r");
        @Generated public static final ColorMapNames PRISM = fromString("prism");
        @Generated public static final ColorMapNames PRISM_R = fromString("prism_r");
        @Generated public static final ColorMapNames PUBU = fromString("pubu");
        @Generated public static final ColorMapNames PUBU_R = fromString("pubu_r");
        @Generated public static final ColorMapNames PUBUGN = fromString("pubugn");
        @Generated public static final ColorMapNames PUBUGN_R = fromString("pubugn_r");
        @Generated public static final ColorMapNames PUOR = fromString("puor");
        @Generated public static final ColorMapNames PUOR_R = fromString("puor_r");
        @Generated public static final ColorMapNames PURD = fromString("purd");
        @Generated public static final ColorMapNames PURD_R = fromString("purd_r");
        @Generated public static final ColorMapNames PURPLES = fromString("purples");
        @Generated public static final ColorMapNames PURPLES_R = fromString("purples_r");
        @Generated public static final ColorMapNames QPE = fromString("qpe");
        @Generated public static final ColorMapNames RAINBOW = fromString("rainbow");
        @Generated public static final ColorMapNames RAINBOW_R = fromString("rainbow_r");
        @Generated public static final ColorMapNames RDBU = fromString("rdbu");
        @Generated public static final ColorMapNames RDBU_R = fromString("rdbu_r");
        @Generated public static final ColorMapNames RDGY = fromString("rdgy");
        @Generated public static final ColorMapNames RDGY_R = fromString("rdgy_r");
        @Generated public static final ColorMapNames RDPU = fromString("rdpu");
        @Generated public static final ColorMapNames RDPU_R = fromString("rdpu_r");
        @Generated public static final ColorMapNames RDYLBU = fromString("rdylbu");
        @Generated public static final ColorMapNames RDYLBU_R = fromString("rdylbu_r");
        @Generated public static final ColorMapNames RDYLGN = fromString("rdylgn");
        @Generated public static final ColorMapNames RDYLGN_R = fromString("rdylgn_r");
        @Generated public static final ColorMapNames REDS = fromString("reds");
        @Generated public static final ColorMapNames REDS_R = fromString("reds_r");
        @Generated public static final ColorMapNames RPLUMBO = fromString("rplumbo");
        @Generated public static final ColorMapNames SCHWARZWALD = fromString("schwarzwald");
        @Generated public static final ColorMapNames SEISMIC = fromString("seismic");
        @Generated public static final ColorMapNames SEISMIC_R = fromString("seismic_r");
        @Generated public static final ColorMapNames SET1 = fromString("set1");
        @Generated public static final ColorMapNames SET1_R = fromString("set1_r");
        @Generated public static final ColorMapNames SET2 = fromString("set2");
        @Generated public static final ColorMapNames SET2_R = fromString("set2_r");
        @Generated public static final ColorMapNames SET3 = fromString("set3");
        @Generated public static final ColorMapNames SET3_R = fromString("set3_r");
        @Generated public static final ColorMapNames SPECTRAL = fromString("spectral");
        @Generated public static final ColorMapNames SPECTRAL_R = fromString("spectral_r");
        @Generated public static final ColorMapNames SPRING = fromString("spring");
        @Generated public static final ColorMapNames SPRING_R = fromString("spring_r");
        @Generated public static final ColorMapNames SUMMER = fromString("summer");
        @Generated public static final ColorMapNames SUMMER_R = fromString("summer_r");
        @Generated public static final ColorMapNames TAB10 = fromString("tab10");
        @Generated public static final ColorMapNames TAB10_R = fromString("tab10_r");
        @Generated public static final ColorMapNames TAB20 = fromString("tab20");
        @Generated public static final ColorMapNames TAB20_R = fromString("tab20_r");
        @Generated public static final ColorMapNames TAB20B = fromString("tab20b");
        @Generated public static final ColorMapNames TAB20B_R = fromString("tab20b_r");
        @Generated public static final ColorMapNames TAB20C = fromString("tab20c");
        @Generated public static final ColorMapNames TAB20C_R = fromString("tab20c_r");
        @Generated public static final ColorMapNames TERRAIN = fromString("terrain");
        @Generated public static final ColorMapNames TERRAIN_R = fromString("terrain_r");
        @Generated public static final ColorMapNames TWILIGHT = fromString("twilight");
        @Generated public static final ColorMapNames TWILIGHT_R = fromString("twilight_r");
        @Generated public static final ColorMapNames TWILIGHT_SHIFTED = fromString("twilight_shifted");
        @Generated public static final ColorMapNames TWILIGHT_SHIFTED_R = fromString("twilight_shifted_r");
        @Generated public static final ColorMapNames USDA_CDL = fromString("usda-cdl");
        @Generated public static final ColorMapNames USDA_CDL_CORN = fromString("usda-cdl-corn");
        @Generated public static final ColorMapNames USDA_CDL_COTTON = fromString("usda-cdl-cotton");
        @Generated public static final ColorMapNames USDA_CDL_SOYBEANS = fromString("usda-cdl-soybeans");
        @Generated public static final ColorMapNames USDA_CDL_WHEAT = fromString("usda-cdl-wheat");
        @Generated public static final ColorMapNames USGS_LCMAP = fromString("usgs-lcmap");
        @Generated public static final ColorMapNames VIIRS_10A1 = fromString("viirs-10a1");
        @Generated public static final ColorMapNames VIIRS_13A1 = fromString("viirs-13a1");
        @Generated public static final ColorMapNames VIIRS_14A1 = fromString("viirs-14a1");
        @Generated public static final ColorMapNames VIIRS_15A2H = fromString("viirs-15a2H");
        @Generated public static final ColorMapNames VIRIDIS = fromString("viridis");
        @Generated public static final ColorMapNames VIRIDIS_R = fromString("viridis_r");
        @Generated public static final ColorMapNames WINTER = fromString("winter");
        @Generated public static final ColorMapNames WINTER_R = fromString("winter_r");
        @Generated public static final ColorMapNames WISTIA = fromString("wistia");
        @Generated public static final ColorMapNames WISTIA_R = fromString("wistia_r");
        @Generated public static final ColorMapNames YLGN = fromString("ylgn");
        @Generated public static final ColorMapNames YLGN_R = fromString("ylgn_r");
        @Generated public static final ColorMapNames YLGNBU = fromString("ylgnbu");
        @Generated public static final ColorMapNames YLGNBU_R = fromString("ylgnbu_r");
        @Generated public static final ColorMapNames YLORBR = fromString("ylorbr");
        @Generated public static final ColorMapNames YLORBR_R = fromString("ylorbr_r");
        @Generated public static final ColorMapNames YLORRD = fromString("ylorrd");
        @Generated public static final ColorMapNames YLORRD_R = fromString("ylorrd_r");
        @Generated public static final ColorMapNames ALGAE = fromString("algae");
        @Generated public static final ColorMapNames ALGAE_R = fromString("algae_r");
        @Generated public static final ColorMapNames AMP = fromString("amp");
        @Generated public static final ColorMapNames AMP_R = fromString("amp_r");
        @Generated public static final ColorMapNames BALANCE = fromString("balance");
        @Generated public static final ColorMapNames BALANCE_R = fromString("balance_r");
        @Generated public static final ColorMapNames CURL = fromString("curl");
        @Generated public static final ColorMapNames CURL_R = fromString("curl_r");
        @Generated public static final ColorMapNames DEEP = fromString("deep");
        @Generated public static final ColorMapNames DEEP_R = fromString("deep_r");
        @Generated public static final ColorMapNames DELTA = fromString("delta");
        @Generated public static final ColorMapNames DELTA_R = fromString("delta_r");
        @Generated public static final ColorMapNames DENSE = fromString("dense");
        @Generated public static final ColorMapNames DENSE_R = fromString("dense_r");
        @Generated public static final ColorMapNames DIFF = fromString("diff");
        @Generated public static final ColorMapNames DIFF_R = fromString("diff_r");
        @Generated public static final ColorMapNames HALINE = fromString("haline");
        @Generated public static final ColorMapNames HALINE_R = fromString("haline_r");
        @Generated public static final ColorMapNames ICE = fromString("ice");
        @Generated public static final ColorMapNames ICE_R = fromString("ice_r");
        @Generated public static final ColorMapNames MATTER = fromString("matter");
        @Generated public static final ColorMapNames MATTER_R = fromString("matter_r");
        @Generated public static final ColorMapNames OXY = fromString("oxy");
        @Generated public static final ColorMapNames OXY_R = fromString("oxy_r");
        @Generated public static final ColorMapNames PHASE = fromString("phase");
        @Generated public static final ColorMapNames PHASE_R = fromString("phase_r");
        @Generated public static final ColorMapNames RAIN = fromString("rain");
        @Generated public static final ColorMapNames RAIN_R = fromString("rain_r");
        @Generated public static final ColorMapNames SOLAR = fromString("solar");
        @Generated public static final ColorMapNames SOLAR_R = fromString("solar_r");
        @Generated public static final ColorMapNames SPEED = fromString("speed");
        @Generated public static final ColorMapNames SPEED_R = fromString("speed_r");
        @Generated public static final ColorMapNames TARN = fromString("tarn");
        @Generated public static final ColorMapNames TARN_R = fromString("tarn_r");
        @Generated public static final ColorMapNames TEMPO = fromString("tempo");
        @Generated public static final ColorMapNames TEMPO_R = fromString("tempo_r");
        @Generated public static final ColorMapNames THERMAL = fromString("thermal");
        @Generated public static final ColorMapNames THERMAL_R = fromString("thermal_r");
        @Generated public static final ColorMapNames TOPO = fromString("topo");
        @Generated public static final ColorMapNames TOPO_R = fromString("topo_r");
        @Generated public static final ColorMapNames TURBID = fromString("turbid");
        @Generated public static final ColorMapNames TURBID_R = fromString("turbid_r");
        @Generated public static final ColorMapNames TURBO = fromString("turbo");
        @Generated public static final ColorMapNames TURBO_R = fromString("turbo_r");
        @Deprecated @Generated public ColorMapNames()
        @Generated public static ColorMapNames fromString(String name)
        @Generated public static Collection<ColorMapNames> values()
    }
    @Immutable
    public final class DefaultLocation implements JsonSerializable<DefaultLocation> {
        @Generated public DefaultLocation(int zoom, List<Double> coordinates)
        @Generated public List<Double> getCoordinates()
        @Generated public int getZoom()
    }
    @Immutable
    public final class ErrorInfo implements JsonSerializable<ErrorInfo> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public ResponseError getError()
    }
    public final class FeatureType extends ExpandableStringEnum<FeatureType> {
        @Generated public static final FeatureType FEATURE = fromString("Feature");
        @Deprecated @Generated public FeatureType()
        @Generated public static FeatureType fromString(String name)
        @Generated public static Collection<FeatureType> values()
    }
    @Fluent
    public final class FileDetails {
        @Generated public FileDetails(BinaryData content)
        @Generated public BinaryData getContent()
        @Generated public String getContentType()
        @Generated public FileDetails setContentType(String contentType)
        @Generated public String getFilename()
        @Generated public FileDetails setFilename(String filename)
    }
    public final class FilterLanguage extends ExpandableStringEnum<FilterLanguage> {
        @Generated public static final FilterLanguage CQL_JSON = fromString("cql-json");
        @Generated public static final FilterLanguage CQL2_JSON = fromString("cql2-json");
        @Generated public static final FilterLanguage CQL2_TEXT = fromString("cql2-text");
        @Deprecated @Generated public FilterLanguage()
        @Generated public static FilterLanguage fromString(String name)
        @Generated public static Collection<FilterLanguage> values()
    }
    @Fluent
    public final class GeoJsonFeature implements JsonSerializable<GeoJsonFeature> {
        @Generated public GeoJsonFeature(GeoJsonGeometry geometry, FeatureType type)
        @Generated public GeoJsonGeometry getGeometry()
        @Generated public Map<String, BinaryData> getProperties()
        @Generated public GeoJsonFeature setProperties(Map<String, BinaryData> properties)
        @Generated public FeatureType getType()
    }
    @Fluent
    public class GeoJsonGeometry implements JsonSerializable<GeoJsonGeometry> {
        @Generated public GeoJsonGeometry()
        @Generated public List<Double> getBoundingBox()
        @Generated public GeoJsonGeometry setBoundingBox(List<Double> boundingBox)
        @Generated public GeometryType getType()
    }
    @Fluent
    public final class GeoJsonLineString extends GeoJsonGeometry {
        @Generated public GeoJsonLineString()
        @Generated @Override public GeoJsonLineString setBoundingBox(List<Double> boundingBox)
        @Generated public List<List<Double>> getCoordinates()
        @Generated public GeoJsonLineString setCoordinates(List<List<Double>> coordinates)
        @Generated public static GeoJsonLineString fromJson(JsonReader jsonReader) throws IOException
        @Generated @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Generated @Override public GeometryType getType()
    }
    @Fluent
    public final class GeoJsonMultiLineString extends GeoJsonGeometry {
        @Generated public GeoJsonMultiLineString()
        @Generated @Override public GeoJsonMultiLineString setBoundingBox(List<Double> boundingBox)
        @Generated public List<List<List<Double>>> getCoordinates()
        @Generated public GeoJsonMultiLineString setCoordinates(List<List<List<Double>>> coordinates)
        @Generated public static GeoJsonMultiLineString fromJson(JsonReader jsonReader) throws IOException
        @Generated @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Generated @Override public GeometryType getType()
    }
    @Fluent
    public final class GeoJsonMultiPoint extends GeoJsonGeometry {
        @Generated public GeoJsonMultiPoint()
        @Generated @Override public GeoJsonMultiPoint setBoundingBox(List<Double> boundingBox)
        @Generated public List<List<Double>> getCoordinates()
        @Generated public GeoJsonMultiPoint setCoordinates(List<List<Double>> coordinates)
        @Generated public static GeoJsonMultiPoint fromJson(JsonReader jsonReader) throws IOException
        @Generated @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Generated @Override public GeometryType getType()
    }
    @Fluent
    public final class GeoJsonMultiPolygon extends GeoJsonGeometry {
        @Generated public GeoJsonMultiPolygon()
        @Generated @Override public GeoJsonMultiPolygon setBoundingBox(List<Double> boundingBox)
        @Generated public List<List<List<List<Double>>>> getCoordinates()
        @Generated public GeoJsonMultiPolygon setCoordinates(List<List<List<List<Double>>>> coordinates)
        @Generated public static GeoJsonMultiPolygon fromJson(JsonReader jsonReader) throws IOException
        @Generated @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Generated @Override public GeometryType getType()
    }
    @Fluent
    public final class GeoJsonPoint extends GeoJsonGeometry {
        @Generated public GeoJsonPoint()
        @Generated @Override public GeoJsonPoint setBoundingBox(List<Double> boundingBox)
        @Generated public List<Double> getCoordinates()
        @Generated public GeoJsonPoint setCoordinates(List<Double> coordinates)
        @Generated public static GeoJsonPoint fromJson(JsonReader jsonReader) throws IOException
        @Generated @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Generated @Override public GeometryType getType()
    }
    @Fluent
    public final class GeoJsonPolygon extends GeoJsonGeometry {
        @Generated public GeoJsonPolygon()
        @Generated @Override public GeoJsonPolygon setBoundingBox(List<Double> boundingBox)
        @Generated public List<List<List<Double>>> getCoordinates()
        @Generated public GeoJsonPolygon setCoordinates(List<List<List<Double>>> coordinates)
        @Generated public static GeoJsonPolygon fromJson(JsonReader jsonReader) throws IOException
        @Generated @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Generated @Override public GeometryType getType()
    }
    public final class GeometryType extends ExpandableStringEnum<GeometryType> {
        @Generated public static final GeometryType POINT = fromString("Point");
        @Generated public static final GeometryType LINE_STRING = fromString("LineString");
        @Generated public static final GeometryType POLYGON = fromString("Polygon");
        @Generated public static final GeometryType MULTI_POINT = fromString("MultiPoint");
        @Generated public static final GeometryType MULTI_LINE_STRING = fromString("MultiLineString");
        @Generated public static final GeometryType MULTI_POLYGON = fromString("MultiPolygon");
        @Deprecated @Generated public GeometryType()
        @Generated public static GeometryType fromString(String name)
        @Generated public static Collection<GeometryType> values()
    }
    @Fluent
    public final class IngestionDefinition implements JsonSerializable<IngestionDefinition> {
        @Generated public IngestionDefinition()
        @Generated public OffsetDateTime getCreationTime()
        @Generated public String getDisplayName()
        @Generated public IngestionDefinition setDisplayName(String displayName)
        @Generated public String getId()
        @Generated public IngestionType getImportType()
        @Generated public IngestionDefinition setImportType(IngestionType importType)
        @Generated public Boolean isKeepOriginalAssets()
        @Generated public IngestionDefinition setKeepOriginalAssets(Boolean keepOriginalAssets)
        @Generated public Boolean isSkipExistingItems()
        @Generated public IngestionDefinition setSkipExistingItems(Boolean skipExistingItems)
        @Generated public String getSourceCatalogUrl()
        @Generated public IngestionDefinition setSourceCatalogUrl(String sourceCatalogUrl)
        @Generated public String getStacGeoparquetUrl()
        @Generated public IngestionDefinition setStacGeoparquetUrl(String stacGeoparquetUrl)
        @Generated public IngestionStatus getStatus()
    }
    @Immutable
    public final class IngestionRun implements JsonSerializable<IngestionRun> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public OffsetDateTime getCreationTime()
        @Generated public String getId()
        @Generated public Boolean isKeepOriginalAssets()
        @Generated public IngestionRunOperation getOperation()
        @Generated public String getParentRunId()
        @Generated public Boolean isSkipExistingItems()
        @Generated public String getSourceCatalogUrl()
    }
    @Immutable
    public final class IngestionRunOperation implements JsonSerializable<IngestionRunOperation> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public OffsetDateTime getCreationTime()
        @Generated public OffsetDateTime getFinishTime()
        @Generated public String getId()
        @Generated public OffsetDateTime getStartTime()
        @Generated public OperationStatus getStatus()
        @Generated public List<OperationStatusHistoryItem> getStatusHistory()
        @Generated public int getTotalFailedItems()
        @Generated public int getTotalItems()
        @Generated public int getTotalPendingItems()
        @Generated public int getTotalSuccessfulItems()
    }
    @Immutable
    public class IngestionSource implements JsonSerializable<IngestionSource> {
        @Generated public IngestionSource(String id)
        @Generated public OffsetDateTime getCreated()
        @Generated public String getId()
        @Generated public IngestionSourceType getKind()
    }
    @Immutable
    public final class IngestionSourceSummary implements JsonSerializable<IngestionSourceSummary> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public OffsetDateTime getCreated()
        @Generated public String getId()
        @Generated public IngestionSourceType getKind()
    }
    public final class IngestionSourceType extends ExpandableStringEnum<IngestionSourceType> {
        @Generated public static final IngestionSourceType SHARED_ACCESS_SIGNATURE_TOKEN = fromString("SasToken");
        @Generated public static final IngestionSourceType BLOB_MANAGED_IDENTITY = fromString("BlobManagedIdentity");
        @Deprecated @Generated public IngestionSourceType()
        @Generated public static IngestionSourceType fromString(String name)
        @Generated public static Collection<IngestionSourceType> values()
    }
    public final class IngestionStatus extends ExpandableStringEnum<IngestionStatus> {
        @Generated public static final IngestionStatus READY = fromString("Ready");
        @Generated public static final IngestionStatus DELETING = fromString("Deleting");
        @Deprecated @Generated public IngestionStatus()
        @Generated public static IngestionStatus fromString(String name)
        @Generated public static Collection<IngestionStatus> values()
    }
    public final class IngestionType extends ExpandableStringEnum<IngestionType> {
        @Generated public static final IngestionType STATIC_CATALOG = fromString("StaticCatalog");
        @Generated public static final IngestionType STAC_GEOPARQUET = fromString("StacGeoparquet");
        @Deprecated @Generated public IngestionType()
        @Generated public static IngestionType fromString(String name)
        @Generated public static Collection<IngestionType> values()
    }
    public final class LegendConfigType extends ExpandableStringEnum<LegendConfigType> {
        @Generated public static final LegendConfigType CONTINUOUS = fromString("continuous");
        @Generated public static final LegendConfigType CLASSMAP = fromString("classmap");
        @Generated public static final LegendConfigType INTERVAL = fromString("interval");
        @Generated public static final LegendConfigType NONE = fromString("none");
        @Deprecated @Generated public LegendConfigType()
        @Generated public static LegendConfigType fromString(String name)
        @Generated public static Collection<LegendConfigType> values()
    }
    @Immutable
    public final class ManagedIdentityConnection implements JsonSerializable<ManagedIdentityConnection> {
        @Generated public ManagedIdentityConnection(String containerUri, String objectId)
        @Generated public String getContainerUri()
        @Generated public String getObjectId()
    }
    @Immutable
    public final class ManagedIdentityIngestionSource extends IngestionSource {
        @Generated public ManagedIdentityIngestionSource(String id, ManagedIdentityConnection connectionInfo)
        @Generated public ManagedIdentityConnection getConnectionInfo()
        @Generated public static ManagedIdentityIngestionSource fromJson(JsonReader jsonReader) throws IOException
        @Generated @Override public IngestionSourceType getKind()
        @Generated @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
    }
    @Immutable
    public final class ManagedIdentityMetadata implements JsonSerializable<ManagedIdentityMetadata> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getObjectId()
        @Generated public String getResourceId()
    }
    @Fluent
    public final class MosaicMetadata implements JsonSerializable<MosaicMetadata> {
        @Generated public MosaicMetadata()
        @Generated public List<String> getAssets()
        @Generated public MosaicMetadata setAssets(List<String> assets)
        @Generated public String getBounds()
        @Generated public MosaicMetadata setBounds(String bounds)
        @Generated public Map<String, String> getDefaults()
        @Generated public MosaicMetadata setDefaults(Map<String, String> defaults)
        @Generated public Integer getMaxZoom()
        @Generated public MosaicMetadata setMaxZoom(Integer maxZoom)
        @Generated public Integer getMinZoom()
        @Generated public MosaicMetadata setMinZoom(Integer minZoom)
        @Generated public String getName()
        @Generated public MosaicMetadata setName(String name)
        @Generated public MosaicMetadataType getType()
        @Generated public MosaicMetadata setType(MosaicMetadataType type)
    }
    public final class MosaicMetadataType extends ExpandableStringEnum<MosaicMetadataType> {
        @Generated public static final MosaicMetadataType MOSAIC = fromString("mosaic");
        @Generated public static final MosaicMetadataType SEARCH = fromString("search");
        @Deprecated @Generated public MosaicMetadataType()
        @Generated public static MosaicMetadataType fromString(String name)
        @Generated public static Collection<MosaicMetadataType> values()
    }
    public final class NoDataType extends ExpandableStringEnum<NoDataType> {
        @Generated public static final NoDataType ALPHA = fromString("Alpha");
        @Generated public static final NoDataType MASK = fromString("Mask");
        @Generated public static final NoDataType INTERNAL = fromString("Internal");
        @Generated public static final NoDataType NODATA = fromString("Nodata");
        @Generated public static final NoDataType NONE = fromString("None");
        @Deprecated @Generated public NoDataType()
        @Generated public static NoDataType fromString(String name)
        @Generated public static Collection<NoDataType> values()
    }
    @Immutable
    public final class Operation implements JsonSerializable<Operation> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Map<String, String> getAdditionalInformation()
        @Generated public String getCollectionId()
        @Generated public OffsetDateTime getCreationTime()
        @Generated public ErrorInfo getError()
        @Generated public OffsetDateTime getFinishTime()
        @Generated public String getId()
        @Generated public OffsetDateTime getStartTime()
        @Generated public OperationStatus getStatus()
        @Generated public List<OperationStatusHistoryItem> getStatusHistory()
        @Generated public String getType()
    }
    public final class OperationStatus extends ExpandableStringEnum<OperationStatus> {
        @Generated public static final OperationStatus PENDING = fromString("Pending");
        @Generated public static final OperationStatus RUNNING = fromString("Running");
        @Generated public static final OperationStatus SUCCEEDED = fromString("Succeeded");
        @Generated public static final OperationStatus CANCELED = fromString("Canceled");
        @Generated public static final OperationStatus CANCELING = fromString("Canceling");
        @Generated public static final OperationStatus FAILED = fromString("Failed");
        @Deprecated @Generated public OperationStatus()
        @Generated public static OperationStatus fromString(String name)
        @Generated public static Collection<OperationStatus> values()
    }
    @Immutable
    public final class OperationStatusHistoryItem implements JsonSerializable<OperationStatusHistoryItem> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getErrorCode()
        @Generated public String getErrorMessage()
        @Generated public OperationStatus getStatus()
        @Generated public OffsetDateTime getTimestamp()
    }
    @Fluent
    public final class PartitionType implements JsonSerializable<PartitionType> {
        @Generated public PartitionType()
        @Generated public PartitionTypeScheme getScheme()
        @Generated public PartitionType setScheme(PartitionTypeScheme scheme)
    }
    public final class PartitionTypeScheme extends ExpandableStringEnum<PartitionTypeScheme> {
        @Generated public static final PartitionTypeScheme YEAR = fromString("year");
        @Generated public static final PartitionTypeScheme MONTH = fromString("month");
        @Generated public static final PartitionTypeScheme NONE = fromString("none");
        @Deprecated @Generated public PartitionTypeScheme()
        @Generated public static PartitionTypeScheme fromString(String name)
        @Generated public static Collection<PartitionTypeScheme> values()
    }
    public final class PixelSelection extends ExpandableStringEnum<PixelSelection> {
        @Generated public static final PixelSelection FIRST = fromString("first");
        @Generated public static final PixelSelection HIGHEST = fromString("highest");
        @Generated public static final PixelSelection LOWEST = fromString("lowest");
        @Generated public static final PixelSelection MEAN = fromString("mean");
        @Generated public static final PixelSelection MEDIAN = fromString("median");
        @Generated public static final PixelSelection STANDARD_DEVIATION = fromString("stdev");
        @Generated public static final PixelSelection LAST_BAND_LOW = fromString("lastbandlow");
        @Generated public static final PixelSelection LAST_BAND_HIGH = fromString("lastbandhigh");
        @Generated public static final PixelSelection COUNT = fromString("count");
        @Deprecated @Generated public PixelSelection()
        @Generated public static PixelSelection fromString(String name)
        @Generated public static Collection<PixelSelection> values()
    }
    @Immutable
    public final class QueryableDefinitionsResponse implements JsonSerializable<QueryableDefinitionsResponse> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Map<String, BinaryData> getAdditionalProperties()
    }
    @Fluent
    public final class RegisterMosaicsSearchOptions {
        @Generated public RegisterMosaicsSearchOptions()
        @Generated public List<Double> getBoundingBox()
        @Generated public RegisterMosaicsSearchOptions setBoundingBox(List<Double> boundingBox)
        @Generated public List<String> getCollections()
        @Generated public RegisterMosaicsSearchOptions setCollections(List<String> collections)
        @Generated public String getDatetime()
        @Generated public RegisterMosaicsSearchOptions setDatetime(String datetime)
        @Generated public Map<String, BinaryData> getFilter()
        @Generated public RegisterMosaicsSearchOptions setFilter(Map<String, BinaryData> filter)
        @Generated public FilterLanguage getFilterLanguage()
        @Generated public RegisterMosaicsSearchOptions setFilterLanguage(FilterLanguage filterLanguage)
        @Generated public List<String> getIds()
        @Generated public RegisterMosaicsSearchOptions setIds(List<String> ids)
        @Generated public GeoJsonGeometry getIntersects()
        @Generated public RegisterMosaicsSearchOptions setIntersects(GeoJsonGeometry intersects)
        @Generated public MosaicMetadata getMetadata()
        @Generated public RegisterMosaicsSearchOptions setMetadata(MosaicMetadata metadata)
        @Generated public Map<String, BinaryData> getQuery()
        @Generated public RegisterMosaicsSearchOptions setQuery(Map<String, BinaryData> query)
        @Generated public List<StacSortExtension> getSortBy()
        @Generated public RegisterMosaicsSearchOptions setSortBy(List<StacSortExtension> sortBy)
    }
    @Fluent
    public final class RenderOption implements JsonSerializable<RenderOption> {
        @Generated public RenderOption(String id, String name)
        @Generated public List<RenderOptionCondition> getConditions()
        @Generated public RenderOption setConditions(List<RenderOptionCondition> conditions)
        @Generated public String getDescription()
        @Generated public RenderOption setDescription(String description)
        @Generated public String getId()
        @Generated public RenderOptionLegend getLegend()
        @Generated public RenderOption setLegend(RenderOptionLegend legend)
        @Generated public Integer getMinZoom()
        @Generated public RenderOption setMinZoom(Integer minZoom)
        @Generated public String getName()
        @Generated public String getOptions()
        @Generated public RenderOption setOptions(String options)
        @Generated public RenderOptionType getType()
        @Generated public RenderOption setType(RenderOptionType type)
        @Generated public RenderOptionVectorOptions getVectorOptions()
        @Generated public RenderOption setVectorOptions(RenderOptionVectorOptions vectorOptions)
    }
    @Fluent
    public final class RenderOptionCondition implements JsonSerializable<RenderOptionCondition> {
        @Generated public RenderOptionCondition(String property)
        @Generated public String getProperty()
        @Generated public String getValue()
        @Generated public RenderOptionCondition setValue(String value)
    }
    @Fluent
    public final class RenderOptionLegend implements JsonSerializable<RenderOptionLegend> {
        @Generated public RenderOptionLegend()
        @Generated public List<String> getLabels()
        @Generated public RenderOptionLegend setLabels(List<String> labels)
        @Generated public Double getScaleFactor()
        @Generated public RenderOptionLegend setScaleFactor(Double scaleFactor)
        @Generated public Integer getTrimEnd()
        @Generated public RenderOptionLegend setTrimEnd(Integer trimEnd)
        @Generated public Integer getTrimStart()
        @Generated public RenderOptionLegend setTrimStart(Integer trimStart)
        @Generated public LegendConfigType getType()
        @Generated public RenderOptionLegend setType(LegendConfigType type)
    }
    public final class RenderOptionType extends ExpandableStringEnum<RenderOptionType> {
        @Generated public static final RenderOptionType RASTER_TILE = fromString("raster-tile");
        @Generated public static final RenderOptionType VT_POLYGON = fromString("vt-polygon");
        @Generated public static final RenderOptionType VT_LINE = fromString("vt-line");
        @Deprecated @Generated public RenderOptionType()
        @Generated public static RenderOptionType fromString(String name)
        @Generated public static Collection<RenderOptionType> values()
    }
    @Fluent
    public final class RenderOptionVectorOptions implements JsonSerializable<RenderOptionVectorOptions> {
        @Generated public RenderOptionVectorOptions(String tilejsonKey, String sourceLayer)
        @Generated public String getFillColor()
        @Generated public RenderOptionVectorOptions setFillColor(String fillColor)
        @Generated public List<String> getFilter()
        @Generated public RenderOptionVectorOptions setFilter(List<String> filter)
        @Generated public String getSourceLayer()
        @Generated public String getStrokeColor()
        @Generated public RenderOptionVectorOptions setStrokeColor(String strokeColor)
        @Generated public Integer getStrokeWidth()
        @Generated public RenderOptionVectorOptions setStrokeWidth(Integer strokeWidth)
        @Generated public String getTilejsonKey()
    }
    public final class Resampling extends ExpandableStringEnum<Resampling> {
        @Generated public static final Resampling NEAREST = fromString("nearest");
        @Generated public static final Resampling BILINEAR = fromString("bilinear");
        @Generated public static final Resampling CUBIC = fromString("cubic");
        @Generated public static final Resampling CUBIC_SPLINE = fromString("cubic_spline");
        @Generated public static final Resampling LANCZOS = fromString("lanczos");
        @Generated public static final Resampling AVERAGE = fromString("average");
        @Generated public static final Resampling MODE = fromString("mode");
        @Generated public static final Resampling GAUSS = fromString("gauss");
        @Generated public static final Resampling RMS = fromString("rms");
        @Deprecated @Generated public Resampling()
        @Generated public static Resampling fromString(String name)
        @Generated public static Collection<Resampling> values()
    }
    @Fluent
    public final class SearchOptionsFields implements JsonSerializable<SearchOptionsFields> {
        @Generated public SearchOptionsFields()
        @Generated public List<String> getExclude()
        @Generated public SearchOptionsFields setExclude(List<String> exclude)
        @Generated public List<String> getInclude()
        @Generated public SearchOptionsFields setInclude(List<String> include)
    }
    public final class SelMethod extends ExpandableStringEnum<SelMethod> {
        @Generated public static final SelMethod NEAREST = fromString("nearest");
        @Generated public static final SelMethod LINEAR = fromString("linear");
        @Generated public static final SelMethod BILINEAR = fromString("bilinear");
        @Generated public static final SelMethod CUBIC = fromString("cubic");
        @Generated public static final SelMethod CUBIC_SPLINE = fromString("cubic_spline");
        @Generated public static final SelMethod LANCZOS = fromString("lanczos");
        @Generated public static final SelMethod AREA = fromString("area");
        @Generated public static final SelMethod MODE = fromString("mode");
        @Deprecated @Generated public SelMethod()
        @Generated public static SelMethod fromString(String name)
        @Generated public static Collection<SelMethod> values()
    }
    @Immutable
    public final class SharedAccessSignatureSignedLink implements JsonSerializable<SharedAccessSignatureSignedLink> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public OffsetDateTime getExpiresOn()
        @Generated public String getHref()
    }
    @Immutable
    public final class SharedAccessSignatureToken implements JsonSerializable<SharedAccessSignatureToken> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public OffsetDateTime getExpiresOn()
        @Generated public String getToken()
    }
    @Fluent
    public final class SharedAccessSignatureTokenConnection implements JsonSerializable<SharedAccessSignatureTokenConnection> {
        @Generated public SharedAccessSignatureTokenConnection(String containerUri)
        @Generated public String getContainerUri()
        @Generated public OffsetDateTime getExpiration()
        @Generated public String getSharedAccessSignatureToken()
        @Generated public SharedAccessSignatureTokenConnection setSharedAccessSignatureToken(String sharedAccessSignatureToken)
    }
    @Immutable
    public final class SharedAccessSignatureTokenIngestionSource extends IngestionSource {
        @Generated public SharedAccessSignatureTokenIngestionSource(String id, SharedAccessSignatureTokenConnection connectionInfo)
        @Generated public SharedAccessSignatureTokenConnection getConnectionInfo()
        @Generated public static SharedAccessSignatureTokenIngestionSource fromJson(JsonReader jsonReader) throws IOException
        @Generated @Override public IngestionSourceType getKind()
        @Generated @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
    }
    @Fluent
    public final class StacAsset implements JsonSerializable<StacAsset> {
        @Generated public StacAsset()
        @Generated public Map<String, BinaryData> getAdditionalProperties()
        @Generated public StacAsset setAdditionalProperties(Map<String, BinaryData> additionalProperties)
        @Generated public String getConstellation()
        @Generated public StacAsset setConstellation(String constellation)
        @Generated public OffsetDateTime getCreated()
        @Generated public StacAsset setCreated(OffsetDateTime created)
        @Generated public String getDescription()
        @Generated public StacAsset setDescription(String description)
        @Generated public Double getGsd()
        @Generated public StacAsset setGsd(Double gsd)
        @Generated public String getHref()
        @Generated public StacAsset setHref(String href)
        @Generated public List<String> getInstruments()
        @Generated public StacAsset setInstruments(List<String> instruments)
        @Generated public String getMission()
        @Generated public StacAsset setMission(String mission)
        @Generated public String getPlatform()
        @Generated public StacAsset setPlatform(String platform)
        @Generated public List<StacProvider> getProviders()
        @Generated public StacAsset setProviders(List<StacProvider> providers)
        @Generated public List<String> getRoles()
        @Generated public StacAsset setRoles(List<String> roles)
        @Generated public String getTitle()
        @Generated public StacAsset setTitle(String title)
        @Generated public String getType()
        @Generated public StacAsset setType(String type)
        @Generated public OffsetDateTime getUpdated()
        @Generated public StacAsset setUpdated(OffsetDateTime updated)
    }
    @Immutable
    public final class StacAssetData {
        @Generated public StacAssetData(AssetMetadata data, FileDetails file)
        @Generated public AssetMetadata getData()
        @Generated public FileDetails getFile()
    }
    public final class StacAssetUrlSigningMode extends ExpandableStringEnum<StacAssetUrlSigningMode> {
        @Generated public static final StacAssetUrlSigningMode TRUE = fromString("true");
        @Generated public static final StacAssetUrlSigningMode FALSE = fromString("false");
        @Deprecated @Generated public StacAssetUrlSigningMode()
        @Generated public static StacAssetUrlSigningMode fromString(String name)
        @Generated public static Collection<StacAssetUrlSigningMode> values()
    }
    @Immutable
    public final class StacCatalogCollections implements JsonSerializable<StacCatalogCollections> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<StacCollection> getCollections()
        @Generated public List<StacLink> getLinks()
    }
    @Fluent
    public final class StacCollection implements JsonSerializable<StacCollection> {
        @Generated public StacCollection(String description, List<StacLink> links, String license, StacExtensionExtent extent)
        @Generated public Map<String, BinaryData> getAdditionalProperties()
        @Generated public StacCollection setAdditionalProperties(Map<String, BinaryData> additionalProperties)
        @Generated public Map<String, StacAsset> getAssets()
        @Generated public StacCollection setAssets(Map<String, StacAsset> assets)
        @Generated public OffsetDateTime getCreatedOn()
        @Generated public StacCollection setCreatedOn(OffsetDateTime createdOn)
        @Generated public String getDescription()
        @Generated public StacExtensionExtent getExtent()
        @Generated public String getId()
        @Generated public Map<String, StacItemAsset> getItemAssets()
        @Generated public StacCollection setItemAssets(Map<String, StacItemAsset> itemAssets)
        @Generated public List<String> getKeywords()
        @Generated public StacCollection setKeywords(List<String> keywords)
        @Generated public String getLicense()
        @Generated public List<StacLink> getLinks()
        @Generated public List<StacProvider> getProviders()
        @Generated public StacCollection setProviders(List<StacProvider> providers)
        @Generated public String getShortDescription()
        @Generated public StacCollection setShortDescription(String shortDescription)
        @Generated public List<String> getStacExtensions()
        @Generated public StacCollection setStacExtensions(List<String> stacExtensions)
        @Generated public String getStacVersion()
        @Generated public StacCollection setStacVersion(String stacVersion)
        @Generated public Map<String, BinaryData> getSummaries()
        @Generated public StacCollection setSummaries(Map<String, BinaryData> summaries)
        @Generated public String getTitle()
        @Generated public StacCollection setTitle(String title)
        @Generated public String getType()
        @Generated public StacCollection setType(String type)
        @Generated public OffsetDateTime getUpdatedOn()
        @Generated public StacCollection setUpdatedOn(OffsetDateTime updatedOn)
    }
    @Immutable
    public final class StacCollectionTemporalExtent implements JsonSerializable<StacCollectionTemporalExtent> {
        @Generated public StacCollectionTemporalExtent(List<List<OffsetDateTime>> interval)
        @Generated public List<List<OffsetDateTime>> getInterval()
    }
    @Immutable
    public final class StacConformanceClasses implements JsonSerializable<StacConformanceClasses> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<String> getConformsTo()
    }
    @Fluent
    public final class StacContextExtension implements JsonSerializable<StacContextExtension> {
        @Generated public StacContextExtension()
        @Generated public Integer getLimit()
        @Generated public StacContextExtension setLimit(Integer limit)
        @Generated public Integer getMatched()
        @Generated public StacContextExtension setMatched(Integer matched)
        @Generated public int getReturned()
        @Generated public StacContextExtension setReturned(int returned)
    }
    @Immutable
    public final class StacExtensionExtent implements JsonSerializable<StacExtensionExtent> {
        @Generated public StacExtensionExtent(StacExtensionSpatialExtent spatial, StacCollectionTemporalExtent temporal)
        @Generated public StacExtensionSpatialExtent getSpatial()
        @Generated public StacCollectionTemporalExtent getTemporal()
    }
    @Fluent
    public final class StacExtensionSpatialExtent implements JsonSerializable<StacExtensionSpatialExtent> {
        @Generated public StacExtensionSpatialExtent()
        @Generated public List<List<Double>> getBoundingBox()
        @Generated public StacExtensionSpatialExtent setBoundingBox(List<List<Double>> boundingBox)
    }
    @Fluent
    public final class StacItem extends StacItemOrStacItemCollection {
        @Generated public StacItem()
        @Generated public Map<String, StacAsset> getAssets()
        @Generated public StacItem setAssets(Map<String, StacAsset> assets)
        @Generated public List<Double> getBoundingBox()
        @Generated public StacItem setBoundingBox(List<Double> boundingBox)
        @Generated public String getCollection()
        @Generated public StacItem setCollection(String collection)
        @Generated @Override public StacItem setCreatedOn(OffsetDateTime createdOn)
        @Generated public String getETag()
        @Generated public StacItem setETag(String eTag)
        @Generated public static StacItem fromJson(JsonReader jsonReader) throws IOException
        @Generated public GeoJsonGeometry getGeometry()
        @Generated public StacItem setGeometry(GeoJsonGeometry geometry)
        @Generated public String getId()
        @Generated @Override public StacItem setLinks(List<StacLink> links)
        @Generated public StacItemProperties getProperties()
        @Generated public StacItem setProperties(StacItemProperties properties)
        @Generated @Override public StacItem setShortDescription(String shortDescription)
        @Generated @Override public StacItem setStacExtensions(List<String> stacExtensions)
        @Generated @Override public StacItem setStacVersion(String stacVersion)
        @Generated public OffsetDateTime getTimestamp()
        @Generated public StacItem setTimestamp(OffsetDateTime timestamp)
        @Generated @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Generated @Override public StacModelType getType()
        @Generated @Override public StacItem setUpdatedOn(OffsetDateTime updatedOn)
    }
    @Fluent
    public final class StacItemAsset implements JsonSerializable<StacItemAsset> {
        @Generated public StacItemAsset(String title, String type)
        @Generated public Map<String, BinaryData> getAdditionalProperties()
        @Generated public StacItemAsset setAdditionalProperties(Map<String, BinaryData> additionalProperties)
        @Generated public String getConstellation()
        @Generated public StacItemAsset setConstellation(String constellation)
        @Generated public OffsetDateTime getCreated()
        @Generated public StacItemAsset setCreated(OffsetDateTime created)
        @Generated public String getDescription()
        @Generated public StacItemAsset setDescription(String description)
        @Generated public Double getGsd()
        @Generated public StacItemAsset setGsd(Double gsd)
        @Generated public String getHref()
        @Generated public StacItemAsset setHref(String href)
        @Generated public List<String> getInstruments()
        @Generated public StacItemAsset setInstruments(List<String> instruments)
        @Generated public String getMission()
        @Generated public StacItemAsset setMission(String mission)
        @Generated public String getPlatform()
        @Generated public StacItemAsset setPlatform(String platform)
        @Generated public List<StacProvider> getProviders()
        @Generated public StacItemAsset setProviders(List<StacProvider> providers)
        @Generated public List<String> getRoles()
        @Generated public StacItemAsset setRoles(List<String> roles)
        @Generated public String getTitle()
        @Generated public String getType()
        @Generated public OffsetDateTime getUpdated()
        @Generated public StacItemAsset setUpdated(OffsetDateTime updated)
    }
    @Immutable
    public final class StacItemBounds implements JsonSerializable<StacItemBounds> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<Double> getBounds()
    }
    @Fluent
    public final class StacItemCollection extends StacItemOrStacItemCollection {
        @Generated public StacItemCollection()
        @Generated public List<Double> getBoundingBox()
        @Generated public StacItemCollection setBoundingBox(List<Double> boundingBox)
        @Generated public StacContextExtension getContext()
        @Generated public StacItemCollection setContext(StacContextExtension context)
        @Generated @Override public StacItemCollection setCreatedOn(OffsetDateTime createdOn)
        @Generated public List<StacItem> getFeatures()
        @Generated public StacItemCollection setFeatures(List<StacItem> features)
        @Generated public static StacItemCollection fromJson(JsonReader jsonReader) throws IOException
        @Generated @Override public StacItemCollection setLinks(List<StacLink> links)
        @Generated @Override public StacItemCollection setShortDescription(String shortDescription)
        @Generated @Override public StacItemCollection setStacExtensions(List<String> stacExtensions)
        @Generated @Override public StacItemCollection setStacVersion(String stacVersion)
        @Generated @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Generated @Override public StacModelType getType()
        @Generated @Override public StacItemCollection setUpdatedOn(OffsetDateTime updatedOn)
    }
    @Fluent
    public class StacItemOrStacItemCollection implements JsonSerializable<StacItemOrStacItemCollection> {
        @Generated public StacItemOrStacItemCollection()
        @Generated public OffsetDateTime getCreatedOn()
        @Generated public StacItemOrStacItemCollection setCreatedOn(OffsetDateTime createdOn)
        @Generated public List<StacLink> getLinks()
        @Generated public StacItemOrStacItemCollection setLinks(List<StacLink> links)
        @Generated public String getShortDescription()
        @Generated public StacItemOrStacItemCollection setShortDescription(String shortDescription)
        @Generated public List<String> getStacExtensions()
        @Generated public StacItemOrStacItemCollection setStacExtensions(List<String> stacExtensions)
        @Generated public String getStacVersion()
        @Generated public StacItemOrStacItemCollection setStacVersion(String stacVersion)
        @Generated public StacModelType getType()
        @Generated public OffsetDateTime getUpdatedOn()
        @Generated public StacItemOrStacItemCollection setUpdatedOn(OffsetDateTime updatedOn)
    }
    @Immutable
    public final class StacItemPointAsset implements JsonSerializable<StacItemPointAsset> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Map<String, StacAsset> getAssets()
        @Generated public List<Double> getBoundingBox()
        @Generated public String getCollectionId()
        @Generated public String getId()
    }
    @Fluent
    public final class StacItemProperties implements JsonSerializable<StacItemProperties> {
        @Generated public StacItemProperties()
        @Generated public Map<String, BinaryData> getAdditionalProperties()
        @Generated public StacItemProperties setAdditionalProperties(Map<String, BinaryData> additionalProperties)
        @Generated public String getConstellation()
        @Generated public StacItemProperties setConstellation(String constellation)
        @Generated public OffsetDateTime getCreated()
        @Generated public StacItemProperties setCreated(OffsetDateTime created)
        @Generated public String getDatetime()
        @Generated public StacItemProperties setDatetime(String datetime)
        @Generated public String getDescription()
        @Generated public StacItemProperties setDescription(String description)
        @Generated public OffsetDateTime getEndDatetime()
        @Generated public StacItemProperties setEndDatetime(OffsetDateTime endDatetime)
        @Generated public Double getGsd()
        @Generated public StacItemProperties setGsd(Double gsd)
        @Generated public List<String> getInstruments()
        @Generated public StacItemProperties setInstruments(List<String> instruments)
        @Generated public String getMission()
        @Generated public StacItemProperties setMission(String mission)
        @Generated public String getPlatform()
        @Generated public StacItemProperties setPlatform(String platform)
        @Generated public List<StacProvider> getProviders()
        @Generated public StacItemProperties setProviders(List<StacProvider> providers)
        @Generated public OffsetDateTime getStartDatetime()
        @Generated public StacItemProperties setStartDatetime(OffsetDateTime startDatetime)
        @Generated public String getTitle()
        @Generated public StacItemProperties setTitle(String title)
        @Generated public OffsetDateTime getUpdated()
        @Generated public StacItemProperties setUpdated(OffsetDateTime updated)
    }
    @Immutable
    public final class StacItemStatisticsGeoJson implements JsonSerializable<StacItemStatisticsGeoJson> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public GeoJsonGeometry getGeometry()
        @Generated public StacItemStatisticsGeoJsonProperties getProperties()
        @Generated public FeatureType getType()
    }
    @Immutable
    public final class StacItemStatisticsGeoJsonProperties implements JsonSerializable<StacItemStatisticsGeoJsonProperties> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Map<String, BinaryData> getAdditionalProperties()
        @Generated public Map<String, BandStatistics> getStatistics()
    }
    @Immutable
    public final class StacLandingPage implements JsonSerializable<StacLandingPage> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<String> getConformsTo()
        @Generated public OffsetDateTime getCreatedOn()
        @Generated public String getDescription()
        @Generated public String getId()
        @Generated public List<StacLink> getLinks()
        @Generated public String getShortDescription()
        @Generated public List<String> getStacExtensions()
        @Generated public String getStacVersion()
        @Generated public String getTitle()
        @Generated public String getType()
        @Generated public OffsetDateTime getUpdatedOn()
    }
    @Fluent
    public final class StacLink implements JsonSerializable<StacLink> {
        @Generated public StacLink()
        @Generated public Map<String, BinaryData> getBody()
        @Generated public StacLink setBody(Map<String, BinaryData> body)
        @Generated public Map<String, String> getHeaders()
        @Generated public StacLink setHeaders(Map<String, String> headers)
        @Generated public String getHref()
        @Generated public StacLink setHref(String href)
        @Generated public String getHreflang()
        @Generated public StacLink setHreflang(String hreflang)
        @Generated public Integer getLength()
        @Generated public StacLink setLength(Integer length)
        @Generated public Boolean isMerge()
        @Generated public StacLink setMerge(Boolean merge)
        @Generated public StacLinkMethod getMethod()
        @Generated public StacLink setMethod(StacLinkMethod method)
        @Generated public String getRel()
        @Generated public StacLink setRel(String rel)
        @Generated public String getTitle()
        @Generated public StacLink setTitle(String title)
        @Generated public StacLinkType getType()
        @Generated public StacLink setType(StacLinkType type)
    }
    public final class StacLinkMethod extends ExpandableStringEnum<StacLinkMethod> {
        @Generated public static final StacLinkMethod GET = fromString("GET");
        @Generated public static final StacLinkMethod POST = fromString("POST");
        @Deprecated @Generated public StacLinkMethod()
        @Generated public static StacLinkMethod fromString(String name)
        @Generated public static Collection<StacLinkMethod> values()
    }
    public final class StacLinkType extends ExpandableStringEnum<StacLinkType> {
        @Generated public static final StacLinkType IMAGE_TIFF_APPLICATION_GEOTIFF = fromString("image/tiff; application=geotiff");
        @Generated public static final StacLinkType IMAGE_JP2 = fromString("image/jp2");
        @Generated public static final StacLinkType IMAGE_PNG = fromString("image/png");
        @Generated public static final StacLinkType IMAGE_JPEG = fromString("image/jpeg");
        @Generated public static final StacLinkType IMAGE_JPG = fromString("image/jpg");
        @Generated public static final StacLinkType IMAGE_WEBP = fromString("image/webp");
        @Generated public static final StacLinkType APPLICATION_X_BINARY = fromString("application/x-binary");
        @Generated public static final StacLinkType APPLICATION_XML = fromString("application/xml");
        @Generated public static final StacLinkType APPLICATION_JSON = fromString("application/json");
        @Generated public static final StacLinkType APPLICATION_GEO_JSON = fromString("application/geo+json");
        @Generated public static final StacLinkType TEXT_HTML = fromString("text/html");
        @Generated public static final StacLinkType TEXT_PLAIN = fromString("text/plain");
        @Generated public static final StacLinkType APPLICATION_X_PROTOBUF = fromString("application/x-protobuf");
        @Deprecated @Generated public StacLinkType()
        @Generated public static StacLinkType fromString(String name)
        @Generated public static Collection<StacLinkType> values()
    }
    public final class StacModelType extends ExpandableStringEnum<StacModelType> {
        @Generated public static final StacModelType FEATURE = fromString("Feature");
        @Generated public static final StacModelType FEATURE_COLLECTION = fromString("FeatureCollection");
        @Deprecated @Generated public StacModelType()
        @Generated public static StacModelType fromString(String name)
        @Generated public static Collection<StacModelType> values()
    }
    @Fluent
    public final class StacMosaic implements JsonSerializable<StacMosaic> {
        @Generated public StacMosaic(String id, String name, List<Map<String, BinaryData>> cql)
        @Generated public List<Map<String, BinaryData>> getCql()
        @Generated public String getDescription()
        @Generated public StacMosaic setDescription(String description)
        @Generated public String getId()
        @Generated public String getName()
    }
    @Immutable
    public final class StacMosaicConfiguration implements JsonSerializable<StacMosaicConfiguration> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Map<String, BinaryData> getDefaultCustomQuery()
        @Generated public DefaultLocation getDefaultLocation()
        @Generated public List<StacMosaic> getMosaics()
        @Generated public List<RenderOption> getRenderOptions()
    }
    @Fluent
    public final class StacProvider implements JsonSerializable<StacProvider> {
        @Generated public StacProvider()
        @Generated public String getDescription()
        @Generated public StacProvider setDescription(String description)
        @Generated public String getName()
        @Generated public StacProvider setName(String name)
        @Generated public List<String> getRoles()
        @Generated public StacProvider setRoles(List<String> roles)
        @Generated public String getUrl()
        @Generated public StacProvider setUrl(String url)
    }
    @Fluent
    public final class StacQueryable implements JsonSerializable<StacQueryable> {
        @Generated public StacQueryable(String name, Map<String, BinaryData> definition)
        @Generated public Boolean isCreateIndex()
        @Generated public StacQueryable setCreateIndex(Boolean createIndex)
        @Generated public StacQueryableDefinitionDataType getDataType()
        @Generated public StacQueryable setDataType(StacQueryableDefinitionDataType dataType)
        @Generated public Map<String, BinaryData> getDefinition()
        @Generated public String getName()
    }
    public final class StacQueryableDefinitionDataType extends ExpandableStringEnum<StacQueryableDefinitionDataType> {
        @Generated public static final StacQueryableDefinitionDataType STRING = fromString("string");
        @Generated public static final StacQueryableDefinitionDataType NUMBER = fromString("number");
        @Generated public static final StacQueryableDefinitionDataType BOOLEAN = fromString("boolean");
        @Generated public static final StacQueryableDefinitionDataType TIMESTAMP = fromString("timestamp");
        @Generated public static final StacQueryableDefinitionDataType DATE = fromString("date");
        @Deprecated @Generated public StacQueryableDefinitionDataType()
        @Generated public static StacQueryableDefinitionDataType fromString(String name)
        @Generated public static Collection<StacQueryableDefinitionDataType> values()
    }
    @Fluent
    public final class StacSearchParameters implements JsonSerializable<StacSearchParameters> {
        @Generated public StacSearchParameters()
        @Generated public List<Double> getBoundingBox()
        @Generated public StacSearchParameters setBoundingBox(List<Double> boundingBox)
        @Generated public List<String> getCollections()
        @Generated public StacSearchParameters setCollections(List<String> collections)
        @Generated public Map<String, BinaryData> getConformanceClass()
        @Generated public StacSearchParameters setConformanceClass(Map<String, BinaryData> conformanceClass)
        @Generated public String getDatetime()
        @Generated public StacSearchParameters setDatetime(String datetime)
        @Generated public List<SearchOptionsFields> getFields()
        @Generated public StacSearchParameters setFields(List<SearchOptionsFields> fields)
        @Generated public Map<String, BinaryData> getFilter()
        @Generated public StacSearchParameters setFilter(Map<String, BinaryData> filter)
        @Generated public String getFilterCoordinateReferenceSystem()
        @Generated public StacSearchParameters setFilterCoordinateReferenceSystem(String filterCoordinateReferenceSystem)
        @Generated public FilterLanguage getFilterLang()
        @Generated public StacSearchParameters setFilterLang(FilterLanguage filterLang)
        @Generated public List<String> getIds()
        @Generated public StacSearchParameters setIds(List<String> ids)
        @Generated public GeoJsonGeometry getIntersects()
        @Generated public StacSearchParameters setIntersects(GeoJsonGeometry intersects)
        @Generated public Integer getLimit()
        @Generated public StacSearchParameters setLimit(Integer limit)
        @Generated public Map<String, BinaryData> getQuery()
        @Generated public StacSearchParameters setQuery(Map<String, BinaryData> query)
        @Generated public List<StacSortExtension> getSortBy()
        @Generated public StacSearchParameters setSortBy(List<StacSortExtension> sortBy)
        @Generated public String getToken()
        @Generated public StacSearchParameters setToken(String token)
    }
    public final class StacSearchSortingDirection extends ExpandableStringEnum<StacSearchSortingDirection> {
        @Generated public static final StacSearchSortingDirection ASC = fromString("asc");
        @Generated public static final StacSearchSortingDirection DESC = fromString("desc");
        @Deprecated @Generated public StacSearchSortingDirection()
        @Generated public static StacSearchSortingDirection fromString(String name)
        @Generated public static Collection<StacSearchSortingDirection> values()
    }
    @Immutable
    public final class StacSortExtension implements JsonSerializable<StacSortExtension> {
        @Generated public StacSortExtension(String field, StacSearchSortingDirection direction)
        @Generated public StacSearchSortingDirection getDirection()
        @Generated public String getField()
    }
    public final class TerrainAlgorithm extends ExpandableStringEnum<TerrainAlgorithm> {
        @Generated public static final TerrainAlgorithm HILLSHADE = fromString("hillshade");
        @Generated public static final TerrainAlgorithm CONTOURS = fromString("contours");
        @Generated public static final TerrainAlgorithm NORMALIZED_INDEX = fromString("normalizedIndex");
        @Generated public static final TerrainAlgorithm TERRARIUM = fromString("terrarium");
        @Generated public static final TerrainAlgorithm TERRAINRGB = fromString("terrainrgb");
        @Generated public static final TerrainAlgorithm SLOPE = fromString("slope");
        @Generated public static final TerrainAlgorithm CAST = fromString("cast");
        @Generated public static final TerrainAlgorithm CEIL = fromString("ceil");
        @Generated public static final TerrainAlgorithm FLOOR = fromString("floor");
        @Generated public static final TerrainAlgorithm MIN = fromString("min");
        @Generated public static final TerrainAlgorithm MAX = fromString("max");
        @Generated public static final TerrainAlgorithm MEDIAN = fromString("median");
        @Generated public static final TerrainAlgorithm MEAN = fromString("mean");
        @Generated public static final TerrainAlgorithm STD = fromString("std");
        @Generated public static final TerrainAlgorithm VAR = fromString("var");
        @Deprecated @Generated public TerrainAlgorithm()
        @Generated public static TerrainAlgorithm fromString(String name)
        @Generated public static Collection<TerrainAlgorithm> values()
    }
    public final class TileAddressingScheme extends ExpandableStringEnum<TileAddressingScheme> {
        @Generated public static final TileAddressingScheme XYZ = fromString("xyz");
        @Generated public static final TileAddressingScheme TMS = fromString("tms");
        @Deprecated @Generated public TileAddressingScheme()
        @Generated public static TileAddressingScheme fromString(String name)
        @Generated public static Collection<TileAddressingScheme> values()
    }
    @Immutable
    public final class TileJsonMetadata implements JsonSerializable<TileJsonMetadata> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getAttribution()
        @Generated public List<Double> getBounds()
        @Generated public List<Double> getCenter()
        @Generated public List<String> getData()
        @Generated public String getDescription()
        @Generated public List<String> getGrids()
        @Generated public String getLegend()
        @Generated public Integer getMaxZoom()
        @Generated public Integer getMinZoom()
        @Generated public String getName()
        @Generated public TileAddressingScheme getScheme()
        @Generated public String getTemplate()
        @Generated public String getTileJson()
        @Generated public List<String> getTiles()
        @Generated public String getVersion()
    }
    @Immutable
    public final class TileMatrix implements JsonSerializable<TileMatrix> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public double getCellSize()
        @Generated public TileMatrixCornerOfOrigin getCornerOfOrigin()
        @Generated public String getDescription()
        @Generated public String getId()
        @Generated public List<String> getKeywords()
        @Generated public int getMatrixHeight()
        @Generated public int getMatrixWidth()
        @Generated public List<Double> getPointOfOrigin()
        @Generated public double getScaleDenominator()
        @Generated public int getTileHeight()
        @Generated public int getTileWidth()
        @Generated public String getTitle()
        @Generated public List<VariableMatrixWidth> getVariableMatrixWidths()
    }
    public final class TileMatrixCornerOfOrigin extends ExpandableStringEnum<TileMatrixCornerOfOrigin> {
        @Generated public static final TileMatrixCornerOfOrigin TOP_LEFT = fromString("topLeft");
        @Generated public static final TileMatrixCornerOfOrigin BOTTOM_LEFT = fromString("bottomLeft");
        @Deprecated @Generated public TileMatrixCornerOfOrigin()
        @Generated public static TileMatrixCornerOfOrigin fromString(String name)
        @Generated public static Collection<TileMatrixCornerOfOrigin> values()
    }
    @Immutable
    public final class TileMatrixSet implements JsonSerializable<TileMatrixSet> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public TileMatrixSetBoundingBox getBoundingBox()
        @Generated public String getCrs()
        @Generated public String getDescription()
        @Generated public String getId()
        @Generated public List<String> getKeywords()
        @Generated public List<String> getOrderedAxes()
        @Generated public List<TileMatrix> getTileMatrices()
        @Generated public String getTitle()
        @Generated public String getUri()
        @Generated public String getWellKnownScaleSet()
    }
    @Immutable
    public final class TileMatrixSetBoundingBox implements JsonSerializable<TileMatrixSetBoundingBox> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getCrs()
        @Generated public List<String> getLowerLeft()
        @Generated public List<String> getOrderedAxes()
        @Generated public List<String> getUpperRight()
    }
    public final class TileMatrixSetId extends ExpandableStringEnum<TileMatrixSetId> {
        @Generated public static final TileMatrixSetId CANADIAN_NAD83_LCC = fromString("CanadianNAD83_LCC");
        @Generated public static final TileMatrixSetId EUROPEAN_ETRS89_LAEAQUAD = fromString("EuropeanETRS89_LAEAQuad");
        @Generated public static final TileMatrixSetId LINZANTARTICA_MAP_TILEGRID = fromString("LINZAntarticaMapTilegrid");
        @Generated public static final TileMatrixSetId NZTM2000QUAD = fromString("NZTM2000Quad");
        @Generated public static final TileMatrixSetId UPSANTARCTIC_WGS84QUAD = fromString("UPSAntarcticWGS84Quad");
        @Generated public static final TileMatrixSetId UPSARCTIC_WGS84QUAD = fromString("UPSArcticWGS84Quad");
        @Generated public static final TileMatrixSetId UTM31WGS84QUAD = fromString("UTM31WGS84Quad");
        @Generated public static final TileMatrixSetId WGS1984QUAD = fromString("WGS1984Quad");
        @Generated public static final TileMatrixSetId WEB_MERCATOR_QUAD = fromString("WebMercatorQuad");
        @Generated public static final TileMatrixSetId WORLD_CRS84QUAD = fromString("WorldCRS84Quad");
        @Generated public static final TileMatrixSetId WORLD_MERCATOR_WGS84QUAD = fromString("WorldMercatorWGS84Quad");
        @Deprecated @Generated public TileMatrixSetId()
        @Generated public static TileMatrixSetId fromString(String name)
        @Generated public static Collection<TileMatrixSetId> values()
    }
    @Immutable
    public final class TileMatrixSetLimitsEntry implements JsonSerializable<TileMatrixSetLimitsEntry> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public int getMaxTileCol()
        @Generated public int getMaxTileRow()
        @Generated public int getMinTileCol()
        @Generated public int getMinTileRow()
        @Generated public String getTileMatrix()
    }
    @Immutable
    public final class TileSetBoundingBox implements JsonSerializable<TileSetBoundingBox> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getCrs()
        @Generated public List<Double> getLowerLeft()
        @Generated public List<Double> getUpperRight()
    }
    @Immutable
    public final class TileSetEntry implements JsonSerializable<TileSetEntry> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getAccessConstraints()
        @Generated public TileSetBoundingBox getBoundingBox()
        @Generated public String getCrs()
        @Generated public String getDataType()
        @Generated public List<TileSetLink> getLinks()
        @Generated public String getTitle()
    }
    @Immutable
    public final class TileSetLink implements JsonSerializable<TileSetLink> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getHref()
        @Generated public String getRel()
        @Generated public String getTitle()
        @Generated public String getType()
    }
    @Immutable
    public final class TileSetList implements JsonSerializable<TileSetList> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<TileSetEntry> getTilesets()
    }
    @Immutable
    public final class TileSetMetadata implements JsonSerializable<TileSetMetadata> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getAccessConstraints()
        @Generated public TileSetBoundingBox getBoundingBox()
        @Generated public String getCrs()
        @Generated public String getDataType()
        @Generated public List<TileSetLink> getLinks()
        @Generated public List<TileMatrixSetLimitsEntry> getTileMatrixSetLimits()
        @Generated public String getTitle()
    }
    @Fluent
    public final class TileSettings implements JsonSerializable<TileSettings> {
        @Generated public TileSettings(int minZoom, int maxItemsPerTile)
        @Generated public DefaultLocation getDefaultLocation()
        @Generated public TileSettings setDefaultLocation(DefaultLocation defaultLocation)
        @Generated public int getMaxItemsPerTile()
        @Generated public int getMinZoom()
    }
    @Immutable
    public final class TilerAssetGeoJson implements JsonSerializable<TilerAssetGeoJson> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Map<String, StacAsset> getAssets()
        @Generated public List<Double> getBoundingBox()
        @Generated public String getCollection()
        @Generated public String getId()
    }
    @Immutable
    public final class TilerCoreModelsResponsesPoint implements JsonSerializable<TilerCoreModelsResponsesPoint> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<String> getBandNames()
        @Generated public List<Double> getCoordinates()
        @Generated public List<Double> getValues()
    }
    public final class TilerImageFormat extends ExpandableStringEnum<TilerImageFormat> {
        @Generated public static final TilerImageFormat PNG = fromString("png");
        @Generated public static final TilerImageFormat NPY = fromString("npy");
        @Generated public static final TilerImageFormat TIF = fromString("tif");
        @Generated public static final TilerImageFormat JPEG = fromString("jpeg");
        @Generated public static final TilerImageFormat JPG = fromString("jpg");
        @Generated public static final TilerImageFormat JP2 = fromString("jp2");
        @Generated public static final TilerImageFormat WEBP = fromString("webp");
        @Generated public static final TilerImageFormat PNGRAW = fromString("pngraw");
        @Deprecated @Generated public TilerImageFormat()
        @Generated public static TilerImageFormat fromString(String name)
        @Generated public static Collection<TilerImageFormat> values()
    }
    @Immutable
    public final class TilerInfo implements JsonSerializable<TilerInfo> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<List<String>> getBandDescriptions()
        @Generated public List<List<BinaryData>> getBandMetadata()
        @Generated public List<Double> getBounds()
        @Generated public List<String> getColorInterpretation()
        @Generated public Map<String, List<String>> getColormap()
        @Generated public String getCoordinateReferenceSystem()
        @Generated public Integer getCount()
        @Generated public String getDriver()
        @Generated public String getDtype()
        @Generated public Integer getHeight()
        @Generated public Integer getMaxZoom()
        @Generated public Integer getMinZoom()
        @Generated public NoDataType getNoDataType()
        @Generated public List<Integer> getOffsets()
        @Generated public List<Integer> getOverviews()
        @Generated public List<Integer> getScales()
        @Generated public Integer getWidth()
    }
    @Immutable
    public final class TilerInfoGeoJsonFeature implements JsonSerializable<TilerInfoGeoJsonFeature> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<Double> getBoundingBox()
        @Generated public GeoJsonGeometry getGeometry()
        @Generated public String getId()
        @Generated public Map<String, TilerInfo> getProperties()
        @Generated public FeatureType getType()
    }
    @Immutable
    public final class TilerInfoMapResponse implements JsonSerializable<TilerInfoMapResponse> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Map<String, TilerInfo> getAdditionalProperties()
    }
    @Immutable
    public final class TilerMosaicSearchRegistrationResponse implements JsonSerializable<TilerMosaicSearchRegistrationResponse> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<StacLink> getLinks()
        @Generated public String getSearchId()
    }
    @Immutable
    public final class TilerStacItemStatistics implements JsonSerializable<TilerStacItemStatistics> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Map<String, BandStatistics> getAdditionalProperties()
    }
    @Immutable
    public final class TilerStacSearchDefinition implements JsonSerializable<TilerStacSearchDefinition> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getHash()
        @Generated public OffsetDateTime getLastUsed()
        @Generated public MosaicMetadata getMetadata()
        @Generated public Map<String, BinaryData> getSearch()
        @Generated public int getUseCount()
    }
    @Immutable
    public final class TilerStacSearchRegistration implements JsonSerializable<TilerStacSearchRegistration> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<StacLink> getLinks()
        @Generated public TilerStacSearchDefinition getSearch()
    }
    @Immutable
    public final class UserCollectionSettings implements JsonSerializable<UserCollectionSettings> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public StacMosaicConfiguration getMosaicConfiguration()
        @Generated public TileSettings getTileSettings()
    }
    @Immutable
    public final class VariableMatrixWidth implements JsonSerializable<VariableMatrixWidth> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public int getCoalesce()
        @Generated public int getMaxTileRow()
        @Generated public int getMinTileRow()
    }
    public final class WarpKernelResampling extends ExpandableStringEnum<WarpKernelResampling> {
        @Generated public static final WarpKernelResampling NEAREST = fromString("nearest");
        @Generated public static final WarpKernelResampling BILINEAR = fromString("bilinear");
        @Generated public static final WarpKernelResampling CUBIC = fromString("cubic");
        @Generated public static final WarpKernelResampling CUBIC_SPLINE = fromString("cubic_spline");
        @Generated public static final WarpKernelResampling LANCZOS = fromString("lanczos");
        @Generated public static final WarpKernelResampling AVERAGE = fromString("average");
        @Generated public static final WarpKernelResampling MODE = fromString("mode");
        @Generated public static final WarpKernelResampling MAX = fromString("max");
        @Generated public static final WarpKernelResampling MIN = fromString("min");
        @Generated public static final WarpKernelResampling MED = fromString("med");
        @Generated public static final WarpKernelResampling Q1 = fromString("q1");
        @Generated public static final WarpKernelResampling Q3 = fromString("q3");
        @Generated public static final WarpKernelResampling SUM = fromString("sum");
        @Generated public static final WarpKernelResampling RMS = fromString("rms");
        @Deprecated @Generated public WarpKernelResampling()
        @Generated public static WarpKernelResampling fromString(String name)
        @Generated public static Collection<WarpKernelResampling> values()
    }
}
```