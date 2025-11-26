package nro.models.player;

import lombok.Getter;
import lombok.Setter;
import nro.consts.ConstNpc;

import java.util.List;
import java.util.ArrayList;

public class IDMark {

    private int typeChangeMap; // capsule, ngọc rồng đen...
    private int indexMenu;     // menu npc
    private int typeInput;     // input
    private int shopId;        // shop open
    private byte typeLuckyRound; // type lucky round
    private byte isTranhNgoc = -1;

    @Getter
    @Setter
    private short idItemUpToTop;

    public boolean isUseTuiBaoVeNangCap;

    // ========= BỔ SUNG CHO HỆ THỐNG GIA ĐÌNH =========
    private List<Player> menuPlayers; // danh sách chọn tạm (dùng cho cầu hôn)
    private long idCauHon;            // id người cầu hôn

    // ========== GET/SET CŨ ==========
    public byte getTranhNgoc() {
        return isTranhNgoc;
    }

    public void setTranhNgoc(byte tn) {
        this.isTranhNgoc = tn;
    }

    public void setTypeLuckyRound(byte type) {
        this.typeLuckyRound = type;
    }

    public byte getTypeLuckyRound() {
        return this.typeLuckyRound;
    }

    public int getIndexMenu() {
        return indexMenu;
    }

    public void setIndexMenu(int indexMenu) {
        this.indexMenu = indexMenu;
    }

    public boolean isBaseMenu() {
        return this.indexMenu == ConstNpc.BASE_MENU;
    }

    public void setTypeInput(int typeInput) {
        this.typeInput = typeInput;
    }

    public int getTypeInput() {
        return this.typeInput;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public int getShopId() {
        return this.shopId;
    }

    public int getTypeChangeMap() {
        return typeChangeMap;
    }

    public void setTypeChangeMap(int typeChangeMap) {
        this.typeChangeMap = typeChangeMap;
    }

    // ========== MỚI THÊM ==========
    /** Lưu danh sách Player trong menu chọn */
    public void setMenuPlayers(List<Player> list) {
        this.menuPlayers = list;
    }

    /** Lấy player theo index trong menu */
    public Player getMenuPlayerByIndex(int index) {
        if (menuPlayers == null || index < 0 || index >= menuPlayers.size()) return null;
        return menuPlayers.get(index);
    }

    /** Set/Get id người cầu hôn */
    public void setIdCauHon(long id) {
        this.idCauHon = id;
    }

    public long getIdCauHon() {
        return this.idCauHon;
    }
}
