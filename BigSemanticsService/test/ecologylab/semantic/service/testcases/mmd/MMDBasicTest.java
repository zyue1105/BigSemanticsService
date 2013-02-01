package ecologylab.semantic.service.testcases.mmd;

import ecologylab.semantic.service.BasicTest;
import ecologylab.semantic.service.ParamType;
import ecologylab.semantic.service.RequestType;
import ecologylab.serialization.formatenums.StringFormat;

public class MMDBasicTest extends BasicTest
{

	public MMDBasicTest(String urlOrName, StringFormat format, ParamType paramType)
	{
		super(urlOrName, RequestType.MMD, format, paramType);
	}

}
