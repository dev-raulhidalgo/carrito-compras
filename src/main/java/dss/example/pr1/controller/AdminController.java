package dss.example.pr1.controller;

import dss.example.pr1.model.Product;
import dss.example.pr1.service.DatabaseExportService;
import dss.example.pr1.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private DatabaseExportService exportService;

    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("product", new Product());
        return "admin";
    }

    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity<byte[]> exportSql() {
        byte[] data = exportService.exportDatabaseToSql();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.sql")
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }
}