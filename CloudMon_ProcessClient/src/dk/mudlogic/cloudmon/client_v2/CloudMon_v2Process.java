package dk.mudlogic.cloudmon.client_v2;

import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.time.TimeHandler;

import java.util.Hashtable;

/**
 * Created by soren.pedersen on 10-07-2016.
 *
 * CloudMon_v2Process is an abstract class that handles the execution of
 * the CloudMon_v2Client it extends
 *
 * It will start a new thread that calls the clients processExecute() method
 * along with the process command for the client to execute
 *
 */
public abstract class CloudMon_v2Process {

    private LogTracer log = new LogFactory().tracer();

    //Config table for the process command
    private v2ProcessConfig config;

    private TimeHandler time = new TimeHandler();

    //Holds the currently active process threads
    private Hashtable<Long,Thread> active_process = new Hashtable<>();

    //Holds command data for the selected process
    private v2ProcessCommand selected_command;

    public CloudMon_v2Process(v2ProcessConfig config) {
        this.config = config;
    }

    //Will update config table for process
    public void update_config(v2ProcessConfig config) { this.config = config; }

    //to be override in the client
    abstract void processExecute(v2ProcessCommand process_command);

    /** Return the currently selected process
     *
     * @return
     */
    public v2ProcessCommand current_command() { return selected_command; }

    public void update_current_command(v2ProcessCommand cmd) {
        selected_command = cmd;
        config.update_row(selected_command);
    }

    /** Returns if the next process is ready to run
     *
     * @return boolean
     */
    private boolean isProcessReady() {
        //Ready next command table in the list
        selected_command = this.config.getNextItem();

        //Get when the command was last run
        int updated = selected_command.lastChanged();
        //Get the interval time for the command
        int interval = selected_command.get_int("check_interval");
        //Get thread id
        long thread_id = selected_command.threadID();

        //Checks if last run time has passed process interval time
        if ( time.unixTimeDiff(updated) >= interval ) {

            //If process is running late, write warning message
            if ( time.unixTimeDiff(updated) > (interval+1) ) {
                log.warning(selected_command.get_str("client_name")+"/"+selected_command.get_str("process_name") + " is " + (time.unixTimeDiff(updated) - interval) + " sec. late!");
            }

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

    /** Runs the process in a new thread
     *
     */
    private void startProcess() {
        //log.trace("Process start ");

        //Create new thread handler with process command
        v2ProcessThread pt = new v2ProcessThread(this, selected_command);
        Thread t = new Thread( pt );

        //For safty
        //Only start the new thread if selected command's thread id does not exits
        if (active_process.containsKey(selected_command.threadID()) == false) {
            active_process.put(t.getId(), t);
            t.start();

            //Update commands last run time
            selected_command.lastChanged(time.unixTime());
            //Update thread id
            selected_command.threadID(t.getId());
        }

    }

    /** Start new process
     *
     */
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
