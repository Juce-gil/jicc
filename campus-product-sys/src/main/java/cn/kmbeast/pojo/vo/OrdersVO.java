package cn.kmbeast.pojo.vo;

import cn.kmbeast.pojo.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Orders view object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrdersVO extends Orders {

    /**
     * Buyer name.
     */
    private String userName;

    private String userAvatar;

    private String productTitle;

    private String productCover;

    private Integer sellerId;

    private String sellerName;

    private String sellerAvatar;

    private String concatPerson;

    private String getAdr;

    private String concatPhone;

    private String adrName;

    private String adrPhone;

    private String adr;
}
