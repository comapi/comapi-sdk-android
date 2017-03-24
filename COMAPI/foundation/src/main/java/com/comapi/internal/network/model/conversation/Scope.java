package com.comapi.internal.network.model.conversation;

/**
 * Public or Participant scope of the Conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public enum Scope {

    PUBLIC("public"),
    PARTICIPANT("participant");

    private final String value;

    Scope(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
