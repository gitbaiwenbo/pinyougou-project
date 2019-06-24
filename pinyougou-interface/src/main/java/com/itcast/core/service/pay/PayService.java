package com.itcast.core.service.pay;

import java.util.Map;

public interface PayService {
    /**
     * 生成支付二维码
     * @return
     */
    public Map<String,String> createNative(String username) throws Exception;

    /**
     * 查询订单
     * @param out_trade_no
     * @return
     * @throws Exception
     */
    public Map<String ,String> queryPayStatus(String out_trade_no) throws Exception;
}
