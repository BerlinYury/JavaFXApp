package com.example.client;


import com.example.api.Group;
import com.example.api.MessageBox;
import com.example.api.Person;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@NoArgsConstructor
public class ControllerClient extends Controller {
    @FXML
    private MenuBar menuBar;
    @FXML
    private ListView<ToggleButton> listView;
    @FXML
    private Label selectedUnitLabel;
    @FXML
    private Label unitsListLabel;
    @FXML
    private VBox messageContainerField;
    @FXML
    private TextField textField;
    @FXML
    private ScrollPane scrollPane;
    ToggleGroup toggleGroup = new ToggleGroup();
    private boolean correspondenceSelected;
    private String selectedCorrespondenceId;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    public void initialize() {
        // Привязываем ширину ScrollPane к ширине TextField
        scrollPane.prefWidthProperty().bind(textField.widthProperty());

        Menu menu = new Menu("...");
        MenuItem menuItem1 = new MenuItem("new Chat");
        MenuItem menuItem2 = new MenuItem("new Group");
        MenuItem menuItem3 = new MenuItem("logout");

        // Добавляем обработчики событий
        menuItem1.setOnAction(event -> createChat());
        menuItem2.setOnAction(event -> createGroup());
        menuItem3.setOnAction(event -> logout());

        menu.getItems().addAll(menuItem1, menuItem2, menuItem3);
        menuBar.getMenus().add(menu);
    }

    private void createChat() {
        uiClient.startCreateChatStage();
        if (Objects.nonNull(controllerCreateChat)) {
            chatClient.sendMessage(new MessageBox.Builder().buildCommandRequestMapAllPerson());
        }
    }

    private void createGroup() {
        uiClient.startCreateGroupStage();
        if (Objects.nonNull(controllerCreateGroup)) {
            chatClient.sendMessage(new MessageBox.Builder().buildCommandRequestMapAllGroup());
        }
    }

    @FXML
    private void clickButtonSend() {
        if (textField.getText().isEmpty()) {
            textField.requestFocus();
            return;
        }
        if (!correspondenceSelected) {
            String title = ("Упс!");
            String text = ("Получатель не выбран");
            showInformationMessage(title, text);
            textField.requestFocus();
            return;
        }
        String textOnTextField = textField.getText().trim();
        Correspondence correspondence = correspondenceMap.get(selectedCorrespondenceId);

        MessageBox messageBox;
        switch (correspondence.getType()) {
            case PERSON -> {
                Person person = (Person) correspondence.getUnit();
                messageBox = new MessageBox.Builder().buildMessageOutingPerson(
                        UUID.randomUUID().toString(),
                        LocalDateTime.now(),
                        myPerson,
                        person,
                        textOnTextField);
            }
            case GROUP -> {
                Group group = (Group) correspondence.getUnit();
                messageBox = new MessageBox.Builder().buildMessageOutingGroup(
                        UUID.randomUUID().toString(),
                        LocalDateTime.now(),
                        myPerson,
                        group,
                        myPerson,
                        textOnTextField
                );
            }
            default -> throw new IllegalArgumentException();

        }
        chatClient.addMessageToMap(messageBox);
        chatClient.sendMessage(messageBox);
        refreshMessageContainerField();
        clearFields();
    }

    public void addIncomingMessage(MessageBox messageBox) {
        Correspondence correspondence = chatClient.addMessageToMap(messageBox);
        String correspondenceId = correspondence.getId();
        if (Objects.isNull(selectedCorrespondenceId) || !selectedCorrespondenceId.equals(correspondenceId)) {
            newUnreadMessage(correspondence);
        } else refreshMessageContainerField();
    }

    private void refreshMessageContainerField() {
        Platform.runLater(() -> {
            messageContainerField.getChildren().clear();
            Correspondence correspondence = correspondenceMap.get(selectedCorrespondenceId);
            List<MessageBox> messageBoxList = correspondence.getMessageBoxList();
            StringBuilder newDate = new StringBuilder();
            messageBoxList.forEach(messageBox -> {
                if (doINeedToAddADate(messageBox, correspondence, newDate)) {
                    addMessageToScreen(new MessageBox.Builder().buildMessageDate(
                            messageBox.getDateTime().format(dateFormat)));
                }
                addMessageToScreen(messageBox);
            });
            correspondence.getTargetDate().setLength(0);
        });
        messageContainerField.heightProperty().addListener((observable, oldValue, newValue) -> scrollPane.setVvalue(1.0));
    }

