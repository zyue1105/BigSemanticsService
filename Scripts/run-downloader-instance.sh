#!/bin/bash

if [[ $EUID -ne 0 ]]; then
  echo "root permission required." 2>&1
	exit 1
fi

LOGFILE=downloader-instance.out
if [ -a $LOGFILE ]; then
  mv --backup=t $LOGFILE $LOGFILE.backup
fi
nohup java -server -Xms1024m -Xmx4096m -cp .:./simplCore.jar:./simplSunSpecifics.jar:./BigSemanticsCore.jar:./BigSemanticsSunSpecifics.jar ecologylab.bigsemantics.downloaders.oodss.OODSSDownloaderInstance 2>&1 > $LOGFILE &

