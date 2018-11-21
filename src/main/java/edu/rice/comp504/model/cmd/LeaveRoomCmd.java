package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class LeaveRoomCmd implements IUserCmd {
    private ChatRoom chatRoom;
    private User user;
    private static LeaveRoomCmd singletonleaveroomcmd;
    private LeaveRoomCmd(ChatRoom chatRoom, User user) {
        this.chatRoom=chatRoom;
        this.user=user;
    }

    public static LeaveRoomCmd makeCmd(ChatRoom chatRoom, User user){
        if ( singletonleaveroomcmd == null ) {
             singletonleaveroomcmd = new LeaveRoomCmd(chatRoom, user);

        }
        else {
            singletonleaveroomcmd.setChatRoom(chatRoom);
            singletonleaveroomcmd.setUser(user);
        }
        return singletonleaveroomcmd;

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
        chatRoom.removeUser(user,user.getName()+ "leaves chatroom");
        user.moveToAvailable(chatRoom);
    }
}
