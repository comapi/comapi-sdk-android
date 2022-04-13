package com.comapi.internal.push;

import java.util.Map;

/**
 * @author Marcin Swierczek
 * @since 1.4.0
 */
public
class Action {

    private String action;
    private String link;
    private String id;

    public Action(Map<String, String> map) {
        action = map.get("action");
        link = map.get("link");
        id = map.get("id");
    }

    public String getLink() {
        return link;
    }

    public String getId() {
        return id;
    }

    public String getAction() {
        return action;
    }
}
