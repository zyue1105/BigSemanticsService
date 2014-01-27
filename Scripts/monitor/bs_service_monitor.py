#!/usr/bin/python

from bs_service_tester import ServiceTester
import smtplib
from email.mime.text import MIMEText

class ServiceMonitor:
  def __init__(self, config):
    self.config = config
    self.service_tester = ServiceTester(config)

  def test_service(self):
    (code, fatal, non_fatal) = self.service_tester.test_service()

    if code < 0:
      self.send_notification("BigSemantics service connection failure", fatal)
    elif code != 200:
      self.send_notification("BigSemantics service failure", fatal)
    else:
      if len(non_fatal) > 0:
        self.send_notification("BigSemantics service issue(s)", non_fatal)
      else:
        print "BS service seems to be working fine."

  def send_notification(subject, message):
    login = self.config['bot_email_login']
    passwd = self.config['bot_email_passwd']
    receiver = "bigsemantics@ecologylab.net"

    mail_text = MIMEText(message)
    mail_text['Subject'] = subject
    mail_text['From'] = login
    mail_text['To'] = receiver

    smtp_server = smtplib.SMTP('smtp.gmail.com', 587)
    smtp_server.starttls()
    smtp_server.login(login, passwd)
    problems = smtp_server.sendmail(login, [receiver], mail_text.as_string())
    quit_status = smtp_server.quit()
    return (quit_status, problems)



if __name__ == '__main__':
  config = {}
  execfile('bot.conf', config)
  monitor = ServiceMonitor(config)
  monitor.test_service()

