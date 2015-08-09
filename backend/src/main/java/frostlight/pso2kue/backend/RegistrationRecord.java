package frostlight.pso2kue.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * The Objectify object model for device registrations we are persisting
 */
@Entity
public class RegistrationRecord {

    @Id
    Long id;

    @Index
    private String regId;

    @Index
    private int ship;

    public RegistrationRecord() {
    }

    public String getRegId() {
        return regId;
    }


    public void setRegId(String regId) {
        this.regId = regId;
    }

    public int getShip() {
        return ship;
    }

    public void setShip(int ship) {
        this.ship = ship;
    }
}