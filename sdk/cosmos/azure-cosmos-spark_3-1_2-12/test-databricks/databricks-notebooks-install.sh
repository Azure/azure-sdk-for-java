#!/bin/bash

CLUSTER_NAME=$1
NOTEBOOKSFOLDER=$2
COSMOSENDPOINT=$3
COSMOSKEY=$4
[[ -z "$CLUSTER_NAME" ]] && exit 1
[[ -z "$NOTEBOOKSFOLDER" ]] && exit 1
[[ -z "$COSMOSENDPOINT" ]] && exit 1
[[ -z "$COSMOSKEY" ]] && exit 1

CLUSTER_ID=$(databricks clusters list --output json | jq -r --arg N "$CLUSTER_NAME" '.clusters[] | select(.cluster_name == $N) | .cluster_id')

echo "Deleting existing notebooks"
databricks workspace rm --recursive /notebooks

echo "Importing notebooks from $NOTEBOOKSFOLDER"
databricks workspace import_dir $NOTEBOOKSFOLDER /notebooks

NOTEBOOKS=$(databricks workspace ls /notebooks)
for f in $NOTEBOOKS
do
	echo "Creating job for $f"
	JOB_ID=$(databricks jobs create --json "{\"existing_cluster_id\": \"$CLUSTER_ID\", \"name\": \"$f\",\"notebook_task\": { \"notebook_path\": \"/notebooks/$f\" }}" | jq -r '.job_id')
  
	if [[ -z "$JOB_ID" ]]
	then
		echo "Could not create job"
		exit 1
	fi
  
	echo "Creating run for job $JOB_ID"
	RUN_ID=$(databricks jobs run-now --job-id $JOB_ID --notebook-params "{\"cosmosEndpoint\": \"$COSMOSENDPOINT\",\"cosmosMasterKey\": \"$COSMOSKEY\"}" | jq -r '.run_id')

	if [[ -z "$RUN_ID" ]]
	then
		echo "Could not run job"
		databricks jobs delete --job-id $JOB_ID
		exit 1
	fi

	JOB_STATE=$(databricks runs get --run-id $RUN_ID | jq -r '.state.life_cycle_state')

	if [[ -z "$JOB_STATE" ]]
	then
		echo "Could not find state for job $JOB_ID"
		databricks jobs delete --job-id $JOB_ID
		exit 1
	fi
	
	echo "Run $RUN_ID is on state '$JOB_STATE'"
	while [[ "$JOB_STATE" != "INTERNAL_ERROR" && "$JOB_STATE" != "TERMINATED" ]]
	do
		echo "Run $RUN_ID is on state '$JOB_STATE'"
		JOB_STATE=$(databricks runs get --run-id $RUN_ID | jq -r '.state.life_cycle_state')
		sleep 10
	done
	
	JOB_MESSAGE=$(databricks runs get --run-id $RUN_ID | jq -r '.state.state_message')
  
	if [[ "$JOB_STATE" != "TERMINATED" ]]
	then
		echo "Run $RUN_ID failed with state $JOB_STATE and $JOB_MESSAGE"
		databricks jobs delete --job-id $JOB_ID
		exit 1
	fi
	
	JOB_RESULT=$(databricks runs get --run-id $RUN_ID | jq -r '.state.result_state')
	
	echo "Run $RUN_ID finished with state $JOB_STATE and result $JOB_RESULT with message $JOB_MESSAGE"
	if [[ "$JOB_RESULT" != "SUCCESS" ]]
	then
		echo "Run $RUN_ID in job $JOB_ID failed, leaving the job available for log troubleshooting in databricks"
		exit 1
	fi
	
	databricks jobs delete --job-id $JOB_ID
done