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

    $("#user-name").click(clearLoginError);
    $("#user-age").click(clearLoginError);

    $("#slt-joined-rooms").click(clearRoomSelectError);
    $("#slt-available-rooms").click(clearRoomSelectError);

    $("#room-name").click(clearCreateRoomError);
    $("#room-min-age").click(clearCreateRoomError);
    $("#room-max-age").click(clearCreateRoomError);
    $("#slt-room-location").click(clearCreateRoomError);
    $("#slt-room-school").click(clearCreateRoomError);

    $("#slt-room-users").click(clearChatRoomError);
    $("#chat-message").click(clearChatRoomError);

    setInterval(pingServer, 1000);

    webSocket.onmessage = function(message) {
        updateChatApp(message);
    };
}

/**
 * Clear chat room GUI elements
 */
function clearRoomUI() {
    $("#chat-dialog").empty();
    $("#slt-room-users").empty();
    $("#room-title").text("Room Name");
    $("#room-notification").empty();
}

/**
 * Clear login errors
 */
function clearLoginError() {
    $("#user-name").removeClass("error");
    $("#user-age").removeClass("error");
}

/**
 * Clear create chat room errors
 */
function clearCreateRoomError() {
    $("#room-name").removeClass("error");
    $("#room-min-age").removeClass("error");
    $("#room-max-age").removeClass("error");
    $("#slt-room-location").removeClass("error");
    $("#slt-room-school").removeClass("error");
}

/**
 * Clear chat room select errors
 */
function clearRoomSelectError() {
    $("#slt-joined-rooms").removeClass("error");
    $("#slt-available-rooms").removeClass("error");
}

/**
 * Clear chat room errors
 */
function clearChatRoomError() {
    $("#slt-room-users").removeClass("error");
    $("#chat-message").removeClass("error");
}

/**
 * Send ping request to keep session active
 */
function pingServer() {
    webSocket.send(JSON.stringify({"type": "ping"}));
}

/**
 * Send request to websocket to create user
 */
function logIn() {
    var userName = $("#user-name").val();
    var userAge = $("#user-age").val();
    var userLocation = $("#user-location").val();
    var userSchool = $("#user-school").val();

    // validate user name and age
    if (userName == "") {
        $("#user-name").addClass("error");
    } else if (userAge == "") {
        $("#user-age").addClass("error");
    } else {
        webSocket.send(JSON.stringify({"type": "login", "body":
                {"name": userName, "age": userAge, "location": userLocation, "school": userSchool}}));
    }
}

/**
 * Send request to websocket to enter room
 */
function enterRoom() {
    var selectedRoom = $("#slt-joined-rooms").val();

    // validate single room selected
    if (selectedRoom.length != 1) {
        $("#slt-joined-rooms").addClass("error");
    } else {
        currentRoom = selectedRoom;
        clearRoomUI();
        webSocket.send(JSON.stringify({"type": "query", "body": {"query": "roomUsers", "roomId": selectedRoom[0]}}));
    }
}

/**
 * Send request to websocket to exit all rooms
 */
function exitRoom() {
    var selectedRoom = $("#slt-joined-rooms").val();

    // validate single room selected
    if (selectedRoom.length != 1) {
        $("#slt-joined-rooms").addClass("error");
    } else {
        if (currentRoom == selectedRoom) {
          currentRoom = null;
          clearRoomUI();
        }
        webSocket.send(JSON.stringify({"type": "leave", "body": {"roomId": selectedRoom[0]}}));
    }
}

/**
 * Send request to websocket to exit all rooms
 */
function exitAllRooms() {
    currentRoom = null;
    clearRoomUI();
    clearRoomSelectError();
    webSocket.send(JSON.stringify({"type": "leave", "body": {"roomId": "All"}}));
}

/**
 * Send request to websocket to join room
 */
