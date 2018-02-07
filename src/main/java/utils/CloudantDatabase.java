package utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;

import config.CloudantPropertiesBean;

public class CloudantDatabase {
	
private Database cloudant;
	
    private static Logger logger =  LoggerFactory.getLogger(CloudantDatabase.class);

	private CloudantPropertiesBean cloudantProperties;
	
	private void setDatabase() throws MalformedURLException {
        logger.debug(cloudantProperties.toString());
        
        try {
            logger.info("Connecting to cloudant at: " + cloudantProperties.getProtocol() + "://" + cloudantProperties.getHost() + ":" + cloudantProperties.getPort());
            /*final CloudantClient cloudantClient = ClientBuilder.url(new URL(cloudantProperties.getProtocol() + "://" + cloudantProperties.getHost() + ":" + cloudantProperties.getPort()))
                    .username(cloudantProperties.getUsername())
                    .password(cloudantProperties.getPassword())
                    .build();*/
                    
              final CloudantClient cloudantClient = ClientBuilder.url(new URL("https://e97790e0-b27a-42e9-873e-47c28c3777d3-bluemix:d723a4d133cddb8e3e9427042bcfdf363a2fbb1c04c924756b6cb62c3c627294@e97790e0-b27a-42e9-873e-47c28c3777d3-bluemix.cloudant.com"))
                            .build();
            
            cloudant = cloudantClient.database(cloudantProperties.getDatabase(), true);
            
            
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
            logger.error(e.getMessage(), e);
            throw e;
        }
        

    }
	
	public Database getCloudantDatabase() throws MalformedURLException{
		setDatabase();
        return cloudant;
    }

}
