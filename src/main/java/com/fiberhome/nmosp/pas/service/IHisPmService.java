package com.fiberhome.nmosp.pas.service;


public interface IHisPmService {
    /**
     * 处理客户端请求
     * @param pmRequest 取性能参数
     */
    void handlePmRequest(HistoricPmRequest pmRequest);
}
