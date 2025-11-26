/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.boss.cdrd;

import nro.consts.ConstPlayer;
import nro.models.boss.BossData;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.map.dungeon.SnakeRoad;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.SkillService;
import nro.services.TaskService;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

/**
 *
 * @author Văn Tuấn - 0337766460
 */
public class Cadich extends CBoss {

    private boolean transformed;

    public Cadich(long id, short x, short y, SnakeRoad dungeon, BossData data) {
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
            ItemMap itemMap = new ItemMap(this.zone, tempId, 20,
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
    public void changeToAttack() {
        chat("Vĩnh biệt chú mày nhé, Na đíc");
        super.changeToAttack();
    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{};
        this.textTalkMidle = new String[]{"Tuyệt chiêu hủy diệt của môn phái Xayda!"};
        this.textTalkAfter = new String[]{"Tốt lắm! Phi thuyền sẽ đến đón ta"};
    }

    @Override
    public short getHead() {
        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        }
        return super.getHead();
    }

    @Override
    public short getBody() {
        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        }
        return super.getBody();
    }

    @Override
    public short getLeg() {
        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        }
        return super.getLeg();
    }

    @Override
    public void update() {
        if (!isDie()) {
            if (!transformed && !this.effectSkill.isMonkey && nPoint.hp <= nPoint.hpMax / 2) {
                transformed = true;
                this.playerSkill.skillSelect = this.getSkillById(Skill.BIEN_KHI);
                SkillService.gI().useSkill(this, null, null,null);
            }
        }
        super.update();
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.TENNIS_SPACE_SHIP);
        super.leaveMap();
    }

}
