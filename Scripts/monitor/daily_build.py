#!/usr/bin/python

import exceptions
import shutil
from os.path import dirname, join
from subprocess import Popen, PIPE

from bs_service_tester import ServiceTester

class DailyBuilder:
  def __init__(self, config):
    self.config = config
    self.code_dir = config["code_dir"]
    self.wrapper_repo = join(self.code_dir, "BigSemanticsWrapperRepository")
    self.wrapper_proj = join(self.wrapper_repo, "BigSemanticsWrappers")
    self.service_repo = join(self.code_dir, "BigSemanticsService")
    self.service_proj = join(self.service_repo, "BigSemanticsService")
    self.service_build = join(self.service_proj, "build")
    self.jetty_dir = config["jetty_dir"]
    self.webapps_dir = join(self.jetty_dir, "webapps")
    self.restart_script = config["restart_script"]
    self.release_script = config["release_script"]

  def _call(self, cmds, wd):
    p = Popen(cmds, stdout=PIPE, stderr=PIPE, cwd = wd)
    (out, err) = p.communicate()
    return (p.returncode, out, err)

  def _check_call(self, cmds, wd):
    (code, out, err) = self._call(cmds, wd)
    if code != 0:
      raise exceptions.RuntimeError(
              "Failed to execute {}\n  OUT:\n{}\n  ERROR:\n{}\n".format(
                cmds, out, err))

  def pull_wrappers(self):
    # clean local wrapper changes
    self._check_call(["git", "checkout", "--", "*"], wd=self.wrapper_repo)
    # pull down latest wrappers
    self._check_call(["git", "pull"], wd=self.wrapper_repo)

  def compile_wrappers_to_jars(self):
    self._check_call(["ant", "clean"], wd=self.wrapper_proj)
    self._check_call("ant", wd=self.wrapper_proj)

  def build_service_war(self):
    self._check_call(["ant", "clean"], wd=self.service_build)
    self._check_call(["ant", "buildwar"], wd=self.service_build)
    shutil.copy2(join(self.service_build, "BigSemanticsService.war"),
                 self.webapps_dir)

  def restart_service(self):
    wd = dirname(self.restart_script)
    self._check_call(["sh", self.restart_script], wd=wd)

  def test_service(self):
    tester_config = {}
    execfile("bot.conf", tester_config)
    service_tester = ServiceTester(tester_config)
    (code, fatal, non_fatal) = service_tester.test_service()
    if code != 200 or len(fatal) > 0 or len(non_fatal) > 0:
      raise "Build broken:\n  code: {}\n  fatal: {}\n  non_fatal: {}".format(
              code, fatal, non_fatal)
    else:
      wd = dirname(self.release_script)
      self._call(["sh", self.release_script], wd=wd)
      print "released.\n"



if __name__ == "__main__":
  config = {}
  execfile("builder.conf", config)
  builder = DailyBuilder(config)
  try:
    builder.pull_wrappers()
    builder.compile_wrappers_to_jars()
    builder.build_service_war()
    builder.restart_service()
    builder.test_service()
  except Exception as e:
    bot_config = {}
    execfile("bot.conf", bot_config)
    monitor = ServiceMonitor(bot_config)
    monitor.send_notification("[Dev] Build failure", str(e))

