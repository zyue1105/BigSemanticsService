#!/usr/bin/python

import unittest
from bs_service_tester import ServiceTester

class TestServiceTester(unittest.TestCase):
  def test_normal_state(self):
    config = {}
    config["tests"] = [
      {
        "url": "http://example.com/",
        "include": "example include",
      },
    ]
    resp = (200, "This is an example include!", None)
    monitor = ServiceTester(config)
    monitor.access_and_download = lambda url: resp
    (code, fatal, non_fatal) = monitor.test_service()
    self.assertEqual(200, code)
    self.assertEqual(0, len(fatal))
    self.assertEqual(0, len(non_fatal))

  def test_connection_failure(self):
    config = {}
    config["tests"] = [
      {
        "url": "http://example.com/",
        "include": "example include",
      },
    ]
    resp = (-1, None, None)
    monitor = ServiceTester(config)
    monitor.access_and_download = lambda url: resp
    (code, fatal, non_fatal) = monitor.test_service()
    self.assertTrue(code < 0)
    self.assertTrue(fatal.find("Access error") >= 0)
    self.assertTrue(fatal.find("example.com") >= 0)

  def test_service_failure(self):
    config = {}
    config["tests"] = [
      {
        "url": "http://example.com/",
        "include": "example include",
      },
    ]
    resp = (500, "Service unavailable.", None)
    monitor = ServiceTester(config)
    monitor.access_and_download = lambda url: resp
    (code, fatal, non_fatal) = monitor.test_service()
    self.assertNotEqual(200, code)
    self.assertTrue(fatal.find("Service error") >= 0)
    self.assertTrue(fatal.find("example.com") >= 0)

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
    resp1 = (200, "This is an example incl", None)
    resp2 = (200, "This is an example include 2.", None)
    resp3 = (200, "something irrelevant", None)
    resps = { url1: resp1, url2: resp2, url3: resp3 }
    monitor = ServiceTester(config)
    monitor.access_and_download = lambda url: resps[url]
    (code, fatal, non_fatal) = monitor.test_service()
    self.assertEqual(0, len(fatal))
    self.assertTrue(non_fatal.find("Expected content not found") >= 0)
    self.assertTrue(non_fatal.find(url1) >= 0)
    self.assertTrue(non_fatal.find(url2) < 0)
    self.assertTrue(non_fatal.find(url3) >= 0)


if __name__ == '__main__':
  unittest.main()

