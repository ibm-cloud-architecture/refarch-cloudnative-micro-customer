package application.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;

import config.CloudantPropertiesBean;
import model.Customer;
import utils.CloudantDatabase;

@Path("/customer")
public class CustomerService {
	
	private Database cloudant;
	
	private CloudantPropertiesBean cloudantProperties;
	
	CloudantDatabase cd = new CloudantDatabase();
	
	
    private void initialize() throws MalformedURLException {
        
        try {
            System.out.println("Connecting to cloudant at: http://cloudant-developer:8080/");
            final CloudantClient cloudantClient = ClientBuilder.url(new URL("https://e97790e0-b27a-42e9-873e-47c28c3777d3-bluemix.cloudant.com:443/"))
                    .username("e97790e0-b27a-42e9-873e-47c28c3777d3-bluemix")
                    .password("d723a4d133cddb8e3e9427042bcfdf363a2fbb1c04c924756b6cb62c3c627294")
                    .build();
            
            cloudant = cloudantClient.database("customers", true);
            
            
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
    public String getCustomers() {
        try {
        	final String customerId = getCustomerId();
        	if (customerId == null) {
        		// if no user passed in, this is a bad request
        		return "Invalid Bearer Token: Missing customer ID";
        	}
        	
        	System.out.println("caller: " + customerId);
			final Customer cust = getCloudantDatabase().find(Customer.class, customerId);
            
            return cust.toString();
        } catch (Exception e) {
            System.err.println(e.getMessage() + e);
            throw e;
        }
        
    }
    
    private String getCustomerId() {
    	// to be replaced with the customer from security context
    	return "";
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
				return "Generated ID" + resp.getId();
            } else {
            	return resp.getError();
            }

        } catch (Exception ex) {
            return "Error creating customer: " + ex.toString();
        }
        
    }

    
}
