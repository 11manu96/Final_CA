"use strict";

const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chatapp");
var loggedIn = false;
var currentRoom = null;
var currentUser = null;
var roomNames = {};
var roomUsers = {};

/**
 * Entry point into chat room
 */
window.onload = function() {
    $("#btn-login").click(logIn);
    $("#btn-enter").click(enterRoom);
    $("#btn-exit").click(exitRoom);
    $("#btn-exit-all").click(exitAllRooms);
    $("#btn-join").click(joinRoom);
    $("#btn-create").click(createRoom);
    $("#slt-room-users").click(loadMessages);
    $("#btn-send").click(sendMessage);
    $("#btn-send-all").click(sendAll);

    webSocket.onmessage = function(message) {
        updateChatApp(message);
    };
}

/**
 * Send request to websocket to create user
 */
function logIn() {
    var userName = $("#user-name").val();
    var userAge = $("#user-age").val();
    var userLocation = $("#user-location").val();
    var userSchool = $("#user-school").val();

    console.log(userLocation + userSchool);
    if (userName == ""){
        document.getElementById("user-name").className = document.getElementById("user-name").className + " error";
    }else if (userAge == ""){
        document.getElementById("user-age").className = document.getElementById("user-age").className + " error";
    }else{
        webSocket.send(JSON.stringify({"type": "login", "body":
                {"name": userName, "age": userAge, "location": userLocation, "school": userSchool}}));
    }




}

/**
 * Send request to websocket to enter room
 */
function enterRoom() {
    var selectedRoom = $("#slt-joined-rooms").val();
    if(selectedRoom.length > 1){
        document.getElementById("slt-joined-rooms").className = document.getElementById("slt-joined-rooms").className + " error";
    }else {
        webSocket.send(JSON.stringify({"type": "query", "body": {"query": "roomUsers", "roomId": selectedRoom}}));
    }
}

/**
 * Send request to websocket to exit all rooms
 */
function exitRoom() {
    var selectedRoom = $("#slt-joined-rooms").val();
    if(selectedRoom.length > 1){
        document.getElementById("slt-joined-rooms").className = document.getElementById("slt-joined-rooms").className + " error";
    }else {
        webSocket.send(JSON.stringify({"type": "leave", "body": {"roomId": selectedRoom}}));
    }
}

/**
 * Send request to websocket to exit all rooms
 */
function exitAllRooms() {
    webSocket.send(JSON.stringify({"type": "leave", "body": {"roomId": "All"}}));
}

/**
 * Send request to websocket to join room
 */
function joinRoom() {
    var selectedRoom = $("#slt-available-rooms").val();
    if(selectedRoom.length > 1){
        document.getElementById("slt-available-rooms").className = document.getElementById("slt-available-rooms").className + " error";
    }else{
        webSocket.send(JSON.stringify({"type": "join", "body": {"roomId": selectedRoom}}));
    }

}

/**
 * Send request to websocket to create room
 */
function createRoom() {
    var roomName = $("#room-name").val();
    var roomMinAge = $("#room-min-age").val();
    var roomMaxAge = $("#room-max-age").val();
    var roomLocations = $("#slt-room-location").val();
    var roomSchools = $("#slt-room-school").val();

    if (roomName == ""){
        document.getElementById("room-name").className = document.getElementById("room-name").className + " error";
    }else if (roomMinAge == ""){
        document.getElementById("room-min-age").className = document.getElementById("room-min-age").className + " error";
    }else if (roomMaxAge == ""){
        document.getElementById("room-max-age").className = document.getElementById("room-max-age").className + " error";
    }else  if (roomLocations.length > 1){
        document.getElementById("slt-room-location").className = document.getElementById("slt-room-location").className + " error";
    }else  if (roomSchools.length > 1){
        document.getElementById("slt-room-school").className = document.getElementById("slt-room-school").className + " error";
    }else{
        console.log(roomMinAge)
        webSocket.send(JSON.stringify({"type": "create", "body":
                {"roomName": roomName, "ageLower": roomMinAge, "ageUpper": roomMaxAge,
                    "location": roomLocations, "school": roomSchools}}));
    }
}



/**
 * Send request to websocket to retrieve message history
 */
