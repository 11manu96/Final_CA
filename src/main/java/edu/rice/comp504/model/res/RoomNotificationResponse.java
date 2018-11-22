package edu.rice.comp504.model.res;

import java.util.List;

public class RoomNotificationResponse extends AResponse {
    private int roomid;
    private List<String> notifications;
    public RoomNotificationResponse(String type, List<String> notifications) {
        super(type);
        this.notifications = notifications;
    }
}
