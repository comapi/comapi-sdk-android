package com.comapi.internal.push;

/**
 * @author Marcin Swierczek
 * @since 1.4.0
 */
public class PushDataKeys {

    /*
    OLD
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

    /*
    NEW
        data: {
            dotdigital : {
                title: "Dotdigital news!",
                body: "Push message send from Dotdigital",
                link: "http:/google.com",
                correlationId: "123",
                actionId: "id-12"
            }
        }
    */

    public static final String KEY_PUSH_MAIN = "dotdigital";
    public static final String PUSH_CLICK_ACTION = "notificationClick";
    public static final String KEY_PUSH_CORRELATION_ID = "correlationId";
    public static final String KEY_PUSH_TITLE = "title";
    public static final String KEY_PUSH_BODY = "body";
    public static final String KEY_PUSH_DEEP_LINK = "deepLink";
    public static final String KEY_PUSH_URL = "url";
    public static final String KEY_PUSH_ACTION_ID = "actionId";
}
