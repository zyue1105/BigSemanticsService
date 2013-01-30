package ecologylab.semantic.service.testcases.mmd;

import com.sun.jersey.api.client.ClientResponse;

import ecologylab.semantic.service.ParamType;
import ecologylab.serialization.formatenums.StringFormat;

public class MMDNotFound extends MMDBasicTest
{

	public MMDNotFound(String urlOrName, StringFormat format, ParamType paramType)
	{
		super(urlOrName, format, paramType);
	}
	
	@Override
	public boolean doTest()
	{
		ClientResponse response = getServiceResponse();
		if (response.getClientResponseStatus() == ClientResponse.Status.NOT_FOUND)
		{
			debug("Test case passed");
			return true;
		}
		else
		{
			warning("Test case failed");
			return false;
		}
	}

}
