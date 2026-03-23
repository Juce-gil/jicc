package cn.kmbeast.pojo.vo;

import cn.kmbeast.pojo.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Product view object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductVO extends Product {

    private String userName;

    private String userAvatar;

    private String categoryName;

    private Integer likeNumber;

    private Integer saveNumber;

    private Integer viewNumber;
}
