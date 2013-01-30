package ecologylab.semantic.service.websocketoodss;

import java.io.IOException;
import java.net.InetAddress;

import ecologylab.net.NetTools;
import ecologylab.oodss.distributed.server.DoubleThreadedNIOServer;
import ecologylab.oodss.distributed.server.WebSocketOodssServer;
import ecologylab.semantic.service.SemanticServiceScope;
import ecologylab.semantic.service.oodss.SemanticsServiceMessageScope;
import ecologylab.serialization.SimplTypesScope;

public class WSOServer
{
	// private static final int idleTimeout = -1;

	// private static final int MTU = 10000000;

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
		// InetAddress[] locals = NetTools.getAllInetAddressesForLocalhost();

		/*
		 * Create the server and start the server so that it can accept incoming connections.
		 */
		WebSocketOodssServer metadataServer = new WebSocketOodssServer(lookupMetadataTranslations,
				sessionScope);

		metadataServer.start();
	}

	public static void main(String[] args) throws IOException
	{
		runInstance();
	}
}
