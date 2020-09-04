#!/bin/bash

cd "$(dirname "$0")"

until ./install_java.sh
do
    echo 'install failed, retry after 3 seconds.'
    sleep 3
done
