package cn.kmbeast.pojo.vo;

import java.util.Objects;

/**
 * 数据源VO（手写构造器/get/set，避免在未启用 Lombok 注解处理时 IDE 构建失败）
 */
public class ChartVO {

    /**
     * 描述项：可以是时间，也可以是具体的统计项
     */
    private String name;
    /**
     * 数据总数
     */
    private Integer count;

    public ChartVO() {
    }

    public ChartVO(String name, Integer count) {
        this.name = name;
        this.count = count;
    }

    public ChartVO(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChartVO chartVO = (ChartVO) o;
        return Objects.equals(name, chartVO.name) && Objects.equals(count, chartVO.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, count);
    }

    @Override
    public String toString() {
        return "ChartVO{" + "name='" + name + '\'' + ", count=" + count + '}';
    }
}
