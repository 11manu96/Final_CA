package edu.rice.comp504.model.cmd;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class JoinRoomCmd implements IUserCmd{
    private ChatRoom chatRoom;
    private User user;
    private static JoinRoomCmd instance;

    private JoinRoomCmd(ChatRoom chatRoom, User user){
        this.chatRoom = chatRoom;
        this.user = user;
    }

    public static JoinRoomCmd makeCmd(ChatRoom chatRoom, User user){
        if (instance == null){
            instance = new JoinRoomCmd(chatRoom, user);
        } else {

        }
        return instance;
    }

    public User getUser(){
        return user;
    }

    public ChatRoom getChatRoom(){
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom){
        this.chatRoom = chatRoom;
    }

    public void setUser(User user){
        this.user = user;
    }

    @Override
    public void execute(User context) {
        chatRoom.addUser(context);
        context.addRoom(chatRoom);
    }
}
