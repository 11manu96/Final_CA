package edu.rice.comp504.model.res;

import edu.rice.comp504.model.obj.Message;

import java.util.List;

public class UserChatHistoryResponse extends AResponse {
    List<Message> chatHistory;

    /**
     * Constructor.
     * @param type the type of the response, i.e. the name of class
     */
    public UserChatHistoryResponse(List<Message> chatHistory) {
        super("UserChatHistoryResponse");
        this.chatHistory = chatHistory;
    }

}
