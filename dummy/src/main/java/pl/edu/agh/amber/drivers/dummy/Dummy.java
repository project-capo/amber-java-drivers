package pl.edu.agh.amber.drivers.dummy;

public class Dummy {

    private boolean enabled;
    private String message;

    public Dummy(){
        this.message = "Message";
        this.enabled = false;
    }

    public synchronized String getMessage(){
        return message;
    }

    public synchronized void setMessage(String message){
        this.message = message;
    }

    public synchronized boolean isEnabled(){
        return enabled;
    }

    public synchronized void setEnabled(boolean enabled){
        this.enabled = enabled;
    }
}
