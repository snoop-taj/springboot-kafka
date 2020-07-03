package com.example.kafka.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Client {
    private final String id;
    private final String firstName;
    private final String lastName;

    @JsonCreator
    public Client(
            @JsonProperty("id") String id,
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
