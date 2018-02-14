package application.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtBuilder;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;
import com.ibm.websphere.security.openidconnect.PropagationHelper;
import com.ibm.websphere.security.openidconnect.token.IdToken;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.gson.Gson;

import model.Customer;
import utils.CloudantDatabase;

@Path("/customer")
public class CustomerService {
	
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
    
    @GET
    @Path("/customer")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCustomerByUsername(String username) {
    {
   
    String custDetails=null;
    try {
    	final String customerId = "111";
    	/*if (customerId == null) {
    		// if no user passed in, this is a bad request
    		return "Invalid Bearer Token: Missing customer ID";
    	}*/
    	
    	System.out.println("caller: " + customerId);
			final Customer cust = new Customer() ;
			cust.setCustomerId("111");
			cust.set_rev("rev string");
			cust.setEmail("xxx@mail.com");
			cust.setFirstName("sss");
			cust.setLastName("hhh");
			cust.setUsername("foo");
			cust.setPassword("bar");
			
			Gson gson = new Gson();
    	    custDetails = gson.toJson(cust);
		    return custDetails;
    } catch (Exception e) {
        System.err.println(e.getMessage() + e);
        throw e;
    }
    }
    }
    
    
    @GET
    @Produces("application/json")
    public String getCustomers() {
    	String custDetails=null;
        try {
        	
        	final String customerId = getCustomerId();
        	if (customerId == null) {
        		// if no user passed in, this is a bad request
        		return "Invalid Bearer Token: Missing customer ID";
        	}
        	
        	System.out.println("caller: " + customerId);
			final Customer cust = getCloudantDatabase().find(Customer.class, customerId);
        	Gson gson = new Gson();
        	custDetails = gson.toJson(cust);
   		    return custDetails;
        } catch (Exception e) {
            System.err.println(e.getMessage() + e);
            throw e;
        }
        
    }
    
    private String getCustomerId() {
    	// to be replaced with the customer from security context
    	return "92d11795f32147d382e6adbc6b31fdbb";
    }
    
    /**
     * Add customer 
     * @return transaction status
     */
    
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public String create(Customer payload) {
        try {
        	// TODO: no one should have access to do this, it's not exposed to APIC
            final Database cloudant = getCloudantDatabase();
            
            if (payload.getCustomerId() != null && cloudant.contains(payload.getCustomerId())) {
                return "Id " + payload.getCustomerId() + " already exists";
            }
            
			final List<Customer> customers = getCloudantDatabase().findByIndex(
				"{ \"selector\": { \"username\": \"" + payload.getUsername() + "\" } }", 
				Customer.class);
 
			if (!customers.isEmpty()) {
                return "Customer with name " + payload.getUsername() + " already exists";
			}
			
			// TODO: hash password
            //cust.setPassword(payload.getPassword());
 
            
            final Response resp = cloudant.save(payload);
            
            if (resp.getError() == null) {
				// HTTP 201 CREATED
            	// To be done - build the URI with location
				return "Generated ID" + resp.getId();
            } else {
            	return resp.getError();
            }

        } catch (Exception ex) {
            return "Error creating customer: " + ex.toString();
        }
        
    }
    
    @Path("/search")
    @GET
    public String searchCustomers(@QueryParam("username") String username) {
    	
    	String custDetails=null;
        try {
        	
        	if (username == null) {
        		return "Missing username";
        	}
        	
        	final List<Customer> customers = getCloudantDatabase().findByIndex(
        			"{ \"selector\": { \"username\": \"" + username + "\" } }", 
        			Customer.class);
        	
        	//  query index
        	Gson gson = new Gson();
        	custDetails = gson.toJson(customers);
   		    return custDetails;
            
        } catch (Exception e) {
            System.err.println(e.getMessage()  + e);
            return e.getLocalizedMessage();
        }
        
    }
    
    /**
     * @return customer by id
     */
    @Path("{id}")
    @GET
    public String getById(@PathParam("id") String id) {
    	String custDetails=null;
        try {
        	final String customerId = getCustomerId();
        	if (customerId == null) {
        		// if no user passed in, this is a bad request
        		return "Invalid Bearer Token: Missing customer ID";
        	}
        	
        	System.out.println("caller: " + customerId);
        	
        	if (!customerId.equals(id)) {
        		// if i'm getting a customer ID that doesn't match my own ID, then return 401
        		return "401 status";
        		//return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        	}
        	
			final Customer cust = getCloudantDatabase().find(Customer.class, customerId);
			
			Gson gson = new Gson();
        	custDetails = gson.toJson(cust);
   		    return custDetails;
        } catch (NoDocumentException e) {
            return "Customer with ID " + id + " not found";
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
    public String update(@PathParam("id") String id, Customer payload) {

        try {
        	final String customerId = getCustomerId();
        	if (customerId == null) {
        		// if no user passed in, this is a bad request
        		return "Invalid Bearer Token: Missing customer ID";
        	}
        	
        	System.out.println("caller: " + customerId);
			if (!customerId.equals("id")) {
        		// if i'm getting a customer ID that doesn't match my own ID, then return 401
        		return "401";
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
            return "Customer with ID " + id + " not found";
        } catch (Exception ex) {
            System.err.println("Error updating customer: " + ex);
            return "Error updating customer: " + ex.toString();
        }
        
        return "Customer Updated Successfully";
    }

    /**
     * Delete customer 
     * @return transaction status
     */
    // This API is currently not called as it is not a function of the BlueCompute application
    @Path("{id}")
    @DELETE
    public String delete(@PathParam("id") String id) {
		// TODO: no one should have access to do this, it's not exposed to APIC
    	
        try {
            final Database cloudant = getCloudantDatabase();
            final Customer cust = getCloudantDatabase().find(Customer.class, id);
            

            cloudant.remove(cust);
        } catch (NoDocumentException e) {
            System.err.println("Customer not found: " + id);
            return "Customer with ID " + id + " not found";
        } catch (Exception ex) {
            System.err.println("Error deleting customer: " + ex);
            return "Error deleting customer: " + ex.toString();
        }
        return "Customer Deleted";
    }
    
    private IdToken getJwt(){
        // ask liberty for the id token from the oauth/oidc exchange protecting
        // this invocation.
        //IdToken id_token = PropagationHelper.getIdToken();
    	IdToken id_token = PropagationHelper.getIdToken();
    	return id_token;
    }
    
}
