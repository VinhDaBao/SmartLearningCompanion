package com.example.smartlearning.security;

import com.example.smartlearning.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // Service tải User từ DB

    /**
     * Đây là logic của "Người Gác Cổng"
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Lấy "Authorization" header từ request
        final String authHeader = request.getHeader("Authorization");

        // 2. Kiểm tra sơ bộ
        // Nếu không có header, hoặc header không bắt đầu bằng "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Cho qua (để đến các API public)
            return;
        }

        // 3. Lấy token (bỏ chữ "Bearer " đi)
        // "Bearer eyJhbGci..." -> "eyJhbGci..."
        final String jwt = authHeader.substring(7);

        // 4. Lấy username từ token
        final String username = jwtService.extractUsername(jwt);

        // 5. Nếu có username VÀ user này chưa được xác thực trong Context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Tải thông tin User (UserDetails) từ DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 7. Kiểm tra xem token có hợp lệ không
            if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {

                // 8. Nếu hợp lệ -> TẠO PHIÊN XÁC THỰC
                // (Đây là cách "bảo" Spring Security: "OK, user này hợp lệ!")
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,    // Đối tượng User (Principal)
                        null,           // Credentials (đã xác thực = null)
                        userDetails.getAuthorities() // Quyền (roles)
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Cập nhật Security Context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. Cho request đi tiếp
        filterChain.doFilter(request, response);
    }
}