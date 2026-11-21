package crudempleados.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IndexPageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sirveLaPaginaConLosControlesCRUDYSeleccion() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Administrar Empleados")))
                .andExpect(content().string(containsString("btnNuevo")))
                .andExpect(content().string(containsString("btnEliminarSeleccionados")))
                .andExpect(content().string(containsString("seleccionarTodos")))
                .andExpect(content().string(containsString("<th>ID</th>")))
                .andExpect(content().string(containsString("href=\"/style.css\"")))
                .andExpect(content().string(containsString("src=\"/app.js\"")));
    }

    @Test
    void sirveLosRecursosEstaticosReferenciadosPorLaPagina() throws Exception {
        mockMvc.perform(get("/style.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".container")));

        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("const URL_API")));
    }
}
