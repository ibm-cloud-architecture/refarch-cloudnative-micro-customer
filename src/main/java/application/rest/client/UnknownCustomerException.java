package application.rest.client;

public class UnknownCustomerException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public UnknownCustomerException() {
        super();
    }

    public UnknownCustomerException(String message) {
        super(message);
    }
}
