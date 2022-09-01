package com.example.wxpayexample.utils;

import com.example.wxpayexample.bean.PayDto;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.UUID;

@Slf4j
public class WxPayV3Util {
    //支付
    private static String JS_API_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
    //退款
    private static String REFUND_API_URL = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";
    //通过商户订单号查询
    private static String QUERY_API_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/";
    //通过微信支付订单号查询
    private static String QUERY_TRANS_API_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/id/";
    //提现接口
    private static String TRANSFER_API_URL = "https://api.mch.weixin.qq.com/v3/transfer/batches";

    private static CertificatesManager certificatesManager;

    private static PayDto payDto;

    public WxPayV3Util(PayDto payDto) {
        this.payDto = payDto;
    }

    static {
        // 获取证书管理器实例
        certificatesManager = CertificatesManager.getInstance();
    }

    /**
     * @Author Yu.Xing
     * @Description 获取证书管理器
     **/
    public static CertificatesManager getCertificatesManager() {
        return certificatesManager;
    }

    /**
     * @Author Yu.Xing
     * @Description 微信V3版本下单接口
     **/
    public static String payOrder() {
        try {
            //获取证书私钥
            WechatPayHttpClientBuilder builder = getWechatPayHttpClientBuilder();

            // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
            CloseableHttpClient httpClient = builder.build();
            HttpPost httpPost = new HttpPost(JS_API_URL);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-type", "application/json; charset=utf-8");

//            //设置签名(使用微信SDK自动处理签名)
//            getToken("POST",new URL(JS_API_URL),)
//            httpPost.addHeader("Authorization", "WECHATPAY2-SHA256-RSA2048 " + );

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("mchid", payDto.getMchId())
                    .put("appid", payDto.getAppId())
                    .put("description", payDto.getBody())
                    .put("notify_url", payDto.getNotify_url() + "/" + payDto.getAppId())//回调地址
                    .put("out_trade_no", payDto.getOrderId());
            rootNode.putObject("amount")
                    .put("total", payDto.getTotal_fee());//支付金额单位是：分
            rootNode.putObject("payer")
                    .put("openid", payDto.getOpenId());//支付者的openId
            objectMapper.writeValue(bos, rootNode);

            log.info("微信支付V3版本请求参数:{}", bos);
            httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String bodyAsString = EntityUtils.toString(response.getEntity());
            log.info("微信支付V3响应信息：{}", bodyAsString);
            return bodyAsString;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @Author Yu.Xing
     * @Description 查询订单信息
     **/
    public static String queryOrder() {

        try {
            WechatPayHttpClientBuilder builder = getWechatPayHttpClientBuilder();
            // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
            CloseableHttpClient httpClient = builder.build();
            HttpGet httpGet = new HttpGet(QUERY_API_URL + payDto.getOut_trade_no() + "?mchid=" + payDto.getMchId());
            httpGet.addHeader("Accept", "application/json");
//            httpGet.addHeader("Content-type", "application/json; charset=utf-8");

            CloseableHttpResponse response = httpClient.execute(httpGet);
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            log.info("微信查询订单状态码为{},响应信息：{}", statusCode, bodyAsString);
            return bodyAsString;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * @Author Yu.Xing
     * @Description 微信提现V3
     **/
    public static String transfer() {
        try {
            WechatPayHttpClientBuilder builder = getWechatPayHttpClientBuilder();
            // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
            CloseableHttpClient httpClient = builder.build();
            HttpPost httpPost = new HttpPost(TRANSFER_API_URL);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-type", "application/json; charset=utf-8");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("out_batch_no", payDto.getPartner_trade_no())
                    .put("appid", payDto.getAppId())
                    .put("batch_name", "测试提现")
                    .put("batch_remark", payDto.getDesc())
                    .put("total_amount", payDto.getAmount())
                    .put("total_num", 1);//默认只能提现1笔
            ArrayNode transfer_detail_list = rootNode.putArray("transfer_detail_list");
            ObjectNode jsonNodes = transfer_detail_list.addObject();
            jsonNodes.put("out_detail_no", payDto.getPartner_trade_no())
                    .put("transfer_amount", payDto.getAmount())
                    .put("transfer_remark", payDto.getDesc())
                    .put("openid", payDto.getOpenId());
            objectMapper.writeValue(bos, rootNode);
            log.info("提现请求参数：{}", bos);
            httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            log.info("微信提现状态码为{},响应信息：{}", statusCode, bodyAsString);
            return bodyAsString;
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonGenerationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * @Author Yu.Xing
     * @Description 申请退款接口
     **/
    public static String refund() {
        try {
            //获取证书私钥
            WechatPayHttpClientBuilder builder = getWechatPayHttpClientBuilder();

            // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
            CloseableHttpClient httpClient = builder.build();
            HttpPost httpPost = new HttpPost(REFUND_API_URL);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-type", "application/json; charset=utf-8");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("transaction_id", payDto.getOut_trade_no())
                    .put("out_refund_no", payDto.getOut_refund_no())
                    .put("reason", payDto.getRefund_reason())
                    .put("notify_url", payDto.getRefund_notify_url() + "/" + payDto.getAppId());
            rootNode.putObject("amount")
                    .put("refund", payDto.getRefund_amount())//金额单位是：分
                    .put("total", payDto.getTotal_fee())//原订单金额
                    .put("currency", "CNY");//退款币种
            objectMapper.writeValue(bos, rootNode);

            log.info("微信支付V3版本退款请求参数:{}", bos);
            httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            log.info("微信退款状态码为{},响应信息：{}", statusCode, bodyAsString);

            return bodyAsString;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (JsonGenerationException e) {
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //构建HttpClient
    public static WechatPayHttpClientBuilder getWechatPayHttpClientBuilder() throws NotFoundException {
        PrivateKey privateKey = loadPrivateKey(payDto.getMchId());
        //获取验签器
        Verifier verifier;
        try {
            verifier = getVerifier(payDto.getMchId());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (HttpCodeException e) {
            throw new RuntimeException(e);
        }

        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(payDto.getMchId(), payDto.getMerchantSerialNumber(), privateKey)
                .withValidator(new WechatPay2Validator(verifier));
        return builder;
    }

    //加载证书中的私钥
    public static PrivateKey loadPrivateKey(String merchantId) {
        PrivateKey merchantPrivateKey;
        //读取resources目录下证书中的私钥
        merchantPrivateKey = PemUtil.loadPrivateKey(Thread.currentThread().getContextClassLoader().getResourceAsStream(""+merchantId + "_key.pem"));
        return merchantPrivateKey;
    }

    // 获取平台证书（测试方法，目前使用微信SDK中的证书管理器）
    public static void getCertificate() throws URISyntaxException, IOException, NotFoundException {
        URIBuilder uriBuilder = new URIBuilder("https://api.mch.weixin.qq.com/v3/certificates");
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.addHeader("Accept", "application/json");
        WechatPayHttpClientBuilder wechatPayHttpClientBuilder = getWechatPayHttpClientBuilder();
        CloseableHttpClient build = wechatPayHttpClientBuilder.build();
        CloseableHttpResponse response = build.execute(httpGet);
        String bodyAsString = EntityUtils.toString(response.getEntity());
        System.out.println(bodyAsString);
    }

    /**
     * @Author Yu.Xing
     * @Description 获取验签器
     **/
    public static Verifier getVerifier(String merchantId) throws
            NotFoundException, GeneralSecurityException, IOException, HttpCodeException {
        PrivateKey privateKey = loadPrivateKey(merchantId);
        // 从证书管理器中获取verifier
        Verifier verifier;
        try {
            verifier = certificatesManager.getVerifier(merchantId);
            BigInteger serialNumber = verifier.getValidCertificate().getSerialNumber();
            String serialnumber = serialNumber.toString(16);
            log.info("获取商户:{}微信支付平台证书证书序列号:{}", merchantId, serialnumber);
            return verifier;
        } catch (NotFoundException e) {
            log.error("获取验签器失败");
            //如果获取失败的话，那么重新将当前商户的信息添加到证书管理器中，重新获取验签器
            // 向证书管理器增加需要自动更新平台证书的商户信息
            certificatesManager.putMerchant(merchantId, new WechatPay2Credentials(merchantId,
                    new PrivateKeySigner(payDto.getMerchantSerialNumber(), privateKey)), payDto.getApiV3Key().getBytes(StandardCharsets.UTF_8));
            verifier = certificatesManager.getVerifier(merchantId);
            return verifier;
        }
    }


/////////////////////////////////////////// 如果需要手动生成签名可使用以下方法///////////////////////////////////////////////////////

    /**
     * 生成签名
     *
     * @param message
     * @return
     * @throws Exception
     */
    public static String sign(byte[] message, String merchantId) throws Exception {
        Signature sign = Signature.getInstance("SHA256withRSA");
        //加载私钥
        PrivateKey privateKey = loadPrivateKey(merchantId);
        // 商户私钥
        sign.initSign(privateKey);
        sign.update(message);
        return Base64.getEncoder().encodeToString(sign.sign());
    }

    /**
     * 生成token 把请求地址URL和参数进行加密处理
     * 为什么要签名：对请求参数和请求地址进行加密
     */
    public static String getToken(String method, URL url, String body, PayDto payDto) throws Exception {
        //下面有这个方法，生成随机数
        String nonceStr = getNonceStr();
        long timestamp = System.currentTimeMillis() / 1000;
        //下面有这个buildMessage 构成签名串的方法
        String message = buildMessage(method, url, timestamp, nonceStr, body);
        //下面有这个sign计算签名的方法
        String signature = sign(message.getBytes("utf-8"), payDto.getMchId());
        return "mchid=\"" + payDto.getMchId() + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + payDto.getMerchantSerialNumber() + "\","
                + "signature=\"" + signature + "\"";
    }

    public static String getNonceStr() {
        return UUID.randomUUID().toString()
                .replaceAll("-", "")
                .substring(0, 32);
    }

    public static String buildMessage(String method, URL url, long timestamp, String nonceStr, String body) {
//        第二步，获取请求的绝对URL，并去除域名部分得到参与签名的URL。如果请求中有查询参数，URL末尾应附加有'?'和对应的查询字符串。
        String canonicalUrl = url.getPath();
        if (url.getQuery() != null) {
            canonicalUrl += "?" + url.getQuery();
        }
        return method + "\n"
                + canonicalUrl + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + body + "\n";
    }

    /**
     * @Author Yu.Xing
     * @Description 调起微信支付签名
     **/
    public static String getAppletToken(String appid, String prepay_id, String nonceStr, Long timestamp) throws Exception {

        //从下往上依次生成
        String message = buildMessage(appid, timestamp, nonceStr, prepay_id);
        //签名
        String signature = sign(message.getBytes("utf-8"), payDto.getMchId());
        return signature;
    }

    /**
     * @Author Yu.Xing
     * @Description 小程序调起支付构造签名串
     **/
    public static String buildMessage(String appid, long timestamp, String nonceStr, String prepay_id) {
        return appid + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + "prepay_id=" + prepay_id + "\n";
    }

    /**
     * 读取请求数据流
     *
     * @param request
     * @return
     */
    public static String getRequestBody(HttpServletRequest request) {

        StringBuffer sb = new StringBuffer();

        try (ServletInputStream inputStream = request.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ) {
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            log.error("读取数据流异常:{}", e);
        }

        return sb.toString();

    }


    public static String readData(HttpServletRequest request) {
        BufferedReader br = null;
        try {
            StringBuilder result = new StringBuilder();
            br = request.getReader();
            for (String line; (line = br.readLine()) != null; ) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
