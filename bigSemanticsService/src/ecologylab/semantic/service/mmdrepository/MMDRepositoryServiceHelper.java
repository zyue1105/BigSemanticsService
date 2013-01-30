package ecologylab.semantic.service.mmdrepository;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ecologylab.semantic.service.SemanticServiceErrorCodes;
import ecologylab.semantic.service.SemanticServiceScope;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Helper class for mmdrepository root resources
 * 
 * @author ajit
 * 
 */

public class MMDRepositoryServiceHelper {
	
	static SemanticServiceScope			semanticsServiceScope;
	
	static 
	{
		semanticsServiceScope = SemanticServiceScope.get();
	}
	
	public static Response getMmdRepository(StringFormat format)
	{
		MetaMetadataRepository mmdRepository = semanticsServiceScope.getMetaMetadataRepository();
		if (mmdRepository != null)
		{
			try
			{
				String responseBody = SimplTypesScope.serialize(mmdRepository, format).toString();
				return Response.status(Status.OK).entity(responseBody).build();
			}
			catch (SIMPLTranslationException e)
			{
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(SemanticServiceErrorCodes.INTERNAL_ERROR).type(MediaType.TEXT_PLAIN).build();
			}
		}
		else
			return Response.status(Status.NOT_FOUND).entity(SemanticServiceErrorCodes.METAMETADATA_NOT_FOUND)
						.type(MediaType.TEXT_PLAIN).build();
	}
}
