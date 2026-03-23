package cn.kmbeast.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    private Integer id;

    private String name;

    private String detail;

    private String coverList;

    private Integer oldLevel;

    private Integer categoryId;

    private Integer userId;

    private Integer inventory;

    private BigDecimal price;

    private Boolean isBargain;

    /**
     * ON_SALE / RESERVED / SOLD / OFFLINE
     */
    private String status;

    /**
     * APPROVED / PENDING / REJECTED
     */
    private String auditStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
