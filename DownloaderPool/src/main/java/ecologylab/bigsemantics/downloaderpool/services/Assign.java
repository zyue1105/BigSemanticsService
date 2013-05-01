package ecologylab.bigsemantics.downloaderpool.services;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import ecologylab.bigsemantics.downloaderpool.AssignedTasks;
import ecologylab.bigsemantics.downloaderpool.Controller;
import ecologylab.bigsemantics.downloaderpool.DownloaderRequest;
import ecologylab.bigsemantics.downloaderpool.Task;

/**
 * 
 * @author quyin
 */
public class Assign extends Action
{

  /**
   * 
   * @param remoteIp
   * @param workerId
   * @param blakclist
   * @param maxTaskCount
   * @return
   */
  public AssignedTasks assignTask(String remoteIp,
                                  String workerId,
                                  String blacklist,
                                  int maxTaskCount)
  {
    logger.info("Downloader[{}]@{} asks for tasks; blacklist: {}; count: {}.",
                workerId,
                remoteIp,
                blacklist,
                maxTaskCount);
    Controller ctrl = getController();

    DownloaderRequest req = new DownloaderRequest();
    req.setWorkerId(workerId);
    if (maxTaskCount > 0)
    {
      req.setMaxTaskCount(maxTaskCount);
    }
    if (blacklist != null && blacklist.length() > 0)
    {
      ArrayList<String> blist = Lists.newArrayList(Splitter.on(',').split(blacklist));
      req.setBlacklist(blist);
    }

    List<Task> tasks = ctrl.getTasksForWork(req);
    int n = tasks == null ? 0 : tasks.size();
    logger.info("{} task(s) will be assigned to Downloader[{}]@{}.", n, workerId, remoteIp);

    AssignedTasks result = new AssignedTasks();
    result.setTasks(tasks);

    return result;
  }

}
