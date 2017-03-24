package com.comapi.internal.network.model.events;

import com.google.gson.annotations.SerializedName;

/**
 * Event received trough socket. Socket connection established.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SocketStartEvent extends Event {

    /**
     * Unique identifier of the socket event.
     */
    public static final String TYPE = "socket.info";

    @SerializedName("socketId")
    protected String socketId;

    /**
     * Gets id of the socket connection.
     *
     * @return Id of the socket connection.
     */
    public String getSocketId() {
        return socketId;
    }

    @Override
    public String toString() {
        return super.toString() + " | socket = " + socketId;
    }
}