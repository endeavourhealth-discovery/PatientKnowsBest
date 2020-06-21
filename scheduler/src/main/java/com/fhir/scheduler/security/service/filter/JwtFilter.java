package com.fhir.scheduler.security.service.filter;

import com.fhir.scheduler.security.service.UserDetailsService;
import com.fhir.scheduler.security.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserDetailsService userDetailsService ;


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
//       When a request comes this filter will intercept and do the authentication of the user and gets all the roles in the
//        Security holder context  have the principle and for every user there will be one security holder context and only one principal

//        Get the authentication header from request and check for the  Jwt token and then get the user details like expiration date and
//        and username then using the username get the user details and then do what authentication manager will do like authenticate the principle and then place in the context


        String username = null;
        String jwtToken = null;

        String authorizationHeader = httpServletRequest.getParameter("Authorization");
        if (authorizationHeader != null ) {
            jwtToken = authorizationHeader;
//            get the jwtToken from the header using the jwtUtil Service
            username = jwtUtil.extractUsername(jwtToken);
        }
        if (username!=null  && SecurityContextHolder.getContext().getAuthentication() == null){
//            System.out.println(authorizationHeader);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if(jwtUtil.validateToken(jwtToken,userDetails)){
//                If the token is valid and the pricipal is not already present in the session then authenticate and place the user for the request processing time

                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());

           token.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));

           SecurityContextHolder.getContext().setAuthentication(token);

            }
        }


filterChain.doFilter(httpServletRequest,httpServletResponse);
    }
}
