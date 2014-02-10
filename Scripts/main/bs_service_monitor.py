#!/usr/bin/python

from bs_service_tester import ServiceTester
from bot_email import send_bot_email_to_maintainers

service_tester = ServiceTester()

def test_service():
  (code, fatal, non_fatal) = service_tester.test_service()

  if code < 0:
    send_bot_email_to_maintainers("BigSemantics service connection failure",
                                  fatal)
  elif code != 200:
    send_bot_email_to_maintainers("BigSemantics service failure", fatal)
  else:
    if len(non_fatal) > 0:
      send_bot_email_to_maintainers("BigSemantics service issue(s)", non_fatal)
    else:
      print "BS service seems to be working fine."



if __name__ == '__main__':
  test_service()

