#!/usr/bin/python

import urllib2

class ServiceTester:
  def __init__(self, config):
    self.config = config
    self.tests = config["tests"]

  def test_service(self):
    code = -1
    fatal = ""
    non_fatal = ""

    for t in self.tests:
      url = t["url"]
      include = t["include"]

      (code, content, error) = self.access_and_download(url)
      if code < 0:
        error_msg = "(no error message available)"
        if error is not None:
          error = str(error)
        fatal = "Access error when trying {}:\n{}".format(url, error)
      elif code != 200:
        fatal = "Service error when trying {}, HTTP code: {}".format(url, code)
      else:
        if content.find(include) < 0:
          non_fatal +=\
            "Expected content not found for {}: {}\n".format(url, include)

    return (code, fatal, non_fatal)

  def access_and_download(self, url):
    f = None
    code = -1
    content = None
    error = None
    try:
      f = urllib2.urlopen(url)
      code = f.getcode()
      content = u'\n'.join(f.readlines())
    except Exception as e:
      code = -1
      error = e
    finally:
      if f is not None:
        f.close()
    return (code, content, error)

