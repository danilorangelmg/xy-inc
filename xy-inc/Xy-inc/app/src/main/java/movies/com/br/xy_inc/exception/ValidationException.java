package movies.com.br.xy_inc.exception;

/**
 * Created by danilo on 12/03/16.
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = 1L;


    public ValidationException(String validationMessage) {
        super(validationMessage);
    }

    public ValidationException() {

    }

}
