#!/bin/bash

if [ -a logfile ]; then
  mv --backup=t logfile logfile.backup
fi
nohup java -server -Xms1024m -Xmx4096m -cp .:./simplCore.jar:./simplSunSpecifics.jar:./BigSemanticsCore.jar:./BigSemanticsSunSpecifics.jar ecologylab.bigsemantics.downloaders.oodss.OODSSDownloaderInstance 2>&1 > logfile &

