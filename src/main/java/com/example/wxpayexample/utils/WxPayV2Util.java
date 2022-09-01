package com.example.wxpayexample.utils;

import cn.hutool.core.date.DateUtil;
import com.example.wxpayexample.bean.PayDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

/**
 * @Author Yu.Xing
 * @Description V2版本微信支付
 **/
@Slf4j
public class WxPayV2Util {

    private static byte[] certData;
    private static final String UNIFIEDORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";//统一下单API接口链接
    private static final String REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";//统一退款API接口链接
    private static final String TRANSFERS_URL = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";//统一企业支付（提现）API接口链接
    private static final String QUERY_ORDER_URL = "https://api.mch.weixin.qq.com/pay/orderquery";//查询订单
    private static final String CLOSE_ORDER = "https://api.mch.weixin.qq.com/pay/closeorder";//关闭订单

    private static PayDto payDto;

    public WxPayV2Util(PayDto payDto) {
        this.payDto = payDto;
    }

    /**
     * @Author Yu.Xing
     * @Description 支付接口
     **/
    public static String pay() throws Exception {
        SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
        parameters.put("appid", payDto.getAppId());
        parameters.put("mch_id", payDto.getMchId());
        parameters.put("nonce_str", getNonceStr());
        //微信支付body字段最多127字节
        if (payDto.getBody().length() > 30) {
            parameters.put("body", payDto.getBody().substring(0, 30));
        } else {
            parameters.put("body", payDto.getBody());
        }
        parameters.put("out_trade_no", payDto.getOrderId());// 订单号
        parameters.put("total_fee", payDto.getTotal_fee().toString());// 支付金额单位：分
        parameters.put("spbill_create_ip", payDto.getSpbill_create_ip());
        parameters.put("notify_url", payDto.getNotify_url());//支付回调地址
        parameters.put("trade_type", "JSAPI");
        parameters.put("openid", payDto.getOpenId());//openid :小程序openId需填写
        parameters.put("time_expire", DateUtil.format(DateUtil.offsetMinute(new Date(), 5), "yyyyMMddHHmmss"));// 交易结束时间,需要动态传入,格式为yyyyMMddHHmmss
        String sign = createSign("UTF-8", parameters);
        parameters.put("sign", sign);
        String requestXML = getRequestXml(parameters);
        log.info("下单请求参数:{}", requestXML);
        //调用统一下单接口
        String result = httpsRequest(UNIFIEDORDER_URL, "POST", requestXML);
        return result;
    }

    /**
     * @Author Yu.Xing
     * @Description 订单查询
     **/
    public static String orderQuery() {
        SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
        parameters.put("appid", payDto.getAppId());
        parameters.put("mch_id", payDto.getMchId());
        parameters.put("out_trade_no", payDto.getOrderId());// 订单号
        parameters.put("nonce_str", getNonceStr());
        String sign = createSign("UTF-8", parameters);
        parameters.put("sign", sign);
        String requestXML = getRequestXml(parameters);
        String result = httpsRequest(QUERY_ORDER_URL, "POST", requestXML);
        return result;
    }

    /**
     * @Author Yu.Xing
     * @Description 关闭订单
     **/
    public static String closeOrder() {
        SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
        parameters.put("appid", payDto.getAppId());
        parameters.put("mch_id", payDto.getMchId());
        parameters.put("out_trade_no", payDto.getOrderId());// 订单号
        parameters.put("nonce_str", getNonceStr());
        String sign = createSign("UTF-8", parameters);
        parameters.put("sign", sign);
        String requestXML = getRequestXml(parameters);
        String result = httpsRequest(CLOSE_ORDER, "POST", requestXML);
        return result;
    }

    /**
     * @Author Yu.Xing
     * @Description 申请退款
     **/
    public static String refund() throws Exception {

        SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
        parameters.put("appid", payDto.getAppId());
        parameters.put("mch_id", payDto.getMchId());
        parameters.put("nonce_str", getNonceStr());
        parameters.put("out_trade_no", payDto.getOrderId()); //商户订单号和微信订单号二选一
        //设置商户退款单号
        Integer randomNumber = new Random().nextInt(900) + 100;
        String orderIncrementId = DateUtil.format(new Date(), "yyyyMMddHHmmssSSS") + randomNumber;
        parameters.put("out_refund_no", orderIncrementId);
        parameters.put("total_fee", payDto.getTotal_fee());
        parameters.put("refund_fee", payDto.getTotal_fee());
        parameters.put("sign_type", "MD5");
        parameters.put("notify_url", payDto.getRefund_notify_url());
        String sign = createSign("UTF-8", parameters);
        //签名算法
        parameters.put("sign", sign);
        String requestXML = getRequestXml(parameters);
        String xmlStr = httpsRequestByCert(REFUND_URL, requestXML);
        return xmlStr;
    }

