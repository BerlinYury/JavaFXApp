package com.example.api;

import lombok.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Getter
public class MessageBox implements Serializable {
    private final String messageId;
    private final MessageTypeFirstLevel messageTypeFirstLevel;
    private final MessageTypeSecondLevel messageTypeSecondLevel;
    private final MessageTypeThirdLevel messageTypeThirdLevel;
    private final MessageTypeFourLevel messageTypeFourLevel;
    private final LocalDateTime dateTime;
    private final Person owner;
    private final Person person;
    private final Group group;
    private final Person sender;
    private final String message;
    private final String email;
    private final String password;
    private final int counterUnit;
    private final List<Person> activePersonList;
    private final List<Person> allPersonList;
    private final List<Group> groupWhereIAmAMemberList;
    private final List<MessageTypeThirdLevel> errorOnFieldList;

    private MessageBox(Builder builder) {
        this.messageId = builder.messageId;
        this.messageTypeFirstLevel = builder.messageTypeFirstLevel;
        this.messageTypeSecondLevel = builder.messageTypeSecondLevel;
        this.messageTypeThirdLevel = builder.messageTypeThirdLevel;
        this.messageTypeFourLevel = builder.messageTypeFourLevel;
        this.dateTime = builder.dateTime;
        this.owner = builder.owner;
        this.person = builder.person;
        this.group = builder.group;
        this.sender = builder.sender;
        this.message = builder.message;
        this.email = builder.email;
        this.password = builder.password;
        this.counterUnit = builder.counterUnit;
        this.activePersonList = builder.activePersonList;
        this.allPersonList = builder.allPersonList;
        this.groupWhereIAmAMemberList = builder.groupWhereIAmAMember;
        this.errorOnFieldList = builder.errorOnFieldList;
    }

