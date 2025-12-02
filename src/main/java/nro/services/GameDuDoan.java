package nro.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nro.models.item.Item;
import nro.models.player.Player;
import nro.server.Client;
import nro.server.io.Message;
import nro.services.func.Input;
import nro.utils.TimeUtil;
import nro.utils.Util;

/**
 *
 * @author Hoàng Việt - 0857853150
 * @copyright VIET
 *
 */
public class GameDuDoan implements Runnable {

    // 0 là tắt chức năng, 1 là mở
    private static final byte START = 1;

    // Chơi bằng Thỏi vàng
    public static final int ID_ITEM_CUOC = 457;             // id Thỏi vàng
    public static final String NAME_ITEM_CUOC = "Thỏi vàng";

    public static final long TIME_SHOW = 3000;              // thời gian show kết quả
    public static final long TIME_TAI_XIU = 35000;          // thời gian 1 ván
    public static final long TIME_NAN = 10000;               // thời gian nặn
    public static final long TIME_THONG_BAO = 15000;        // thời gian xuất hiện dòng thông báo
    public static final int TI_LE_BIP = 70;                 // tỉ lệ bịp
    public static final int TI_LE_AN = 90;                  // tỉ lệ ăn (1.9x)

    public static final String LON = "Lớn";
    public static final String NHO = "Nhỏ";

    public int soPhien = 1;

    // Tổng số Thỏi vàng đặt Tài / Xỉu của tất cả người chơi trong phiên hiện tại
    public long goldTai;
    public long goldXiu;

    public boolean ketquaTai = false;
    public boolean ketquaXiu = false;

    public boolean baotri = false;
    public long lastTimeEnd;

    public List<Player> PlayersTai = new ArrayList<>();
    public List<Player> PlayersXiu = new ArrayList<>();

    private static GameDuDoan instance;

    public int xucxac1, xucxac2, xucxac3;
    public List<Integer> List_KetQua = new ArrayList<>();
    public List<String> noiDungChat = new ArrayList<>();

    public boolean showKQ = false;
    public long lastTimeShow;
    public long lastTimeThongBao;

    public boolean nanKG = false;
    public long lastTimeNan;

    // thời gian mở/đóng trong ngày
    public static long TIME_OPEN;
    public static long TIME_CLOSE;

    public static final byte HOUR_OPEN = 0;
    public static final byte MIN_OPEN = 0;
    public static final byte SECOND_OPEN = 0;

    public static final byte HOUR_CLOSE = 23;
    public static final byte MIN_CLOSE = 0;
    public static final byte SECOND_CLOSE = 0;

    private int day = -1;

    // Singleton
    public static GameDuDoan gI() {
        if (instance == null) {
            instance = new GameDuDoan();
        }
        instance.setTime();
        return instance;
    }

    public void addPlayerXiu(Player pl) {
        if (!PlayersXiu.contains(pl)) {
            PlayersXiu.add(pl);
        }
    }

    public void addPlayerTai(Player pl) {
        if (!PlayersTai.contains(pl)) {
            PlayersTai.add(pl);
        }
    }

    // Thiết lập TIME_OPEN / TIME_CLOSE theo ngày hiện tại
    public void setTime() {
        if (instance.day == -1 || instance.day != TimeUtil.getCurrDay()) {
            instance.day = TimeUtil.getCurrDay();
            try {
                TIME_OPEN = TimeUtil.getTime(
                        TimeUtil.getTimeNow("dd/MM/yyyy") + " "
                                + HOUR_OPEN + ":" + MIN_OPEN + ":" + SECOND_OPEN,
                        "dd/MM/yyyy HH:mm:ss");
                TIME_CLOSE = TimeUtil.getTime(
                        TimeUtil.getTimeNow("dd/MM/yyyy") + " "
                                + HOUR_CLOSE + ":" + MIN_CLOSE + ":" + SECOND_CLOSE,
                        "dd/MM/yyyy HH:mm:ss");
            } catch (Exception e) {
            }
        }
    }

    // Trong khung giờ chơi hay không
    public static boolean isTimeOpen() {
        long now = System.currentTimeMillis();
        return now > TIME_OPEN && now < TIME_CLOSE;
    }

