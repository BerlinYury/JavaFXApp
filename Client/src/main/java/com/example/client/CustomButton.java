package com.example.client;

import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomButton {
    private final ToggleButton button;
    private final Circle indicatorStatus;
    private final Ellipse indicatorUnreadMessages;
    private final Label nameLabel;
    private final Label unreadMessageLabel;
}
