/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.boss.cdrd;

import nro.models.boss.BossData;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.map.dungeon.SnakeRoad;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.services.EffectSkillService;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.SkillService;
import nro.services.TaskService;
import nro.utils.Log;
import nro.utils.Util;

/**
 *
 * @author Văn Tuấn - 0337766460
 */
public class Saibamen extends CBoss {

    private boolean selfExplosion;

    public Saibamen(long id, short x, short y, SnakeRoad dungeon, BossData data) {
        super(id, x, y, dungeon, data);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

@Override
     public void rewards(Player pl) {
     int[] tempIds1 = new int[]{1577};
   

        int tempId = -1;
        if (Util.isTrue(20, 100)) {
            tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
        }
        if (tempId != -1) {
            ItemMap itemMap = new ItemMap(this.zone, tempId, 4,
                    pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
            if (tempId == 1571) {
//                itemMap.options.add(new ItemOption(77, Util.nextInt(33, 37)));
//                itemMap.options.add(new ItemOption(103, Util.nextInt(33, 37)));
//                itemMap.options.add(new ItemOption(50, Util.nextInt(33, 37)));
            itemMap.options.add(new ItemOption(30, Util.nextInt(30, 1)));

            }
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type, itemMap.options);
            Service.getInstance().dropItemMap(this.zone, itemMap);
        }
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
        generalRewards(pl);
    }


    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{};
        this.textTalkMidle = new String[]{};
        this.textTalkAfter = new String[]{};
    }

    @Override
    public long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        long hp = nPoint.hp;
        if (!selfExplosion) {
            if (hp > 1) {
                if (damage > hp) {
                    damage = hp - 1;
                    selfExplosion = true;
                    chat("He he he");
                    if (plAtt != null) {
                        Service.getInstance().chat(plAtt, "Trời ơi muộn mất rồi");
                        Service.getInstance().sendThongBao(plAtt, plAtt.name + " coi chừng đấy!");
                        EffectSkillService.gI().setBlindDCTT(plAtt, System.currentTimeMillis(), 3000);
                        EffectSkillService.gI().sendEffectPlayer(this, plAtt, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.BLIND_EFFECT);
                    }
                    selfExplosion();
                }
            } else {
                damage = 0;
            }
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    private void selfExplosion() {
        try {
            this.nPoint.hpMax = 1000000000;
            this.playerSkill.skillSelect = this.getSkillById(Skill.TU_SAT);
            SkillService.gI().useSkill(this, null, null,null);
            Util.setTimeout(() -> {
                SkillService.gI().useSkill(this, null, null,null);
            }, 2000);
        } catch (Exception e) {
            Log.error(Saibamen.class, e);
        }
    }

}
