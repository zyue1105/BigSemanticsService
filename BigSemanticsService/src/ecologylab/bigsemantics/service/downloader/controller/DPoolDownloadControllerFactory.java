package ecologylab.bigsemantics.service.downloader.controller;

import ecologylab.bigsemantics.downloaders.controllers.DownloadController;
import ecologylab.bigsemantics.downloaders.controllers.DownloadControllerFactory;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;

/**
 * Factory for NewDPoolDownloadController.
 * 
 * @author quyin
 */
public class DPoolDownloadControllerFactory implements DownloadControllerFactory
{
  
  @Override
  public DownloadController createDownloadController(DocumentClosure closure)
  {
    DPoolDownloadController ctrl = new DPoolDownloadController();
    ctrl.setDocumentClosure(closure);
    return ctrl;
  }

}
