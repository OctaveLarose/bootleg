#!/bin/bash

AWFY_FILES="./input_data/disl_awfy_calltraces/*"
NBR_FAILS=0

for FILE in $AWFY_FILES; 
do
    echo "Running with CT file: $FILE"
    ./gradlew run --args="--ct-file $FILE"
    if [ $? -eq 1 ]; then ((NBR_FAILS++)); fi    
done

echo "---$NBR_FAILS total number of build/run fails---"