    private boolean doINeedToAddADate(MessageBox messageBox, Correspondence correspondence, StringBuilder newDate) {
        newDate.append(messageBox.getDateTime().format(dateFormat));
        StringBuilder targetDate = correspondence.getTargetDate();
        if (!correspondence.getTargetDate().toString().contentEquals(newDate)) {
            targetDate.setLength(0);
            targetDate.append(newDate);
            newDate.setLength(0);
            return true;
        } else {
            newDate.setLength(0);
            return false;
        }
    }

    private void addMessageToScreen(MessageBox messageBox) {
        Label messageLabel = new Label(messageBox.getMessage());
        messageLabel.setWrapText(true);
        HBox messageContainer = new HBox();
        messageContainer.getStyleClass().add("message-container");
        messageContainer.setPrefWidth(scrollPane.getWidth());
        messageContainer.setMaxWidth(scrollPane.getWidth());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        switch (messageBox.getMessageTypeSecondLevel()) {
            case INCOMING -> {
                HBox innerContainer = new HBox(messageLabel, spacer);
                innerContainer.getStyleClass().add("incoming");
                innerContainer.setMaxWidth(0.6 * scrollPane.getWidth());
                innerContainer.setPadding(new Insets(10)); // Добавляем отступы
                messageContainer.getChildren().addAll(innerContainer, spacer);
                messageContainer.setAlignment(Pos.CENTER_LEFT);
            }
            case OUTGOING -> {
                HBox innerContainer = new HBox(spacer, messageLabel);
                innerContainer.setAlignment(Pos.CENTER_RIGHT);
                innerContainer.getStyleClass().add("outgoing");
                innerContainer.setMaxWidth(0.6 * scrollPane.getWidth());
                innerContainer.setPadding(new Insets(10)); // Добавляем отступы
                messageContainer.getChildren().addAll(spacer, innerContainer);
                messageContainer.setAlignment(Pos.CENTER_RIGHT);
            }
            case DATE -> {
                messageContainer.setAlignment(Pos.CENTER);
                messageContainer.getChildren().add(messageLabel);
                messageContainer.getStyleClass().add("date");
            }
        }
        messageContainerField.getChildren().add(messageContainer);
    }

    public void updatePersonStatus(List<Person> personOnlineStatusList) {
        Platform.runLater(() -> {
            correspondenceMap.values().forEach(correspondence -> {
                if (correspondence.getType().equals(CorrespondenceType.PERSON)) {
                    CustomButton customButton = correspondence.getCustomButton();
                    Person person = (Person) correspondence.getUnit();
                    boolean status = personOnlineStatusList.contains(person);
                    changeStatus(person, customButton, status);
                }
            });
        });
    }

