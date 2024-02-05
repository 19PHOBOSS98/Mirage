package net.phoboss.mirage.utility;

public class RedstoneStateChecker {
    public boolean previousState;
    public RedstoneStateChecker() {
        this.previousState = false;
    }
    public RedstoneStateChecker(boolean previousState) {
        this.previousState = previousState;
    }

    public boolean isRisingEdge(Boolean currentState) {
        return currentState && !this.previousState;
    }

    public boolean getPreviousState(){
        return this.previousState;
    }

    public void setPreviousState(Boolean currentState){
        this.previousState = currentState;
    }
}
