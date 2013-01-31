package ecologylab.semantic.service.mmdrepository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ecologylab.semantics.namesandnums.SemanticsAssetVersions;

/**
 * mmdrepository version root resource
 * 
 * @author ajit
 * 
 */

@Path("/mmdrepository.version")
@Component
@Scope("singleton")
public class MMDRepositoryVersion {

	@GET
	@Produces("text/plain")
	public float getMmdRepositoryVersion()
	{
		return SemanticsAssetVersions.METAMETADATA_ASSET_VERSION;
	}
}
