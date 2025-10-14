#!/bin/bash

CLUSTER_NAME=$1
NOTEBOOKSFOLDER=$2
COSMOSENDPOINT=$3
COSMOSKEY=$4
SUBSCRIPTIONID=$5
TENANTID=$6
RESOURCEGROUPNAME=$7
CLIENTID=$8
CLIENTSECRET=$9
COSMOSCONTAINERNAME=${10}
COSMOSDATABASENAME=${11}
[[ -z "$CLUSTER_NAME" ]] && exit 1
[[ -z "$NOTEBOOKSFOLDER" ]] && exit 1
[[ -z "$COSMOSENDPOINT" ]] && exit 1
[[ -z "$COSMOSKEY" ]] && exit 1
[[ -z "$SUBSCRIPTIONID" ]] && exit 1
[[ -z "$TENANTID" ]] && exit 1
[[ -z "$RESOURCEGROUPNAME" ]] && exit 1
[[ -z "$CLIENTID" ]] && exit 1
[[ -z "$CLIENTSECRET" ]] && exit 1
[[ -z "$COSMOSCONTAINERNAME" ]] && exit 1
[[ -z "$COSMOSDATABASENAME" ]] && exit 1

CLUSTER_ID=$(databricks clusters list --output json | jq -r --arg N "$CLUSTER_NAME" '.[] | select(.cluster_name == $N) | .cluster_id')

echo "Deleting existing notebooks"
if databricks workspace list / | grep -q notebooks; then
  databricks workspace delete --recursive /notebooks
fi

echo "Importing notebooks from $NOTEBOOKSFOLDER"
databricks workspace import-dir "$NOTEBOOKSFOLDER" /notebooks

echo "Validating Notebooks in workspace"
databricks workspace list /notebooks

NOTEBOOKS=$(databricks workspace list /notebooks)
for f in $NOTEBOOKS
do
	echo "Creating job for $f"
	JOB_ID=$(databricks jobs create --json "{\"name\": \"$f\", \"tasks\": [{\"task_key\": \"${f}_task\", \"existing_cluster_id\": \"$CLUSTER_ID\", \"notebook_task\": { \"notebook_path\": \"/notebooks/$f\" }}]}" | jq -r '.job_id')

	if [[ -z "$JOB_ID" ]]
	then
		echo "Could not create job"
		exit 1
	fi

	echo "Creating run for job $JOB_ID"
	DBG_JSON="\"{\\\"job_id\\\": $JOB_ID, \\\"notebook_params\\\": {\\\"cosmosEndpoint\\\": \\\"$COSMOSENDPOINT\\\",\\\"cosmosMasterKey\\\": \\\"$COSMOSKEY\\\",\\\"subscriptionId\\\": \\\"$SUBSCRIPTIONID\\\",\\\"tenantId\\\": \\\"$TENANTID\\\",\\\"resourceGroupName\\\": \\\"$RESOURCEGROUPNAME\\\",\\\"clientId\\\": \\\"$CLIENTID\\\",\\\"clientSecret\\\": \\\"$CLIENTSECRET\\\",\\\"cosmosContainerName\\\": \\\"$COSMOSCONTAINERNAME\\\", \\\"cosmosDatabaseName\\\": \\\"$COSMOSDATABASENAME\\\"}}\""
	echo "DBG_JSON: $DBG_JSON"
	RUN_ID=$(databricks jobs run-now --json "$DBG_JSON" | jq -r '.run_id')

	if [[ -z "$RUN_ID" ]]
	then
		echo "Could not run job $JOB_ID"
		databricks jobs delete $JOB_ID
		exit 1
	fi

	JOB_STATE=$(databricks jobs get-run $RUN_ID | jq -r '.state.life_cycle_state')

	if [[ -z "$JOB_STATE" ]]
	then
		echo "Could not find state for job $JOB_ID"
		databricks jobs delete $JOB_ID
		exit 1
	fi

	echo "Run $RUN_ID is on state '$JOB_STATE'"
	while [[ "$JOB_STATE" != "INTERNAL_ERROR" && "$JOB_STATE" != "TERMINATED" ]]
	do
		echo "Run $RUN_ID is on state '$JOB_STATE'"
		JOB_STATE=$(databricks jobs get-run $RUN_ID | jq -r '.state.life_cycle_state')
		sleep 10
	done

	JOB_MESSAGE=$(databricks jobs get-run $RUN_ID | jq -r '.state.state_message')

	if [[ "$JOB_STATE" != "TERMINATED" ]]
	then
		echo "Run $RUN_ID failed with state $JOB_STATE and $JOB_MESSAGE"
		databricks jobs delete $JOB_ID
		exit 1
	fi

	JOB_RESULT=$(databricks jobs get-run $RUN_ID | jq -r '.state.result_state')

	echo "Run $RUN_ID finished with state $JOB_STATE and result $JOB_RESULT with message $JOB_MESSAGE"
	if [[ "$JOB_RESULT" != "SUCCESS" ]]
	then
		echo "Run $RUN_ID in job $JOB_ID failed, leaving the job available for log troubleshooting in databricks"
		exit 1
	fi

	databricks jobs delete $JOB_ID
done
