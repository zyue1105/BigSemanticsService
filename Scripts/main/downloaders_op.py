#!/usr/bin/python

import sys
import os
import csv

def read_node_data(file_name):
  with open(file_name, 'r') as nodes_csv:
    reader = csv.reader(nodes_csv)
    for row in reader:
      if len(row) == 4:
        yield { 'identity': row[0],
                'user': row[1],
                'host': row[2],
                'port': row[3] }

def print_run(node, cmd):
  print "ssh -n -f -i {0} -p {3} {1}@{2} \"{4}\"".format(
            node['identity'],
            node['user'],
            node['host'],
            node['port'],
            cmd)

def print_copy_to(node, src, dest):
  ''' src is local, dest is remote and relative to home folder. '''
  print ("scp -i {0} -P {3} {4} {1}@{2}:/home/{1}/{5}").format(
            node['identity'],
            node['user'],
            node['host'],
            node['port'],
            src,
            dest)

def print_copy_from(node, src, dest):
  ''' src is remote and relative to home folder, dest is local. '''
  print ("scp -i {0} -P {3} {1}@{2}:/home/{1}/{4} {5}").format(
            node['identity'],
            node['user'],
            node['host'],
            node['port'],
            src,
            dest)

def setup(file_name):
  nodes = read_node_data(file_name)
  for node in nodes:
    print_run(node, "mkdir downloader; mkdir setup")
    print_run(node, "curl http://ecology-service.cse.tamu.edu/jre-7u45-linux-i586.rpm > setup/jre-7u45-linux-i586.rpm")
    print_run(node, "sudo rpm -ivh setup/jre-7u45-linux-i586.rpm")

def update_downloaders(file_name):
  cwd = os.getcwd()
  print "cd ../../DownloaderPool"
  print "ant clean"
  print "ant downloader-jar"
  print "cd " + cwd

  nodes = read_node_data(file_name)
  for node in nodes:
    print_copy_to(node,
                  "../../DownloaderPool/build/Downloader.jar",
                  "/downloader/Downloader.jar")
    print_copy_to(node, "dpool.properties", "/downloader/dpool.properties")
    print_copy_to(node, "run-downloader.sh", "/downloader/run-downloader.sh")

def run_downloaders(file_name):
  nodes = read_node_data(file_name)
  for node in nodes:
    print_run(node, "cd downloader; ./run-downloader.sh")

def collect_logs(file_name):
  nodes = read_node_data(file_name)
  for node in nodes:
    print_copy_from(node,
                    "/downloader/downloader.log",
                    node['host'] + "--downloader.log")

def stop_downloaders(file_name):
  nodes = read_node_data(file_name)
  for node in nodes:
    print_run(node, "killall java");

if __name__ == '__main__':
  if len(sys.argv) < 3:
    sys.stderr.write("usage: {0} <op> <nodes_csv_file>\n".format(sys.argv[0]))
    sys.stderr.write("  <op> can be setup, update, run, log, or stop.\n")
  else:
    op = sys.argv[1]
    nodes_csv_file_name = sys.argv[2]
    if op == 'setup':
      setup(nodes_csv_file_name)
    elif op == 'update':
      update_downloaders(nodes_csv_file_name)
    elif op == 'run':
      run_downloaders(nodes_csv_file_name)
    elif op == 'log':
      collect_logs(nodes_csv_file_name)
    elif op == 'stop':
      stop_downloaders(nodes_csv_file_name)
    else:
      sys.stderr.write("unknown op: " + op)

