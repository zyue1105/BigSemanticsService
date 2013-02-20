#!/bin/bash

if [[ $EUID -ne 0 ]]; then
  echo "root permission required." 2>&1
	exit 1
fi

LOGFILE=service.out
if [ -a $LOGFILE ]; then
  mv --backup=t $LOGFILE $LOGFILE.backup
fi
nohup java -server -jar start.jar 2>&1 > $LOGFILE &

