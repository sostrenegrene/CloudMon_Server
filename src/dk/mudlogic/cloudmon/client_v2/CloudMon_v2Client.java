package dk.mudlogic.cloudmon.client_v2;

import dk.mudlogic.ServerGlobalData;
import dk.mudlogic.cloudmon.store.DB_ProcessReturnData;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

/**
 * Created by soren.pedersen on 10-07-2016.
 */
public class CloudMon_v2Client extends CloudMon_v2Process {

    private LogTracer log = new LogFactory().tracer();

    private DB_ProcessReturnData returnData;

    public CloudMon_v2Client(DB_ProcessReturnData returnData,v2ProcessConfig config) {
        super(config);
        this.returnData = returnData;

        log.setTracerTitle(CloudMon_v2Client.class);

    }

    /** The active content of what is running
     * in the thread in the background
     * This will be call when the thread is activated
     *
     */
    @Override
    void processExecute(v2ProcessCommand process_table) {

        switch(process_table.get_str("process_type")) {
            case "query":
                execDBQuery(process_table);
                break;

            case "command":
                execCommandLine(process_table);
                break;

            default:
                break;
        }

    }

    /** Executes command line process
     *
     * @param pTable Hashtable
     */
    private void execCommandLine(v2ProcessCommand pTable) {
        //log.trace("Exec Command line");
        new Process_CMDQuery(ServerGlobalData.PROCESS_RETURN_DATA,pTable);
    }

    /** Executes SQL query
     *
     * @param pTable
     */
    private void execDBQuery(v2ProcessCommand pTable) {
        //log.trace("Exec Database query");

        new Process_DBQuery(this.returnData,pTable);

    }

}
