package cn.kmbeast.controller;

import cn.kmbeast.aop.Log;
import cn.kmbeast.aop.Pager;
import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.ProductQueryDto;
import cn.kmbeast.pojo.dto.update.OrdersDTO;
import cn.kmbeast.pojo.entity.Product;
import cn.kmbeast.pojo.vo.ChartVO;
import cn.kmbeast.pojo.vo.OrdersDeliverDto;
import cn.kmbeast.pojo.vo.ProductVO;
import cn.kmbeast.service.ProductService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Product controller.
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Resource
    private ProductService productService;

    /**
     * Buyer creates a reservation request.
     */
    @Log(detail = "reservation request")
    @PostMapping(value = "/buyProduct")
    @ResponseBody
    public Result<String> buyProduct(@RequestBody OrdersDTO ordersDTO) {
        return productService.buyProduct(ordersDTO);
    }

    @Log(detail = "publish product")
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody Product product) {
        return productService.save(product);
    }

    @Log(detail = "update product")
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody Product product) {
        return productService.update(product);
    }

    @PostMapping(value = "/batchDelete")
    @ResponseBody
    public Result<String> batchDelete(@RequestBody List<Integer> ids) {
        return productService.batchDelete(ids);
    }

    @Pager
    @PostMapping(value = "/query")
    @ResponseBody
    public Result<List<ProductVO>> query(@RequestBody ProductQueryDto productQueryDto) {
        return productService.query(productQueryDto);
    }

    /**
     * Seller confirms the reservation request.
     */
    @Log(detail = "seller confirm reservation")
    @PostMapping(value = "/placeAnOrder/{ordersId}")
    @ResponseBody
    public Result<String> placeAnOrder(@PathVariable Integer ordersId) {
        return productService.placeAnOrder(ordersId);
    }

    /**
     * Buyer or seller cancels the reservation.
     */
    @Log(detail = "cancel reservation")
    @PostMapping(value = "/refund/{ordersId}")
    @ResponseBody
    public Result<String> refund(@PathVariable Integer ordersId) {
        return productService.refund(ordersId);
    }

    /**
     * Buyer confirms offline trade completion.
     */
    @Log(detail = "buyer confirm trade completion")
    @PostMapping(value = "/getGoods/{ordersId}")
    @ResponseBody
    public Result<String> getGoods(@PathVariable Integer ordersId) {
        return productService.getGoods(ordersId);
    }

    /**
     * Legacy body-based alias of seller completion confirmation.
     */
    @Log(detail = "seller confirm trade completion legacy")
    @PostMapping(value = "/deliverGoods")
    @ResponseBody
    public Result<String> deliverGoods(@RequestBody OrdersDeliverDto ordersDeliverDto) {
        return productService.deliverGoods(ordersDeliverDto);
    }

    /**
     * Seller confirms offline trade completion.
     */
    @Log(detail = "seller confirm trade completion")
    @PostMapping(value = "/confirmTradeBySeller/{ordersId}")
    @ResponseBody
    public Result<String> confirmTradeBySeller(@PathVariable Integer ordersId) {
        return productService.confirmTradeBySeller(ordersId);
    }

    @PostMapping(value = "/queryProductInfo")
    @ResponseBody
    public Result<List<ChartVO>> queryProductInfo(@RequestBody ProductQueryDto productQueryDto) {
        productQueryDto.setUserId(LocalThreadHolder.getUserId());
        return productService.queryProductInfo(productQueryDto);
    }

    @PostMapping(value = "/queryUser")
    @ResponseBody
    public Result<List<ProductVO>> queryUser(@RequestBody ProductQueryDto productQueryDto) {
        productQueryDto.setUserId(LocalThreadHolder.getUserId());
        return productService.query(productQueryDto);
    }

    @GetMapping(value = "/queryProductList/{id}")
    @ResponseBody
    public Result<List<ProductVO>> queryProductList(@PathVariable Integer id) {
        return productService.queryProductList(id);
    }
}

