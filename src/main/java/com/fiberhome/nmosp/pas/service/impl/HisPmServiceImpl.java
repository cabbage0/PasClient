package com.fiberhome.nmosp.pas.service.impl;

import com.fiberhome.nmosp.pas.constants.Constant;
import com.fiberhome.nmosp.pas.dao.HistoricalPmDao;
import com.fiberhome.nmosp.pas.entity.HistoricPm;
import com.fiberhome.nmosp.pas.entity.HistoricPmRequest;
import com.fiberhome.nmosp.pas.entity.LineNoMap;
import com.fiberhome.nmosp.pas.entity.PmCodeName;
import com.fiberhome.nmosp.pas.hikaricp.HiDataSourceBeanBuilder;
import com.fiberhome.nmosp.pas.hikaricp.HiDataSourceContext;
import com.fiberhome.nmosp.pas.service.IHisPmService;
import com.fiberhome.nmosp.pas.threadpool.ThreadPoolGroup;
import com.fiberhome.nmosp.pas.thrift.exception.RPCApplicationException;
import com.fiberhome.nmosp.pas.utils.*;
import com.fiberhome.nmosp.pas.xml2obj.STDVO;
import com.fiberhome.nmosp.pas.zip.ZipTask;
import com.fiberhome.nmosp.pas.zip.ZipTaskQueue;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fiberhome.nmosp.pas.constants.Constant.*;

/**
 * 历史性能Service
 */
@Service
public class HisPmServiceImpl implements IHisPmService {
    /**
     * log.
     */
    private static final Logger TRACER = LoggerFactory.getLogger(HisPmServiceImpl.class);
    private final static String TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";
    private final static String PM15_DB_PREFIX = "pasdatahis15mdb_";
    private final static String PM24H_DB_NAME = "pasdatahis24hdb";
    private AtomicInteger totalTaskCount = null;
    private AtomicInteger count = null;
    @Autowired
    private HistoricalPmDao historicalPmDao;

    @Override
    public void handlePmRequest(HistoricPmRequest pmRequest) {
        count = new AtomicInteger(0);
        TRACER.info("------beginTime: " + System.currentTimeMillis());
        boolean b24H = pmRequest.isB24H();
        if (b24H) {
            findAndSave24hPm(pmRequest);
        } else {
            findAndSave15mPm(pmRequest);
        }
    }

    private void findAndSave24hPm(HistoricPmRequest pmRequest) {
        try {
            List<String> pm24hDbNames = Collections.singletonList(PM24H_DB_NAME);
            totalTaskCount = new AtomicInteger(pm24hDbNames.size());
            for (String dbName : pm24hDbNames) {
                handleByDateConcurrency(dbName, 0, pmRequest);
            }
        } catch (Exception e) {
            TRACER.error(e.getMessage(), e);
        }
    }

    private void findAndSave15mPm(HistoricPmRequest pmRequest) {
        TRACER.info("----pmRequest: " + pmRequest);
        try {
            List<String> pm15mDbNames = historicalPmDao.getAllHis15mDbName();
            List<Integer> pmTypes = pmRequest.getPmTypes();
            //TODO： 将所有性能代码分组

            if (!CollectionUtils.isEmpty(pmTypes)) {
                totalTaskCount = new AtomicInteger(pmTypes.size());
                for (Integer pmCode : pmTypes) {
                    Integer dbIndex = get15mPmDBId(pmCode);
                    String dbName = PM15_DB_PREFIX + dbIndex;
                    boolean exist = false;
                    for (String name : pm15mDbNames) {
                        if (name.equalsIgnoreCase(dbName)) {
                            exist = true;
                            break;
                        }
                    }
                    if (exist) {
                        handleByDateConcurrency(dbName, dbIndex, pmRequest);
                    } else {
                        TRACER.error(dbName + " db not exist...");
                    }
                }
            } else {
                if (!CollectionUtils.isEmpty(pm15mDbNames)) {
                    totalTaskCount = new AtomicInteger(pm15mDbNames.size());
                    for (String dbName : pm15mDbNames) {
                        //匹配对应时间区间的数据表
                        Integer dbId = null;
                        try {
                            dbId = Integer.valueOf(dbName.split("_")[1]);
                        } catch (NumberFormatException e) {
                            TRACER.error(e.getMessage(), e);
                        }
                        handleByDateConcurrency(dbName, dbId, pmRequest);
                    }
                }
            }
        } catch (Exception e) {
            TRACER.error(e.getMessage(), e);
        }
    }

