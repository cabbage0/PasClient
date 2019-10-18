package com.fiberhome.nmosp.pas.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 历史性能数据请求实体，对应存储本地文件
 * 标志一次请求调用
 */
public class HistoricPmRequest {

    private String taskId;

    /**
     * 判断是否是24小时或者15分钟性能
     */
    private boolean b24H;

    /**
     * neId-boardId-portNo
     */
    private Map<Integer, Map<Integer, List<Integer>>> neIdBoardIdPortIdMap;

    /**
     * 性能代码数组
     */
    private List<Integer> pmTypes;

    /**
     * 性能开始时间
     */
    private Date beginTime;

    /**
     * 性能结束时间
     */
    private Date endTime;
    /**
     * EMS IP
     */
    private String emsIp;
    /**
     * 是否是物理口
     */
    private boolean isPhy;
    /**
     * 网管类型(otnm2000, unm2000)
     */
    private String nmType;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public boolean isB24H() {
        return b24H;
    }

    public void setB24H(boolean b24H) {
        this.b24H = b24H;
    }

    public Map<Integer, Map<Integer, List<Integer>>> getNeIdBoardIdPortIdMap() {
        return neIdBoardIdPortIdMap;
    }

    public void setNeIdBoardIdPortIdMap(Map<Integer, Map<Integer, List<Integer>>> neIdBoardIdPortIdMap) {
        this.neIdBoardIdPortIdMap = neIdBoardIdPortIdMap;
    }

    public List<Integer> getPmTypes() {
        return pmTypes;
    }

    public void setPmTypes(List<Integer> pmTypes) {
        this.pmTypes = pmTypes;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getEmsIp() {
        return emsIp;
    }

    public void setEmsIp(String emsIp) {
        this.emsIp = emsIp;
    }

    public boolean isPhy() {
        return isPhy;
    }

    public void setPhy(boolean phy) {
        isPhy = phy;
    }

    public String getNmType() {
        return nmType;
    }

    public void setNmType(String nmType) {
        this.nmType = nmType;
    }

    @Override
    public String toString() {
        return "HistoricPmRequest{" +
                "taskId='" + taskId + '\'' +
                ", b24H=" + b24H +
                ", neIdBoardIdPortIdMap=" + neIdBoardIdPortIdMap +
                ", pmTypes=" + pmTypes +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                ", emsIp='" + emsIp + '\'' +
                ", isPhy=" + isPhy +
                ", nmType='" + nmType + '\'' +
                '}';
    }
}
