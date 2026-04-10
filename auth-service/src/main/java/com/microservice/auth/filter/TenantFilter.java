package com.microservice.auth.filter;

import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.microservice.auth.util.TenantContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String tenantId = req.getHeader("X-Tenant-ID");
		if (tenantId != null && !tenantId.isEmpty()) {
			TenantContext.setCurrentTenant(tenantId);
		}
		try {
			chain.doFilter(request, response);
		} finally {
			TenantContext.clear();
		}
	}
}