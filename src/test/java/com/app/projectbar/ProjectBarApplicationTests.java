package com.app.projectbar;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class ProjectBarApplicationTests {

    @Test
    void contextLoads() {
        BCryptPasswordEncoder e = new BCryptPasswordEncoder();
        System.out.println("ADMIN_HASH: " + e.encode("admin"));
        System.out.println("MESERO_HASH: " + e.encode("mesero"));
    }

}
