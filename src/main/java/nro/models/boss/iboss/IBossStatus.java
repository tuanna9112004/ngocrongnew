package nro.models.boss.iboss;

import nro.models.player.Player;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public interface IBossStatus extends IBossInit {

    void attack(); //attack

    void idle(); //trong lúc attack có thể đứng nghỉ

    void checkPlayerDie(Player pl); //attack player nào đó rồi kiểm tra

    void die();

    void respawn();
}
