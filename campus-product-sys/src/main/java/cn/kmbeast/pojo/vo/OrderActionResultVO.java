package cn.kmbeast.pojo.vo;

import cn.kmbeast.pojo.em.TradeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified payload for order action endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderActionResultVO {

    private Integer orderId;

    private String orderCode;

    private Integer beforeTradeStatus;

    private Integer afterTradeStatus;

    private String beforeTradeStatusName;

    private String afterTradeStatusName;

    private String action;

    private String message;

    public static OrderActionResultVO of(
            Integer orderId,
            String orderCode,
            Integer beforeStatus,
            Integer afterStatus,
            String action,
            String message
    ) {
        TradeStatusEnum before = TradeStatusEnum.fromCode(beforeStatus);
        TradeStatusEnum after = TradeStatusEnum.fromCode(afterStatus);
        return OrderActionResultVO.builder()
                .orderId(orderId)
                .orderCode(orderCode)
                .beforeTradeStatus(beforeStatus)
                .afterTradeStatus(afterStatus)
                .beforeTradeStatusName(before == null ? null : before.getName())
                .afterTradeStatusName(after == null ? null : after.getName())
                .action(action)
                .message(message)
                .build();
    }
}
