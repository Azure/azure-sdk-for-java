#!/bin/bash

CLUSTER_NAME=$1
NOTEBOOKSFOLDER=$2
[[ -z "$CLUSTER_NAME" ]] && exit 1
[[ -z "$NOTEBOOKSFOLDER" ]] && exit 1

CLUSTER_ID=$(databricks clusters list --output json | jq -r --arg N "$CLUSTER_NAME" '.clusters[] | select(.cluster_name == $N) | .cluster_id')

databricks workspace import_dir --overwrite $NOTEBOOKSFOLDER /notebooks