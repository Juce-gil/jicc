package cn.kmbeast.pojo.dto.query.extend;

import cn.kmbeast.pojo.dto.query.base.QueryDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Orders query DTO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrdersQueryDto extends QueryDto {

    private String code;

    private String detail;

    private Integer userId;

    private Integer productId;

    private Integer tradeStatus;

    private Integer refundStatus;

    private List<Integer> productIds;
}
