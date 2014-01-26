
# read configs first
code_dir = config["code_dir"]

class DailyBuilder:
  def __init__(self, config):
    self.config = config
    self.code_dir = config["code_dir"]
    self.wrapper_repo = os.path.join(code_dir, "BigSemanticsWrapperRepository")
    self.wrapper_proj = os.path.join(self.wrapper_repo, "BigSemanticsWrappers")
    self.service_repo = os.path.join(code_dir, "BigSemanticsService")
    self.service_proj = os.path.join(self.service_repo, "BigSemanticsService")
    self.service_build = os.path.join(self.service_proj, "build")

  def _call(self, cmds, wd):
    p = subprocess.Popen(cmds,
                         stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE,
                         cwd = wd)
    (out, err) = p.communicate()
    return (p.returncode, out, err)

  def _check_call(self, cmds, wd):
    (code, out, err) = self._call(cmds, wd)
    if code != 0:
      raise exceptions.RuntimeError(
          "Failed to execute {}\n  OUT:\n{}\n  ERROR:\n{}\n".format(out, err))

  def pull_wrappers(self):
    # clean local wrapper changes
    self._check_call(["git", "checkout", "--", "*"], cwd=self.wrapper_repo)
    # pull down latest wrappers
    self._check_call(["git", "pull"], cwd=self.wrapper_repo)

  def compile_wrappers_to_jars(self):
    self._check_call(["ant", "clean"], cwd=self.wrapper_proj)
    self._check_call("ant", cwd=self.wrapper_proj)

  def build_service_war(self):
    self._check_call(["ant", "clean"], cwd=self.service_build)
    self._check_call(["ant", "buildwar"], cwd=self.service_build)

  def restart_service(self):
    # restarted bs service + dpool service + downloader
    pass

  def test_service(self):
    # test service
    pass

if __name__ == "__main__":
  config = {}
  execfile("builder.conf", config)
  builder = DailyBuilder(config)
  builder.pull_wrappers()
  builder.compile_wrappers_to_jars()
  builder.build_service_war()

