package com.personal.course.service;

import com.personal.course.entity.DTO.PaymentTradeQueryResponse;
import com.personal.course.entity.Status;
import com.personal.course.entity.TradePayResponse;

public interface PaymentService {
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
    TradePayResponse tradePayInWebPage(String tradeNo, int price, String subject, String returnUrl);

    PaymentTradeQueryResponse getTradeStatusFromPayTradeNo(String payTradeNo, String tradeNo, Status tradeStatus);

    void closeOrder(String payTradeNo, String tradeNo);
}
