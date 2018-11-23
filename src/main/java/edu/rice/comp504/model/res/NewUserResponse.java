package edu.rice.comp504.model.res;

public class NewUserResponse extends AResponse {
    private int userID;
    private String userName;

    public NewUserResponse(int userID, String userName) {
        super("NewUserResponse");
        this.userID = userID;
        this.userName = userName;
    }

}
