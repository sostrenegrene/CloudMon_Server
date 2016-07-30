package dk.mudlogic.cloudmon.config;

/**
 * Created by soren.pedersen on 29-07-2016.
 */
public class CloudMon_ConfigThread implements Runnable {

    private boolean isAlive = true;
    private CloudMon_PrepareConfig pconfig;

    public CloudMon_ConfigThread(CloudMon_PrepareConfig pconfig) {
        this.pconfig = pconfig;
    }

    public CloudMon_PrepareConfig getConfig() {
        return pconfig;
    }

    public void stop() {
        this.isAlive = false;
    }

    private void sleep(int time) {
        try { Thread.sleep( (time * 1000) ); }
        catch(Exception e) {}
    }

    @Override
    public void run() {

        while(isAlive) {

            pconfig.setup_config();
            pconfig.setup_clients();

            sleep(30);
        }

    }
}
