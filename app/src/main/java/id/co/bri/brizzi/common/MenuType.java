package id.co.bri.brizzi.common;

/**
 * Created by indra on 25/11/15.
 */
public enum MenuType {
    ListMenu(0),
    Form(1),
    PopupBerhasil(2),
    PopupGagal(3),
    PopupLogout(4);

    private int id;

    MenuType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
