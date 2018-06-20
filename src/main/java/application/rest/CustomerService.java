package application.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.temporal.ChronoUnit;

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
import model.Customer;
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

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.*;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;

@Path("/customer")
@RequestScoped
@Produces("application/json")
@OpenAPIDefinition(
    info = @Info(
        title = "Customer Service",
        version = "0.0",
        description = "getCustomer API",
        contact = @Contact(url = "https://github.com/ibm-cloud-architecture", name = "IBM CASE"),
        license = @License(name = "License", url = "https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer/blob/master/LICENSE")
        )
    )
public class CustomerService {
	
    @Inject
    private JsonWebToken jwt;

    @Inject
    @RestClient
    private CloudantClientService defaultCloudantClient;

    @Inject
    MetricRegistry registry;
    

    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, maxDuration= 10000)
    @Fallback(fallbackMethod = "fallbackService")
    //@Produces("application/json")
    @GET
    @APIResponses(value = {
        @APIResponse(
            responseCode = "404",
            description = "The cloudant database cannot be fround. ",
            content = @Content(
                        mediaType = "text/plain")),
        @APIResponse(
            responseCode = "200",
            description = "The Customer Data has been retrieved successfully.",
            content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = Customer.class)))})
    @Operation(
        summary = "Get customer by jwt username.",
        description = "Retrieves the customer informtation for the jwt subject."
    )
    public javax.ws.rs.core.Response getCustomerByUsername() throws Exception{
        try {
            String username = "usernames:" + jwt.getSubject();
            return defaultCloudantClient.getCustomerByUsername(username, "true");
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println(javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build());
            throw new Exception(e.toString());
        }
    }
    
    @Produces("application/json")
    @GET
    public javax.ws.rs.core.Response fallbackService() {
        System.out.println();
        return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity("Cloudant Service is down.").build();
    }

}
