package com.olx.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.olx.controller.dto.UserDTO;
import com.olx.repository.UserRepository;
import com.olx.service.UserDetailServiceImpl;
import com.olx.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerTestOld {

	@MockBean
	private UserService userService;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private UserDetailServiceImpl userDetailServiceImpl;

	@Autowired
	private WebApplicationContext context;

	protected MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@Test
	// @DisplayName("Test Should Pass When Comment do not Contains Swear Words")
	public void firstTest() throws Exception {

		ResultActions result = mockMvc.perform(get("/api/user/listAll")).andExpect(status().isOk()).andDo(print());
		// .andExpect(content().json("{'message':'ok'}"));

		String content = result.andReturn().getResponse().getContentAsString();
		System.out.println("content: " + content);

		// .andExpect(jsonPath("$[0].name", is("teste")));

	}

	@Test
	public void ping() throws Exception {

		mockMvc.perform(get("/api/user/ping")).andExpect(status().isOk()).andExpect(content().string("pong"));
	}

	@Test
	public void whenValidEmail_thenUserShouldBeFound() {
		String email = "tee@teste.com";
		UserDTO found = userService.getByEmail(email);
		

		//assertEquals(found.getEmail(), email);
	}

}
