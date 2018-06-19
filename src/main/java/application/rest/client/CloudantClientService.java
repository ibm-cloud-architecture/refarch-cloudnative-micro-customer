package application.rest.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import javax.enterprise.context.Dependent;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@Dependent
@RegisterRestClient
@Produces("application/json")
@RegisterProvider(UnknownCustomerExceptionManager.class)
@Path("/customers")
public interface CloudantClientService {
    
    /*
    GET /_design/username_searchIndex/_search/usernames?query=user
    */
    // ?query=usernames:user&include_docs=true
    @GET
    @Path("/_design/username_searchIndex/_search/usernames")
    @Produces("application/json")
    public javax.ws.rs.core.Response getCustomerByUsername(@QueryParam("query") String query, @QueryParam("include_docs") String include_docs) throws UnknownCustomerException;
    
}
