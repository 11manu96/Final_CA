'use strict';

const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chatapp");

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
    webSocket.send(JSON.stringify({'type': 'login', 'body':
            {'name': userName, 'age': userAge, 'location': userLocation, 'school': userSchool}}));
}

/**
 * Send request to websocket to enter room
 */
function enterRoom() {
    var selectedRoom = $("#slt-joined-rooms").val();
}

/**
 * Send request to websocket to exit all rooms
 */
function exitRoom() {
    var selectedRoom = $("#slt-joined-rooms").val();
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
    var selectedRoom = $("#slt-available-rooms").val();
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
}

/**
 * Send request to websocket to send message to all users
 */
function sendAll() {
    var message = $("#chat-message").val();
}

/**
 * Receive data from websocket to update view
 * @param message data from websocket
 */
function updateChatApp(message) {
    // parse message to determine how to update view
}