    private void sendZipMessage(HistoricPmRequest pmRequest) {
        //发消息压缩
        ZipTask zipTask = new ZipTask();
        zipTask.setZipTaskId(pmRequest.getTaskId());
        zipTask.setData(false);
        ZipTaskQueue.offerTask(zipTask);
    }

    private void handleByDateConcurrency(String dbName, Integer dbId, HistoricPmRequest pmRequest) {
        Integer neSplitCount = getNeSplitCount(pmRequest.isB24H(), dbId);
        Integer reserveMonth = getHisDataReserveMonth();
        Map<Integer, Map<Integer, List<Integer>>> neIdBoardIdPortIdMap = pmRequest.getNeIdBoardIdPortIdMap();
        Map<Integer, List<Integer>> tableId2NeIdListMap = new HashMap<>();
        if (null != neIdBoardIdPortIdMap) {
            Set<Integer> neIds = neIdBoardIdPortIdMap.keySet();
            //将网元分组
            for (int i = 1; i <= neSplitCount; i++) {
                final int index = i;
                List<Integer> neIdGroup = neIds.stream()
                        .filter(neId -> neId % neSplitCount + 1 == index).collect(Collectors.toList());
                TRACER.debug("index=" + index + ", boradIdsGroup size=" + neIdGroup.size());
                tableId2NeIdListMap.put(index, neIdGroup);
            }
//            tableId2NeIdListMap.forEach((tableId, neIdList) -> {
//                TRACER.info("tableId=" + tableId + ", neIdList=" + neIdList);
//            });
            AtomicInteger subTotalTaskCount = new AtomicInteger(neSplitCount);
            //将每个网元【映射号】对应的数据提交一个任务
            for (Map.Entry<Integer, List<Integer>> entry : tableId2NeIdListMap.entrySet()) {
                //每个entry对应一个表，对应一个任务
                Integer tableId = entry.getKey();
                List<Integer> neIdList = entry.getValue();
                TRACER.debug("handleByDateConcurrency neList: " + neIdList + ", pmRequest=" + pmRequest);
                List<Date> betweenDates = DateUtils.getBetweenDates(pmRequest.getBeginTime(), pmRequest.getEndTime());
                TRACER.debug("subTask betweenDates=" + betweenDates);
                AtomicInteger subTaskCount = new AtomicInteger(betweenDates.size());
                try {
                    for (Date date : betweenDates) {
                        PullPmTask task = new PullPmTask(dbName, tableId,
                                reserveMonth, neIdList, pmRequest, date, subTaskCount, subTotalTaskCount);
                        ThreadPoolExecutor executor = ThreadPoolGroup.getThreadPool(tableId);
                        TRACER.debug("Executor: " + tableId + "--" + executor.getCorePoolSize());
                        executor.submit(task);
                    }
                } catch (Exception e) {
                    TRACER.error(e.getMessage(), e);
                }
            }
        }
    }


    /**
     * 根据性能代码获取数据库分类Id
     *
     * @param pmCode 性能代码
     * @return 分库id
     */
    private Integer get15mPmDBId(Integer pmCode) {
        //根据性能代码查询数据库
        Integer dbIndex = historicalPmDao.queryPm15mDbCategory(pmCode);
        if (null == dbIndex) {
            switch (pmCode) {
                case 839:
                case 840:
                    dbIndex = 2;
                    break;
                case 316:
                case 317:
                case 318:
                case 319:
                case 320:
                    dbIndex = 3;
                    break;
                default:
                    dbIndex = 1;
                    break;
            }
        }

        return dbIndex;
    }

