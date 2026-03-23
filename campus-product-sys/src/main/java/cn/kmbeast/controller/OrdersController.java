package cn.kmbeast.controller;

import cn.kmbeast.aop.Pager;
import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.OrdersQueryDto;
import cn.kmbeast.pojo.entity.Orders;
import cn.kmbeast.pojo.vo.OrdersVO;
import cn.kmbeast.service.OrdersService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Orders controller.
 */
@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Resource
    private OrdersService ordersService;

    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody Orders orders) {
        return ordersService.save(orders);
    }

    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody Orders orders) {
        return ordersService.update(orders);
    }

    @PostMapping(value = "/batchDelete")
    @ResponseBody
    public Result<String> batchDelete(@RequestBody List<Integer> ids) {
        return ordersService.batchDelete(ids);
    }

    @PostMapping(value = "/queryUser")
    @ResponseBody
    public Result<List<OrdersVO>> queryUser(@RequestBody OrdersQueryDto ordersQueryDto) {
        ordersQueryDto.setUserId(LocalThreadHolder.getUserId());
        return ordersService.query(ordersQueryDto);
    }

    @PostMapping(value = "/queryOrdersList")
    @ResponseBody
    public Result<List<OrdersVO>> queryOrdersList(@RequestBody OrdersQueryDto ordersQueryDto) {
        return ordersService.queryOrdersList(ordersQueryDto);
    }

    /**
     * Legacy admin/seller close-order alias.
     */
    @PostMapping(value = "/returnMoney/{ordersId}")
    @ResponseBody
    public Result<String> returnMoney(@PathVariable Integer ordersId) {
        return ordersService.returnMoney(ordersId);
    }

    @Pager
    @PostMapping(value = "/query")
    @ResponseBody
    public Result<List<OrdersVO>> query(@RequestBody OrdersQueryDto ordersQueryDto) {
        return ordersService.query(ordersQueryDto);
    }
}
