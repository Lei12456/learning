package com.yl.javaweb.servlet;

import jakarta.servlet.GenericServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

public class TestServlet extends GenericServlet {
    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {

    }

    public static void main(String[] args) {
        System.out.println(0.05D+0.01D);
        BigDecimal add = new BigDecimal("0.05").add(new BigDecimal("0.01"));
        System.out.printf("0.05 + 0.01 = %s\n", add);
    }
}