function loadMessages() {
    var user = $("#slt-room-users").val()[0];
    console.log(user);
    webSocket.send(JSON.stringify({"type": "query", "body":
            {"query": "userChatHistory", "roomId": currentRoom, "otherUserId": user}}));
}

/**
 * Send request to websocket to send message
 */
function sendMessage() {
    // enforce one user selected
    var user = $("#slt-room-users").val()[0];
    var message = $("#chat-message").val();
    webSocket.send(JSON.stringify({"type": "send", "body":
            {"roomId": currentRoom, "message": message, "receiverId": user}}));
}

/**
 * Send request to websocket to send message to all users
 */
function sendAll() {
    var message = $("#chat-message").val();
    webSocket.send(JSON.stringify({"type": "send", "body":
            {"roomId": currentRoom, "message": message, "receiverId": "All"}}));
}

/**
 * Receive data from websocket to update view
 * @param message data from websocket
 */
function updateChatApp(message) {
    // parse message to determine how to update view
    var responseBody = JSON.parse(message.data);
    console.log(responseBody);
    if (responseBody.type === "UserRoom") {
        $("#slt-joined-rooms").empty();
        $("#slt-available-rooms").empty();

        // need to get room name somehow
        responseBody.joinedRoomIds.forEach(function (roomId) {
            $("#slt-joined-rooms").append($("<option></option>").attr("value", roomId).text(roomNames[roomId].name));
        });
        responseBody.availableRoomIds.forEach(function (roomId) {
            $("#slt-available-rooms").append($("<option></option>").attr("value", roomId).text(roomNames[roomId].name));
        });
    } else if (responseBody.type === "RoomUsers") {
        roomUsers = {};
        $("#slt-room-users").empty();
        var userList = responseBody.users;
        Object.keys(userList).map(function (key) {
            $("#slt-room-users").append($("<option></option>").attr("value", Number(key)).text(userList[key]));
            roomUsers[key] = userList[key];
        });
        $("#room-title").text(roomNames[responseBody.roomId].name);
        currentRoom = responseBody.roomId;
        // disable send all button unless owner
        if (currentUser === roomNames[currentRoom].owner) {
            $(".owner-only").prop("disabled", false);
        } else {
            $(".owner-only").prop("disabled", true);
        }
    } else if (responseBody.type === "NewUser") {
        // enable buttons
        if (loggedIn === false) {
            loggedIn = true;
            $(".logged-in").prop("disabled", false);
            $(".not-logged-in").prop("disabled", true);
        }
        currentUser = responseBody.userId;
    } else if (responseBody.type === "NewRoom") {
        roomNames[responseBody.roomId] = {"name": responseBody.roomName, "owner": responseBody.ownerId};
    } else if (responseBody.type === "UserChatHistory") {
        // TODO: check that message is for the room currently open
        $("#chat-dialog").empty();
        responseBody.chatHistory.forEach(function (message) {
            $("#chat-dialog").append($("<ul></ul>").text(
                roomUsers[message.senderId] + "->" + roomUsers[message.receiverId] + ": " + message.message));
        });
    } else if (responseBody.type === "RoomNotifications") {
        $("#room-notification").text(responseBody.notifications[responseBody.notifications.length - 1]);
    }
}

function clearError(){
    document.getElementById("user-name").className = document.getElementById("user-name").className.replace(" error", "");
    document.getElementById("user-age").className = document.getElementById("user-age").className.replace(" error", "");
}

function clearChatRoomError(){
    document.getElementById("room-name").className = document.getElementById("room-name").className.replace(" error", "");
    document.getElementById("room-min-age").className = document.getElementById("room-min-age").className.replace(" error", "");
    document.getElementById("room-max-age").className = document.getElementById("room-max-age").className.replace(" error", "");
    document.getElementById("slt-room-location").className = document.getElementById("slt-room-location").className.replace(" error", "");
    document.getElementById("slt-room-school").className = document.getElementById("slt-room-school").className.replace(" error", "");
}

function clearRoomSelectError() {
    document.getElementById("slt-joined-rooms").className = document.getElementById("slt-joined-rooms").className.replace(" error", "");
    document.getElementById("slt-available-rooms").className = document.getElementById("slt-available-rooms").className.replace(" error", "");
}