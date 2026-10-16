package dss.example.pr1.service;

import dss.example.pr1.model.Product;
import dss.example.pr1.repository.ProductRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public List<Product> searchProducts(String name, Double minPrice, Double maxPrice) {
        String filtro = (name == null) ? "" : name.trim().toLowerCase();
        return productRepo.findAll().stream()
                .filter(p -> filtro.isEmpty()
                        || (p.getName() != null && p.getName().toLowerCase().contains(filtro)))
                .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .toList();
    }

    public Product getProductById(Long id) {
        return productRepo.findById(id).orElse(null);
    }

    public Product saveProduct(Product product) {
        return productRepo.save(product);
    }

    public void deleteProduct(Long id) {
        productRepo.deleteById(id);
    }
}