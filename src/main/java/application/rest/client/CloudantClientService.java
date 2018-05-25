package application.rest.client;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.util.Properties;
import javax.enterprise.context.Dependent;
import javax.ws.rs.ProcessingException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.RequestScoped;

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
    
    /*
    GET /_all_docs
    */
    /*
    @GET
    @Path("/_all_docs")
    @Produces("application/json")
    public javax.ws.rs.core.Response getCustomers();*/
    
}
