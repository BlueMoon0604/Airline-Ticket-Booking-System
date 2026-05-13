package com.skybooker.api;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ApiGatewayApplicationTest {

    @Test
    void mainMethodShouldExist() throws Exception {
        Method method = ApiGatewayApplication.class.getMethod("main", String[].class);
        assertThat(method).isNotNull();
    }
}
