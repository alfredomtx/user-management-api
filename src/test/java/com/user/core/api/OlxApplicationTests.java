package com.user.core.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = { "classpath:email_configuration.yml" })

public class OlxApplicationTests {

	@Test
	void main() {
		OlxApplication.main(new String[]{});
	}

}
