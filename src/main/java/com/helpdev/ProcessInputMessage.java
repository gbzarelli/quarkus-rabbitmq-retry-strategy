package com.helpdev;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProcessInputMessage {
    public void doProcess(InputMessage inputMessage) {
        if (inputMessage.age < 18) {
            throw new IllegalArgumentException("Invalid age. Can't be less than 18");
        }
    }
}
