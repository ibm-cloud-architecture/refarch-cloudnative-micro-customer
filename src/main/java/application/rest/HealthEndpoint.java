package application.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.HttpsURLConnection;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Health
@ApplicationScoped

public class HealthEndpoint implements HealthCheck {
	
	private Config config = ConfigProvider.getConfig();  
	private String auth_url = config.getValue("auth_health", String.class);
    private String cloudant_url = config.getValue("application.rest.client.CloudantClientService/mp-rest/url", String.class);
    		
	@Override
	public HealthCheckResponse call() {
		// TODO Auto-generated method stub
		if (!isAuthReady()) {
		      return HealthCheckResponse.named(CustomerService.class.getSimpleName())
		                                .withData("Auth Service", "DOWN").down()
		                                .build();
		    }
		
		if (!isCloudantReady()) {
		      return HealthCheckResponse.named(CustomerService.class.getSimpleName())
		                                .withData("Cloudant Service", "DOWN").down()
		                                .build();
		    }
		
		return HealthCheckResponse.named(CustomerService.class.getSimpleName()).withData("Customer Service", "UP").up().build();
	}

	private boolean isCloudantReady() {
		// Checking if the Cloudant database is UP
		URL url;
		try {
			url = new URL(cloudant_url); 
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			if(con!=null){
				if(con.getResponseMessage().equals("OK"))
					return true;
				else
					return false;
			}
		}
		catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;		
	}

	private boolean isAuthReady() {
		// Checking if the Auth service is UP
		URL url;
		try {
			url = new URL(auth_url); 
			HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
			if(con!=null){
				if(con.getResponseMessage().equals("OK"))
					return true;
				else
					return false;
			}
		}
		catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
