package customer;

import java.util.Random;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import customer.model.Customer;


public class CustomerControllerTest {
	
	
	@Test
	public void testMarshalToJson() throws Exception {
		final Customer inv = new Customer();
		
		final String id = "abcdefgh";
		
		final ObjectMapper mapper = new ObjectMapper();
		
		
		inv.setCustomerId(id);
		inv.setFirstName("Name");
		inv.setLastName("Last");
		inv.setUsername("user1");
		inv.setPassword("asdf");
		inv.setEmail("my@email.com");
		inv.setImageUrl("/image/myimage.jpg");
		
		
		final String json = mapper.writeValueAsString(inv);
		
		// construct a json string with the above properties
		
		final StringBuilder myJsonStr = new StringBuilder();
		
		myJsonStr.append("{");
		myJsonStr.append("\"customerId\":\"").append(id).append("\",");
		myJsonStr.append("\"email\":").append("\"my@email.com\"").append(",");
		myJsonStr.append("\"firstName\":").append("\"Name\"").append(",");
		myJsonStr.append("\"lastName\":").append("\"Last\"").append(",");
		myJsonStr.append("\"imageUrl\":").append("\"/image/myimage.jpg\"").append(",");
		myJsonStr.append("\"username\":").append("\"user1\"").append(",");
		myJsonStr.append("\"password\":").append("\"asdf\"");
		myJsonStr.append("}");
		
		final String myJson = myJsonStr.toString();
		System.out.println("Marshalled Customer to JSON:" + myJson);
		System.out.println("My JSON String:" + myJson);
		
		final JsonNode jsonObj = mapper.readTree(json);
		final JsonNode myJsonObj = mapper.readTree(myJson);
		
		
		assert(jsonObj.equals(myJsonObj));
	}
	
	@Test
	public void testMarshalFromJson() throws Exception {
		final String id = "asdgadsfads";
		
		final ObjectMapper mapper = new ObjectMapper();
		
		// construct a json string with the above properties
		
		final StringBuilder myJsonStr = new StringBuilder();
		
		myJsonStr.append("{");
		myJsonStr.append("\"customerId\":\"").append(id).append("\",");
		myJsonStr.append("\"email\":").append("\"my@email.com\"").append(",");
		myJsonStr.append("\"firstName\":").append("\"John\"").append(",");
		myJsonStr.append("\"lastName\":").append("\"Smith\"").append(",");
		myJsonStr.append("\"imageUrl\":").append("\"/image/myimage.jpg\"").append(",");
		myJsonStr.append("\"username\":").append("\"user1\"").append(",");
		myJsonStr.append("\"password\":").append("\"asdf\"");
		myJsonStr.append("}");
		
		final String myJson = myJsonStr.toString();
		System.out.println("My JSON String:" + myJson);
		
		// marshall json to Customer object
		
		final Customer inv = mapper.readValue(myJson, Customer.class);
		
		// make sure all the properties match up
		assert(inv.getCustomerId().equals(id));
		assert(inv.getFirstName().equals("John"));
		assert(inv.getLastName().equals("Smith"));
		assert(inv.getImageUrl().equals("/image/myimage.jpg"));
		assert(inv.getUsername().equals("user1"));
		assert(inv.getPassword().equals("asdf"));
		assert(inv.getEmail().equals("my@email.com"));
		
		
	}
}