#!/bin/bash

CLUSTER_ID="test"

echo "Deleting existing notebooks"


echo "Importing notebooks from $NOTEBOOKSFOLDER"

f="fff"
echo "Creating job for $f"
	JOB_ID="JOBID"
  
	if [[ -z "$JOB_ID" ]]
	then
		echo "Could not create job"
		exit 1
	fi
  
	echo "Creating run for job $JOB_ID"
	RUN_ID="RUNID"

	if [[ -z "$RUN_ID" ]]
	then
		echo "Could not run job"
		databricks jobs delete --job-id $JOB_ID
		exit 1
	fi

	JOB_STATE="TEST"

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
		JOB_STATE="TERMINATED"
		sleep 10
	done
	
	JOB_MESSAGE="MESSAGE"
  
	if [[ "$JOB_STATE" != "TERMINATED" ]]
	then
		echo "Run $RUN_ID failed with state $JOB_STATE and $JOB_MESSAGE"
		exit 1
	fi
	
	JOB_RESULT="RESULT"
	echo "Run $RUN_ID finished with state $JOB_STATE and result $JOB_RESULT with message $JOB_MESSAGE"