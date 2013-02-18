/**
 * 
 */
package ecologylab.bigsemantics.service;

/**
 * Error messages to be returned by semantic service
 * 
 * @author ajit
 *
 */

public interface SemanticServiceErrorCodes
{
	//for error code 400
	public final String BAD_REQUEST 	 = "Invalid query parameter";
	
	//404
	public final String METADATA_NOT_FOUND = "Metadata service is currently unable to handle the request. Try again later.";
	public final String METAMETADATA_NOT_FOUND = "Meta-Metadata Not Found";
	
	//500
	public final String INTERNAL_ERROR = "Internal Error. Contact Administrator.";
}
