package com.skillsync.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return route("auth-service-route")
                .route(path("/auth/**"), http())
                .filter(lb("auth-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> mentorServiceRoute() {
        return route("mentor-service-route")
                .route(path("/mentors/**"), http())
                .filter(lb("mentor-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> skillServiceRoute() {
        return route("skill-service-route")
                .route(path("/skills/**"), http())
                .filter(lb("skill-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> groupServiceRoute() {
        return route("group-service-route")
                .route(path("/groups/**"), http())
                .filter(lb("group-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceRoute() {
        return route("notification-service-route")
                .route(path("/notifications/**"), http())
                .filter(lb("notification-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return route("user-service-route")
                .route(path("/users/**"), http())
                .filter(lb("user-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> adminUserServiceRoute() {
        return route("admin-user-service-route")
                .route(path("/admin/users/**"), http())
                .filter(lb("user-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> adminGroupServiceRoute() {
        return route("admin-group-service-route")
                .route(path("/admin/groups/**"), http())
                .filter(lb("group-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> adminMentorServiceRoute() {
        return route("admin-mentor-service-route")
                .route(path("/admin/mentors/**"), http())
                .filter(lb("mentor-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> adminSkillServiceRoute() {
        return route("admin-skill-service-route")
                .route(path("/admin/skills/**"), http())
                .filter(lb("skill-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> sessionServiceRoute() {
        return route("session-service-route")
                .route(path("/sessions/**"), http())
                .filter(lb("session-service"))
                .build();
    }
    
    @Bean
    public RouterFunction<ServerResponse> reviewServiceRoute() {
        return route("review-service-route")
                .route(path("/reviews/**"), http())
                .filter(lb("review-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> adminSessionServiceRoute() {
        return route("admin-session-service-route")
                .route(path("/admin/sessions/**"), http())
                .filter(lb("session-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> adminReviewServiceRoute() {
        return route("admin-review-service-route")
                .route(path("/admin/reviews/**"), http())
                .filter(lb("review-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> paymentServiceRoute() {
        return route("payment-service-route")
                .route(path("/payments/**"), http())
                .filter(lb("payment-service"))
                .build();
    }

}
