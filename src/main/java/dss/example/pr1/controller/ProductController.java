package dss.example.pr1.controller;

import dss.example.pr1.model.Product;
import dss.example.pr1.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private static final List<String> EXTENSIONES_PERMITIDAS =
            List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @Autowired
    private ProductService productService;

    @GetMapping
    public String listProducts(@RequestParam(value = "q", required = false) String q,
                               @RequestParam(value = "min", required = false) Double min,
                               @RequestParam(value = "max", required = false) Double max,
                               Model model) {
        model.addAttribute("products", productService.searchProducts(q, min, max));
        model.addAttribute("q", q);
        model.addAttribute("min", min);
        model.addAttribute("max", max);
        return "productos";
    }

    @GetMapping("/add")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "formulario-producto";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/admin";
        }
        model.addAttribute("product", product);
        return "formulario-producto";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile)
            throws IOException, NoSuchAlgorithmException {

        if (imageFile != null && !imageFile.isEmpty()) {
            String original = imageFile.getOriginalFilename();
            String extension = "";
            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf('.')).toLowerCase();
            }
            // Solo se aceptan imágenes; cualquier otro archivo se ignora
            if (EXTENSIONES_PERMITIDAS.contains(extension)) {
                byte[] contenido = imageFile.getBytes();
                // El nombre sale del contenido: la misma imagen siempre da el mismo nombre
                String huella = HexFormat.of()
                        .formatHex(MessageDigest.getInstance("SHA-256").digest(contenido));
                String nombreArchivo = huella + extension;

                Path carpeta = Paths.get("uploads");
                Files.createDirectories(carpeta);
                Path destino = carpeta.resolve(nombreArchivo);
                if (!Files.exists(destino)) {
                    Files.write(destino, contenido);
                }
                product.setImageUrl("/uploads/" + nombreArchivo);
            }
        }

        productService.saveProduct(product);
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin";
    }
}