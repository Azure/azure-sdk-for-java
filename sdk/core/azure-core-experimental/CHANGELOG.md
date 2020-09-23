# Release History

## 1.0.0-beta.5 (Unreleased)

- Added `JsonPatchDocument` to support JSON Patch functionality.

## 1.0.0-beta.4 (2020-09-08)

- Updated `azure-core` version to pickup bug fix.

## 1.0.0-beta.3 (2020-09-03)

- Added `AvroSerializer` interface containing Avro specific serializer APIs.
- Added `AvroSerializerProvider` interface as a service provider for `AvroSerializer`.

## 1.0.0-beta.2 (2020-08-07)

- Moved `ObjectSerializer` and some implementation of `JsonSerializer` into `azure-core`.
- Created sub-interface of `JsonSerializer` in `azure-core` to include APIs that weren't moved.

## 1.0.0-beta.1 (2020-07-02)

- Added `ObjectSerializer` interface containing generic serializer APIs.
- Added `JsonSerializer` interface containing JSON specific serializer APIs.
- Added `JsonNode`, and subclasses, which are strongly type representations of a JSON tree.
- Added GeoJSON classes and serializers.
