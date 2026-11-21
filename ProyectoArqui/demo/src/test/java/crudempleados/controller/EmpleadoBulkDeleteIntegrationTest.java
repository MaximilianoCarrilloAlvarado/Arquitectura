package crudempleados.controller;

import crudempleados.repository.EmpleadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmpleadoBulkDeleteIntegrationTest {

    private static final String API = "/api/empleados";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @BeforeEach
    void limpiarEmpleados() {
        empleadoRepository.deleteAll();
    }

    @Test
    void eliminaSoloLosEmpleadosSeleccionados() throws Exception {
        int primerId = crearEmpleado("María");
        int segundoId = crearEmpleado("Carlos");
        int tercerId = crearEmpleado("Elena");

        mockMvc.perform(delete(API).queryParam("ids", primerId + "," + segundoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idEmpleado").value(tercerId))
                .andExpect(jsonPath("$[0].nombre").value("Elena"));
    }

    private int crearEmpleado(String nombre) throws Exception {
        MvcResult resultado = mockMvc.perform(post(API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"%s","edad":28,"salario":1200.00}
                                """.formatted(nombre)))
                .andExpect(status().isCreated())
                .andReturn();

        Matcher matcher = Pattern.compile("\"idEmpleado\"\\s*:\\s*(\\d+)")
                .matcher(resultado.getResponse().getContentAsString());
        if (!matcher.find()) {
            throw new AssertionError("La respuesta no contiene el identificador del empleado.");
        }
        return Integer.parseInt(matcher.group(1));
    }
}
