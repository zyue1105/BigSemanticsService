#!/bin/bash

killall -9 java

cd /home/ecologylab/semantic-service-jetty-dist
./start.sh
cd /home/ecologylab/OODSSDownloaderInstance
./run.sh

