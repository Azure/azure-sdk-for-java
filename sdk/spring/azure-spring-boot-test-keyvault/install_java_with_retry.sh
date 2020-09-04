#!/bin/bash

cd "$(dirname "$0")"

for i in $(seq 1 5); do
    [ $i -gt 1 ] && sleep 15;
    command && s=0 && break || s=$?;
done;

(exit $s)
