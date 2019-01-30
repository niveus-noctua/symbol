package com.symbol.user;

import java.util.Date;

public class UserPresence {

    private boolean status;
    private Date timestamp;

    public UserPresence() {}

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getStatus() {
        return status;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
