package com.legaoyi.storer.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.legaoyi.storer.util.ExchangeMessage;

/**
 * 网关中间件链路管理消息通知
 * 
 * @author 高胜波
 *
 */
@Component("gatewayLinkMessageHandler")
public class GatewayLinkMessageHandler extends MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(GatewayLinkMessageHandler.class);

    private Map<Long, Integer> mainLinkStateMap = new HashMap<Long, Integer>();

    @Autowired
    @Qualifier("urgentDownstreamMessageSendHandler")
    private UrgentDownstreamMessageSendHandler urgentDownstreamMessageSendHandler;

    @Override
    public void handle(ExchangeMessage exchangeMessage) throws Exception {
        if (exchangeMessage.getMessageId().equals(ExchangeMessage.MESSAGEID_GATEWAY_LINK_STATUS_MESSAGE)) {
            Map<?, ?> data = (Map<?, ?>) exchangeMessage.getMessage();
            logger.info("******链路管理消息,link state message,message={}", data);
            Long accessCode = Long.parseLong(String.valueOf(data.get("accessCode")));
            Integer linkType = Integer.parseInt(String.valueOf(data.get("linkType")));
            Integer result = Integer.parseInt(String.valueOf(data.get("result")));

            if (linkType == 1) {
                mainLinkStateMap.put(accessCode, result);
            } else {
                if (result == 0) {
                    Integer state = mainLinkStateMap.get(accessCode);
                    if (state != null && state == 1) {
                        // 主链路正常，从链路已断开
                        // 这里模拟重新连接从链路，平台可根据业务情况处理,todo
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("accessCode", accessCode);
                        map.put("action", 3);
                        //urgentDownstreamMessageSendHandler
                        //        .handle(new ExchangeMessage(ExchangeMessage.MESSAGEID_EXCHANGE_LINK_STATUS_MESSAGE, map, "", exchangeMessage.getGatewayId()));
                    }
                }
            }
        } else if (getSuccessor() != null) {
            getSuccessor().handle(exchangeMessage);
        }
    }
}
