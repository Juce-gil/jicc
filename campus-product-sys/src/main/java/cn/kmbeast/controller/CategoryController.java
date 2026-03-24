package cn.kmbeast.controller;

import cn.kmbeast.aop.Pager;
import cn.kmbeast.aop.Protector;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.CategoryQueryDto;
import cn.kmbeast.pojo.entity.Category;
import cn.kmbeast.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Product category controller.
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @Protector(roleCode = Protector.ROLE_ADMIN)
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody Category category) {
        return categoryService.save(category);
    }

    @Protector(roleCode = Protector.ROLE_ADMIN)
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody Category category) {
        return categoryService.update(category);
    }

    @Protector(roleCode = Protector.ROLE_ADMIN)
    @PostMapping(value = "/batchDelete")
    @ResponseBody
    public Result<String> batchDelete(@RequestBody List<Integer> ids) {
        return categoryService.batchDelete(ids);
    }

    @Pager
    @PostMapping(value = "/query")
    @ResponseBody
    public Result<List<Category>> query(@RequestBody CategoryQueryDto categoryQueryDto) {
        return categoryService.query(categoryQueryDto);
    }
}
