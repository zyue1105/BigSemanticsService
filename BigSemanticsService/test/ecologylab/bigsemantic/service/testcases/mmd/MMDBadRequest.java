package ecologylab.bigsemantic.service.testcases.mmd;

import com.sun.jersey.api.client.ClientResponse;

import ecologylab.bigsemantic.service.ParamType;
import ecologylab.serialization.formatenums.StringFormat;

public class MMDBadRequest extends MMDBasicTest
{

	public MMDBadRequest(String urlOrName, StringFormat format, ParamType paramType)
	{
		super(urlOrName, format, paramType);
	}
	
	@Override
	public boolean doTest()
	{
		ClientResponse response = getServiceResponse();
		if (response.getClientResponseStatus() == ClientResponse.Status.BAD_REQUEST)
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
