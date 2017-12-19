/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.bri.brizzi.common;

/**
 *
 * @author indra
 */
public enum CompType {

    ListMenuItem(0),
    TextView(1),
    EditText(2),
    PasswordField(3),
    ComboBox(4),
    CheckBox(5),
    RadioButton(6),
    Button(7),
    MagneticSwipe(8),
    ChipInsert(9),
    TapCard(10),
    SwipeInsert(11),
    SwipeTap(12),
    InsertTap(13),
    SwipeInsertTap(14);

    private int id;

    CompType(int id) {
    }

    public int getId() {
        return id;
    }

    public CompType getValue(int id){
        return CompType.values()[id];
    }
}
