#!/bin/bash

if [[ $EUID -ne 0 ]]; then
  echo "root permission required." 2>&1
	exit 1
fi

killall -9 java

cd /bigsemantics-service/jetty-dist
./start-service.sh
cd /bigsemantics-service/downloader-instance
./run-downloader-instance.sh

