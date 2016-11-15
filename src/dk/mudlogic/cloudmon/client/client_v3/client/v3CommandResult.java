package dk.mudlogic.cloudmon.client.client_v3.client;

/** Data class for result data and error data, for v3Command_Process
 *
 * Created by soren.pedersen on 11-09-2016.
 */
public class v3CommandResult {

    private final String RESULT;
    private final int RESULT_HASH;

    private String parsed_result = "";
    private String error_messages = "";

    private boolean HAS_ERRORS;
    private boolean HAS_RESULT;

    /** Constructor
     *
     * @param result String
     */
    public v3CommandResult(String result) {
        this.RESULT      = result;
        this.RESULT_HASH = this.RESULT.trim().hashCode();

        if (getResultHash() == "".hashCode()) {
            this.HAS_RESULT = false;
        }
        else {
            this.HAS_RESULT = true;
        }
    }

    /** Get hash values of raw result data
     *
     * @return int
     */
    public int getResultHash() {
        return this.RESULT_HASH;
    }

    /** Returns raw result string
     *
     * @return String
     */
    public String getResult() {
        return this.RESULT;
    }

    /** Returns parsed result
     *
     * @return String
     */
    public String getParsedResult() {
        return parsed_result;
    }

    /** Adds parsed result data
     *
     * @param parsed_result String
     */
    public void setParsedResult(String parsed_result) {
        this.parsed_result = parsed_result;
    }

    /** Returns error messages for result
     *
     * @return String
     */
    public String getErrorMessages() {
        return error_messages;
    }

    /** Set error message for result
     * If the error string is not empty, hasErrors() == true
     *
     * @param error_messages String
     */
    public void setErrorMessages(String error_messages) {

        try {
            if (error_messages.trim().length() > 0) {
                this.HAS_ERRORS = true;
            } else {
                this.HAS_ERRORS = false;
            }

            this.error_messages = error_messages;
        }
        catch(Exception e) {
            this.HAS_ERRORS = false;
            this.error_messages = "";
        }


    }

    public boolean hasErrors() {
        return HAS_ERRORS;
    }

    public boolean hasResult() {
        return HAS_RESULT;
    }
}
