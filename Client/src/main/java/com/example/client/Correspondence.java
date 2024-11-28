package com.example.client;

import com.example.api.MessageBox;
import com.example.api.Unit;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Correspondence {
    private final String id;
    private final Unit unit;
    private final CorrespondenceType type;
    @Setter
    private CustomButton customButton;
    private final List<MessageBox> messageBoxList;
    private final AtomicInteger unreadMessageCounter = new AtomicInteger(0);
    private final StringBuilder targetDate = new StringBuilder("");

    public Correspondence(String id, Unit unit, CorrespondenceType type, List<MessageBox> messageBoxList) {
        this.id = id;
        this.unit = unit;
        this.type = type;
        this.messageBoxList = messageBoxList;
    }
}
