package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class LeaveRoomCmd implements IUserCmd {
    private ChatRoom chatRoom;
    private User user;
    private String reason;


    public LeaveRoomCmd(ChatRoom chatRoom, User user, String reason) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.reason = reason;
    }


    public User getUser() {
        return user;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void execute(User context) {
        if (context.getId() == user.getId()) {
            chatRoom.removeUser(user, reason);
            user.moveToAvailable(chatRoom);
        }
    }
}
