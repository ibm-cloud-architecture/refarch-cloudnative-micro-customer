package application.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.RequestScoped;

import config.JwtConfig;

import application.rest.client.CloudantClientService;

import java.net.MalformedURLException;
import java.net.URL;

import java.net.HttpURLConnection;
import java.net.URI;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.ApplicationPath;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/customer")
@RequestScoped
@Produces("application/json")
public class CustomerService {
	
    @Inject
    private JsonWebToken jwt;

    @Inject
    @RestClient
    private CloudantClientService defaultCloudantClient;
    
    /**
     * check
     */
    @GET
    @Path("/check")
    @Produces("application/json")
    public String check() {
    	try {
            return  "Customer Service is up and running.";
    	} catch (Exception e) {
    		System.err.println(e.getMessage());
            return e.getMessage();
    	}
    }
    
    @Produces("application/json")
    @GET
    public javax.ws.rs.core.Response getCustomerByUsername(){
        try {
            String username = "usernames:" + jwt.getSubject();
            return defaultCloudantClient.getCustomerByUsername(username, "true");
        }
        catch (Exception e){
            e.printStackTrace();
            return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
        }
    }
    
}
