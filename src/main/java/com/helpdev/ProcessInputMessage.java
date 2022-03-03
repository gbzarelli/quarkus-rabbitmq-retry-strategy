package com.helpdev;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProcessInputMessage {
    public void doProcess(InputMessageDto inputMessageDto) {
        if (inputMessageDto.age < 18) {
            throw new IllegalArgumentException("Invalid age. Can't be less than 18");
        }
    }
}
