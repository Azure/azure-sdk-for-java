#!/bin/bash

# Clone the azure rest api spec repo, to get the spec for Azure Search
mkdir azure-rest-api-spec
cd azure-rest-api-spec
git clone https://github.com/Azure/azure-rest-api-specs.git .

# Copy the directory  of intreset and clean up
cd ..
mkdir 2019-05-06
cp -r azure-rest-api-spec/specification/search/data-plane/Microsoft.Azure.Search.Data/stable/2019-05-06/* 2019-05-06/.
rm -rf azure-rest-api-spec

# Tweaking a bit the spec file
sed 's/SearchIndexClient/SearchIndexRestClient/g' 2019-05-06/searchindex.json > 2019-05-06/modifiedSearchIndex.json

# Generate
autorest --use=../../../autorest.java ./readme.md

# Cleanup
rm -rf 2019-05-06
