#!/bin/bash

set -eux

cd "$(dirname "$0")"

chmod +x install_java.sh

for i in $(seq 1 5)
do
    if [ $i -gt 1 ]
    then
        echo "Failed, retry after 5 seconds."
        sleep 5
    fi
    if ./install_java.sh
    then
        s=0
        break
    else
        s=$?
    fi
done

exit $s
