package com.dffrs.handler;

public final class APIHandler {
    private static APIHandler instance;

    private APIHandler() {

    }

    public static APIHandler getInstance(){
        if (instance == null)
            instance = new APIHandler();
        return instance;
    }
}
