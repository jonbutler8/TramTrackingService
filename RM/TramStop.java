package tramsimulate;

/**  Class for representing tram stop connections as a linked list ***/
public class TramStop {
    private TramStop next; // the next stop in the route (forward)
    private TramStop previous; // the previous stop in the route (forward)
    private int num; // tram stop number.
    private boolean finalized = false; // Flag allowing the stop to be set up only once
    
    public TramStop (int num, TramStop previous) {
        this.num = num;
        this.previous = previous;
    }
    
    // Sets the next stop. May only be set once, to make the stop immutable
    // Beyond the initial creation stage
    public void setNext(TramStop next) {
        if (!finalized) {
            finalized = true;
            this.next = next;
        }
    }
    
    // Get the stop number
    public int getNum() {
        return num;
    }

    // Returns the next stop for a tram going forward
    public TramStop getNextStopForward() {
        return next == null ? previous : next;
    }
    
    // Returns the next stop for a tram going backward
    public TramStop getNextStopBackward() {
        return previous == null ? next : previous;
    }

    public TramStop getPrevious() {
        return previous;
    }
    public TramStop getNext() {
        return next;
    }
}