package com.fiberhome.nmosp.pas.dao;

import com.fiberhome.nmosp.pas.entity.HistoricPm;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SelectPmData {

    List<HistoricPm> query15mHistoricPmByPage(Map<String, Object> map);

    List<HistoricPm> query24hHistoricPmByPage(Map<String, Object> map);

    List<String> queryTableNames(@Param("dbName") String dbName);

    Integer queryPm15mDbCategory(@Param("pmCode") Integer pmCode);

    /**
     * 获取网元分表数量
     * @return 分表数
     */
    Integer getNeSplitCount(Map<String, Object> map);

    Integer getReserveMonth();

    /**
     * 获取所有15分钟性能数据库名
     * @return 数据库名
     */
    List<String> getAllHis15mDbName();

    /**
     * 根据neId获取BoardIds
     * @param neId 网元id
     * @return boardIds
     */
    List<Integer> getBoardIdsByNeId(Integer neId);
}
