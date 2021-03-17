#!/bin/bash

CLUSTER_ID=$1
[[ -z "$CLUSTER_ID" ]] && exit 1

export STATE=$(databricks clusters list --output json | jq -r --arg I "$CLUSTER_ID" '.clusters[] | select(.cluster_id == $I) | .state')
echo "Cluster $CLUSTER_ID is on state $STATE"
if [[ "$STATE" != "TERMINATED" ]]
then
	echo "Restarting cluster $CLUSTER_ID"
	databricks clusters restart --cluster-id $CLUSTER_ID
else
	echo "Starting cluster $CLUSTER_ID"
	databricks clusters start --cluster-id $CLUSTER_ID	
fi

while [[ "$STATE" != "PENDING" ]]
do
	export STATE=$(databricks clusters list --output json | jq -r --arg I "$CLUSTER_ID" '.clusters[] | select(.cluster_id == $I) | .state')
done

echo "Cluster $CLUSTER_ID is on state $STATE"
