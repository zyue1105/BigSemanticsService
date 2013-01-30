package ecologylab.semantics.compiler.orm;

import java.util.HashMap;
import java.util.Map;

public class MetadataBuiltinDeclarationNamesHelper
{
	
	static protected Map<String, String> builtinToDeclarationSpecialCases = new HashMap<String, String>();
	static protected Map<String, String> declarationToBuiltinSpecialCases = new HashMap<String, String>();
	
	static
	{
		builtinToDeclarationSpecialCases.put("ecologylab.semantics.metadata.Metadata", "ecologylab.semantics.metadata.builtins.declarations.MetadataDeclaration");
		
		declarationToBuiltinSpecialCases.put("ecologylab.semantics.metadata.builtins.declarations.MetadataDeclaration", "ecologylab.semantics.metadata.Metadata");
		declarationToBuiltinSpecialCases.put("ecologylab.semantics.metadata.builtins.declarations.InformationCompositionDeclaration", null);
	}

	public String getDeclarationClassName(String builtinClassName)
	{
		if (builtinToDeclarationSpecialCases.containsKey(builtinClassName))
			return builtinToDeclarationSpecialCases.get(builtinClassName);
		
		int lastDotPos = builtinClassName.lastIndexOf('.');
		String packageName = builtinClassName.substring(0, lastDotPos);
		packageName = packageName + ".declarations";
		String classSimpleName = builtinClassName.substring(lastDotPos + 1, builtinClassName.length());
		classSimpleName = classSimpleName + "Declaration";
		return packageName + "." + classSimpleName;
	}
	
	public String getBuiltinClassName(String declarationClassName)
	{
		if (declarationToBuiltinSpecialCases.containsKey(declarationClassName))
			return declarationToBuiltinSpecialCases.get(declarationClassName);
			
		int lastDotPos = declarationClassName.lastIndexOf('.');
		String packageName = declarationClassName.substring(0, lastDotPos);
		packageName = packageName.substring(0, packageName.lastIndexOf('.')); // go up one level of packages
		String classSimpleName = declarationClassName.substring(lastDotPos + 1, declarationClassName.length());
		classSimpleName = classSimpleName.substring(0, classSimpleName.lastIndexOf("Declaration")); // remove suffix
		return packageName + "." + classSimpleName;
	}
	
}
