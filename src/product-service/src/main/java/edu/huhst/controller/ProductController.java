package edu.huhst.controller;

import edu.huhst.domain.Product;
import edu.huhst.dto.Result;
import edu.huhst.service.ProductService;
import edu.huhst.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    /**
     * Query product by ID
     * GET /api/product/{id}
     */

    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Integer id){
        Product product = productService.getProductById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * Deduct inventory
     * PUT /api/product/updateInventory/{productId}/{buyNum}
     */


    @PutMapping("/updateInventory/{productId}/{buyNum}")
    public Result<Void>updateInventory(@PathVariable Integer productId,@PathVariable Integer buyNum){
        // Validate buyNum
        if (buyNum == null || buyNum <= 0) {
            return Result.error("购买数量必须大于0");
        }

        boolean success = productService.increaseInventory(productId, buyNum);
        if (!success) {
            return Result.error("库存不足，更新失败");
        }
        return Result.success(null);

    }

    @PostMapping("/add")
    public Result<Product> addProduct(@RequestBody ProductVO product) {
        try {
            Product saved = productService.createProduct(product);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("添加商品失败: " + e.getMessage());
        }
    }
    /**
     * 获取产品列表（分页）
     * GET /api/product/list?page=1&size=12
     */
    @GetMapping("/list")
    public Result<Page<Product>> listProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {

        // 参数二次校验（防御性）
        if (page == null || page <= 0) page = 1;
        if (size == null || size <= 0) size = 12;

        Page<Product> productPage = productService.getProductList(page, size);
        return Result.success(productPage);
    }
    /**
     * 删除产品
     * DELETE /api/product/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Integer id) {
        boolean deleted = productService.deleteProduct(id);
        if (!deleted) {
            return Result.error("商品不存在，删除失败");
        }
        return Result.success(null);
    }

    /**
     * 修改产品信息（部分/全量更新）
     * PUT /api/product/{id}
     */
    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable Integer id, @RequestBody ProductVO productVO) {
        try {
            Product updated = productService.updateProduct(id, productVO);
            return Result.success(updated);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("修改商品失败: " + e.getMessage());
        }
    }
}
