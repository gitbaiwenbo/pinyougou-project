package com.itcast.core.service.pay;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.itcast.core.dao.log.PayLogDao;
import com.itcast.core.pojo.log.PayLog;
import com.itcast.core.utils.httpclient.HttpClient;
import com.itcast.core.utils.uniquekey.IdWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Service
public class PayServiceImpl implements PayService {
    @Resource
    private IdWorker idWorker;
    @Value("${appid}")
    private String appid;// 微信公众账号或开放平台APP的唯一标识
    @Value("${partner}")
    private String partner;// 财付通平台的商户账号
    @Value("${partnerkey}")
    private String partnerkey;// 财付通平台的商户密钥
    @Value("${notifyurl}")
    private String notifyurl;// 回调地址
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private PayLogDao payLogDao;

    /**
     * 生成支付二维码
     * @return
     */
    @Override
    public Map<String, String> createNative(String username)throws Exception {
        PayLog payLog= (PayLog) redisTemplate.boundHashOps("payLog").get(username);
        String out_trade_no = payLog.getOutTradeNo();
        //long out_trade_no = idWorker.nextId();
        // 接口需要接收的数据：xml格式的（map转成xml）
        Map<String,String> data=new HashMap<>();
        // 调用微信统一下单的接口地址
        String url="https://api.mch.weixin.qq.com/pay/unifiedorder";
        //        公众账号ID   appid  是  String(32) wxd678efh567hg6787 微信支付分配的公众账号ID（企业号corpid即为此appId）
        data.put("appid", appid);
//        商户号  mch_id 是  String(32) 1230000109 微信支付分配的商户号
        data.put("mch_id", partner);
//        随机字符串    nonce_str  是  String(32) 5K8264ILTKCH16CQ2502SI8ZNMTM67VS   随机字符串，长度要求在32位以内。推荐随机数生成算法
        data.put("nonce_str", WXPayUtil.generateNonceStr());
//        签名   sign   是  String(32) C380BEC2BFD727A4B6845133519F3AD6   通过签名算法计算得出的签名值，详见签名生成算法
//        商品描述 body   是  String(128)    腾讯充值中心-QQ会员充值
        data.put("body", "品优购订单支付");
//        商户订单号    out_trade_no   是  String(32) 20150806125346 商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|* 且在同一个商户号下唯一。详见商户订单号
        //data.put("out_trade_no", String.valueOf(out_trade_no));
        data.put("out_trade_no",out_trade_no);
//        标价金额 total_fee  是  Int    88 订单总金额，单位为分，详见支付金额
        data.put("total_fee", String.valueOf(payLog.getTotalFee()));
        //data.put("total_fee", "1");  // 支付金额
//        终端IP spbill_create_ip   是  String(64) 123.12.12.123  支持IPV4和IPV6两种格式的IP地址。调用微信支付API的机器IP
        data.put("spbill_create_ip", "123.12.12.123");
//        通知地址 notify_url 是  String(256)    http://www.weixin.qq.com/wxpay/pay.php 异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        data.put("notify_url", notifyurl);
//        交易类型 trade_type 是  String(16) JSAPI -JSAPI支付 NATIVE -Native支付 APP -APP支付
        data.put("trade_type", "NATIVE");
        // 将map数据转成xml
        // "<xml><node1>xxxx</node1>...</xml>"
        String xmlParam  = WXPayUtil.generateSignedXml(data, partnerkey);
        // 通过httpclient模拟浏览器发送请求
        HttpClient httpClient=new HttpClient(url);
        httpClient.setXmlParam(xmlParam );// 请求需要的数据
        httpClient.post();
        httpClient.setHttps(true);
        //成功调用后，响应数据
        String strXML = httpClient.getContent();
        // 将xml转成map
        Map<String, String> map = WXPayUtil.xmlToMap(strXML);
        //map.put("out_trade_no", String.valueOf(out_trade_no));
        map.put("out_trade_no",out_trade_no);
        //map.put("total_fee", "1");  // 展示的金额
        map.put("total_fee", String.valueOf(payLog.getTotalFee()));
        return map;
    }

    /**
     * 查询订单
     * @param out_trade_no
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) throws Exception {
        // 查询订单的接口地址
        String url="https://api.mch.weixin.qq.com/pay/orderquery";
        // 封装接口需要的数据
        Map<String,String> data=new HashMap<>();
        //公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        data.put("appid",appid);
        //商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        data.put("mch_id",partner);
        //商户订单号	out_trade_no	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 详见商户订单号
        data.put("out_trade_no",out_trade_no);
        //随机字符串	nonce_str	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	随机字符串，不长于32位。推荐随机数生成算法
        data.put("nonce_str",WXPayUtil.generateNonceStr());
        // 通过httpclient模拟浏览器发送请求
        String xmlParam = WXPayUtil.generateSignedXml(data, partnerkey);
        HttpClient httpClient=new HttpClient(url);
        httpClient.setHttps(true);
        httpClient.setXmlParam(xmlParam);
        httpClient.post();
        // 响应结果（xml转成map）
        String strXML = httpClient.getContent();
        Map<String, String> map = WXPayUtil.xmlToMap(strXML);
        String tradeState = map.get("trade_state");
        // 如果支付成功，更新交易日志
        if ("SUCCESS".equals(tradeState)){
            PayLog payLog=new PayLog();
            payLog.setOutTradeNo(out_trade_no);
            payLog.setPayTime(new Date());
            payLog.setTradeState("1");
            payLog.setTransactionId("transaction_id");
            payLogDao.updateByPrimaryKeySelective(payLog);
        }
        return map;
    }
}
