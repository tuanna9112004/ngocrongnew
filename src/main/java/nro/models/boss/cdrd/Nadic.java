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
import nro.services.RewardService;
import nro.services.Service;
import nro.services.TaskService;
import nro.utils.Util;

/**
 *
 * @author Văn Tuấn - 0337766460
 */
public class Nadic extends CBoss {

    public Nadic(long id, short x, short y, SnakeRoad dungeon, BossData data) {
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
            ItemMap itemMap = new ItemMap(this.zone, tempId, 10,
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
        chat("Ha ha ha");
        super.changeToAttack(); 
    }
    
    

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{};
        this.textTalkMidle = new String[]{"Ốp la...Xay da da!"};
        this.textTalkAfter = new String[]{"Sếp hãy giết nó, trả thù cho em!"};
    }
    
}
