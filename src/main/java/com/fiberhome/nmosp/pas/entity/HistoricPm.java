package com.fiberhome.nmosp.pas.entity;

import java.io.Serializable;
import java.util.UUID;

/**
 * HistoricPm
 *
 * @author dongqiang  
 * @since 2019/3/4 11:34
 * @version 1.0
 */
public class HistoricPm implements Serializable {
    private String id = UUID.randomUUID().toString();
    private Integer objectId;
    private String objectName;
    private Integer neId;
    private Integer boardId;
    private Integer boardType;
    private Integer lineNo;
    private Integer seqNo;
    private Integer portNo;
    private String portName;
    private Integer portLevel;
    private String portKey;
    private Integer pmValueType;
    private double minValue;
    private double maxValue;
    private double curValue;
    private boolean hasMaxValue;
    private boolean hasMinValue;
    private boolean hasCurValue;
    private Integer isBothPmValue;
    private Integer pmCode;
    private String pmCodeName;
    private String unit;
    private String beginTime;
    private String endTime;
    private Integer locateTag;
    private long locateHash;
    private String locateStr;
    private Integer networkcrossTag;
    private String networkCrossStr;
    private String rowLocateStr;
    private Integer pmUnicol;
    private String ename;
    private String cname;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Integer getNeId() {
        return neId;
    }

    public void setNeId(Integer neId) {
        this.neId = neId;
    }

    public Integer getBoardId() {
        return boardId;
    }

    public void setBoardId(Integer boardId) {
        this.boardId = boardId;
    }

    public Integer getBoardType() {
        return boardType;
    }

    public void setBoardType(Integer boardType) {
        this.boardType = boardType;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    public Integer getPortNo() {
        return portNo;
    }

    public void setPortNo(Integer portNo) {
        this.portNo = portNo;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public Integer getPortLevel() {
        return portLevel;
    }

    public void setPortLevel(Integer portLevel) {
        this.portLevel = portLevel;
    }

    public String getPortKey() {
        return portKey;
    }

    public void setPortKey(String portKey) {
        this.portKey = portKey;
    }

    public Integer getPmValueType() {
        return pmValueType;
    }

    public void setPmValueType(Integer pmValueType) {
        this.pmValueType = pmValueType;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getCurValue() {
        return curValue;
    }

    public void setCurValue(double curValue) {
        this.curValue = curValue;
    }

    public boolean isHasMaxValue() {
        return hasMaxValue;
    }

    public void setHasMaxValue(boolean hasMaxValue) {
        this.hasMaxValue = hasMaxValue;
    }

    public boolean isHasMinValue() {
        return hasMinValue;
    }

    public void setHasMinValue(boolean hasMinValue) {
        this.hasMinValue = hasMinValue;
    }

    public boolean isHasCurValue() {
        return hasCurValue;
    }

    public void setHasCurValue(boolean hasCurValue) {
        this.hasCurValue = hasCurValue;
    }

    public Integer getIsBothPmValue() {
        return isBothPmValue;
    }

    public void setIsBothPmValue(Integer isBothPmValue) {
        this.isBothPmValue = isBothPmValue;
    }

    public Integer getPmCode() {
        return pmCode;
    }

    public void setPmCode(Integer pmCode) {
        this.pmCode = pmCode;
    }

    public String getPmCodeName() {
        return pmCodeName;
    }

    public void setPmCodeName(String pmCodeName) {
        this.pmCodeName = pmCodeName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getLocateTag() {
        return locateTag;
    }

    public void setLocateTag(Integer locateTag) {
        this.locateTag = locateTag;
    }

    public long getLocateHash() {
        return locateHash;
    }

    public void setLocateHash(long locateHash) {
        this.locateHash = locateHash;
    }

    public String getLocateStr() {
        return locateStr;
    }

    public void setLocateStr(String locateStr) {
        this.locateStr = locateStr;
    }

    public Integer getNetworkcrossTag() {
        return networkcrossTag;
    }

    public void setNetworkcrossTag(Integer networkcrossTag) {
        this.networkcrossTag = networkcrossTag;
    }

    public String getNetworkCrossStr() {
        return networkCrossStr;
    }

    public void setNetworkCrossStr(String networkCrossStr) {
        this.networkCrossStr = networkCrossStr;
    }

    public String getRowLocateStr() {
        return rowLocateStr;
    }

    public void setRowLocateStr(String rowLocateStr) {
        this.rowLocateStr = rowLocateStr;
    }

    public Integer getPmUnicol() {
        return pmUnicol;
    }

    public void setPmUnicol(Integer pmUnicol) {
        this.pmUnicol = pmUnicol;
    }

    public String getEname() {
        return ename;
    }

    public void setEname(String ename) {
        this.ename = ename;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }
}
