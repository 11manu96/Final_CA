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
    $("#slt-room-users").click(queryMessages);
    $("#btn-send").click(sendMessage);
    $("#btn-send-all").click(sendAll);

    webSocket.onmessage = function(message) {
        updateChatApp(message);
    };
}

function clearRoomUI() {
    $("#chat-dialog").empty();
    $("#slt-room-users").empty();
    $("#room-title").text("Room Name");
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
    clearRoomUI();
    webSocket.send(JSON.stringify({"type": "query", "body": {"query": "roomUsers", "roomId": selectedRoom}}));
}

/**
 * Send request to websocket to exit all rooms
 */
function exitRoom() {
    // TODO: enforce single selection
    var selectedRoom = $("#slt-joined-rooms").val()[0];

    if (currentRoom == selectedRoom) {
        currentRoom = null;
        clearRoomUI();
    }
    webSocket.send(JSON.stringify({"type": "leave", "body": {"roomId": selectedRoom}}));
}

/**
 * Send request to websocket to exit all rooms
 */
function exitAllRooms() {
    currentRoom = null;
    clearRoomUI();
    webSocket.send(JSON.stringify({"type": "leave", "body": {"roomId": "All"}}));
}

/**
 * Send request to websocket to join room
 */
function joinRoom() {
    // TODO: enforce single selection
    var selectedRoom = $("#slt-available-rooms").val()[0];

    currentRoom = selectedRoom;
    clearRoomUI();
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
function queryMessages() {
    var user = $("#slt-room-users").val()[0];
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
    $("#chat-message").val("");
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


function loadRoomLists(responseBody) {
    $("#slt-joined-rooms").empty();
    $("#slt-available-rooms").empty();

    // need to get room name somehow
    responseBody.joinedRoomIds.forEach(function (roomId) {
        $("#slt-joined-rooms").append($("<option></option>").attr("value", roomId).text(roomNames[roomId].name));
    });
    responseBody.availableRoomIds.forEach(function (roomId) {
        $("#slt-available-rooms").append($("<option></option>").attr("value", roomId).text(roomNames[roomId].name));
    });
}


function loadRoomUsers(responseBody) {
    // only update room users if room is currently open
    if (currentRoom == responseBody.roomId) {
        roomUsers = {};
        $("#slt-room-users").empty();
        var userList = responseBody.users;
        Object.keys(userList).map(function (key) {
            $("#slt-room-users").append($("<option></option>").attr("value", Number(key)).text(userList[key]));
            roomUsers[key] = userList[key];
        });
        $("#room-title").text(roomNames[responseBody.roomId].name);
        // disable send all button unless owner
        if (currentUser === roomNames[currentRoom].owner) {
            $(".owner-only").prop("disabled", false);
        } else {
            $(".owner-only").prop("disabled", true);
        }
    }
}

function addNewUser(responseBody) {
    // enable buttons
    if (loggedIn === false) {
        loggedIn = true;
        $(".logged-in").prop("disabled", false);
        $(".not-logged-in").prop("disabled", true);
    }
    currentUser = responseBody.userId;
}


function addNewRoom(responseBody) {
    roomNames[responseBody.roomId] = {"name": responseBody.roomName, "owner": responseBody.ownerId};
    // put owner in room
    if (currentUser == responseBody.ownerId) {
        currentRoom = responseBody.roomId;
    }
}


function loadMessages(responseBody) {
    // TODO: check that message is for the room currently open?
    $("#chat-dialog").empty();
    responseBody.chatHistory.forEach(function (message) {
        $("#chat-dialog").append($("<ul></ul>").text(
            roomUsers[message.senderId] + "->" + roomUsers[message.receiverId] + ": " + message.message));
    });
}


function loadNotifications(responseBody) {
    $("#room-notification").text(responseBody.notifications[responseBody.notifications.length - 1]);
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
        loadRoomLists(responseBody);
    } else if (responseBody.type === "RoomUsers") {
        loadRoomUsers(responseBody);
    } else if (responseBody.type === "NewUser") {
        addNewUser(responseBody);
    } else if (responseBody.type === "NewRoom") {
        addNewRoom(responseBody);
    } else if (responseBody.type === "UserChatHistory") {
        loadMessages(responseBody);
    } else if (responseBody.type === "RoomNotifications") {
        loadNotifications(responseBody);
    }
}