    // ✅ Cộng Thỏi vàng thắng vào túi đồ
    private void congTienThang(Player pl, int soThoiVang) {
        try {
            if (pl == null || soThoiVang <= 0) {
                return;
            }
            Item tv = ItemService.gI().createNewItem((short) ID_ITEM_CUOC, soThoiVang);
            InventoryService.gI().addItemBag(pl, tv, 9999);
            InventoryService.gI().sendItemBags(pl);
        } catch (Exception e) {
        }
    }

    // Lấy tổng số Thỏi vàng (457) trong túi để hiển thị UI
    private int getSoThoiVang(Player pl) {
        try {
            Item tv = InventoryService.gI().findItem(pl.inventory.itemsBag, (short) ID_ITEM_CUOC);
            if (tv != null) {
                return tv.quantity;
            }
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // hết thời gian show kết quả thì tắt cờ
                if ((this.lastTimeShow - System.currentTimeMillis()) / 1000 <= 0) {
                    this.showKQ = false;
                }

                // thông báo định kỳ
                if ((this.lastTimeThongBao - System.currentTimeMillis()) / 1000 <= 0) {
                    thongbao("Đây là minigame tranh tài ai chiến thắng, vật phẩm trong trò chơi đều là ảo, "
                            + "KHÔNG tổ chức buôn bán trái phép. Admin chúc mọi người chơi game vui vẻ !");
                    this.lastTimeThongBao = System.currentTimeMillis() + TIME_THONG_BAO;
                }

                int xx1, xx2, xx3;

                // hết thời gian 1 ván → bắt đầu nặn
                if (!this.nanKG && (this.lastTimeEnd - System.currentTimeMillis()) / 1000 <= 0) {
                    this.nanKG = true;
                    this.lastTimeNan = System.currentTimeMillis() + TIME_NAN;

                    // Logic bịp như cũ, chỉ là tiền bây giờ là Thỏi vàng
                    if (this.goldTai >= this.goldXiu) {
                        // Ưu tiên Xỉu thắng
                        if (Util.isTrue(TI_LE_BIP, 100)) {
                            xx1 = Util.nextInt(1, 4);
                            xx2 = Util.nextInt(1, 3);
                            xx3 = Util.nextInt(1, 3);
                        } else {
                            xx1 = Util.nextInt(4, 6);
                            xx2 = Util.nextInt(4, 6);
                            xx3 = Util.nextInt(3, 6);
                        }
                        if (xx1 == xx2 && xx2 == xx3) {
                            if (Util.isTrue(TI_LE_BIP, 100)) {
                                xx1 = Util.nextInt(1, 4);
                                xx2 = Util.nextInt(1, 3);
                                xx3 = Util.nextInt(1, 3);
                            } else {
                                xx1 = Util.nextInt(4, 6);
                                xx2 = Util.nextInt(4, 6);
                                xx3 = Util.nextInt(3, 6);
                            }
                        }
                    } else {
                        // Ưu tiên Tài thắng
                        if (Util.isTrue(TI_LE_BIP, 100)) {
                            xx1 = Util.nextInt(4, 6);
                            xx2 = Util.nextInt(4, 6);
                            xx3 = Util.nextInt(3, 6);
                        } else {
                            xx1 = Util.nextInt(1, 4);
                            xx2 = Util.nextInt(1, 3);
                            xx3 = Util.nextInt(1, 3);
                        }
                        if (xx1 == xx2 && xx2 == xx3) {
                            if (Util.isTrue(TI_LE_BIP, 100)) {
                                xx1 = Util.nextInt(4, 6);
                                xx2 = Util.nextInt(4, 6);
                                xx3 = Util.nextInt(3, 6);
                            } else {
                                xx1 = Util.nextInt(1, 4);
                                xx2 = Util.nextInt(1, 3);
                                xx3 = Util.nextInt(1, 3);
                            }
                        }
                    }

                    this.xucxac1 = xx1;
                    this.xucxac2 = xx2;
                    this.xucxac3 = xx3;
                    this.soPhien++;

                } else {
                    xx1 = this.xucxac1;
                    xx2 = this.xucxac2;
                    xx3 = this.xucxac3;
                }

                int tong = xx1 + xx2 + xx3;

                // hết thời gian nặn → chốt kết quả, trả thưởng
                if (this.nanKG && (this.lastTimeNan - System.currentTimeMillis()) / 1000 <= 0) {
                    this.nanKG = false;
                    this.showKQ = true;
                    this.lastTimeShow = System.currentTimeMillis() + TIME_SHOW;
                    this.lastTimeEnd = System.currentTimeMillis() + TIME_TAI_XIU;


                    if (tong <= 10) {
                        ketquaXiu = true;
                        ketquaTai = false;
                        if (List_KetQua.size() >= 20) {
                            List_KetQua.remove(0);
                        }
                        List_KetQua.add(1); // 1 = Xỉu
                    } else {
                        ketquaXiu = false;
                        ketquaTai = true;
                        if (List_KetQua.size() >= 20) {
                            List_KetQua.remove(0);
                        }
                        List_KetQua.add(0); // 0 = Tài
                    }

                    // ✅ Xử lý thắng Tài
                    if (ketquaTai && !ketquaXiu) {
                        if (!PlayersTai.isEmpty()) {
                            for (Player pl : PlayersTai) {
                                if (pl == null || Client.gI().getPlayer(pl.name) == null) {
                                    continue;
                                }
                                int nohu;
                                if (xx1 == xx2 && xx2 == xx3) {
                                    nohu = Util.nextInt(3, 5); // nổ hũ
                                } else {
                                    nohu = 1;
                                }

                                String text = "Số hệ thống quay ra\n" + xx1 + " : " + xx2 + " : " + xx3
                                        + "\nTổng là : " + tong + "\n " + LON
                                        + "\n\nBạn đã chiến thắng!!";
                                if (nohu > 1) {
                                    text += "\nBạn đã nổ hũ nhận x" + nohu + " lần số tiền cược";
                                }

                                // pl.goldTai là số Thỏi vàng đã đặt
                                double goldC = (double) (pl.goldTai + nohu * pl.goldTai * (TI_LE_AN / 100D));
                                int tienAn = Util.DoubleToInter(goldC);

                                Service.getInstance().sendThongBao(pl, text);

                                // Cộng Thỏi vàng thắng vào túi
                                congTienThang(pl, tienAn);

                                Service.getInstance().sendThongBao(pl,
                                        "Chúc mừng bạn đã dành chiến thắng và nhận được "
                                                + Util.format(tienAn) + " " + NAME_ITEM_CUOC);
                            }
                        }

                        // Bên thua (Xỉu)
                        if (!PlayersXiu.isEmpty()) {
                            for (Player pl : PlayersXiu) {
                                if (pl == null || Client.gI().getPlayer(pl.name) == null) {
                                    continue;
                                }
                                Service.getInstance().sendThongBao(pl,
                                        "Số hệ thống quay ra\n" + xx1 + " : " + xx2 + " : " + xx3
                                                + "\nTổng là : " + tong + "\n " + LON
                                                + "\n\nTrắng tay gòi, chơi lại đi!!!");
                            }
                        }

                    } else { // ✅ Xử lý thắng Xỉu
                        if (!PlayersXiu.isEmpty()) {
                            for (Player pl : PlayersXiu) {
                                if (pl == null || Client.gI().getPlayer(pl.name) == null) {
                                    continue;
                                }
                                int nohu;
                                if (xx1 == xx2 && xx2 == xx3) {
                                    nohu = Util.nextInt(3, 5);
                                } else {
                                    nohu = 1;
                                }

                                String text = "Số hệ thống quay ra\n" + xx1 + " : " + xx2 + " : " + xx3
                                        + "\nTổng là : " + tong + "\n " + NHO
                                        + "\n\nBạn đã chiến thắng!!";
                                if (nohu > 1) {
                                    text += "\nBạn đã nổ hũ nhận x" + nohu + " lần số tiền cược";
                                }

                                double goldC = (double) (pl.goldXiu + nohu * pl.goldXiu * (TI_LE_AN / 100D));
                                int tienAn = Util.DoubleToInter(goldC);

                                Service.getInstance().sendThongBao(pl, text);

                                congTienThang(pl, tienAn);

                                Service.getInstance().sendThongBao(pl,
                                        "Chúc mừng bạn đã dành chiến thắng và nhận được "
                                                + Util.format(tienAn) + " " + NAME_ITEM_CUOC);
                            }
                        }

                        // Bên thua (Tài)
                        if (!PlayersTai.isEmpty()) {
                            for (Player pl : PlayersTai) {
                                if (pl == null || Client.gI().getPlayer(pl.name) == null) {
                                    continue;
                                }
                                Service.getInstance().sendThongBao(pl,
                                        "Số hệ thống quay ra\n" + xx1 + " : " + xx2 + " : " + xx3
                                                + "\nTổng là : " + tong + "\n " + NHO
                                                + "\n\nTrắng tay gòi, chơi lại đi!!!");
                            }
                        }
                    }

                    // reset dữ liệu phiên
                    for (Player pl : PlayersTai) {
                        if (pl != null) {
                            pl.goldTai = 0;
                        }
                    }
                    for (Player pl : PlayersXiu) {
                        if (pl != null) {
                            pl.goldXiu = 0;
                        }
                    }

                    this.ketquaXiu = false;
                    this.ketquaTai = false;
                    this.goldTai = 0;
                    this.goldXiu = 0;
                    this.PlayersTai.clear();
                    this.PlayersXiu.clear();
                    this.lastTimeEnd = System.currentTimeMillis() + TIME_TAI_XIU;
                }

                Thread.sleep(500);
            }
        } catch (Exception e) {
        }
    }

    public void Send_TaiXiu(Player pl) {
        Message msg = null;
        try {
            msg = new Message(111);
            msg.writer().writeByte(0);
            msg.writer().writeByte(START);

            // thời gian còn lại
            msg.writer().writeLong((this.lastTimeEnd - System.currentTimeMillis()) / 1000);

            // ✅ số Thỏi vàng hiện tại của player (thay cho vnd)
            msg.writer().writeInt(getSoThoiVang(pl));

            // tổng player Tài / Xỉu
            msg.writer().writeInt(this.PlayersTai.size());
            msg.writer().writeInt(this.PlayersXiu.size());

            // tổng Thỏi vàng đã cược Tài / Xỉu
            msg.writer().writeLong(this.goldTai);
            msg.writer().writeLong(this.goldXiu);

            // xúc xắc
            msg.writer().writeInt(this.xucxac1);
            msg.writer().writeInt(this.xucxac2);
            msg.writer().writeInt(this.xucxac3);

            // trạng thái nặn & show kết quả
            msg.writer().writeBoolean(this.nanKG);
            msg.writer().writeBoolean(this.showKQ);

            // lịch sử 20 ván gần nhất
            msg.writer().writeInt(this.List_KetQua.size());
            for (int i = 0; i < this.List_KetQua.size(); i++) {
                msg.writer().writeInt(this.List_KetQua.get(i));
            }

            // chat
            msg.writer().writeInt(this.noiDungChat.size());
            for (int k = 0; k < this.noiDungChat.size(); k++) {
                msg.writer().writeUTF(this.noiDungChat.get(k));
            }

            pl.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void thongbao(String text) {
        Message msg;
        try {
            msg = new Message(111);
            msg.writer().writeByte(4);
            msg.writer().writeUTF(text);
            Service.getInstance().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void DatTai(Player pl) {
        if (this.nanKG) {
            Service.getInstance().sendThongBao(pl, "Ngoài thời gian đặt cược");
            return;
        }
        if (!isTimeOpen()) {
            Service.getInstance().sendThongBao(pl, "Chưa mở");
            return;
        }
        if (!pl.getSession().actived) {
            Service.getInstance().sendThongBao(pl, "Yêu cầu mở thành viên để trải nghiệm.");
            return;
        }
        if (PlayersXiu.contains(pl)) {
            Service.getInstance().sendThongBao(pl, "Bạn chỉ được trợ giúp thêm 1 bên thôi.");
            return;
        }
        Input.gI().input_Tai(pl);
    }

    public void DatXiu(Player pl) {
        if (this.nanKG) {
            Service.getInstance().sendThongBao(pl, "Ngoài thời gian đặt cược");
            return;
        }
        if (!isTimeOpen()) {
            Service.getInstance().sendThongBao(pl, "Chưa mở");
            return;
        }
        if (!pl.getSession().actived) {
            Service.getInstance().sendThongBao(pl, "Yêu cầu mở thành viên để trải nghiệm.");
            return;
        }
        if (PlayersTai.contains(pl)) {
            Service.getInstance().sendThongBao(pl, "Bạn chỉ được trợ giúp thêm 1 bên thôi.");
            return;
        }
        Input.gI().input_Xiu(pl);
    }

    public void chat(Player pl) {
        if (!pl.getSession().actived) {
            Service.getInstance().sendThongBao(pl, "Yêu cầu mở thành viên để trải nghiệm.");
            return;
        }
        Input.gI().chat_TaiXiu(pl);
    }
}
