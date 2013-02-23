package ecologylab.bigsemantics.service.downloaderpool;

/**
 * 
 * @author quyin
 * 
 */
public enum TaskState
{

  /**
   * The task is newly created.
   */
  INIT,

  /**
   * The task is a duplication of an existing task.
   */
  DEDUP,

  /**
   * The task is waiting in the queue for being assigned to a downloader.
   */
  WAITING,

  /**
   * The task is taken out of the queue and being matched with a downloader request.
   */
  MATCHING,

  /**
   * The task is matched with a downloader request and is sent to the downloader for execution.
   */
  ONGOING,

  /**
   * The task is attempted but failed for some reason, and will be reentered into the queue soon.
   */
  ATTEMPT_FAILED,

  /**
   * The task has been attempted for several times without success, thus marked as failed and
   * terminated.
   */
  TERMINATED,

}
