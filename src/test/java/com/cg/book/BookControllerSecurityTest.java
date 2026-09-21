package com.cg.book;

import com.cg.auth.CustomUserDetailsService;
import com.cg.config.SecurityConfig;
import com.cg.config.SecurityErrorHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class})
class BookControllerSecurityTest {
    @Autowired
    MockMvc mvc;
    @MockitoBean
    BookService service;
    @MockitoBean
    CustomUserDetailsService details;

    @Test
    void postBookWithoutAuthReturns401() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"T\",\"author\":\"A\",\"isbn\":\"1\",\"price\":10,\"stockQuantity\":1}")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void postBookAsUserReturns403() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"T\",\"author\":\"A\",\"isbn\":\"1\",\"price\":10,\"stockQuantity\":1}")).andExpect(status().isForbidden());
    }
}