    /**
     * 查询网元分割数
     *
     * @param is24Hour 是否24小时
     * @param dbId     数据库id
     * @return 分表数
     */
    private Integer getNeSplitCount(boolean is24Hour, Integer dbId) {
        Map<String, Object> map = new HashMap<>();
        map.put("is24Hour", is24Hour);
        map.put("dbId", dbId);
        return historicalPmDao.getNeSplitCount(map);
    }

    /**
     * 获取历史性能保留月数
     *
     * @return 月数
     */
    private Integer getHisDataReserveMonth() {

        return historicalPmDao.getReserveMonth();
    }

    private class PullPmTask implements Callable<Void> {
        private volatile Integer tableId;
        private volatile Integer reserveMonth;
        private volatile List<Integer> neIdList;
        private volatile HistoricPmRequest pmRequest;
        private volatile String dbName;
        private volatile Date date;
        private AtomicInteger subTaskCount;
        private AtomicInteger subTotalTaskCount;

        PullPmTask(String dbName, Integer tableId, Integer reserveMonth, List<Integer> neIdList,
                   HistoricPmRequest pmRequest, Date date,
                   AtomicInteger subTaskCount, AtomicInteger subTotalTaskCount) {
            this.dbName = dbName;
            this.tableId = tableId;
            this.reserveMonth = reserveMonth;
            this.neIdList = neIdList;
            this.pmRequest = pmRequest;
            this.date = date;
            this.subTaskCount = subTaskCount;
            this.subTotalTaskCount = subTotalTaskCount;
        }

        @Override
        public Void call() throws Exception {
            String tableName = getTableName(pmRequest.isB24H(), tableId, reserveMonth, date);
            TRACER.info("table name is:"+tableName);
            Map<String, Object> map = new HashMap<>();
            map.put(TABLE_NAME_KEY, "`" + tableName + "`");

            Date beginTime = pmRequest.getBeginTime();
            Date endTime = pmRequest.getEndTime();
            map.put(BEGIN_TIME_KEY, "'" + DateUtils.getTimeString(TIME_FORMATTER, beginTime) + "'");
            map.put(END_TIME_KEY, "'" + DateUtils.getTimeString(TIME_FORMATTER, endTime) + "'");

            List<Integer> boardIdList = new ArrayList<>();
            Map<Integer, Map<Integer, List<Integer>>> neIdBoardIdPortIdMap = pmRequest.getNeIdBoardIdPortIdMap();
            TRACER.info("---neIdBoardIdPortIdMap=" + neIdBoardIdPortIdMap);
            for (Integer neId : neIdList) {
                TRACER.info("neId=" + neId);
                Map<Integer, List<Integer>> boardPortMap = neIdBoardIdPortIdMap.get(neId);
                TRACER.info("boardPortMap=" + boardPortMap);
                //取出所有的boardId
                if (!CollectionUtils.isEmpty(boardPortMap)) {
                    Set<Integer> boardIds = boardPortMap.keySet();
                    TRACER.info("boardIds---------" + boardIds);
                    boardIdList.addAll(boardIds);
                } else {
                    //查询数据库t_pmobjinfo获取该网元对应的所有boardId
                    List<Integer> boardIds = historicalPmDao.getBoardIdsByNeId(neId);
                    TRACER.info("getBoardIdsByNeId from db =" + boardIds);
                    boardIdList.addAll(boardIds);
                }
            }
            TRACER.info("boardIdList=" + boardIdList);
            //获取请求中的单盘数量，超过1000后返回错误码
            int boardCount = boardIdList.size();
            if(boardCount>1000){
                throw new RPCApplicationException(TOO_MORE_BOARD,null,-1);
            }

            if (!CollectionUtils.isEmpty(pmRequest.getPmTypes())) {
                map.put(PM_TYPE_KEY, pmRequest.getPmTypes());
            }

            map.put(IS_PHY_PORT, pmRequest.isPhy());

            if (!CollectionUtils.isEmpty(boardIdList)) {
                for (List list : Iterables.partition(boardIdList, 10)) {
                    map.put(BOARD_ID_KEY, list);
                    saveHisPmByPage(dbName, pmRequest, map);
                }
            } /*else {
                for (List list : Iterables.partition(neIdList, 10)) {
                    map.put(NE_ID_KEY, list);
                    saveHisPmByPage(dbName, pmRequest, map);
                }
            }*/

            if (subTaskCount.decrementAndGet() == 0) {
                TRACER.info("-----TASK FINISH!!!!!!!");
                int iSubTotal = subTotalTaskCount.decrementAndGet(); //subTotal-1
                if (iSubTotal == 0) { //代表当前DB的数据处理完了，还要处理其他db
                    TRACER.debug("----------------------CURRENT GROUP SUB TASK FINISH!!!!!!!  count=" + count.get()); //当前DB的四组数据处理完了
                    int total = totalTaskCount.decrementAndGet(); //对总任务数 -1
                    if (total == 0) { //所有任务结束
                        TRACER.info("------start zip----------------ALL TASK FINISH!!!!!!!  count=" + count.get());
                        TRACER.info("------>endTime: " + System.currentTimeMillis());
                        sendZipMessage(pmRequest);
                    }
                }
            }
            return null;
        }
    }

