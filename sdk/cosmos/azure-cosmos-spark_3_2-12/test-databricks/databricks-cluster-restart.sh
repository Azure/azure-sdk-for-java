#!/bin/bash

CLUSTER_ID=$1
[[ -z "$CLUSTER_ID" ]] && exit 1

STATE=$(databricks clusters list --output json | jq -r --arg I "$CLUSTER_ID" '.clusters[] | select(.cluster_id == $I) | .state')
echo "Cluster $CLUSTER_ID is on state $STATE"
if [[ "$STATE" != "TERMINATED" ]]
then
	while [[ "$STATE" != "RUNNING" ]]
	do
		echo "Waiting until cluster $CLUSTER_ID is running, now on state $STATE"
		STATE=$(databricks clusters list --output json | jq -r --arg I "$CLUSTER_ID" '.clusters[] | select(.cluster_id == $I) | .state')	
		sleep 10
	done

	echo "Restarting cluster $CLUSTER_ID"
	databricks clusters restart --cluster-id $CLUSTER_ID
else
	echo "Starting cluster $CLUSTER_ID"
	databricks clusters start --cluster-id $CLUSTER_ID	
fi

STATE=$(databricks clusters list --output json | jq -r --arg I "$CLUSTER_ID" '.clusters[] | select(.cluster_id == $I) | .state')
while [[ "$STATE" != "RUNNING" ]]
do
	echo "Waiting until cluster $CLUSTER_ID is running, now on state $STATE"
	STATE=$(databricks clusters list --output json | jq -r --arg I "$CLUSTER_ID" '.clusters[] | select(.cluster_id == $I) | .state')	
	sleep 10
done

echo "Cluster $CLUSTER_ID is on state $STATE"
