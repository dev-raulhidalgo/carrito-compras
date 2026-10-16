package dss.example.pr1;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndCartTests {

    @Autowired
    private MockMvc mvc;

    // ---------- Usuarios del enunciado ----------

    @Test
    void adminAdminPuedeIniciarSesion() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("admin"))
           .andExpect(authenticated().withRoles("ADMIN"));
    }

    @Test
    void userUserPuedeIniciarSesion() throws Exception {
        mvc.perform(formLogin("/login").user("user").password("user"))
           .andExpect(authenticated().withRoles("USER"));
    }

    @Test
    void credencialesIncorrectasSeRechazan() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("mala"))
           .andExpect(unauthenticated());
    }

    // ---------- Acceso público ----------

    @Test
    void catalogoEsPublico() throws Exception {
        mvc.perform(get("/products")).andExpect(status().isOk());
    }

    @Test
    void filtroPorPrecioFunciona() throws Exception {
        // data.sql: kiwi 1.63, crema 5.78 → solo kiwi está en [1, 2]
        mvc.perform(get("/products").param("min", "1").param("max", "2"))
           .andExpect(status().isOk())
           .andExpect(model().attribute("products", hasSize(1)));
    }

    // ---------- Gestión del catálogo: solo ADMIN ----------

    @Test
    void anonimoNoPuedeVerFormularioDeAlta() throws Exception {
        mvc.perform(get("/products/add"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void userRecibe403EnFormularioDeAlta() throws Exception {
        mvc.perform(get("/products/add").with(user("user").roles("USER")))
           .andExpect(status().isForbidden());
    }

    @Test
    void userRecibe403EnEdicion() throws Exception {
        mvc.perform(get("/products/edit/1").with(user("user").roles("USER")))
           .andExpect(status().isForbidden());
    }

    @Test
    void userNoPuedeGuardarProductosAunqueTengaCsrf() throws Exception {
        mvc.perform(post("/products/save").with(user("user").roles("USER")).with(csrf())
                .param("name", "Intruso").param("price", "1"))
           .andExpect(status().isForbidden());
    }

    @Test
    void userNoPuedeEliminarProductosAunqueTengaCsrf() throws Exception {
        mvc.perform(post("/products/delete/1").with(user("user").roles("USER")).with(csrf()))
           .andExpect(status().isForbidden());
    }

    @Test
    void adminAccedeAlFormularioDeAlta() throws Exception {
        mvc.perform(get("/products/add").with(user("admin").roles("ADMIN")))
           .andExpect(status().isOk());
    }

    @Test
    void adminGuardaProductoConCsrf() throws Exception {
        mvc.perform(post("/products/save").with(user("admin").roles("ADMIN")).with(csrf())
                .param("name", "Gorra").param("price", "9.99"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin"));
    }

    // ---------- CSRF ----------

    @Test
    void guardarSinCsrfSeRechazaIncluidoAdmin() throws Exception {
        mvc.perform(post("/products/save").with(user("admin").roles("ADMIN"))
                .param("name", "Sin token").param("price", "1"))
           .andExpect(status().isForbidden());
    }

    @Test
    void eliminarSinCsrfSeRechazaIncluidoAdmin() throws Exception {
        mvc.perform(post("/products/delete/1").with(user("admin").roles("ADMIN")))
           .andExpect(status().isForbidden());
    }

    // ---------- Zona /admin y exportación ----------

    @Test
    void userRecibe403EnPanelAdmin() throws Exception {
        mvc.perform(get("/admin").with(user("user").roles("USER")))
           .andExpect(status().isForbidden());
    }

    @Test
    void userRecibe403EnExportacion() throws Exception {
        mvc.perform(get("/admin/export").with(user("user").roles("USER")))
           .andExpect(status().isForbidden());
    }

    @Test
    void adminDescargaProductsSqlConInserts() throws Exception {
        mvc.perform(get("/admin/export").with(user("admin").roles("ADMIN")))
           .andExpect(status().isOk())
           .andExpect(header().string("Content-Disposition", containsString("products.sql")))
           .andExpect(content().string(containsString("INSERT INTO product")));
    }

    // ---------- Carrito independiente por sesión ----------

    @Test
    void carritoEsPublicoYSeAnadeConCsrf() throws Exception {
        mvc.perform(post("/cart/add/1").with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void anadirAlCarritoSinCsrfSeRechaza() throws Exception {
        mvc.perform(post("/cart/add/1"))
           .andExpect(status().isForbidden());
    }

    @Test
    void cadaSesionTieneSuPropioCarrito() throws Exception {
        MockHttpSession sesionA = new MockHttpSession();
        MockHttpSession sesionB = new MockHttpSession();

        mvc.perform(post("/cart/add/1").session(sesionA).with(csrf()))
           .andExpect(status().is3xxRedirection());

        mvc.perform(get("/cart").session(sesionA))
           .andExpect(model().attribute("items", hasSize(1)));

        mvc.perform(get("/cart").session(sesionB))
           .andExpect(model().attribute("items", empty()));
    }
}
