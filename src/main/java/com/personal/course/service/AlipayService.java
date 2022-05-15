package com.personal.course.service;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.PayStatus;
import com.personal.course.entity.Status;
import com.personal.course.entity.TradePayResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class AlipayService implements PaymentService {
    @Value("${alipay.application.appId}")
    private String appId;
    @Value("${alipay.application.privateKey}")
    private String privateKey;
    @Value("${alipay.application.alipayPublicKey}")
    private String alipayPublicKey;
    @Value("${alipay.application.serverUrl}")
    private String serverUrl;

    private final AlipayClient alipayClient;

    public AlipayService() {
        this.alipayClient = new DefaultAlipayClient(serverUrl, appId, privateKey, "json", StandardCharsets.UTF_8.name(), alipayPublicKey, "RSA2");
    }

    /**
     * @param returnUrl 成功请求后重定向的 url
     * @param tradeNo   交易号
     * @param price     价格 单位 分
     * @param subject   商品信息
     * @return 表单格式，可嵌入页面
     * <form name="submit_form" method="post" action="https://openapi.alipay.com/gateway.do?charset=UTF-8&method=alipay.trade.page.pay&sign=k0w1DePFqNMQWyGBwOaEsZEJuaIEQufjoPLtwYBYgiX%2FRSkBFY38VuhrNumXpoPY9KgLKtm4nwWz4DEQpGXOOLaqRZg4nDOGOyCmwHmVSV5qWKDgWMiW%2BLC2f9Buil%2BEUdE8CFnWhM8uWBZLGUiCrAJA14hTjVt4BiEyiPrtrMZu0o6%2FXsBu%2Fi6y4xPR%2BvJ3KWU8gQe82dIQbowLYVBuebUMc79Iavr7XlhQEFf%2F7WQcWgdmo2pnF4tu0CieUS7Jb0FfCwV%2F8UyrqFXzmCzCdI2P5FlMIMJ4zQp%2BTBYsoTVK6tg12stpJQGa2u3%2BzZy1r0KNzxcGLHL%2BwWRTx%2FCU%2Fg%3D%3D&notify_url=http%3A%2F%2F114.55.81.185%2Fopendevtools%2Fnotify%2Fdo%2Fbf70dcb4-13c9-4458-a547-3a5a1e8ead04&version=1.0&app_id=2014100900013222&sign_type=RSA&timestamp=2021-02-02+14%3A11%3A40&alipay_sdk=alipay-sdk-java-dynamicVersionNo&format=json">
     * <input type="submit" value="提交" style="display:none" >
     * </form>
     * <script>document.forms[0].submit();</script>
     */
    @Override
    public TradePayResponse tradePayInWebPage(String tradeNo, int price, String subject, String returnUrl) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl("");
        request.setReturnUrl(returnUrl);
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", tradeNo);
        bizContent.put("total_amount", price / 10000); // price 单位 分
        bizContent.put("subject", subject);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        // 15 分钟付款时间
        String after15Minutes = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("UTC"))
                .format(Instant.now().plus(Duration.ofMillis(15)));

        bizContent.put("time_expire", after15Minutes);

        request.setBizContent(bizContent.toString());
        AlipayTradePagePayResponse response;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw HttpException.of(HttpStatus.GATEWAY_TIMEOUT, "请求支付宝出错，请稍后重试");
        }
        if (response.isSuccess()) {
            return TradePayResponse.of(response.getBody(), response.getTradeNo());
        }
        throw HttpException.of(HttpStatus.INTERNAL_SERVER_ERROR, response.getBody());
    }

    @Override
    public Status getTradeStatusFromPayTradeNo(String payTradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("trade_no", payTradeNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw HttpException.of(HttpStatus.GATEWAY_TIMEOUT, "请求支付宝出错，请稍后重试");
        }
        if (response.isSuccess()) {
            String tradeStatus = response.getTradeStatus();
            if (PayStatus.WAIT_BUYER_PAY.name().equals(tradeStatus)) {
                return Status.UNPAID;
            }
            if (PayStatus.TRADE_CLOSED.name().equals(tradeStatus)) {
                return Status.CLOSED;
            }
            return Status.PAID;
        }
        throw HttpException.of(HttpStatus.INTERNAL_SERVER_ERROR, response.getBody());
    }

    @Override
    public void closeOrder(String payTradeNo) {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("trade_no", payTradeNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeCloseResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw HttpException.of(HttpStatus.GATEWAY_TIMEOUT, "请求支付宝出错，请稍后重试");
        }
        if (!response.isSuccess()) {
            throw HttpException.of(HttpStatus.INTERNAL_SERVER_ERROR, response.getBody());
        }
    }
}