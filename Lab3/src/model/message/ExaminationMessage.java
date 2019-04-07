package model.message;

import model.Injury;

public class ExaminationMessage extends Message {

    private String surname;
    private Injury injury;

    public ExaminationMessage(MessageType messageType, String surname, Injury injury) {
        super(messageType);

        this.surname = surname;
        this.injury = injury;
    }

    public String getSurname() {
        return surname;
    }

    public Injury getInjury() {
        return injury;
    }

    @Override
    public String toString() {
        return super.toString() + surname + " : " + injury;
    }

}
