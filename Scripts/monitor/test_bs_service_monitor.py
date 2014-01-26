#!/usr/bin/python

import unittest
from bs_service_monitor import Monitor

class FakeMonitor(Monitor):
  def __init__(self, config):
    Monitor.__init__(self, config)
    self.subject = ""
    self.message = ""

  def send_notification(self, subject, message):
    self.subject = subject
    self.message = message

class TestMonitor(unittest.TestCase):
  def test_normal_state(self):
    config = {}
    config["tests"] = [
      {
        "url": "http://example.com/",
        "include": "example include",
      },
    ]
    resp = {}
    resp["code"] = 200
    resp["content"] = "This is an example include!"
    resp["error"] = None
    monitor = FakeMonitor(config)
    monitor.access_and_download = lambda url: resp
    monitor.test_bs_service()
    self.assertEqual(0, len(monitor.subject))
    self.assertEqual(0, len(monitor.message))
    pass

  def test_connection_failure(self):
    config = {}
    config["tests"] = [
      {
        "url": "http://example.com/",
        "include": "example include",
      },
    ]
    resp = {}
    resp["code"] = -1
    resp["content"] = None
    resp["error"] = None
    monitor = FakeMonitor(config)
    monitor.access_and_download = lambda url: resp
    monitor.test_bs_service()
    self.assertTrue(monitor.subject.find("connection failure") >= 0)
    self.assertTrue(monitor.message.find("example.com") >= 0)

  def test_service_failure(self):
    config = {}
    config["tests"] = [
      {
        "url": "http://example.com/",
        "include": "example include",
      },
    ]
    resp = {}
    resp["code"] = 500
    resp["content"] = "Service unavailable."
    resp["error"] = None
    monitor = FakeMonitor(config)
    monitor.access_and_download = lambda url: resp
    monitor.test_bs_service()
    self.assertTrue(monitor.subject.find("service failure") >= 0)
    self.assertTrue(monitor.message.find("example.com") >= 0)

  def test_include_error(self):
    url1 = "http://example.com"
    url2 = "http://example2.com"
    url3 = "http://example3.com"
    config = {}
    config["tests"] = [
      {
        "url": url1,
        "include": "example include 1",
      },
      {
        "url": url2,
        "include": "example include 2",
      },
      {
        "url": url3,
        "include": "example include 3",
      },
    ]
    resp1 = {}
    resp1["code"] = 200
    resp1["content"] = "This is an example incl"
    resp1["error"] = None
    resp2 = {}
    resp2["code"] = 200
    resp2["content"] = "This is an example include 2."
    resp2["error"] = None
    resp3 = {}
    resp3["code"] = 200
    resp3["content"] = "something irrelevant"
    resp3["error"] = None
    resps = { url1: resp1, url2: resp2, url3: resp3 }
    monitor = FakeMonitor(config)
    monitor.access_and_download = lambda url: resps[url]
    monitor.test_bs_service()
    self.assertTrue(monitor.subject.find("service issue(s)") >= 0)
    self.assertTrue(monitor.message.find(url1) >= 0)
    self.assertTrue(monitor.message.find(url2) < 0)
    self.assertTrue(monitor.message.find(url3) >= 0)


if __name__ == '__main__':
  unittest.main()

