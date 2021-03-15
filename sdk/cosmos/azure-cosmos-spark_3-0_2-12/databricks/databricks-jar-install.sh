#!/bin/bash

CLUSTER_NAME=$1
JARFILE=$2
[[ -z "$CLUSTER_NAME" ]] && exit 1
[[ -z "$JARFILE" ]] && exit 1

CLUSTER_ID=$(databricks clusters list --output json | jq -r --arg N "$CLUSTER_NAME" '.clusters[] | select(.cluster_name == $N) | .cluster_id')

databricks libraries uninstall --cluster-id $cluster_id --jar dbfs:/tmp/$dbfs_jar_name

dbfs cp $JARFILE dbfs:/tmp/$JARFILE
dbfs ls dbfs:/tmp

databricks libraries install --cluster-id $cluster_id --jar dbfs:/tmp/$dbfs_jar_name