    @Override
    public String toString() {
        Class<MessageBox> messageBoxClass = MessageBox.class;
        Field[] declaredFields = messageBoxClass.getDeclaredFields();
        StringBuilder resultString = new StringBuilder(String.format("%s{", messageBoxClass.getSimpleName()));

        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            try {
                Class<?> fieldType = declaredField.getType();
                String fieldName = declaredField.getName();
                Object fieldValue = declaredField.get(this);
                if (fieldType.equals(int.class) && (int) fieldValue == 0 || Objects.isNull(fieldValue)) {
                    continue;
                }
                resultString.append(String.format("%s= %s, ", fieldName, fieldValue));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        resultString.setLength(resultString.length() - 2);
        resultString.append("};");
        return resultString.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageBox that)) return false;
        return getCounterUnit() == that.getCounterUnit() &&
                getMessageTypeFirstLevel() == that.getMessageTypeFirstLevel() &&
                getMessageTypeSecondLevel() == that.getMessageTypeSecondLevel() &&
                getMessageTypeThirdLevel() == that.getMessageTypeThirdLevel() &&
                getMessageTypeFourLevel() == that.getMessageTypeFourLevel() &&
                Objects.equals(getDateTime(), that.getDateTime()) &&
                Objects.equals(getOwner(), that.getOwner()) &&
                Objects.equals(getPerson(), that.getPerson()) &&
                Objects.equals(getGroup(), that.getGroup()) &&
                Objects.equals(getSender(), that.getSender()) &&
                Objects.equals(getMessage(), that.getMessage()) &&
                Objects.equals(getEmail(), that.getEmail()) &&
                Objects.equals(getPassword(), that.getPassword()) &&
                Objects.equals(getActivePersonList(), that.getActivePersonList()) &&
                Objects.equals(getAllPersonList(), that.getAllPersonList()) &&
                Objects.equals(getGroupWhereIAmAMemberList(), that.getGroupWhereIAmAMemberList()) &&
                Objects.equals(getErrorOnFieldList(), that.getErrorOnFieldList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getMessageTypeFirstLevel(), getMessageTypeSecondLevel(),
                getMessageTypeThirdLevel(), getMessageTypeFourLevel(), getDateTime(),
                getOwner(), getPerson(), getGroup(), getSender(), getMessage(), getEmail(),
                getPassword(), getCounterUnit(), getActivePersonList(), getAllPersonList(),
                getGroupWhereIAmAMemberList(), getErrorOnFieldList());
    }

    @NoArgsConstructor
    public static class Builder {
        private String messageId;
        private MessageTypeFirstLevel messageTypeFirstLevel;
        private MessageTypeSecondLevel messageTypeSecondLevel;
        private MessageTypeThirdLevel messageTypeThirdLevel;
        private MessageTypeFourLevel messageTypeFourLevel;
        private LocalDateTime dateTime;
        private Person owner;
        private Person person;
        private Group group;
        private Person sender;
        private String message;
        private String email;
        private String password;
        private int counterUnit;
        private List<Person> activePersonList;
        private List<Person> allPersonList;
        private List<Group> groupWhereIAmAMember;
        private List<MessageTypeThirdLevel> errorOnFieldList;


        public MessageBox buildMessageOutingPerson(
                String messageId,
                LocalDateTime dateTime,
                Person owner,
                Person person,
                String message) {
            this.messageId = messageId;
            this.messageTypeFirstLevel = MessageTypeFirstLevel.MESSAGE;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.OUTGOING;
            this.messageTypeFourLevel = MessageTypeFourLevel.PERSON;
            this.dateTime = dateTime;
            this.owner = owner;
            this.person = person;
            this.message = message;
            return new MessageBox(this);
        }

        public MessageBox buildMessageOutingGroup(
                String messageId,
                LocalDateTime dateTime,
                Person owner,
                Group group,
                Person sender,
                String message) {
            this.messageId = messageId;
            this.messageTypeFirstLevel = MessageTypeFirstLevel.MESSAGE;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.OUTGOING;
            this.messageTypeFourLevel = MessageTypeFourLevel.GROUP;
            this.dateTime = dateTime;
            this.owner = owner;
            this.group = group;
            this.sender = sender;
            this.message = message;
            return new MessageBox(this);
        }

        public MessageBox buildMessageIncomingPerson(
                String messageId,
                LocalDateTime dateTime,
                Person owner,
                Person person,
                String message) {
            this.messageId = messageId;
            this.messageTypeFirstLevel = MessageTypeFirstLevel.MESSAGE;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.INCOMING;
            this.messageTypeFourLevel = MessageTypeFourLevel.PERSON;
            this.dateTime = dateTime;
            this.owner = owner;
            this.person = person;
            this.message = message;
            return new MessageBox(this);
        }

        public MessageBox buildMessageIncomingGroup(
                String messageId,
                LocalDateTime dateTime,
                Person owner,
                Group group,
                Person sender,
                String message) {
            this.messageId = messageId;
            this.messageTypeFirstLevel = MessageTypeFirstLevel.MESSAGE;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.INCOMING;
            this.messageTypeFourLevel = MessageTypeFourLevel.GROUP;
            this.dateTime = dateTime;
            this.owner = owner;
            this.group = group;
            this.sender = sender;
            this.message = message;
            return new MessageBox(this);
        }

        public MessageBox buildCommandRequestAuthPerson(String email, String password) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.REQUEST;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.AUTH;
            this.messageTypeFourLevel = MessageTypeFourLevel.PERSON;
            this.email = email;
            this.password = password;
            return new MessageBox(this);
        }

        public MessageBox buildCommandAcceptAuthPerson(Person owner) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.ACCEPT;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.AUTH;
            this.messageTypeFourLevel = MessageTypeFourLevel.PERSON;
            this.owner = owner;
            return new MessageBox(this);
        }

        public MessageBox buildCommandFailedAuthPerson() {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.FAILED;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.AUTH;
            this.messageTypeFourLevel = MessageTypeFourLevel.PERSON;
            return new MessageBox(this);
        }

        public MessageBox buildCommandRequestRegPerson(Person person, String email, String password) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.REQUEST;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.REG;
            this.messageTypeFourLevel = MessageTypeFourLevel.PERSON;
            this.person = person;
            this.email = email;
            this.password = password;
            return new MessageBox(this);
        }

        public MessageBox buildCommandRequestRegGroup(Group group) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.REQUEST;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.REG;
            this.messageTypeFourLevel = MessageTypeFourLevel.GROUP;
            this.group = group;
            return new MessageBox(this);
        }

        public MessageBox buildCommandAcceptRegPerson() {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.ACCEPT;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.REG;
            this.messageTypeFourLevel = MessageTypeFourLevel.PERSON;
            return new MessageBox(this);
        }

        public MessageBox buildCommandAcceptRegGroup(Group group) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.ACCEPT;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.REG;
            this.messageTypeFourLevel = MessageTypeFourLevel.GROUP;
            this.group = group;
            return new MessageBox(this);
        }

        public MessageBox buildCommandFailedRegPerson(List<MessageTypeThirdLevel> errorOnFieldList) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.FAILED;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.REG;
            this.messageTypeFourLevel = MessageTypeFourLevel.PERSON;
            this.errorOnFieldList = errorOnFieldList;
            return new MessageBox(this);
        }

        public MessageBox buildCommandFailedRegGroup() {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.FAILED;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.REG;
            this.messageTypeFourLevel = MessageTypeFourLevel.GROUP;
            return new MessageBox(this);
        }

        public MessageBox buildCommandRequestMapAllPerson() {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.REQUEST;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.LIST_ALL_PERSON;
            this.messageTypeFourLevel = MessageTypeFourLevel.PERSON;
            return new MessageBox(this);
        }

        public MessageBox buildCommandRequestMapAllGroup() {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.REQUEST;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.LIST_ALL_PERSON;
            this.messageTypeFourLevel = MessageTypeFourLevel.GROUP;
            return new MessageBox(this);
        }

        public MessageBox buildCommandChangeMapAllPersonForPerson(List<Person> allPersonList) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.CHANGE;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.LIST_ALL_PERSON;
            this.messageTypeFourLevel = MessageTypeFourLevel.PERSON;
            this.allPersonList = allPersonList;
            return new MessageBox(this);
        }

        public MessageBox buildCommandChangeMapAllPersonForGroup(List<Person> allPersonList) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.CHANGE;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.LIST_ALL_PERSON;
            this.messageTypeFourLevel = MessageTypeFourLevel.GROUP;
            this.allPersonList = allPersonList;
            return new MessageBox(this);
        }

        public MessageBox buildCommandChangeGroupIMemberGroup(List<Group> groupWhereIAmAMember) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.CHANGE;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.LIST_GROUP_I_MEMBER;
            this.messageTypeFourLevel = MessageTypeFourLevel.GROUP;
            this.groupWhereIAmAMember = groupWhereIAmAMember;
            return new MessageBox(this);
        }

        public MessageBox buildCommandChangeStatusPerson(List<Person> activePersonList) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.CHANGE;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.STATUS;
            this.messageTypeFourLevel = MessageTypeFourLevel.PERSON;
            this.activePersonList = activePersonList;
            return new MessageBox(this);
        }


        public MessageBox buildCommandRecoveryCorrespondenceHistory(int counterUnit) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.RECOVERY;
            this.messageTypeThirdLevel = MessageTypeThirdLevel.CORRESPONDENCE_HISTORY;
            this.counterUnit = counterUnit;
            return new MessageBox(this);
        }

        public MessageBox buildCommandEnd() {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.COMMAND;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.END;
            return new MessageBox(this);
        }

        public MessageBox buildMessageDate(String dateInTextFormat) {
            this.messageTypeFirstLevel = MessageTypeFirstLevel.MESSAGE;
            this.messageTypeSecondLevel = MessageTypeSecondLevel.DATE;
            this.message = dateInTextFormat;
            return new MessageBox(this);
        }
    }

}
