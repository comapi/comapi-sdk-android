package com.comapi.internal.push;

/**
 * @author Marcin Swierczek
 * @since 1.4.0
 */
public class PushDataKeys {

    /*
        "data": {
	        "dotdigital" : {
	            "messageId": "123",
	            "notification": {
	            	"title": "Comapi news!",
	            	"body": "Push message send from Comapi",
	            	"channelId": "id"
	            },
	            "actions": [
		            {
		            	"action": "notificationClick",
		            	"link": "http:/google.com",
		            	"id": "123"
		            }
	            ]
	        }
	    }
     */

    public static final String KEY_PUSH_MAIN = "dotdigital";
    public static final String KEY_PUSH_MESSAGE_ID = "messageId";
    public static final String KEY_PUSH_NOTIFICATION = "notification";
    public static final String KEY_PUSH_ACTIONS = "actions";
    public static final String KEY_PUSH_ACTION = "action";

    public static final String PUSH_CLICK_ACTION = "notificationClick";

    public static final String KEY_DEEP_LINK = "link";
    public static final String KEY_ACTION_ID = "id";
}
