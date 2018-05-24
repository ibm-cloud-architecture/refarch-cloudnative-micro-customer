package application.rest.client;

import java.util.logging.Logger;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class UnknownCustomerExceptionManager
    implements ResponseExceptionMapper<UnknownCustomerException> {

  Logger LOG = Logger.getLogger(UnknownCustomerExceptionManager.class.getName());

  @Override
  public boolean handles(int status, MultivaluedMap<String, Object> headers) {
    LOG.info("status = " + status);
    return status == 404;
  }

  @Override
  public UnknownCustomerException toThrowable(Response response) {
    return new UnknownCustomerException();
  }
}
