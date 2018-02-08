package utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;

public class CloudantDatabase {
	
private Database cloudant;
    
    Config config = ConfigProvider.getConfig();

    private String protocol = config.getValue("protocol", String.class);
    private String host = config.getValue("host", String.class);
    private String port = config.getValue("port", String.class);
    private String database = config.getValue("database", String.class);
	
	private void setDatabase() throws MalformedURLException {
        
        try {
              
        	System.out.println("Connecting to cloudant at: " + protocol + "://" + host + ":" + port);
                    
            final CloudantClient cloudantClient = ClientBuilder.url(new URL(protocol + "://" + host + ":" + port))
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

                cloudant.save(view_ddoc);        
            }
            
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage() + e);
            throw e;
        }
        

    }
	
	public Database getCloudantDatabase() throws MalformedURLException{
		setDatabase();
        return cloudant;
    }

}
