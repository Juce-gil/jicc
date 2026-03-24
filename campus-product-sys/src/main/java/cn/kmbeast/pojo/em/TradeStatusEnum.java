package cn.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Reservation order status.
 */
@Getter
@AllArgsConstructor
public enum TradeStatusEnum {

    PENDING_CONFIRM(1, "PENDING_CONFIRM", "pending seller confirmation"),
    RESERVED(2, "RESERVED", "reserved for meetup"),
    PARTIAL_CONFIRMED(3, "PARTIAL_CONFIRMED", "one side confirmed completion"),
    COMPLETED(4, "COMPLETED", "trade completed"),
    CANCELLED(5, "CANCELLED", "reservation cancelled");

    private final Integer code;
    private final String name;
    private final String description;

    public static TradeStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TradeStatusEnum value : TradeStatusEnum.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
