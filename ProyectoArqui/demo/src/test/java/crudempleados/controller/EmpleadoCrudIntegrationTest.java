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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmpleadoCrudIntegrationTest {

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
    void creaConsultaActualizaYEliminaEmpleado() throws Exception {
        MvcResult resultadoCreacion = mockMvc.perform(post(API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana López","edad":30,"salario":1500.50}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ana López"))
                .andReturn();

        int id = extraerId(resultadoCreacion);

        mockMvc.perform(get(API + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.edad").value(30));

        mockMvc.perform(put(API + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana García","edad":31,"salario":1800.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana García"))
                .andExpect(jsonPath("$.salario").value(1800.00));

        mockMvc.perform(delete(API + "/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString("id " + id)));
    }

    private int extraerId(MvcResult resultado) throws Exception {
        Matcher matcher = Pattern.compile("\"idEmpleado\"\\s*:\\s*(\\d+)")
                .matcher(resultado.getResponse().getContentAsString());
        if (!matcher.find()) {
            throw new AssertionError("La respuesta no contiene el identificador del empleado.");
        }
        return Integer.parseInt(matcher.group(1));
    }
}
