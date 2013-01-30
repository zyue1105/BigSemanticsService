package ecologylab.semantic.service.testcases.metadata;

import ecologylab.semantic.service.BasicTest;
import ecologylab.semantic.service.RequestType;
import ecologylab.serialization.formatenums.StringFormat;

public class MetadataBasicTest extends BasicTest
{

	protected MetadataBasicTest(String uri, StringFormat format)
	{
		super(uri, RequestType.METADATA, format);
	}

}
