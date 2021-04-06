#!/bin/bash

CLUSTER_NAME=$1
JARPATH=$2
[[ -z "$CLUSTER_NAME" ]] && exit 1
[[ -z "$JARPATH" ]] && exit 1

echo "Looking for cluster '$CLUSTER_NAME'"

CLUSTER_ID=$(databricks clusters list --output json | jq -r --arg N "$CLUSTER_NAME" '.clusters[] | select(.cluster_name == $N) | .cluster_id')

if [[ -z "$CLUSTER_ID" ]]
then
	echo "Cannot find a cluster named '$CLUSTER_NAME'"
	exit 1
fi

echo "Uninstalling libraries in $CLUSTER_ID"
LIBRARIES=$(databricks libraries list --cluster-id $CLUSTER_ID | jq -r '.library_statuses[] | .library.jar')
for library in $LIBRARIES
do
	echo "Uninstalling $library"
	databricks libraries uninstall --cluster-id $CLUSTER_ID --jar $library
done

bash sdk/cosmos/azure-cosmos-spark_3-1_2-12/test-databricks/databricks-cluster-restart.sh $CLUSTER_ID

echo "Deleting files in dbfs:/tmp/libraries"
dbfs rm --recursive dbfs:/tmp/libraries

for file in $JARPATH/*.jar
do
	filename=${file##*/}
	if [[ "$filename" != *"original"* && "$filename" != *"sources"* && "$filename" != *"javadoc"* ]]
	then
		JARFILE=$filename
	fi
done

if [[ -z "$JARFILE" ]]
then
	echo "Cannot find a Jar file name azure-cosmos-spark_3-1_2-12"
	exit 1
fi

echo "Copying files to DBFS $JARPATH/$JARFILE"
dbfs cp $JARPATH/$JARFILE dbfs:/tmp/libraries/$JARFILE --overwrite
dbfs ls dbfs:/tmp/libraries/

echo "Installing $JARFILE in $CLUSTER_ID"
databricks libraries install --cluster-id $CLUSTER_ID --jar dbfs:/tmp/libraries/$JARFILE

bash sdk/cosmos/azure-cosmos-spark_3-1_2-12/test-databricks/databricks-cluster-restart.sh $CLUSTER_ID
