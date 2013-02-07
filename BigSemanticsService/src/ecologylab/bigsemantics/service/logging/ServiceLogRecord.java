package ecologylab.bigsemantics.service.logging;

import java.util.Date;

import ecologylab.bigsemantics.metadata.output.DocumentLogRecord;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class ServiceLogRecord extends DocumentLogRecord
{
	@simpl_scalar
	Date beginTime;
	
	@simpl_scalar
	long msTotal;
	
	@simpl_scalar
	String requesterIp;
	
	@simpl_scalar
	ParsedURL requestUrl;

	public Date getBeginTime()
	{
		return beginTime;
	}

	public void setBeginTime(Date beginTime)
	{
		this.beginTime = beginTime;
	}

	public long getMsTotal()
	{
		return msTotal;
	}

	public void setMsTotal(long msTotal)
	{
		this.msTotal = msTotal;
	}

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
