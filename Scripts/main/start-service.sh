#!/bin/bash

# Set BSS_DIR to the service directory.
_BSS_DIR=${BSS_DIR:="$HOME/bigsemantics-service"}

cd "$_BSS_DIR"

if [ -d "$_BSS_DIR" ]; then
  killall -9 java

  cd $_BSS_DIR/jetty-dist
  # By default, jetty runs on port 8080
  # You may want to map port 8080 to 80 in your system, e.g.:
  #   sudo iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080
  nohup java -server -jar start.jar 2>&1 > /dev/null &

  sleep 60

  cd $_BSS_DIR/downloader
  nohup java -server -Xms128m -Xmx256m -jar Downloader.jar 2>&1 >/dev/null &
else
  echo "Service directory not found: $_BSS_DIR"
fi

