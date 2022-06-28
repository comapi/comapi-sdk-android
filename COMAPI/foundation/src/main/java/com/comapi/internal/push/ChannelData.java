package com.comapi.internal.push;

/**
 * @author Marcin Swierczek
 * @since 1.4.0
 */
class ChannelData {

    private final String id;
    private final String name;
    private final String description;

    ChannelData(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
