package com.example.client;


import com.example.api.Group;
import com.example.api.MessageBox;
import com.example.api.Person;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;

import java.util.*;
import java.util.stream.Collectors;

public class ControllerCreateGroup extends Controller {
    @FXML
    private Label searchLabel;
    @FXML
    private Label allPersonsListLabel;
    @FXML
    private Label selectedPersonLabel;
    @FXML
    private ListView<String> listViewPersonSelected;
    @FXML
    private ListView<String> listViewPerson;
    @FXML
    private TextField nameField;
    @FXML
    private TextField searchField;
    private ObservableList<String> itenNameObservableList;
    private final HashMap<String, Person> nameAndPersonMap = new HashMap<>();


    @FXML
    public void initialize() {
        listViewPerson.setFixedCellSize(50);
        listViewPersonSelected.setFixedCellSize(50);

        allPersonsListLabel.setFont(new Font("Sriracha Regular", 13));
        selectedPersonLabel.setFont(new Font("Sriracha Regular", 13));
        searchLabel.setFont(new Font("Sriracha Regular", 15));
    }

    public void addAllPersonList(List<Person> allPersonList) {
        allPersonList.forEach(person -> {
            nameAndPersonMap.put(person.getName(), person);
        });

        itenNameObservableList = FXCollections.observableArrayList(nameAndPersonMap.keySet());
        listViewPerson.setItems(itenNameObservableList);

        // Слушатель изменений для поля поиска
        searchField.textProperty().addListener(this::filterList);

        // Добавляем обработчик события для клика по элементу списка
        listViewPerson.setOnMouseClicked(event -> {
            String itemName = listViewPerson.getSelectionModel().getSelectedItem();
            if (listViewPersonSelected.getItems().contains(itemName)){
                return;
            }
            listViewPersonSelected.getItems().add(itemName);
            listViewPersonSelected.setMaxHeight(
                    (listViewPersonSelected.getItems().size() * listViewPersonSelected.getFixedCellSize()) + 4
            );
            searchField.requestFocus();
        });
    }

    private void filterList(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        List<String> filteredList = itenNameObservableList.stream()
                .filter(person -> person.toLowerCase().startsWith(newValue.toLowerCase()))
                .collect(Collectors.toList());
        ObservableList<String> strings = FXCollections.observableArrayList(filteredList);
        listViewPerson.setItems(strings);
        listViewPerson.setMaxHeight(
                (listViewPerson.getItems().size() * listViewPerson.getFixedCellSize()) + 4
        );
    }

    public void clickButtonCreateGroup() {
        if (nameField.getText().isEmpty()) {
            String title = "Empty error";
            String text = "Заполните название группы";
            showInformationMessage(title, text);
            nameField.requestFocus();
            return;
        }
        if (listViewPersonSelected.getItems().isEmpty()) {
            String title = "Empty error";
            String text = "Выберите участников группы";
            showInformationMessage(title, text);
            searchField.requestFocus();
            return;
        }
        List<Person> personInGroupList = new ArrayList<>();
        listViewPersonSelected.getItems().forEach(itemName->{
            Person person = nameAndPersonMap.get(itemName);
            personInGroupList.add(person);
        });
        personInGroupList.add(myPerson);
        chatClient.sendMessage(
                new MessageBox.Builder().
                buildCommandRequestRegGroup(
                        new Group(
                                UUID.randomUUID().toString(),
                                nameField.getText(),
                                personInGroupList,
                                myPerson
                        )
                )
        );
    }

    public void onFailedRegistrationGroup() {
        Platform.runLater(() -> {
            String title = "Ошибка регистрации";
            String text = "Эта группа уже существует";
            showInformationMessage(title, text);
            clearFields();
        });
    }

    public void onAcceptRegistrationGroup(MessageBox messageBox) {
        Platform.runLater(() -> {
            String title = "Success";
            String text = "Успешная регистрация!";
            showInformationMessage(title, text);
            clearFields();

            Group group = messageBox.getGroup();
            String groupId = messageBox.getGroup().getId();
            Correspondence correspondence = new Correspondence(groupId, group, CorrespondenceType.GROUP,new ArrayList<>());
            controllerClient.addNewUnitToMap(correspondence, true);
            createGroupStage.close();
        });
    }

    @Override
    public void clearFields() {
        nameField.clear();
        searchField.clear();
        listViewPersonSelected.getItems().clear();
    }

    @FXML
    private void nextField() {
        searchField.requestFocus();
    }

    @FXML
    private void nextField1() {
        clickButtonCreateGroup();
    }

}
