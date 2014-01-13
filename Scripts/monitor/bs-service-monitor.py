#!/usr/bin/python

import urllib2
import smtplib
from email.mime.text import MIMEText

config = {}
execfile('bot.conf', config)

def main():
  urls = config['urls']
  for url in urls:
    resp = access_url(url)
    if resp[0] < 0:
      send_notification("BigSemantics service failure",
                        "Runtime error: " + str(resp[1]) + "\n"
                        + "when trying " + url)
      return
    elif resp[0] != 200:
      send_notification("BigSemantics service failure",
                        "Access error, HTTP code: " + resp[0] + "\n"
                        + "when trying " + url)
      return
    else:
      print url + " works fine."

def access_url(url):
  f = None
  code = -1
  error = None
  try:
    f = urllib2.urlopen(url)
    code = f.getcode()
  except Exception as e:
    code = -1
    error = e
  finally:
    if f is not None:
      f.close()
  return (code, error)

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
  main()

