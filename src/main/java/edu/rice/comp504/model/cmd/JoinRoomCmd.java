package edu.rice.comp504.model.cmd;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class JoinRoomCmd implements IUserCmd{
    private ChatRoom chatRoom;
    private User user;
    private static JoinRoomCmd instance;

    /**
     * Constructor.
     * @param chatRoom chatRoom
     * @param user user
     */
    private JoinRoomCmd(ChatRoom chatRoom, User user){
        this.chatRoom = chatRoom;
        this.user = user;
    }

    /**
     * Return to a JoinRoomCmd instance.
     * @param chatRoom chatRoom
     * @param user user
     * @return a JoinRoomCmd
     */
    public static JoinRoomCmd makeCmd(ChatRoom chatRoom, User user){
        if (instance == null){
            instance = new JoinRoomCmd(chatRoom, user);
        } else {
            instance.setChatRoom(chatRoom);
            instance.setUser(user);
        }
        return instance;
    }

    /**
     * Helper function get user.
     * @return user
     */
    public User getUser(){
        return user;
    }

    /**
     * Helper function get chatRoom.
     * @return chatRoom
     */
    public ChatRoom getChatRoom(){
        return chatRoom;
    }

    /**
     * Helper function set chatRoom.
     * @param chatRoom chatRoom
     */
    public void setChatRoom(ChatRoom chatRoom){
        this.chatRoom = chatRoom;
    }

    /**
     * Helper function set user.
     * @param user user
     */
    public void setUser(User user){
        this.user = user;
    }

    /**
     * Execute the command.
     * @param context user
     */
    @Override
    public void execute(User context) {
        chatRoom.addUser(context);
        context.moveToJoined(chatRoom);
    }
}
