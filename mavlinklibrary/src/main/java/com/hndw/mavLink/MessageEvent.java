package com.hndw.mavLink;

import android.os.Bundle;

public class MessageEvent {
    private String eventKey;
    private Bundle bundle;
    public Bundle getBundle() {
        return bundle;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}
