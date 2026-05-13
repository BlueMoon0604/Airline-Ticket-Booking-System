package com.skybooker.eureka;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class EurekaServerApplicationTest {

    @Test
    void mainMethodShouldExist() throws Exception {
        Method method = EurekaServerApplication.class.getMethod("main", String[].class);
        assertThat(method).isNotNull();
    }
}
