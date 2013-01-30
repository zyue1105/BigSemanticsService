/**
 * 
 */
package ecologylab.semantic.service.oodss;

import ecologylab.collections.Scope;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.semantic.service.SemanticServiceScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * RequestMessage for document download 
 * now moved to semantics project, retained for gsoc m1
 * 
 * @author ajit
 * 
 */

public class MetadataRequest extends RequestMessage implements Continuation<DocumentClosure>
{

	@simpl_scalar
	private String				url;

	Document							metadata;

	SemanticServiceScope	semanticServiceScope;

	public MetadataRequest()
	{
	}

	public MetadataRequest(String url)
	{
		this.url = url;
	}

	@Override
	public MetadataResponse performService(Scope clientSessionScope)
	{
		ParsedURL thatPurl = ParsedURL.getAbsolute(url);
		semanticServiceScope = SemanticServiceScope.get();

		Document document = semanticServiceScope.getOrConstructDocument(thatPurl);
		DocumentClosure documentClosure = document.getOrConstructClosure();

		documentClosure.addContinuation(this);
		documentClosure.queueDownload();
		semanticServiceScope.getDownloadMonitors().requestStops();

		synchronized (this)
		{
			try
			{
				wait();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		return new MetadataResponse(this.metadata);
	}

	@Override
	public synchronized void callback(DocumentClosure incomingClosure)
	{
		output(incomingClosure);
		semanticServiceScope.getDownloadMonitors().stop(false);
		notify();
	}

	protected void output(DocumentClosure incomingClosure)
	{
		this.metadata = incomingClosure.getDocument();
	}

}
