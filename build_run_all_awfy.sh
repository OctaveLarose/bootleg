#!/bin/bash

AWFY_CT_DIR="./input_data/disl_awfy_calltraces"
AWFY_OP_DIR="./input_data/disl_awfy_operations"
NBR_FAILS=0

for FILE in $AWFY_CT_DIR/*.txt;
do
    echo "Running with CT file: $FILE"
    CT_NAME=`echo $FILE | sed -e 's/.*calltrace_\(.*\)\.txt/\1/'`
    ./gradlew run --args="--ct-file $FILE --op-file $AWFY_OP_DIR/operations_$CT_NAME.txt"
    if [ $? -eq 1 ]; then ((NBR_FAILS++)); fi    
done

echo "---$NBR_FAILS total number of build/run fails (out of `ls $AWFY_CT_DIR | wc -l` files)---"
