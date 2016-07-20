package dk.mudlogic.cloudmon.client_v2;

import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.time.TimeHandler;

import java.util.Hashtable;

/**
 * Created by soren.pedersen on 10-07-2016.
 */
public abstract class CloudMon_v2Process {

    private LogTracer log = new LogFactory().tracer();

    private v2ProcessConfig config;
    private TimeHandler time = new TimeHandler();

    //Holds the currently active process threads
    private Hashtable<Long,Thread> active_process = new Hashtable<>();

    //Holds command data for the selected process
    private v2ProcessCommand selected_command;

    public CloudMon_v2Process(v2ProcessConfig config) {
        this.config = config;
    }

    abstract void processExecute(v2ProcessCommand process_command);

    /** Return the currently selected process
     *
     * @return
     */
    public v2ProcessCommand current_command() { return selected_command; }

    /** Returns if the next process is ready to run
     *
     * @return boolean
     */
    private boolean isProcessReady() {
        //Ready next command table in the list
        selected_command = this.config.getNextItem();

        int updated = selected_command.lastChanged();
        int interval = selected_command.get_int("check_interval");
        long thread_id = selected_command.threadID();

        //Checks if time has passed process interval
        if ( time.unixTimeDiff(updated) > interval ) {

            //If process does not exist or exist but is finished | TRUE
            if ( (active_process.containsKey(thread_id) == false) ||  ((active_process.containsKey(thread_id)) && (active_process.get(thread_id).isAlive() == false)) ) {
                //log.trace("Process time ");

                //Try to remove thread from list
                //to ensure thread is not cached when done no matter what
                try { active_process.remove(thread_id); }
                catch (Exception e) { }

                return true;
            }
            //Assume process running | FALSE
            else {
                return false;
            }

        }
        //If interval is not passed | FALSE
        else {
            return false;
        }

    }

    private void startProcess() {
        //log.trace("Process start ");

        v2ProcessThread pt = new v2ProcessThread(this, selected_command);

        Thread t = new Thread( pt );

        active_process.put(t.getId(),t);
        t.start();

        selected_command.lastChanged(time.unixTime());
        selected_command.threadID(t.getId());

    }

    public void start() {

        if (isProcessReady()) {
            startProcess();

            config.update_row(selected_command);
        }

    }

    /** This runnable is used to execute the selected process
     * It is a fire and forget class only to execute process
     *
     */
    private class v2ProcessThread implements Runnable {
        private CloudMon_v2Process p;
        private v2ProcessCommand pCmd;

        public v2ProcessThread(CloudMon_v2Process p,v2ProcessCommand pCmd) {
            this.p = p;
            this.pCmd = pCmd;
        }

        @Override
        public void run() {
            this.p.processExecute(this.pCmd);
        }
    }

}
