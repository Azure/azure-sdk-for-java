# Azure Search

> see https://aka.ms/autorest

This is the AutoRest configuration file for Search Client

---

## Getting Started

To build the SDK for Search, simply [Install AutoRest](https://aka.ms/autorest/install) and in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

---

## Configuration

### Basic Information

These are the global settings for Search Client.

``` yaml
directive:

  - from: swagger-document
    where: "$"
    transform: >
        $["securityDefinitions"] = {};
  - from: swagger-document
    where: "$"
    transform: >
        $["security"] = [];

  - rename-model:
      from: SearchAddressResult
      to: SearchAddressResultPrivate             
  - rename-model:
      from: ReverseSearchAddressResult
      to: ReverseSearchAddressResultPrivate     
  - rename-model:
      from: ReverseSearchAddressResultItem
      to: ReverseSearchAddressResultItemPrivate  
  - rename-model:
      from: ReverseSearchCrossStreetAddressResult
      to: ReverseSearchCrossStreetAddressResultPrivate
  - rename-model:
      from: ReverseSearchCrossStreetAddressResultItem
      to: ReverseSearchCrossStreetAddressResultItemPrivate   
  - rename-model:
      from: SearchSummary
      to: SearchSummaryPrivate
  - rename-model:
      from: SearchAddressBatchResult
      to: SearchAddressBatchResultPrivate
  - rename-model:
      from: SearchAddressBatchItem
      to: SearchAddressBatchItemPrivate
  - rename-model:
      from: ReverseSearchAddressBatchProcessResult
      to: ReverseSearchAddressBatchResultPrivate
  - rename-model:
      from: ReverseSearchAddressBatchItem
      to: ReverseSearchAddressBatchItemPrivate
  - rename-model:
      from: BoundingBox
      to: BoundingBoxPrivate
  - rename-model:
      from: Polygon
      to: PolygonPrivate

title: SearchClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Search/preview/1.0/search.json
namespace: com.azure.maps.search
java: true
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
payload-flattening-threshold: 0
add-context-parameter: true
context-client-method-parameter: true
client-logger: true
generate-client-as-impl: true
sync-methods: all
generate-sync-async-clients: false
polling: {}
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: Address,AddressRanges,EntryPoint,BatchResultSummary,BrandName,Classification,ClassificationName,DataSource,ErrorAdditionalInfo,ErrorDetail,ErrorResponseException,ElectricVehicleConnector,EntryPointType,GeographicEntityType,GeometryIdentifier,LocalizedMapView,OperatingHoursTime,OperatingHoursRange,MatchType,OperatingHours,OperatingHoursTimeRange,PointOfInterest,PointOfInterestCategory,PointOfInterestCategorySet,PointOfInterestCategoryTreeResult,PointOfInterestExtendedPostalCodes,RoadUseType,SearchAddressResultType,SearchAddressResultItem,SearchIndexes,ErrorResponse,QueryType
customization-jar-path: target/azure-maps-search-customization-1.0.0-beta.1.jar
customization-class: SearchCustomization
```
