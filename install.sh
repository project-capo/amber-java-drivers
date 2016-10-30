#!/bin/bash

export __dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export __lib="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/location/lib

echo $__dir
echo $__lib

cd $__lib
make clean
make
cd $__dir
./protoc.sh
mvn clean install
