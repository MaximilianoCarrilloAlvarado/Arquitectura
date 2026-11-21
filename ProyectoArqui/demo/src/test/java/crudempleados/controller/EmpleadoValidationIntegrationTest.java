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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmpleadoValidationIntegrationTest {

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
    void rechazaNombreEnBlanco() throws Exception {
        mockMvc.perform(post(API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"   ","edad":25,"salario":1000.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void rechazaEdadCero() throws Exception {
        mockMvc.perform(post(API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","edad":0,"salario":1000.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void rechazaSalarioNegativo() throws Exception {
        mockMvc.perform(post(API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","edad":25,"salario":-0.01}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void rechazaSalarioMayorAlLimiteDeDecimal10Con2() throws Exception {
        mockMvc.perform(post(API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","edad":25,"salario":100000000.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "El salario no puede superar 99999999.99."));
    }

    @Test
    void rechazaSalarioQueRequiereRedondeo() throws Exception {
        mockMvc.perform(post(API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","edad":25,"salario":1000.001}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "El salario no puede tener más de dos decimales."));
    }

    @Test
    void aceptaElLimiteMaximoDelSalario() throws Exception {
        mockMvc.perform(post(API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","edad":25,"salario":99999999.99}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.salario").value(99999999.99));
    }

    @Test
    void devuelve404CuandoNoExisteElEmpleado() throws Exception {
        mockMvc.perform(get(API + "/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