    public static String transfers() throws Exception {
        SortedMap<Object, Object> parameters = new TreeMap<>();
        parameters.put("mch_appid", payDto.getAppId());//商户账号appid
        parameters.put("mchid", payDto.getMchId());//商户号
        parameters.put("nonce_str", getNonceStr());//随机字符串
        parameters.put("partner_trade_no", payDto.getPartner_trade_no());// 订单号 //商户订单号
        parameters.put("openid", payDto.getOpenId());//openid :需填写 用户openid
        parameters.put("check_name", "NO_CHECK"); //校验用户姓名选项
        parameters.put("amount", payDto.getAmount());// 支付金额单位：分
        parameters.put("desc", payDto.getDesc());//企业付款备注
        String sign = createSign("UTF-8", parameters);
        parameters.put("sign", sign);
        String requestXML = getRequestXml(parameters);
        String result = httpsRequestByCert(TRANSFERS_URL, requestXML);
        return result;
    }

    /**
     * 生成带证书的请求
     */
    private static String httpsRequestByCert(String url, String data) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try {
            //读取指定目录下的证书信息
            InputStream certStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(payDto.getMchId() + ".p12");
            certData = IOUtils.toByteArray(certStream);
            certStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ByteArrayInputStream is = new ByteArrayInputStream(certData);
        try {
            keyStore.load(is, payDto.getMchId().toCharArray());
        } finally {
            is.close();
        }
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, payDto.getMchId().toCharArray())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
        try {
            HttpPost httpost = new HttpPost(url); // 设置响应头信息
            httpost.addHeader("Connection", "keep-alive");
            httpost.addHeader("Accept", "*/*");
            httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpost.addHeader("Host", "api.mch.weixin.qq.com");
            httpost.addHeader("X-Requested-With", "XMLHttpRequest");
            httpost.addHeader("Cache-Control", "max-age=0");
            httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
            httpost.setEntity(new StringEntity(data, "UTF-8"));
            CloseableHttpResponse response = httpclient.execute(httpost);
            try {
                HttpEntity entity = response.getEntity();
                String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
                EntityUtils.consume(entity);
                return jsonStr;
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }


    public static String httpsRequest(String requestUrl, String requestMethod,
                                      String outputStr) {
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = {new MyX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(requestUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(ssf);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("content-type",
                    "application/x-www-form-urlencoded");
            // 当outputStr不为null时向输出流写数据
            if (null != outputStr) {
                OutputStream outputStream = conn.getOutputStream();
                // 注意编码格式
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            // 从输入流读取返回内容
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            // 释放资源
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            conn.disconnect();
            return buffer.toString();
        } catch (ConnectException ce) {
            System.out.println("连接超时");
        } catch (Exception e) {
            System.out.println("请求异常");
        }
        return null;
    }

    /**
     * @Author Yu.Xing
     * @Description 创建签名
     **/
    public static String createSign(String characterEncoding, SortedMap<Object, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + payDto.getMchKey());
        String sign = MD5Encode(sb.toString(), characterEncoding).toUpperCase();
        return sign;
    }

    /**
     * @Author Yu.Xing
     * @Description MD5签名字符串
     **/
    public static String MD5Encode(String origin, String charsetname) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetname == null || "".equals(charsetname))
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            else
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
        } catch (Exception exception) {
        }
        return resultString;
    }

    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String getNonceStr() {
        return UUID.randomUUID().toString()
                .replaceAll("-", "")
                .substring(0, 32);
    }

    /**
     * @Author Yu.Xing
     * @Description 将请求参数组装成XML
     **/
    public static String getRequestXml(SortedMap<Object, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k) || "sign".equalsIgnoreCase(k)) {
                sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
            } else {
                sb.append("<" + k + ">" + v + "</" + k + ">");
            }
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * @Author Yu.Xing
     * @Description 解析响应回来的xml
     **/
    public static Map doXMLParse(String strxml) throws Exception {
        strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");

        if (StringUtils.isEmpty(strxml)) {
            return null;
        }

        Map m = new HashMap();

        InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(in);
        Element root = doc.getRootElement();
        List list = root.getChildren();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Element e = (Element) it.next();
            String k = e.getName();
            String v = "";
            List children = e.getChildren();
            if (children.isEmpty()) {
                v = e.getTextNormalize();
            } else {
                v = getChildrenText(children);
            }

            m.put(k, v);
        }
        // 关闭流
        in.close();
        return m;
    }

    /**
     * @Author Yu.Xing
     * @Description 获取子节点的xml
     **/
    public static String getChildrenText(List children) {
        StringBuffer sb = new StringBuffer();
        if (!children.isEmpty()) {
            Iterator it = children.iterator();
            while (it.hasNext()) {
                Element e = (Element) it.next();
                String name = e.getName();
                String value = e.getTextNormalize();
                List list = e.getChildren();
                sb.append("<" + name + ">");
                if (!list.isEmpty()) {
                    sb.append(getChildrenText(list));
                }
                sb.append(value);
                sb.append("</" + name + ">");
            }
        }
        return sb.toString();
    }

    public static Map<String, Object> notifyUrl(HttpServletRequest httpRequest) {
        try {
            return XmlUtil.parseXML(httpRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
