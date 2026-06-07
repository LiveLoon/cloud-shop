package edu.huhst.service;

import edu.huhst.domain.Product;
import edu.huhst.repository.ProductRepository;
import edu.huhst.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;  // 改名以更符合 JPA 习惯

    @Override
    public Product getProductById(Integer id) {
        Optional<Product> optional = productRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    @Transactional
    public boolean increaseInventory(Integer productId, Integer buyNum) {
        Optional<Product> optional = productRepository.findById(productId);
        if (!optional.isPresent()) {
            return false;
        }

        Product product = optional.get();
        Integer currentInventory = product.getInventory();
        if (currentInventory == null || currentInventory < buyNum) {
            return false; // 库存不足
        }

        // 更新库存
        product.setInventory(currentInventory + buyNum);
        productRepository.save(product);  // JPA 的保存方法（更新或插入）
        return true;
    }

    @Override
    @Transactional
    public Product createProduct( ProductVO product) {
        if(product.getProductName() == null || product.getProductName().trim().isEmpty()){
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (product.getPrice() == null || product.getPrice() .compareTo(BigDecimal.ZERO) <=0) {
            throw new IllegalArgumentException("商品价格必须大于0");
        }

        if (product.getInventory() == null || product.getInventory() < 0) {
            throw new IllegalArgumentException("库存不能为负数");
        }
        // 保存并返回带 ID 的实体
        Product save = new Product();
        save.setInventory(product.getInventory());
        save.setProductName(product.getProductName());
        save.setPrice(product.getPrice());
        save.setImageUrl(product.getImageUrl());
        return productRepository.save(save);
    }

    @Override
    public Page<Product> getProductList(int page, int size) {
        // 参数校验：确保 page 和 size 合法
        if (page < 1) page = 1;
        if (size < 1) size = 12;
        // Spring Data JPA 分页从 0 开始，所以传入 page-1
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        return productRepository.findAll(pageRequest);
    }


    @Override
    @Transactional
    public boolean deleteProduct(Integer id) {
        if (!productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public Product updateProduct(Integer id, ProductVO productVO) {
        // 1. 检查产品是否存在
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在，ID: " + id));

        // 2. 校验传入的参数（可根据需求调整校验规则）
        if (productVO.getProductName() != null && productVO.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (productVO.getPrice() != null && productVO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("商品价格必须大于0");
        }
        if (productVO.getInventory() != null && productVO.getInventory() < 0) {
            throw new IllegalArgumentException("库存不能为负数");
        }

        // 3. 选择性更新（只更新非 null 字段）
        if (productVO.getProductName() != null) {
            existingProduct.setProductName(productVO.getProductName());
        }
        if (productVO.getPrice() != null) {
            existingProduct.setPrice(productVO.getPrice());
        }
        if (productVO.getInventory() != null) {
            existingProduct.setInventory(productVO.getInventory());
        }
        if (productVO.getImageUrl() != null) {
            existingProduct.setImageUrl(productVO.getImageUrl());
        }

        // 4. 保存更新后的实体
        return productRepository.save(existingProduct);
    }



}