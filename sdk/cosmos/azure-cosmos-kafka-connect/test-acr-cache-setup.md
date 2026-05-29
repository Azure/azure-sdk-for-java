# ACR Cache Setup for Kafka Connect TestContainers

This document describes how to set up Azure Container Registry (ACR) Cache to proxy Docker Hub
images used by the Kafka Connect e2e tests. This avoids Docker Hub rate limiting and connectivity
issues in CI pipelines.

## Background

The Kafka Connect e2e tests use [TestContainers](https://www.testcontainers.org/) to spin up
Kafka, Schema Registry, and Kafka Connect containers. TestContainers also pulls internal
infrastructure images (`testcontainers/ryuk` for cleanup, `alpine` for health checks).

All images are pulled from Docker Hub by default, which is subject to rate limits (100 pulls/6hr
for anonymous, 200 pulls/6hr for free authenticated). In CI pipelines, this causes intermittent
test failures.

## Solution: ACR Cache

[ACR Cache](https://learn.microsoft.com/azure/container-registry/container-registry-artifact-cache)
creates transparent cache rules in your own Azure Container Registry. When an image is requested,
ACR checks its cache first and only pulls from Docker Hub on cache miss.

### Required Images

The following Docker Hub images need ACR Cache rules:

| Image | Used By | Purpose |
|-------|---------|---------|
| `testcontainers/ryuk:0.12.0` | TestContainers (internal) | Resource cleanup sidecar |
| `alpine:3.17` | TestContainers (internal) | Docker health check |
| `confluentinc/cp-kafka:7.6.0` | Kafka e2e tests | Kafka broker |
| `confluentinc/cp-schema-registry:7.6.0` | Kafka e2e tests | Schema Registry |
| `confluentinc/cp-kafka-connect:7.6.0` | Kafka e2e tests | Kafka Connect |

### Step 1: Create an ACR (if needed)

```bash
ACR_NAME="<your-acr-name>"
RESOURCE_GROUP="<your-resource-group>"

az acr create \
  --name $ACR_NAME \
  --resource-group $RESOURCE_GROUP \
  --sku Standard
```

### Step 2: (Optional) Create Docker Hub Credential Set

For higher rate limits (5000 pulls/6hr), create a credential set with Docker Hub authentication.
Store your Docker Hub credentials in Azure Key Vault first.

```bash
# Create credential set for Docker Hub
az acr credential-set create \
  --registry $ACR_NAME \
  --name dockerhub-credentials \
  --login-server docker.io \
  --username-id <key-vault-secret-uri-for-username> \
  --password-id <key-vault-secret-uri-for-password>
```

Skip this step if anonymous Docker Hub access is sufficient for your workload.

### Step 3: Create ACR Cache Rules

Create a cache rule for each image. If using authenticated Docker Hub, add
`--cred-set dockerhub-credentials` to each command.

```bash
# testcontainers/ryuk
az acr cache create \
  --registry $ACR_NAME \
  --name testcontainers-ryuk \
  --source-repo docker.io/testcontainers/ryuk \
  --target-repo testcontainers/ryuk

# alpine (official image, note the library/ prefix for source)
az acr cache create \
  --registry $ACR_NAME \
  --name alpine \
  --source-repo docker.io/library/alpine \
  --target-repo library/alpine

# confluentinc/cp-kafka
az acr cache create \
  --registry $ACR_NAME \
  --name confluentinc-cp-kafka \
  --source-repo docker.io/confluentinc/cp-kafka \
  --target-repo confluentinc/cp-kafka

# confluentinc/cp-schema-registry
az acr cache create \
  --registry $ACR_NAME \
  --name confluentinc-cp-schema-registry \
  --source-repo docker.io/confluentinc/cp-schema-registry \
  --target-repo confluentinc/cp-schema-registry

# confluentinc/cp-kafka-connect
az acr cache create \
  --registry $ACR_NAME \
  --name confluentinc-cp-kafka-connect \
  --source-repo docker.io/confluentinc/cp-kafka-connect \
  --target-repo confluentinc/cp-kafka-connect
```

### Step 4: Enable Anonymous Pull (recommended for CI)

Enable anonymous pull so CI agents can pull without `docker login`:

```bash
az acr update --name $ACR_NAME --anonymous-pull-enabled true
```

Alternatively, configure Docker login credentials as pipeline variables (see below).

### Step 5: Configure Pipeline Variables

Set the following pipeline variables in the Azure DevOps pipeline definition or via a variable
group:

| Variable | Required | Example | Description |
|----------|----------|---------|-------------|
| `TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX` | Yes | `myacr.azurecr.io/` | Prefix for all Docker Hub images. **Must end with `/`**. |
| `TESTCONTAINERS_DOCKER_REGISTRY_URL` | No | `myacr.azurecr.io` | Registry URL for `docker login`. Only needed if anonymous pull is disabled. |
| `TESTCONTAINERS_DOCKER_REGISTRY_USERNAME` | No | `<service-principal-id>` | Username for `docker login`. Only needed if anonymous pull is disabled. |
| `TESTCONTAINERS_DOCKER_REGISTRY_PASSWORD` | No | `<service-principal-secret>` | Password for `docker login` (mark as secret). Only needed if anonymous pull is disabled. |

### How It Works

TestContainers supports the `TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX` environment variable via its
built-in `PrefixingImageNameSubstitutor`. When set, it prepends the prefix to all Docker Hub
image references (images without an explicit registry).

For example, with `TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX=myacr.azurecr.io/`:
- `testcontainers/ryuk:0.12.0` → `myacr.azurecr.io/testcontainers/ryuk:0.12.0`
- `alpine:3.17` → `myacr.azurecr.io/alpine:3.17`
- `confluentinc/cp-kafka:7.6.0` → `myacr.azurecr.io/confluentinc/cp-kafka:7.6.0`

The ACR Cache transparently proxies these requests to Docker Hub and caches the result.

### Local Development

For local development, images pull from Docker Hub by default. To use ACR Cache locally:

```bash
export TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX="myacr.azurecr.io/"
az acr login --name myacr
# Then run tests as usual
```

## Verification

To verify the setup, check that the cache rules exist:

```bash
az acr cache list --registry $ACR_NAME -o table
```

To verify an image is cached:

```bash
az acr repository list --name $ACR_NAME -o table
```

After a test run, the cached images should appear in the ACR repository list.
