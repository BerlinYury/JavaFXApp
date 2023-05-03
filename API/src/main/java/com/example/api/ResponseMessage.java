package com.example.api;

public class ResponseMessage {
    private final String[] clientsChangeList;
    private final ResponseType type;
    private final String nick;
    private final String message;
    private final String fromNick;

    public ResponseMessage(ResponseType type, String[] clientsChangeList) {
        this.clientsChangeList = clientsChangeList;
        this.type = type;
        this.nick = null;
        this.message=null;
        this.fromNick=null;
    }
    public ResponseMessage(ResponseType type, String fromNick, String message) {
        this.type = type;
        this.fromNick=fromNick;
        this.message=message;
        this.nick = null;
        this.clientsChangeList = null;
    }

    public ResponseMessage(ResponseType type, String nick) {
        this.type = type;
        this.nick = nick;
        this.clientsChangeList = null;
        this.message=null;
        this.fromNick=null;
    }

    public ResponseMessage(ResponseType type) {
        this.type = type;
        this.nick = null;
        this.clientsChangeList = null;
        this.message=null;
        this.fromNick=null;
    }

    public String[] getClientsChangeList() {
        return clientsChangeList;
    }

    public ResponseType getType() {
        return type;
    }

    public String getNick() {
        return nick;
    }

    public String getMessage() {
        return message;
    }

    public String getFromNick() {
        return fromNick;
    }

    public static ResponseMessage createMessage(String msg) {
        String[] partsOfMessage = msg.split(" ", 2);
        String prefix = partsOfMessage[0];
        ResponseType responseType = ResponseType.getRequestType(prefix);
        if (responseType != null) {
            if (responseType == ResponseType.AUTH_FAILED ||
                    responseType == ResponseType.AUTH_NICK_BUSY ||
                    responseType == ResponseType.END) {
                return responseType.createMessage(null);
            }
            return responseType.createMessage(partsOfMessage[1]);
        }
        //TODO: Бросить ошибку
        return null;
    }

}
