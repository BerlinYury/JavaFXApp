package com.example.server;

import com.example.api.MessageTypeThirdLevel;
import com.example.api.Person;
import lombok.Getter;
import lombok.ToString;

import java.util.List;


@Getter
@ToString
public class DBResponse {
    private final boolean flag;
    private final List<MessageTypeThirdLevel> errorOnFieldList;

    public DBResponse(boolean flag,  List<MessageTypeThirdLevel> errorOnFieldList) {
        this.errorOnFieldList = errorOnFieldList;
        this.flag = flag;
    }

}
