package dk.mudlogic.cloudmon.query;

import dk.mudlogic.ScriptManager;
import dk.mudlogic.cloudmon.client_v2.v2ProcessCommand;

/**
 * Created by soren.pedersen on 20-07-2016.
 */
public class Process_CMDQuery {

    private v2ProcessCommand pTable;

    private String result_str;

    public Process_CMDQuery(v2ProcessCommand pTable) {
        this.pTable = pTable;

        connect();
    }

    private void connect() {
        result_str = new CMDQuery(pTable.get_str("host_name"),pTable.get_str("query_string")).result();
    }

    private void process() {

        //Run any parser scripts for the process
        if (result_str != null) {
            //Run script manager with result string
            result_str = new ScriptManager(pTable.get_str("parser_script")).parse("toJAVA",result_str);
        }

    }

    private void finish() {

    }

}
