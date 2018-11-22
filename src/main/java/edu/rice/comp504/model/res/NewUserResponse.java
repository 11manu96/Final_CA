package edu.rice.comp504.model.res;

public class NewUserResponse extends AResponse {
    private int userID;
    private String userName;

    public NewUserResponse(String type, int userID, String userName) {
        super(type);
        this.userID = userID;
        this.userName = userName;
    }

}
