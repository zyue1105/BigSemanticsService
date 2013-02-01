/**
 * 
 */
package ecologylab.semantic.service.oodss;

import java.io.IOException;
import java.net.InetAddress;

import ecologylab.net.NetTools;
import ecologylab.oodss.distributed.server.DoubleThreadedNIOServer;
import ecologylab.semantic.service.SemanticServiceScope;
import ecologylab.serialization.SimplTypesScope;

/**
 * represents pool of machines for downloading document
 * now moved to semantics project, retained for gsoc m1
 * 
 * @author ajit
 * 
 */
public class MetadataOODSSServer
{

	private static final int	idleTimeout	= -1;

	private static final int	MTU					= 10000000;

	public static void runInstance() throws IOException
	{
		SimplTypesScope lookupMetadataTranslations = SemanticsServiceMessageScope
				.get();

		/*
		 * Creates a scope for the server to use as an application scope as well as individual client
		 * session scopes.
		 */
		SemanticServiceScope sessionScope = SemanticServiceScope.get();

		/*
		 * Initialize the ECHO_HISTORY registry in the application scope so that the performService(...)
		 * of HistoryEchoRequest modifies the history in the application scope.
		 */

		/* Acquire an array of all local ip-addresses */
		InetAddress[] locals = NetTools.getAllInetAddressesForLocalhost();

		/*
		 * Create the server and start the server so that it can accept incoming connections.
		 */
		DoubleThreadedNIOServer metadataServer = DoubleThreadedNIOServer.getInstance(2107, locals,
				lookupMetadataTranslations, sessionScope, idleTimeout, MTU);
		metadataServer.start();
	}

	public static void main(String[] args) throws IOException
	{
		runInstance();
	}

}
