package com.example.kafka.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Account {
    private final String accountNumber;
    private final String accountName;

    @JsonCreator
    public Account(
            @JsonProperty("account_number") String accountNumber,
            @JsonProperty("account_name") String accountName) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
    }
}
