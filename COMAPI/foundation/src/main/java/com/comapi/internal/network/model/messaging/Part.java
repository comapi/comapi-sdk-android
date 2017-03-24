package com.comapi.internal.network.model.messaging;

import com.google.gson.annotations.SerializedName;

/**
 * Part of the message.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Part {

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("url")
    private String url;

    @SerializedName("data")
    private String data;

    @SerializedName("size")
    private long size;

    /**
     * Message part name
     *
     * @return Message part name
     */
    public String getName() {
        return name;
    }

    /**
     * Message part type
     *
     * @return Message part type
     */
    public String getType() {
        return type;
    }

    /**
     * Message part url
     *
     * @return Message part url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Message part data
     *
     * @return Message part data
     */
    public String getData() {
        return data;
    }

    /**
     * Message part size
     *
     * @return Message part size
     */
    public long getSize() {
        return size;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        Part part;

        public Builder() {
            part = new Part();
        }

        /**
         * Sets the name of the message part.
         */
        public Builder setName(String name) {
            part.name = name;
            return this;
        }

        /**
         * Sets the type of the message part.
         */
        public Builder setType(String type) {
            part.type = type;
            return this;
        }

        /**
         * Sets url.
         */
        public Builder setUrl(String url) {
            part.url = url;
            return this;
        }

        /**
         * Sets the message data.
         */
        public Builder setData(String data) {
            part.data = data;
            return this;
        }

        /**
         * Sets the message data size.
         */
        public Builder setSize(long size) {
            part.size = size;
            return this;
        }

        public Part build() {
            return part;
        }
    }
}
