package ecologylab.bigsemantic.service.testcases.mmd;

import ecologylab.bigsemantic.service.BasicTest;
import ecologylab.bigsemantic.service.ParamType;
import ecologylab.bigsemantic.service.RequestType;
import ecologylab.serialization.formatenums.StringFormat;

public class MMDBasicTest extends BasicTest
{

	public MMDBasicTest(String urlOrName, StringFormat format, ParamType paramType)
	{
		super(urlOrName, RequestType.MMD, format, paramType);
	}

}
