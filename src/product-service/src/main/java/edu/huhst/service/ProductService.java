package edu.huhst.service;

import edu.huhst.domain.Product;
import edu.huhst.vo.ProductVO;
import org.springframework.data.domain.Page;

public interface ProductService {

    /**
     * Query product by ID
     * @param id product primary key
     * @return product entity, or null if not found
     */
    Product getProductById(Integer id);
    /**
     * Deduct inventory for a product
     * @param productId product ID
     * @param buyNum quantity to deduct (must be positive)
     * @return true if successful, false if product not found or insufficient stock
     */
    boolean increaseInventory(Integer productId, Integer buyNum);

    Product createProduct( ProductVO product);

    // 新增：分页查询产品列表
    Page<Product> getProductList(int page, int size);

    // 新增：删除产品
    boolean deleteProduct(Integer id);

    // 新增：更新产品信息（全量或部分更新）
    Product updateProduct(Integer id, ProductVO productVO);


}
