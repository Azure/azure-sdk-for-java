#!/usr/bin/env pwsh

docker run -it --rm --network host -e STORAGE_CONNECTION_STRING azure-storage-blob-perfstress-java @args
