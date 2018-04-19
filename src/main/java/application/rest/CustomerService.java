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

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.gson.Gson;

import model.Customer;
import utils.CloudantDatabase;

@Path("/customer")
@RequestScoped
@Produces("application/json")
public class CustomerService {
	
    @Inject
    private JsonWebToken jwt;

	private Database cloudant;
	
	CloudantDatabase cd = new CloudantDatabase();
	
	Config config = ConfigProvider.getConfig();

    private String protocol = config.getValue("protocol", String.class);
    private String host = config.getValue("host", String.class);
    private String port = config.getValue("port", String.class);
    private String user = config.getValue("user", String.class);
    private String password = config.getValue("password", String.class);
    private String database = config.getValue("database", String.class);
	
    private void initialize() throws MalformedURLException {
        
        try {
            System.out.println("Connecting to cloudant at: " + protocol + "://" + host + ":" + port);
            final CloudantClient cloudantClient = ClientBuilder.url(new URL(protocol + "://" + host + ":" + port))
                    .username(user)
                    .password(password)
                    .build();
            
            cloudant = cloudantClient.database(database, true);
            
            // create the design document if it doesn't exist
            if (!cloudant.contains("_design/username_searchIndex")) {
                final Map<String, Object> names = new HashMap<String, Object>();
                names.put("index", "function(doc){index(\"usernames\", doc.username); }");

                final Map<String, Object> indexes = new HashMap<>();
                indexes.put("usernames", names);

                final Map<String, Object> view_ddoc = new HashMap<>();
                view_ddoc.put("_id", "_design/username_searchIndex");
                view_ddoc.put("indexes", indexes);
                System.out.println("map" + view_ddoc);
                cloudant.save(view_ddoc);
            }
            
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage() + e);
            throw e;
        }
    }
    
    private Database getCloudantDatabase()  {
    	try {
			initialize();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return cloudant;
    }
    
    /**
     * check
     */
    @GET
    @Path("/check")
    @Produces("application/json")
    public String check() {
    	// test the cloudant connection
    	try {
			getCloudantDatabase().info();
            return  "It works!";
    	} catch (Exception e) {
    		System.err.println(e.getMessage());
            return e.getMessage();
    	}
    }
    
    @Path("/search")
    @Produces("application/json")
    @GET
    public javax.ws.rs.core.Response getCustomerByUsername(@QueryParam("username") String username) {
        try {
        	
        	if (username == null) {
        		return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity("Missing username").build();
        	}
        	
        	final List<Customer> customers = getCloudantDatabase().findByIndex(
        			"{ \"selector\": { \"username\": \"" + username + "\" } }",
        			Customer.class);
        	
        	GenericEntity<List<Customer>> list = new GenericEntity<List<Customer>>(customers) {
            };
            return javax.ws.rs.core.Response.ok(list).build();
            
        } catch (Exception e) {
            System.err.println(e.getMessage()  + e);
            return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
        }
    }
    
    @GET
    @Produces("application/json")
    public javax.ws.rs.core.Response getCustomers() {
        try {
        	final String customerId = jwt.getTokenID();
        	if (customerId == null) {
        		return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity("Invalid Bearer Token: Missing customer ID").build();
        	}
        	
        	System.out.println("caller: " + customerId);
            final List<Customer> customers = getCloudantDatabase().findByIndex(
				"{ \"selector\": { \"username\": \"" + jwt.getSubject() + "\" } }",
				Customer.class);

            final Customer cust = customers.get(0);
        	return javax.ws.rs.core.Response.ok(cust).build();
        } catch (Exception e) {
            System.err.println(e.getMessage() + e);
            throw e;
        }
    }
    
    /**
     * @return customer by id
     */
    @Path("/{id}")
    @GET
    @Produces("application/json")
    public javax.ws.rs.core.Response getById(@PathParam("id") String id) {
        try {
        	final String customerId = jwt.getTokenID();
        	
        	if (customerId == null) {
        		// if no user passed in, this is a bad request
        		return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity("Invalid Bearer Token: Missing customer ID").build();
        	}
        	
        	if (!customerId.equals(id)) {
        		return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.UNAUTHORIZED).build();
        	}
        	
			final Customer cust = getCloudantDatabase().find(Customer.class, customerId);
			
        	return javax.ws.rs.core.Response.ok(cust).build();
        }
        catch (NoDocumentException e) {
        	return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity("Customer with ID " + id + " not found").build();
        }
    }
    
    /**
     * Add customer
     * @return transaction status
     */
    
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public javax.ws.rs.core.Response create(Customer payload,  @Context UriInfo uriInfo) {
    	//JwtConfig jwt = getJwt();
    	//if(jwt.getScope().contains("admin")){
        try {
        	// TODO: no one should have access to do this, it's not exposed to APIC
            final Database cloudant = getCloudantDatabase();
            
            if (payload.getCustomerId() != null && cloudant.contains(payload.getCustomerId())) {
            	return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity("Id " + payload.getCustomerId() + " already exists").build();
            }
            
			final List<Customer> customers = getCloudantDatabase().findByIndex(
				"{ \"selector\": { \"username\": \"" + payload.getUsername() + "\" } }",
				Customer.class);
 
			if (!customers.isEmpty()) {
                return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity("Customer with name " + payload.getUsername() + " already exists").build();
			}
			
			// TODO: hash password
            //cust.setPassword(payload.getPassword());
  
            final Response resp = cloudant.save(payload);
            
            if (resp.getError() == null) {
            	UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                builder.path(resp.getId());
                return javax.ws.rs.core.Response.created(builder.build()).build();
            }
            else {
            	return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).build();
            }

        } catch (Exception ex) {
        	return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating customer: " + ex.toString()).build();
        }
        
    }
    
    /**
     * Update customer
     * @return transaction status
     */
    // This API is currently not called as it is not a function of the BlueCompute application
    @Path("{id}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public javax.ws.rs.core.Response update(@PathParam("id") String id, Customer payload) {
        try {
        	final String customerId = jwt.getTokenID();
        	if (customerId == null) {
        		// if no user passed in, this is a bad request
        		return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity("Invalid Bearer Token: Missing customer ID").build();
        	}
        	
        	System.out.println("caller: " + customerId);
			if (!customerId.equals("id")) {
        		// if i'm getting a customer ID that doesn't match my own ID, then return 401
				return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.UNAUTHORIZED).build();
        	}

            final Database cloudant = getCloudantDatabase();
            final Customer cust = getCloudantDatabase().find(Customer.class, id);
    
            cust.setFirstName(payload.getFirstName());
            cust.setLastName(payload.getLastName());
            cust.setImageUrl(payload.getImageUrl());
            cust.setEmail(payload.getEmail());
            
            // TODO: hash password
            cust.setPassword(payload.getPassword());
            
            cloudant.save(payload);
        } catch (NoDocumentException e) {
            System.err.println("Customer not found: " + id);
            return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity("Customer with ID " + id + " not found").build();
        } catch (Exception ex) {
            System.err.println("Error updating customer: " + ex);
            return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating customer: " + ex.toString()).build();
        }
        return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.OK).build();
    }

    /**
     * Delete customer
     * @return transaction status
     */
    // This API is currently not called as it is not a function of the BlueCompute application
    @Path("{id}")
    @DELETE
    public javax.ws.rs.core.Response delete(@PathParam("sub") String id) {
		// TODO: no one should have access to do this, it's not exposed to APIC
        try {
            final Database cloudant = getCloudantDatabase();
            final Customer cust = getCloudantDatabase().find(Customer.class, id);
            

            cloudant.remove(cust);
        } catch (NoDocumentException e) {
            System.err.println("Customer not found: " + id);
            return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity("Customer with ID " + id + " not found").build();
        } catch (Exception ex) {
            System.err.println("Error deleting customer: " + ex);
            return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity("Error deleting customer: " + ex.toString()).build();
        }
        return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.OK).build();
    }
    
    
}
