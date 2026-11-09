# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Copy to Set-TestEnvironment.local.ps1, edit, and run in the same PowerShell terminal as Maven.
# Authenticate separately with az login. No API keys or managed identity on your computer are needed.
$env:AZURE_TEST_MODE = 'PLAYBACK' # Change to RECORD or LIVE as needed.
$env:SEARCH_SERVICE_ENDPOINT = 'https://<search-service>.search.windows.net'

# Uncomment only settings used by your selected tests.

# Azure OpenAI knowledge retrieval:
# $env:SEARCH_OPENAI_ENDPOINT = 'https://<openai-resource>.openai.azure.com'
# $env:SEARCH_OPENAI_DEPLOYMENT_NAME = '<chat-deployment>'
# $env:SEARCH_OPENAI_MODEL_NAME = 'gpt-4.1-nano'

# Embeddings (also uses SEARCH_OPENAI_ENDPOINT):
# $env:SEARCH_OPENAI_EMBEDDING_DEPLOYMENT_NAME = '<embedding-deployment>'
# $env:SEARCH_OPENAI_EMBEDDING_MODEL_NAME = 'text-embedding-3-large'

# Blob-backed tests:
# $env:SEARCH_STORAGE_ACCOUNT_NAME = '<storage-account>'
# $env:SEARCH_STORAGE_CONTAINER_NAME = '<container>'
# $env:SEARCH_SUBSCRIPTION_ID = '<storage-subscription-id>'
# $env:SEARCH_RESOURCE_GROUP = '<storage-resource-group>'

# AI Services extraction and Content Understanding:
# $env:SEARCH_AI_SERVICES_ENDPOINT = 'https://<ai-resource>.cognitiveservices.azure.com'
