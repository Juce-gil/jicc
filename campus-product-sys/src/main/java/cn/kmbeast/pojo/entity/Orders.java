package cn.kmbeast.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Reservation order entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Orders {

    private Integer id;

    private String code;

    /**
     * Buyer message or extra note.
     */
    private String detail;

    /**
     * Buyer id.
     */
    private Integer userId;

    private Integer productId;

    private BigDecimal buyPrice;

    private Integer buyNumber;

    /**
     * 1=PENDING_CONFIRM, 2=RESERVED, 3=PARTIAL_CONFIRMED, 4=COMPLETED, 5=CANCELLED
     */
    private Integer tradeStatus;

    private Integer refundStatus;

    /**
     * @deprecated Kept only for legacy compatibility. Use sellerFinishTime instead.
     */
    @Deprecated
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    /**
     * @deprecated Kept only for legacy compatibility. Use sellerConfirmTime instead.
     */
    @Deprecated
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeTime;

    /**
     * Legacy field reused as seller finish confirmation flag.
     */
    private Boolean isRefundConfirm;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Integer addressId;

    /**
     * Legacy field reused as buyer finish confirmation flag.
     */
    private Boolean isConfirm;

    /**
     * @deprecated Kept only for legacy compatibility. Use buyerConfirmTime instead.
     */
    @Deprecated
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime isConfirmTime;

    private Boolean isDeliver;

    private Integer deliverAdrId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliverTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;

    private String appointmentAddress;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sellerConfirmTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime buyerConfirmTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sellerFinishTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    private String cancelReason;

    public BigDecimal calculateTotalPrice() {
        if (buyPrice == null || buyNumber == null) {
            return BigDecimal.ZERO;
        }
        return buyPrice.multiply(BigDecimal.valueOf(buyNumber))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
