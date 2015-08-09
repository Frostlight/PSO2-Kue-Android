package frostlight.pso2kue.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * The Objectify object model for device registrations we are persisting
 */
@Entity
public class ShipNotifyRecord {

    @Id
    Long id;

    @Index
    private long lastDate;
    // you can add more fields...

    public ShipNotifyRecord() {
    }

    public long getLastDate() {
        return lastDate;
    }

    public void setLastDate(long lastDate) {
        this.lastDate = lastDate;
    }
}