package dk.mudlogic.cloudmon.client_v2;

import dk.mudlogic.cloudmon.store.DB_ProcessReturnData;
import dk.mudlogic.mail.MailMan;
import dk.mudlogic.tools.config.GroupConfig;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

/** @deprecated
 * Created by soren.pedersen on 10-07-2016.
 *
 * CloudMon_v2Client does all command call handling
 * to either database or commandline.
 * Client is loaded in CloudMon_PrepareConfig
 * This client is saved in the main client array
 *
 * Extends Cloudmon_v2Process that handles all execution of the client
 * v2Process handles the thread that runs and calls the clients processExecute() method
 *
 */
public class CloudMon_v2Client extends CloudMon_v2Process {

    private LogTracer log = new LogFactory().tracer();

    private DB_ProcessReturnData returnData;
    private GroupConfig MAIN_CONFIG;
    private MailMan MAILMAN;

    private int result_hash;

    public CloudMon_v2Client(GroupConfig main_config,DB_ProcessReturnData returnData,v2ProcessConfig config) {
        super(config);
        this.returnData = returnData;
        this.MAIN_CONFIG = main_config;
        this.result_hash = 0;
        this.MAILMAN = new MailMan(main_config.group("mail").get("mail_url").toString());

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
       Process_CMDQuery cmdQuery = new Process_CMDQuery(this.MAIN_CONFIG,this.returnData,pTable,this.MAILMAN);
        this.update_current_command(cmdQuery.getpTable());
    }

    /** Executes SQL query
     *
     * @param pTable
     */
    private void execDBQuery(v2ProcessCommand pTable) {
        //log.trace("Exec Database query");
        Process_DBQuery dbQuery = new Process_DBQuery(this.MAIN_CONFIG,this.returnData,pTable,this.MAILMAN);
        this.update_current_command(dbQuery.getpTable());
    }

}
