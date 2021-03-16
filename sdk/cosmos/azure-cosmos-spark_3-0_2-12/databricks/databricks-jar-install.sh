#!/bin/bash

CLUSTER_NAME=$1
JARPATH=$2
JARFILE=$3
[[ -z "$CLUSTER_NAME" ]] && exit 1
[[ -z "$JARFILE" ]] && exit 1
[[ -z "$JARFILE" ]] && exit 1

CLUSTER_ID=$(databricks clusters list --output json | jq -r --arg N "$CLUSTER_NAME" '.clusters[] | select(.cluster_name == $N) | .cluster_id')

databricks libraries uninstall --cluster-id $cluster_id --jar dbfs:/tmp/sparkconnector/$JARFILE

dbfs cp $JARPATH/$JARFILE dbfs:/tmp/sparkconnector
dbfs ls dbfs:/tmp/sparkconnector

databricks libraries install --cluster-id $cluster_id --jar dbfs:/tmp/sparkconnector/$JARFILE
