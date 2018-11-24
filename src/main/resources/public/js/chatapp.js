"use strict";

const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chatapp");
var loggedIn = false;
var currentRoom = null;

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
    $(".opt-room-user").click(loadMessages);
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
    webSocket.send(JSON.stringify({"type": "login", "body":
            {"name": userName, "age": userAge, "location": userLocation, "school": userSchool}}));
}

/**
 * Send request to websocket to enter room
 */
function enterRoom() {
    var selectedRoom = $("#slt-joined-rooms").val()[0];
    currentRoom = selectedRoom;
    webSocket.send(JSON.stringify({"type": "query", "body": {"query": "roomUsers", "roomId": selectedRoom}}));
}

/**
 * Send request to websocket to exit all rooms
 */
function exitRoom() {
    // TODO: enforce single selection
    var selectedRoom = $("#slt-joined-rooms").val()[0];
    webSocket.send(JSON.stringify({"type": "leave", "body": {"roomId": selectedRoom}}));
}

/**
 * Send request to websocket to exit all rooms
 */
function exitAllRooms() {

}

/**
 * Send request to websocket to join room
 */
function joinRoom() {
    // TODO: enforce single selection
    var selectedRoom = $("#slt-available-rooms").val()[0];
    webSocket.send(JSON.stringify({"type": "join", "body": {"roomId": selectedRoom}}));
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
    webSocket.send(JSON.stringify({"type": "create", "body":
            {"roomName": roomName, "ageLower": roomMinAge, "ageUpper": roomMaxAge,
                "location": roomLocations, "school": roomSchools}}));
}

/**
 * Send request to websocket to retrieve message history
 */
function loadMessages() {
    var user = $("#slt-room-users").val();
}

/**
 * Send request to websocket to send message
 */
function sendMessage() {
    var user = $("#slt-room-users").val();
    var message = $("#chat-message").val();
    webSocket.send(JSON.stringify({"type": "send", "body":
            {"roomId": userName, "message": message, "receiverId": user}}));
}

/**
 * Send request to websocket to send message to all users
 */
function sendAll() {
    var message = $("#chat-message").val();
    webSocket.send(JSON.stringify({"type": "send", "body":
            {"roomId": userName, "message": message, "receiverId": "All"}}));
}

/**
 * Receive data from websocket to update view
 * @param message data from websocket
 */
function updateChatApp(message) {
    // parse message to determine how to update view
    var responseBody = JSON.parse(message.data);
    console.log(responseBody);
    if (responseBody.type === "UserRoomResponse") {
        // login, create room, join room, exit room

        // enable buttons
        if (loggedIn === false) {
            loggedIn = true;
            $(".logged-in").prop("disabled", false);
            $(".not-logged-in").prop("disabled", true);
        }
        $("#slt-joined-rooms").empty();
        $("#slt-available-rooms").empty();
        // need to get room name somehow
        responseBody.joinedRoomIds.forEach(function(roomId) {
            $("#slt-joined-rooms").append($("<option></option>").attr("value", roomId).text('Room ' + roomId));
        });
        responseBody.availableRoomIds.forEach(function(roomId) {
            $("#slt-available-rooms").append($("<option></option>").attr("value", roomId).text('Room ' + roomId));
        });
    } else if (responseBody.type === "RoomUsersResponse") {
        console.log(responseBody.users);
        console.log(responseBody.users[0])
    }
}