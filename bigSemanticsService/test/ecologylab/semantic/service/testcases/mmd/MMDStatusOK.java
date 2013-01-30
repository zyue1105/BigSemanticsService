package ecologylab.semantic.service.testcases.mmd;

import com.sun.jersey.api.client.ClientResponse;

import ecologylab.semantic.service.BasicTest;
import ecologylab.semantic.service.ParamType;
import ecologylab.semantic.service.RequestType;
import ecologylab.serialization.formatenums.StringFormat;

public class MMDStatusOK extends MMDBasicTest
{
	public MMDStatusOK(String urlOrName, StringFormat format, ParamType paramType)
	{
		super(urlOrName, format, paramType);
	}
	
	@Override
	public boolean doTest()
	{
		ClientResponse response = getServiceResponse();
		if (response.getClientResponseStatus() == ClientResponse.Status.OK)
		{
			String serviceMmd = response.getEntity(String.class);
			String localMmd = getMmdLocally();
			
			debug("Service MMD: " + serviceMmd);
			debug("Local MMD: " + localMmd);
			
			if (serviceMmd != null && localMmd != null && serviceMmd.equals(localMmd))
			{
				debug("Test case passed");
				return true;
			}
		}
		warning("Test case failed");
		return false;
	}
	
	public static void main(String[] args)
	{
		MMDStatusOK t = new MMDStatusOK("http://ecologylab.net", StringFormat.XML, ParamType.URL);
		t.doTest();
	}

}
