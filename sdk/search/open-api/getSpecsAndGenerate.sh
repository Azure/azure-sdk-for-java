#!/bin/bash

AUTOREST_JAVA_DIR=$1

# Clone the azure rest api spec repo, to get the spec for Azure Search
mkdir azure-rest-api-spec
cd azure-rest-api-spec
git clone https://github.com/Azure/azure-rest-api-specs.git .

# Copy the directories of interest and clean up
cd ..
mkdir 2019-05-06
mkdir 2019-05-06/data
mkdir 2019-05-06/service

cp -r azure-rest-api-spec/specification/search/data-plane/Microsoft.Azure.Search.Data/stable/2019-05-06/* 2019-05-06/data/.
cp -r azure-rest-api-spec/specification/search/data-plane//Microsoft.Azure.Search.Service/stable/2019-05-06/* 2019-05-06/service/.
rm -rf azure-rest-api-spec

# Generate the data api
autorest --use=$AUTOREST_JAVA_DIR ./data-readme.md

# Generate the service api
# Todo: Uncomment when working on the Service API
# autorest --input-file=2019-05-06/service/searchservice.json --output-folder=../azure-search-service --namespace=com.azure.search.service --azure-arm=true --add-credentials=true --java

# Cleanup
rm -rf 2019-05-06
