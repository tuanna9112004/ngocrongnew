package nro.services.func;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public class InventoryServiceNew {

    private static InventoryServiceNew i;

    public static InventoryServiceNew gI() {
        if (i == null) {
            i = new InventoryServiceNew();
        }
        return i;
    }

}
