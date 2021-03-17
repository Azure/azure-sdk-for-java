#!/bin/bash

CLUSTER_NAME=$1
JARPATH=$2
JARFILE=$3
[[ -z "$CLUSTER_NAME" ]] && exit 1
[[ -z "$JARFILE" ]] && exit 1
[[ -z "$JARFILE" ]] && exit 1

echo "Looking for cluster '$CLUSTER_NAME'"

export CLUSTER_ID=$(databricks clusters list --output json | jq -r --arg N "$CLUSTER_NAME" '.clusters[] | select(.cluster_name == $N) | .cluster_id')

echo "Uninstalling previous $JARFILE in $CLUSTER_ID"
databricks libraries uninstall --cluster-id $CLUSTER_ID --jar dbfs:/tmp/sparkconnector/$JARFILE

bash sdk/cosmos/azure-cosmos-spark_3-0_2-12/databricks/databricks-cluster-restart.sh $CLUSTER_ID

echo "Copying files to DBFS $JARPATH/$JARFILE"
dbfs cp --overwrite $JARPATH/$JARFILE dbfs:/tmp/sparkconnector/$JARFILE
dbfs ls dbfs:/tmp/sparkconnector

echo "Installing $JARFILE in $CLUSTER_ID"
databricks libraries install --cluster-id $CLUSTER_ID --jar dbfs:/tmp/sparkconnector/$JARFILE

bash sdk/cosmos/azure-cosmos-spark_3-0_2-12/databricks/databricks-cluster-restart.sh $CLUSTER_ID
