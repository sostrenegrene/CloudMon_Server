package scripts;

/**
 * Created by soren.pedersen on 26-07-2016.
 */
public class ScriptResult {

    public final String result;
    public final boolean has_error;
    public final String error_message;

    public ScriptResult(String result,String error_message,boolean has_error) {
        this.result = result;
        this.error_message = error_message;
        this.has_error = has_error;
    }

}
