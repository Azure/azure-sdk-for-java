# Release History

## 1.0.0 (2024-05-03)

- GA release

### Breaking Changes

- Client Changes
    - POST call replaced with PUT
- Request changes:
    - Renamed createdDateTime into createdAt
    - Patients - Info renamed into Patients - Details
    - Unique ID required to be added in the request parameters
- Response changes:
    - "Datetime" field on FollowupCommunication renamed into "createdAt" field
    - Renamed createdDateTime into createdAt
    - Renamed expirationDateTime into expiresAt
    - Renamed lastUpdateDateTime into updatedAt

## 1.0.0-beta.1 (2024-02-15)

- Initial preview of the Azure Health Insights Radiology Insights client library.

### Features Added
* Radiology Insights API: Scans radiology reports as text to provide quality checks as feedback on errors and inconsistencies (mismatches). Critical findings are identified and communicated using the full context of the report. Follow-up recommendations and clinical findings with measurements (sizes) documented by the radiologist are also identified.
