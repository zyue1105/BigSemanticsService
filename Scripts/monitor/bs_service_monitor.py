#!/usr/bin/python

import urllib2
import smtplib
from email.mime.text import MIMEText

class Monitor:
  def __init__(self, config):
    self.config = config
    self.tests = config["tests"]

  def test_bs_service(self):
    msg_body = ""

    for t in self.tests:
      url = t["url"]
      include = t["include"]

      resp = self.access_and_download(url)
      code = resp["code"]
      content = resp["content"]
      error = resp["error"]

      if code < 0:
        error_msg = "(no error message available)"
        if error is not None:
          error = unicode(error)
        self.send_notification("BigSemantics service connection failure",
                     "Runtime error: " + error_msg + "\n"
                     + "when trying " + url)
        return
      elif code != 200:
        self.send_notification("BigSemantics service failure",
                     "Access error, HTTP code: " + unicode(code)
                     + "\n" + "when trying " + url)
        return
      else:
        if content.find(include) < 0:
          msg_body +=\
            "Expected content not found for {}: {}\n".format(url, include)

    if len(msg_body) > 0:
      self.send_notification("BigSemantics service issue(s)", msg_body)

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
    return {"code": code, "content": content, "error": error}

  def send_notification(subject, message):
    execfile('bot.conf', config)
    login = config['bot_email_login']
    passwd = config['bot_email_passwd']
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
  execfile(config_file_path, config)
  monitor = Monitor(config)
  monitor.test_bs_service()

