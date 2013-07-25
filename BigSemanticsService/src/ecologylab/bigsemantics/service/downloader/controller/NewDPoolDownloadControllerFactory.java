package ecologylab.bigsemantics.service.downloader.controller;

import ecologylab.bigsemantics.downloaders.controllers.DownloadControllerFactory;
import ecologylab.bigsemantics.downloaders.controllers.NewDownloadController;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;

/**
 * Factory for NewDPoolDownloadController.
 * 
 * @author quyin
 */
public class NewDPoolDownloadControllerFactory implements DownloadControllerFactory
{
  
  @Override
  public NewDownloadController createDownloadController(DocumentClosure closure)
  {
    NewDPoolDownloadController ctrl = new NewDPoolDownloadController();
    ctrl.setDocumentClosure(closure);
    return ctrl;
  }

}
