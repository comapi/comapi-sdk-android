package com.comapi.internal.network.model.events;

import com.google.gson.annotations.SerializedName;

/**
 * Generic socket event.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Event {

    public static final String KEY_NAME = "name";

    public static final String KEY_ID = "eventId";

    @SerializedName(KEY_ID)
    protected String eventId;

    @SerializedName(KEY_NAME)
    protected String name;

    /**
     * Gets unique socket event identifier.
     *
     * @return Unique socket event identifier.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Gets event name.
     *
     * @return Event name.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Comapi event " + name + " : eventId = " + eventId;
    }
}