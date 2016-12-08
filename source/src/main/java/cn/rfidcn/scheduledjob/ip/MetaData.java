package cn.rfidcn.scheduledjob.ip;

/**
 * IP meta data
 *
 * @author senhui.li
 */
public class MetaData {

    private int _BaseAddr = 0;
    private int _DBCount = 0;
    private int _DBColumn = 0;
    private int _DBType = 0;
    private int _DBDay = 1;
    private int _DBMonth = 1;
    private int _DBYear = 1;
    private int _BaseAddrIPv6 = 0;
    private int _DBCountIPv6 = 0;
    private boolean _OldBIN = false;

    int getBaseAddr() {
        return this._BaseAddr;
    }

    int getDBCount() {
        return this._DBCount;
    }

    int getDBColumn() {
        return this._DBColumn;
    }

    int getDBType() {
        return this._DBType;
    }

    int getDBDay() {
        return this._DBDay;
    }

    int getDBMonth() {
        return this._DBMonth;
    }

    int getDBYear() {
        return this._DBYear;
    }

    int getBaseAddrIPv6() {
        return this._BaseAddrIPv6;
    }

    int getDBCountIPv6() {
        return this._DBCountIPv6;
    }

    boolean getOldBIN() {
        return this._OldBIN;
    }

    void setBaseAddr(int paramInt) {
        this._BaseAddr = paramInt;
    }

    void setDBCount(int paramInt) {
        this._DBCount = paramInt;
    }

    void setDBColumn(int paramInt) {
        this._DBColumn = paramInt;
    }

    void setDBType(int paramInt) {
        this._DBType = paramInt;
    }

    void setDBDay(int paramInt) {
        this._DBDay = paramInt;
    }

    void setDBMonth(int paramInt) {
        this._DBMonth = paramInt;
    }

    void setDBYear(int paramInt) {
        this._DBYear = paramInt;
    }

    void setBaseAddrIPv6(int paramInt) {
        this._BaseAddrIPv6 = paramInt;
    }

    void setDBCountIPv6(int paramInt) {
        this._DBCountIPv6 = paramInt;
    }

    void setOldBIN(boolean paramBoolean) {
        this._OldBIN = paramBoolean;
    }

}
