#!/bin/bash

command -v protoc >/dev/null 2>&1 || { echo >&2 "I require protoc but it's not installed. Aborting."; exit; }

export script_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set -x

pushd ${script_directory}
	mkdir -p common/src/generated-sources/java
	cd ./common/src/main/resources/protobuf
	protoc drivermsg.proto --java_out=./../../../../../common/src/generated-sources/java
	cd ./../../../../../
	
	for submodule in $(ls -d "dummy" "pid-follow-trajectory" "location"); do
		mkdir -p ${submodule}/src/generated-sources/java
		for proto_file in $(find ${submodule}/src/main/resources/protobuf -name "*.proto"); do
			protoc --proto_path=./common/src/main/resources/protobuf --proto_path=${submodule}/src/main/resources/protobuf/ ${proto_file} --java_out=${submodule}/src/generated-sources/java
		done
	done
popd
