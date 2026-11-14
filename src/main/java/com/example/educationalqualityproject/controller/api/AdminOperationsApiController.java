package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.dto.MaterialCheckoutRequest;
import com.example.educationalqualityproject.dto.ResourceUsageReport;
import com.example.educationalqualityproject.entity.MaterialCheckout;
import com.example.educationalqualityproject.service.MaterialCheckoutService;
import com.example.educationalqualityproject.service.ResourceUsageReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminOperationsApiController {

    private final MaterialCheckoutService checkoutService;
    private final ResourceUsageReportService reportService;

    public AdminOperationsApiController(
            MaterialCheckoutService checkoutService,
            ResourceUsageReportService reportService) {
        this.checkoutService = checkoutService;
        this.reportService = reportService;
    }

    @GetMapping("/checkouts")
    public List<MaterialCheckout> checkouts() {
        return checkoutService.list();
    }

    @PostMapping("/checkouts")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialCheckout withdraw(
            @Valid @RequestBody MaterialCheckoutRequest request,
            Authentication authentication) {
        return checkoutService.withdraw(request, authentication.getName());
    }

    @PostMapping("/checkouts/{id}/return")
    public MaterialCheckout returnMaterial(@PathVariable String id, Authentication authentication) {
        return checkoutService.returnMaterial(id, authentication.getName());
    }

    @GetMapping("/reports/resource-usage")
    public List<ResourceUsageReport> resourceUsage() {
        return reportService.getReport();
    }
}
