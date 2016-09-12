package dk.mudlogic.cloudmon.client_v3.client;

/**
 * Created by soren.pedersen on 11-09-2016.
 */
public class v3CommandResult {

    private final String RESULT;
    private final int RESULT_HASH;

    private String parsed_result;

    private String error_messages;

    public v3CommandResult(String result) {
        this.RESULT = result;
        this.RESULT_HASH = this.RESULT.hashCode();
    }

    public int getResultHash() {
        return this.RESULT_HASH;
    }

    public String getResult() {
        return this.RESULT;
    }

    public String getParsedResult() {
        return parsed_result;
    }

    public void setParsedResult(String parsed_result) {
        this.parsed_result = parsed_result;
    }

    public String getErrorMessages() {
        return error_messages;
    }

    public void setErrorMessages(String error_messages) {
        this.error_messages = error_messages;
    }
}
