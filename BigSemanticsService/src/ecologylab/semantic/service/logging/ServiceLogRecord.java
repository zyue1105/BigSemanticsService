package ecologylab.semantic.service.logging;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.output.DocumentLogRecord;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class ServiceLogRecord extends DocumentLogRecord
{
	@simpl_scalar
	String requesterIp;
	
	@simpl_scalar
	ParsedURL requestUrl;

	public String getRequesterIp()
	{
		return requesterIp;
	}

	public void setRequesterIp(String requesterIp)
	{
		this.requesterIp = requesterIp;
	}

	public ParsedURL getRequestUrl()
	{
		return requestUrl;
	}

	public void setRequestUrl(ParsedURL requestUrl)
	{
		this.requestUrl = requestUrl;
	}
}
