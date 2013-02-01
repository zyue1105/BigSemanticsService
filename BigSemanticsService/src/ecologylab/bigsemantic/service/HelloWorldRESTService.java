package ecologylab.bigsemantic.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

// TODO: play with Autowired, Context, Scope types, and Response types

// The Java class will be hosted at the URI path "/helloworld"
@Path("/helloworld")
@Component
@Scope("prototype")
public class HelloWorldRESTService {
	
	// The Java method will process HTTP GET requests
    @GET
    // The Java method will produce content identified by the MIME Media type "text/plain"
    @Produces("application/json")
    public String getIt() {
    	return "Hello World!";
    }

}
