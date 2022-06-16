package com.user.api.email;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.user.api.email.model.Email;
import com.user.api.email.model.EmailDTO;
import com.user.api.security.UserDetailService;
import com.user.api.user.UserRepository;
import com.user.api.user.UserService;

@AutoConfigureMockMvc
@WebMvcTest(EmailControllerTest.class)
class EmailControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private EmailService service;
	@MockBean
	private EmailRepository repository;

	/*
	* User related dependencies below are for needed Spring Security
	* */
	@MockBean
	private UserDetailService userDetailService;
	@MockBean
	private UserRepository userRepository;
	@MockBean
	private UserService userService;


	public static final String API_URL = "/api/email";

	public static final Long ID = 1L;
	public static final String EMAIL = "test@test.com";
	public static final String EMAIL_TO = "test@test.com";
	public static final String SUBJECT = "test";
	public static final String BODY = "<b>email body</b>";

	private Email email;
	private EmailDTO emailDto;

	@BeforeEach
	void setUp() {
		startEmail();
	}

	private void startEmail() {
		email = new Email();
		email.setSubject(SUBJECT);
		email.setBody(BODY);
		email.setAddressTo(EMAIL_TO);
		email.setAddressFrom(EMAIL);

		emailDto = new EmailDTO();
		emailDto.setSubject(SUBJECT);
		emailDto.setBody(BODY);
		emailDto.setAddressTo(EMAIL_TO);
		emailDto.setAddressFrom(EMAIL);
	}

	@Test
	public void shouldReturnPageOfEmail_When_getAll() throws Exception {
		List<EmailDTO> emailList = new ArrayList<>();
		emailList.add(emailDto);
		Page<EmailDTO> emailListPage = new PageImpl(emailList);

		when(service.getAll(any())).thenReturn(emailListPage);

		this.mockMvc.perform(get(API_URL + "/")
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.size()", is(1)))
				.andExpect(jsonPath("$.content.[0].id", is(ID.intValue())))
				.andReturn();
	}

	@Test
	public void shouldReturnEmail_When_getById() throws Exception {
		when(service.getById(anyLong())).thenReturn(emailDto);

		this.mockMvc.perform(get(API_URL + "/" + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(ID.intValue())))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andReturn();
	}

}