package com.comapi.internal.network.model.events.conversation.message;

/**
 * Event received trough socket. Message was read.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MessageReadEvent extends MessageUpdateEvent {

    public static final String TYPE = "conversationMessage.read";

}