    private String getTableName(boolean is24Hour, Integer tableId, Integer reserveMonth, Date date) {
        StringBuilder tableName = new StringBuilder();
        if (is24Hour) {
            tableName.append("t_pmhis24_");
        } else {
            tableName.append("t_pmhis15_");
        }

        tableName.append(tableId).append("_");
        Calendar calendar = DateUtils.getCalendar(date);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        tableName.append(((month - 1) % reserveMonth) + 1).append("_").append(day);
        return tableName.toString();
    }

    /**
     * 按页拉取数据并存储
     *
     * @param pmRequest request
     * @param map       查询参数
     */
    private void saveHisPmByPage(String dbName, HistoricPmRequest pmRequest, Map<String, Object> map) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageSize(MYSQL_FETCH_PAGESIZE);
        int index = 1;
        while (true) {
            pageInfo.setPageNo(index);
            pageInfo.setDbIndex((index - 1) * MYSQL_FETCH_PAGESIZE);
            map.put(PAGEINFO_KEY, pageInfo);
            List<HistoricPm> historicPmList;
            changeHiDataSource(dbName);
            if (pmRequest.isB24H()) {
                historicPmList = historicalPmDao.query24hHistoricPmByPage(map);
            } else {
                historicPmList = historicalPmDao.query15mHistoricPmByPage(map);
            }
            resetHiDataSource();
            int resultSize = historicPmList.size();
            count.addAndGet(resultSize);
            /*TRACER.info("index=" + index + ", table=" + map.get(TABLE_NAME_KEY)
                    + ", historicPmList size =" + resultSize + ", count=" + count);*/
            if (resultSize == 0) {
                break;
            }

            //Send Store Data Task to Queue..
            if (!CollectionUtils.isEmpty(historicPmList)) {
                long beginFilter = System.currentTimeMillis();
                List<HistoricPm> hisList = filterDataByPort(pmRequest, historicPmList);
                //TRACER.info("filter cost time: " + (System.currentTimeMillis() - beginFilter) + "ms");
                ZipTask zipTask = new ZipTask();
                zipTask.setData(true);
                zipTask.setZipTaskId(pmRequest.getTaskId());
                zipTask.setSavePath(FileUtil.getDataStoragePath(pmRequest.getTaskId()));
                zipTask.setPmList(hisList);
                ZipTaskQueue.offerTask(zipTask);
            }
            index++;
        }
    }

    private List<HistoricPm> filterDataByPort(HistoricPmRequest pmRequest, List<HistoricPm> historicPmList) {
        List<HistoricPm> resultList = new ArrayList<>();
        Map<Integer, Map<Integer, List<Integer>>> neIdBoardIdPortIdMap = pmRequest.getNeIdBoardIdPortIdMap();

        for (Map.Entry<Integer, Map<Integer, List<Integer>>> entry : neIdBoardIdPortIdMap.entrySet()) {
            Integer neId = entry.getKey();
            Map<Integer, List<Integer>> boardPortMap = entry.getValue();
            if (CollectionUtils.isEmpty(boardPortMap)) {
                //返回neId对应的所有记录
                List<HistoricPm> list = historicPmList.stream().filter(pm -> pm.getNeId().equals(neId)).collect(Collectors.toList());
                resultList.addAll(list);
            } else {
                for (Map.Entry<Integer, List<Integer>> e : boardPortMap.entrySet()) {
                    Integer boardId = e.getKey();
                    List<Integer> portList = e.getValue();
                    if (!CollectionUtils.isEmpty(portList)) {
                        for (Integer port : portList) {
                            List<HistoricPm> list = historicPmList.stream().filter(pm -> pm.getPortNo().equals(port)
                                    && pm.getNeId().equals(neId) && pm.getBoardId().equals(boardId)).collect(Collectors.toList());
                            resultList.addAll(list);
                        }
                    } else {
                        //返回所有board的所有端口记录
                        List<HistoricPm> list = historicPmList.stream().filter(pm -> pm.getBoardId().equals(boardId)).collect(Collectors.toList());
                        resultList.addAll(list);
                    }
                }
            }
        }

        //添加性能名
        resultList.forEach(pm -> {
            int pmCode = pm.getPmCode();
            try {
                STDVO stdvo = STDVO.getInstance(SysConfigUtil.getProperty(Constant.PAS_PMCODE_MAPPING_FILE));
                PmCodeName pmCodeName = STDVO.getPmCodeName(stdvo, String.valueOf(pmCode), Constant.CLASS_TYPE);
                pm.setEname(pmCodeName.getEname());
                pm.setCname(pmCodeName.getCname());
                pm.setPortName(getPortInfo(pm).getPortName());

                //针对U网管，转换性能代码
                //当接收U2000网管处理请求时，目前App传递给Provider的性能代码是O2000的性能代码，由Provider转为U2000性能代码传给当前程序
                //当前程序查询PAS数据库，取到的性能代码为U2000性能代码，需要转换为O2000性能代码提供给APP
                if (pmRequest.getNmType().trim().equalsIgnoreCase(Constant.UNM2000)) {
                    String pmUnicol = String.valueOf(pmCode);
                    Integer o2000PmCode = STDVO.transferUnicol2PmCode(stdvo, pmUnicol, Constant.CLASS_TYPE);
                    pm.setPmCode(o2000PmCode);
                    pm.setPmUnicol(pmCode);
                }
            } catch (Exception e) {
                TRACER.error(e.getMessage(), e);
            }
        });

        return resultList;
    }


    private HistoricPm getPortInfo(HistoricPm pm) {
        Integer boardTypeId = pm.getBoardType();
        Integer lineNo = pm.getLineNo();
        Integer portNo = pm.getPortNo();

        List<LineNoMap> boardTypes = BoardTypeUtil.getBoardTypes();
        for (LineNoMap lnm : boardTypes) {
            if (lnm.getBoardtypeid().equals(String.valueOf(boardTypeId))
                    && lnm.getPhyportno().equals(String.valueOf(portNo))
                    && lnm.getPhyportlineno().equals(String.valueOf(lineNo))) {
                pm.setPortName(lnm.getPhyportname());
                break;
            }
        }

        return pm;
    }

    private void changeHiDataSource(String dbName) {
        HiDataSourceBeanBuilder builder = new HiDataSourceBeanBuilder(dbName,
                SysConfigUtil.getProperty("pas_ip", "127.0.0.1"),
                SysConfigUtil.getProperty("pas_mysql_port", "60298"),
                dbName,
                SysConfigUtil.getProperty("pas_mysql_uname", "root"),
                SysConfigUtil.getProperty("pas_mysql_pass", "vislecaina"),
                dbName);
        HiDataSourceContext.setDataSource(builder);
    }

    private void resetHiDataSource() {
        HiDataSourceContext.clearDataSource();
    }

    public void traverse() {
        try {
            changeHiDataSource(PM24H_DB_NAME);
            List<String> tableNames = historicalPmDao.queryTableNames(PM24H_DB_NAME);
            for (String tableName : tableNames) {
                List<HistoricPm> pmList = historicalPmDao.findRecord(tableName);
                if (!CollectionUtils.isEmpty(pmList))
                    TRACER.info("tableName=" + tableName + "pmList=" + pmList);
            }
            resetHiDataSource();
            TRACER.info("finished......");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
