package dss.example.pr1.service;

import dss.example.pr1.model.Product;
import dss.example.pr1.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class DatabaseExportService {

    @Autowired
    private ProductRepo productRepo;

    public byte[] exportDatabaseToSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("-- Exportación de la tabla product\n");
        sql.append("CREATE TABLE IF NOT EXISTS product (")
           .append("id BIGINT AUTO_INCREMENT PRIMARY KEY, ")
           .append("name VARCHAR(255), price DOUBLE, image_url VARCHAR(1000));\n\n");

        for (Product p : productRepo.findAll()) {
            String name = p.getName() == null ? "" : p.getName().replace("'", "''");
            String image = p.getImageUrl() == null ? "" : p.getImageUrl().replace("'", "''");
            sql.append("INSERT INTO product (id, name, price, image_url) VALUES (")
               .append(p.getId()).append(", '")
               .append(name).append("', ")
               .append(p.getPrice()).append(", '")
               .append(image).append("');\n");
        }
        return sql.toString().getBytes(StandardCharsets.UTF_8);
    }
}