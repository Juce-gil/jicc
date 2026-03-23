package cn.kmbeast.pojo.dto.query.extend;

import cn.kmbeast.pojo.dto.query.base.QueryDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Product query DTO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductQueryDto extends QueryDto {

    private String name;

    private Integer categoryId;

    private Integer userId;

    private Boolean isBargain;

    private String status;

    private String auditStatus;
}
