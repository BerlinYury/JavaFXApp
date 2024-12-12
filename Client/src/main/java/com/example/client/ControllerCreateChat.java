package com.example.client;


import com.example.api.Person;
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

public class ControllerCreateChat extends Controller {
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
    private TextField searchField;
    private ObservableList<String> itenNameObservableList;
    HashMap<String, Person> nameAndPersonMap = new HashMap<>();
    String itemName;

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
            if (!correspondenceMap.containsKey(person.getId()) && !person.equals(myPerson)) {
                nameAndPersonMap.put(person.getName(), person);
            }
        });

        itenNameObservableList = FXCollections.observableArrayList(nameAndPersonMap.keySet());
        listViewPerson.setItems(itenNameObservableList);

        // Слушатель изменений для поля поиска
        searchField.textProperty().addListener(this::filterList);

        // Добавляем обработчик события для клика по элементу списка
        listViewPerson.setOnMouseClicked(event -> {
            itemName = listViewPerson.getSelectionModel().getSelectedItem();
            if (listViewPersonSelected.getItems().size() > 0) {
                listViewPersonSelected.getItems().clear();
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

    public void clickButtonCreateChat() {
        if (listViewPersonSelected.getItems().isEmpty()){
                String title = "Empty error";
                String text = "Выберите собеседника";
                showInformationMessage(title, text);
                searchField.requestFocus();
                return;
        }
        Person person = nameAndPersonMap.get(itemName);
        controllerClient.addNewUnitToMap(
                new Correspondence(person.getId(),person,CorrespondenceType.PERSON,new ArrayList<>()),
               true);
        createChatStage.close();
    }

    @Override
    public void clearFields() {
    }
    @FXML
    private void nextField() {clickButtonCreateChat();}

}
