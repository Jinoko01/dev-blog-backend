package com.okojin.dev.blog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"DB_URL=jdbc:postgresql://localhost:5432/test",
		"DB_USERNAME=test",
		"DB_PASSWORD=test",
		"JWT_SECRET=test-secret-that-is-long-enough-for-256bit-hmac",
		"ADMIN_USERNAME=admin",
		"ADMIN_PASSWORD=admin",
		"SUPABASE_URL=https://test.supabase.co",
		"SUPABASE_SERVICE_ROLE_KEY=test-key"
})
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
