package ecologylab.bigsemantic.service.testcases.metadata;

import ecologylab.bigsemantic.service.BasicTest;
import ecologylab.bigsemantic.service.RequestType;
import ecologylab.serialization.formatenums.StringFormat;

public class MetadataBasicTest extends BasicTest
{

	protected MetadataBasicTest(String uri, StringFormat format)
	{
		super(uri, RequestType.METADATA, format);
	}

}
