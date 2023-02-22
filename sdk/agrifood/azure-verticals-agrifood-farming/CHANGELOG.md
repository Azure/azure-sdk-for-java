# Release History

## 1.0.0-beta.3 (2023-02-23)
### Features Added
- Adding clients for Sensor Integration which includes crud operations on DeviceDataModels, Devices, SensorDataModels, Sensors, SensorMappings, SensorPartnerIntegration and get Sensor events.
- Adding new APIs for STAC search and get feature
- Adding breedingMethods and Measurements as part of Crop Entity
- Adding geographicIdentifier as part of Season Entity
- Adding trait, relativeMeasurements and treatments as part of CropVariety Entity
- Adding type, crs, centroid and bbox(bounding box of the geometry) as part of Boundary Entity
- Adding Source field in Farmer, Farm, Field, Seasonal Field, Boundary, Crop, Crop variety, Season and Attachment
- CreatedBy and ModifiedBy in all entities
- Measure renamed to measurements in Prescription & Crop
- Acreage renamed to area in Boundary
- Get Feature and Search Feature APIs for Sentinel 2 L2A and Sentinel 2 L1C STAC collections
- Adding Weather Data APIs to fetch IBM weather data

### Breaking Changes
- Removing primaryBoundaryId & boundaryIds from Field and Seasonal Field
- Removing isPrimary flag from Boundary
- Removing avgYields from Seasonal Field
- Renaming Farmer to Party
- Renaming CropVariety to CropProduct

## 1.0.0-beta.2 (2021-05-27)

- Update README with more service documentation.
- Update body parameter names (https://github.com/Azure/azure-rest-api-specs/pull/14408).

## 1.0.0-beta.1 (2021-05-11)

- Initial beta release for FarmBeats client library.
