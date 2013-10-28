#!/bin/bash

killall java

LOGFILE=downloader.out
if [ -a $LOGFILE ]; then
  mv --backup=t $LOGFILE $LOGFILE.backup
fi
nohup java -server -Xms256m -Xmx512m -jar Downloader.jar > $LOGFILE 2>&1 &

