package com.comapi.internal.network.model.events;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Event received trough socket.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ProfileUpdateEvent extends Event {

    public static final String TYPE = "profile.update";

    @SerializedName("profileId")
    protected String profileId;

    @SerializedName("publishedOn")
    protected String publishedOn;

    @SerializedName("revision")
    protected int revision;

    @SerializedName("context")
    protected ProfileContext context;

    @SerializedName("payload")
    protected Map<String, Object> payload;

    @SerializedName("apiSpaceId")
    protected String apiSpaceId;

    /**
     * Gets id for a Comapi user profile that was updated.
     *
     * @return Gets id for a Comapi user profile that was updated.
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Gets time when the update event was published.
     *
     * @return Time when the update event was published.
     */
    public String getPublishedOn() {
        return publishedOn;
    }

    /**
     * Gets revision of the profile details on the server.
     *
     * @return Revision of the profile details on the server.
     */
    public int getRevision() {
        return revision;
    }

    /**
     * Gets profileId of an user that performed this update.
     *
     * @return ProfileId of an user that performed this update.
     */
    public String getCreatedBy() {
        return context != null ? context.createdBy : null;
    }

    /**
     * Gets profile update details.
     *
     * @return Profile update details.
     */
    public Map<String, Object> getPayload() {
        return payload;
    }

    /**
     * Gets API Space in which profile exist.
     *
     * @return API Space in which profile exist.
     */
    public String getApiSpaceId() {
        return apiSpaceId;
    }

    private class ProfileContext {

        @SerializedName("createdBy")
        protected String createdBy;

    }

    @Override
    public String toString() {
        return super.toString() +
                " | profile " + profileId +
                " | updated on " + publishedOn +
                " | created by = " + getCreatedBy();
    }
}