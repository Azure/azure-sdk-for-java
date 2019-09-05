#!/bin/bash

docker run -it --rm --network host storagemock "$@"
