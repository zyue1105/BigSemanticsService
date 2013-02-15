#!/bin/bash

# to be executed as sudo ./start.sh

if [ -a logfile ]; then
  mv --backup=t logfile logfile.backup
fi
nohup java -server -jar start.jar 2>&1 > logfile &

