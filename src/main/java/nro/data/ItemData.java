package nro.data;

import java.util.Arrays;
import java.util.List;
import nro.models.Part.ArrHead2Frames;
import nro.models.item.ItemOptionTemplate;
import nro.models.item.ItemTemplate;

import nro.server.Manager;
import nro.server.io.Message;
import nro.server.io.Session;

public class ItemData {

    public static List<Integer> list_thuc_an = Arrays.asList(663, 664, 665, 666, 667);
    
    public static List<Integer> list_dapdo = Arrays.asList(1196, 1197, 1198, 1221
            , 1222, 1223, 1229, 1230, 1326, 1462, 1415, 1524, 1502, 1345);
    
    public static List<Integer> phieu = Arrays.asList(459);
    
    public static List<Integer> IdMiniPet = Arrays.asList(936, 892, 893, 942, 943, 944, 967, 1039,1193
            , 1040, 1046, 916, 917, 918, 919, 1188, 1202, 1203, 1207, 1213, 1243, 1244
            , 1196, 1197, 1198, 1221, 1222, 1223, 1229, 1230, 1415, 1462, 1497, 1498, 1524, 2017, 2018, 1570, 1539, 1540, 1541, 1542, 1543, 1545, 1546, 1524);

    public static void updateItem(Session session) {
        updateItemOptionItemplate(session);
        updateTocBay(session);
        updateItemTemplate(session, 930);
        updateItemTemplate(session, 930, Manager.ITEM_TEMPLATES.size());
    }
    private static final short[][] head3Htinh = {
        {1802, 1803, 1804},//Trái đất
        {1807, 1808, 1809},//Namec
        {1812, 1813, 1814}//Xayda
    };

    private static void updateTocBay(Session session) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);
            msg.writer().writeByte(DataGame.vsItem);//vcitem
            msg.writer().writeByte(100); //type NroItem100
            msg.writer().writeShort(head3Htinh.length);//tổng list lặp head
            for (int i = 0; i < head3Htinh.length; i++) {
                msg.writer().writeByte(head3Htinh[i].length);//chiều dài 1 chuỗi
                for (int j = 0; j < head3Htinh[i].length; j++) {
                    msg.writer().writeShort(head3Htinh[i][j]);
                }
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    private static void updateItemOptionItemplate(Session session) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);
            msg.writer().writeByte(DataGame.vsItem); //vcitem
            msg.writer().writeByte(0); //update option
            msg.writer().writeByte(Manager.ITEM_OPTION_TEMPLATES.size());
            for (ItemOptionTemplate io : Manager.ITEM_OPTION_TEMPLATES) {
                msg.writer().writeUTF(io.name);
                msg.writer().writeByte(io.type);
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    private static void updateItemTemplate(Session session, int count) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);

            msg.writer().writeByte(DataGame.vsItem); //vcitem
            msg.writer().writeByte(1); //reload itemtemplate
            msg.writer().writeShort(count);
            for (int i = 0; i < count; i++) {
                ItemTemplate itemTemplate = Manager.ITEM_TEMPLATES.get(i);
                msg.writer().writeByte(itemTemplate.type);
                msg.writer().writeByte(itemTemplate.gender);
                msg.writer().writeUTF(itemTemplate.name);
                msg.writer().writeUTF(itemTemplate.description);
                msg.writer().writeByte(itemTemplate.level);
                msg.writer().writeInt(itemTemplate.strRequire);
                msg.writer().writeShort(itemTemplate.iconID);
                msg.writer().writeShort(itemTemplate.part);
                msg.writer().writeBoolean(itemTemplate.isUpToUp);
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void updateItem2Heads(Session session) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);

            msg.writer().writeByte(DataGame.vsItem);
            msg.writer().writeByte(100);

            msg.writer().writeShort(Manager.CAITRANG_2HEADS.size());
            for (int i = 0; i < Manager.CAITRANG_2HEADS.size(); i++) {
                msg.writer().writeByte(Manager.CAITRANG_2HEADS.get(i).head2.size() + 1);
                msg.writer().writeShort(Manager.CAITRANG_2HEADS.get(i).head);
                for (int j = 0; j < Manager.CAITRANG_2HEADS.get(i).head2.size(); j++) {
                    msg.writer().writeShort(Manager.CAITRANG_2HEADS.get(i).head2.get(j));
                }
            }

            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }
    private static void updateItemTemplate(Session session, int start, int end) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);

            msg.writer().writeByte(DataGame.vsItem); //vcitem
            msg.writer().writeByte(2); //add itemtemplate
            msg.writer().writeShort(start);
            msg.writer().writeShort(end);
            for (int i = start; i < end; i++) {
//                System.out.println("start: " + start + " -> " + end + " id " + Manager.ITEM_TEMPLATES.get(i).id);
                msg.writer().writeByte(Manager.ITEM_TEMPLATES.get(i).type);
                msg.writer().writeByte(Manager.ITEM_TEMPLATES.get(i).gender);
                msg.writer().writeUTF(Manager.ITEM_TEMPLATES.get(i).name);
                msg.writer().writeUTF(Manager.ITEM_TEMPLATES.get(i).description);
                msg.writer().writeByte(Manager.ITEM_TEMPLATES.get(i).level);
                msg.writer().writeInt(Manager.ITEM_TEMPLATES.get(i).strRequire);
                msg.writer().writeShort(Manager.ITEM_TEMPLATES.get(i).iconID);
                msg.writer().writeShort(Manager.ITEM_TEMPLATES.get(i).part);
                msg.writer().writeBoolean(Manager.ITEM_TEMPLATES.get(i).isUpToUp);
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   private static void updateItemArrHead2FItemplate(Session session) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);
            msg.writer().writeByte(DataGame.vsItem); // vcitem
            msg.writer().writeByte(100); // update ArrHead2F
            msg.writer().writeShort(Manager.ARR_HEAD_2_FRAMES.size());
            for (ArrHead2Frames io : Manager.ARR_HEAD_2_FRAMES) {
                msg.writeByte(io.frames.size());
                for (int i : io.frames) {
                    msg.writer().writeShort(i);
                }
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }
}


