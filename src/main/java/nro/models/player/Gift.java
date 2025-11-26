package nro.models.player;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public class Gift {

    private Player player;

    public Gift(Player player) {
        this.player = player;
    }

    public boolean goldTanThu;
    public boolean gemTanThu;

    public void dispose() {
        this.player = null;
    }

}
