#!/usr/bin/python

from downloaders import downloader_config, Downloaders

nodes = downloader_config["downloaders"]
ds = Downloaders()
i = 0
code_out_errs = ds.do("ps -C java -o command --no-headers")
for (code, out, err) in code_out_errs:
  node = nodes[i]
  if code == 0 and out.startswith("java ") and out.find("Downloader.jar") >= 0:
    print "Running: " + node.host + ": " + out
  else:
    print "Not running: " + node.host
    run_on_host = lambda h: h == node.host
    ds.run_downloaders(run_on_host)
  i += 1

