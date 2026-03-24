package cn.kmbeast.controller;

import cn.kmbeast.aop.Protector;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.vo.ChartVO;
import cn.kmbeast.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * Dashboard controller.
 */
@RestController
@RequestMapping(value = "/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @Protector(roleCode = Protector.ROLE_ADMIN)
    @GetMapping(value = "/staticCount")
    @ResponseBody
    public Result<List<ChartVO>> staticCount() {
        return dashboardService.staticCount();
    }

    @Protector(roleCode = Protector.ROLE_ADMIN)
    @GetMapping(value = "/productShelvesInfo/{day}")
    @ResponseBody
    public Result<List<ChartVO>> productShelvesInfo(@PathVariable Integer day) {
        return dashboardService.productShelvesInfo(day);
    }
}
