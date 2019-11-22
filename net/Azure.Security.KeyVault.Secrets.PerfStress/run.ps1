#!/usr/bin/env pwsh

docker run -it --rm --network host -e KEYVAULT_URI -e AZURE_CLIENT_ID -e AZURE_CLIENT_SECRET -e AZURE_TENANT_ID azure-security-keyvault-secrets-perfstress-net @args