    public void addButtonsForCorrespondence(Correspondence correspondence) {
        try {
            Platform.runLater(() -> {
                CustomButton customButton = createCustomButton(correspondence);
                correspondence.setCustomButton(customButton);
                ToggleButton button = customButton.getButton();
                button.setToggleGroup(toggleGroup);
                listView.setPadding(new Insets(5, 0, 0, 0)); // Отступ только сверху
                listView.getItems().add(button);

                button.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        this.selectedCorrespondenceId = button.getId();
                        correspondenceSelected = true;
                        Correspondence selectedCorrespondence = correspondenceMap.get(selectedCorrespondenceId);
                        refreshMessageContainerField();

                        removeUnreadMessages(selectedCorrespondence);

                        labelMenuSettings(selectedCorrespondence);
                    } else {
                        this.selectedCorrespondenceId = null;
                        correspondenceSelected = false;
                        messageContainerField.getChildren().clear();
                        selectedUnitLabel.setText("Выберите, кому хотели бы написать");
                    }
                    textField.requestFocus();
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void labelMenuSettings(Correspondence correspondence) {
        switch (correspondence.getType()) {
            case PERSON -> selectedUnitLabel.setText(correspondence.getUnit().getName());
            case GROUP -> {
                ContextMenu contextMenu = new ContextMenu();
                Group selectedGroup = (Group) correspondence.getUnit();
                selectedUnitLabel.setText(String.format("%s (участников группы кроме вас: %d)", selectedGroup.getName(),
                        selectedGroup.getPersonInGroupList().size() - 1));

                HashMap<MenuItem, Person> menuItemPersonMap = new HashMap<>();

                selectedGroup.getPersonInGroupList().forEach(person -> {
                    if (!person.equals(myPerson)) {
                        MenuItem item = new MenuItem(person.getName());
                        menuItemPersonMap.put(item, person);

                        item.setOnAction(event -> {
                            Person selectedPerson = menuItemPersonMap.get(item);
                            String selectedPersonId = selectedPerson.getId();
                            if (correspondenceMap.containsKey(selectedPersonId)) {
                                correspondenceMap.get(selectedPersonId).getCustomButton().getButton().fire();
                            } else {
                                questionOfCreatingNewChat(
                                        new Correspondence(selectedPersonId, selectedPerson, CorrespondenceType.PERSON,new ArrayList<>())
                                );
                            }
                        });
                        contextMenu.getItems().add(item);
                    }
                });

                selectedUnitLabel.setOnMouseClicked(event -> {
                    if (!contextMenu.isShowing()) {
                        double x =
                                selectedUnitLabel.localToScreen(selectedUnitLabel.getBoundsInLocal()).getMinX();
                        double y =
                                selectedUnitLabel.localToScreen(selectedUnitLabel.getBoundsInLocal()).getMaxY();
                        contextMenu.show(selectedUnitLabel, x, y);
                    }
                });
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private void questionOfCreatingNewChat(Correspondence correspondence) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Хотите создать новый чат?");
        ButtonType buttonYes = new ButtonType("Да");
        ButtonType buttonNo = new ButtonType("Нет");
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        // Показываем диалог и ждем ответа
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonYes) {
            addNewUnitToMap(correspondence, true);
        }
    }


    private CustomButton createCustomButton(Correspondence correspondence) {
        String unitName = correspondence.getUnit().getName();
        Label nameLabel = new Label(unitName);
        ToggleButton button = new ToggleButton();
        button.setId(correspondence.getId());
        button.getStyleClass().add("toggle-button-client");
        button.setMinWidth(170);
        button.setMinHeight(45);

        Circle indicatorStatus = new Circle(5); // Индикатор в виде круга
        indicatorStatus.setFill(Color.web("#229ED9"));
        indicatorStatus.setVisible(false);

        Ellipse indicatorUnreadMessages = new Ellipse(21, 12); // Установите размеры эллипса
        indicatorUnreadMessages.setFill(Color.GRAY);
        indicatorUnreadMessages.setVisible(false);
        Label unreadMessageLabel = new Label(); // Пример цифры

        StackPane buttonContent = new StackPane(indicatorStatus, nameLabel, indicatorUnreadMessages,
                unreadMessageLabel);
        StackPane.setAlignment(nameLabel, Pos.CENTER);
        StackPane.setAlignment(indicatorStatus, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(unreadMessageLabel, Pos.CENTER_RIGHT);
        StackPane.setAlignment(indicatorUnreadMessages, Pos.CENTER_RIGHT);
        button.setGraphic(buttonContent);

        return new CustomButton(button, indicatorStatus, indicatorUnreadMessages, nameLabel,
                unreadMessageLabel);
    }

    @FXML
    public void logout() {
        try {
            exit();
            startStage.close();
            uiClient.start(startStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        chatClient.sendMessage(new MessageBox.Builder().buildCommandEnd());
    }

    public void showStartStage() {
        startStage.setTitle(String.format("Мой Nick: %s", myPerson.getName()));
        startStage.show();
    }

    public void setUnitsListLabel() {
        unitsListLabel.setFont(new Font("Sriracha Regular", 15));
        selectedUnitLabel.setFont(new Font("Sriracha Regular", 15));
    }

    @Override
    public void clearFields() {
        textField.clear();
        textField.requestFocus();
    }

    public void addNewUnitToMap(Correspondence correspondence, boolean needPressButton) {
        String correspondenceId = correspondence.getId();
        addButtonsForCorrespondence(correspondence);
        correspondenceMap.put(correspondenceId, correspondence);
        if (needPressButton) {
            Platform.runLater(() -> {
                correspondence.getCustomButton().getButton().fire();
                listView.scrollTo(listView.getItems().size() - 1);
            });
        }
    }


    public void newUnreadMessage(Correspondence correspondence) {
        Platform.runLater(() -> {
            CustomButton customButton = correspondence.getCustomButton();
            String newMessages = Integer.toString(correspondence.getUnreadMessageCounter().incrementAndGet());
            customButton.getIndicatorUnreadMessages().setVisible(true);
            customButton.getUnreadMessageLabel().setText(String.format("%s new ", newMessages));
        });
    }

    public void removeUnreadMessages(Correspondence correspondence) {
        Platform.runLater(() -> {
            CustomButton customButton = correspondence.getCustomButton();
            correspondence.getUnreadMessageCounter().set(0);
            customButton.getIndicatorUnreadMessages().setVisible(false);
            customButton.getUnreadMessageLabel().setText("");
        });
    }

    public void changeStatus(Person person, CustomButton customButton, boolean newStatus) {
        Platform.runLater(() -> {
            person.setStatus(newStatus);
            customButton.getIndicatorStatus().setVisible(person.isStatus());
        });
    }

}