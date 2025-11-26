package nro.models.mob;

import nro.utils.Util;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public class MobPoint {

    public final Mob mob;
    public long hp;
    public long maxHp;
    public long dame;

    public long clanMemHighestDame; //dame lớn nhất trong clan
    public long clanMemHighestHp; //hp lớn nhất trong clan

    public int xHpForDame = 50; //dame gốc = highesHp / xHpForDame;
    public int xDameForHp = 10; //hp gốc = xDameForHp * highestDame;

    public MobPoint(Mob mob) {
        this.mob = mob;
    }

    public long getHpFull() {
        return maxHp;
    }

    public void setHpFull(long hp) {
        maxHp = hp;
    }

    public long getHP() {
        return hp;
    }

    public void setHP(long hp) {
        if (this.hp < 0) {
            this.hp = 0;
        } else {
            this.hp = hp;
        }
    }

    public long getDameAttack() {
        return this.dame != 0 ? this.dame + Util.nextdame(-(this.dame / 100), (this.dame / 100))
                : this.getHpFull() * Util.nextdame(mob.pDame - 1, mob.pDame + 1) / 100
                + Util.nextdame(-(mob.level * 10), mob.level * 10);
    }
}