function joinRoom() {
    var selectedRoom = $("#slt-available-rooms").val();

    // validate single room selected
    if (selectedRoom.length != 1) {
        $("#slt-available-rooms").addClass("error");
    } else {
        currentRoom = selectedRoom;
        clearRoomUI();
        webSocket.send(JSON.stringify({"type": "join", "body": {"roomId": selectedRoom[0]}}));
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

    // validate room name and restrictions
    if (roomName == "") {
        $("#room-name").addClass("error");
    } else if (roomMinAge == "") {
        $("#room-min-age").addClass("error");
    } else if (roomMaxAge == "") {
        $("#room-max-age").addClass("error");
    } else if (roomLocations.length < 1) {
        $("#slt-room-location").addClass("error");
    } else if (roomSchools.length < 1) {
        $("#slt-room-school").addClass("error");
    } else {
        webSocket.send(JSON.stringify({"type": "create", "body":
                {"roomName": roomName, "ageLower": roomMinAge, "ageUpper": roomMaxAge,
                    "location": roomLocations, "school": roomSchools}}));
    }
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
    var user = $("#slt-room-users").val();
    var message = $("#chat-message").val();

    // validate send message
    if (user.length != 1) {
        $("#slt-room-users").addClass("error");
    } else if (message == "") {
        $("#chat-message").addClass("error");
    } else {
        $("#chat-message").val("");
        webSocket.send(JSON.stringify({"type": "send", "body":
                {"roomId": currentRoom, "message": message, "receiverId": user[0]}}));
    }
}

/**
 * Send request to websocket to send message to all users
 */
function sendAll() {
    var message = $("#chat-message").val();

    if (message == "") {
        $("#chat-message").addClass("error");
    } else {
        webSocket.send(JSON.stringify({"type": "send", "body":
                {"roomId": currentRoom, "message": message, "receiverId": "All"}}));
    }
}

/**
 * Update UI chat room lists
 * @param responseBody
 */
function loadRoomLists(responseBody) {
    $("#slt-joined-rooms").empty();
    $("#slt-available-rooms").empty();

    var exitCurrentRoom = true;

    // add joined roomrs
    responseBody.joinedRoomIds.forEach(function (roomId) {
        if (currentRoom == roomId) {
            exitCurrentRoom = false;
        }
        $("#slt-joined-rooms").append($("<option></option>").attr("value", roomId).text(roomNames[roomId].name));
    });

    // add available rooms
    responseBody.availableRoomIds.forEach(function (roomId) {
        $("#slt-available-rooms").append($("<option></option>").attr("value", roomId).text(roomNames[roomId].name));
    });

    // if room is gone for any reason exit room
    if (exitCurrentRoom) {
        currentRoom = null;
        clearRoomUI();
    }
}

/**
 * Update UI users in chat room
 * @param responseBody
 */
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

/**
 * Update UI user logged in
 * @param responseBody
 */
function addNewUser(responseBody) {
    // enable buttons
    if (loggedIn === false) {
        loggedIn = true;
        $(".logged-in").prop("disabled", false);
        $(".not-logged-in").prop("disabled", true);
    }
    currentUser = responseBody.userId;
}

/**
 * Update UI add new room mapping
 * @param responseBody
 */
function addNewRoom(responseBody) {
    roomNames[responseBody.roomId] = {"name": responseBody.roomName, "owner": responseBody.ownerId};
    // put owner in room
    if (currentUser == responseBody.ownerId) {
        currentRoom = responseBody.roomId;
    }
}

/**
 * Update UI load messages
 * @param responseBody
 */
function loadMessages(responseBody) {
    // select user with no message history
    if (responseBody.chatHistory == null) {
        $("#chat-dialog").empty();
    }
    // only update messages if chat room is open
    if (responseBody.chatHistory != null) {
        if (currentRoom == responseBody.chatHistory[0].roomId) {
            $("#chat-dialog").empty();
            responseBody.chatHistory.forEach(function (message) {
                var messageElement = $("<ul></ul>")
                    .text(roomUsers[message.senderId] + "->" + roomUsers[message.receiverId] + ": " + message.message);

                // mark messages sent by user as read
                if (message.isReceived == true){
                    if (message.senderId == currentUser) {
                        messageElement.addClass("received");
                    }
                } else {
                    // acknowledge receipt of message by user
                    if (message.receiverId == currentUser){
                        webSocket.send(JSON.stringify({"type": "ack", "body": {"messageId": message.id}}));
                    }
                }
                $("#chat-dialog").append(messageElement);
            });
        }
    }

}

/**
 * Update load notification
 * @param responseBody
 */
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
