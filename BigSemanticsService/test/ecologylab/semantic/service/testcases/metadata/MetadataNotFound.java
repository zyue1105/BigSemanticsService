package ecologylab.semantic.service.testcases.metadata;

import com.sun.jersey.api.client.ClientResponse;

import ecologylab.serialization.formatenums.StringFormat;

public class MetadataNotFound extends MetadataBasicTest
{

	public MetadataNotFound(String uri, StringFormat format)
	{
		super(uri, format);
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
