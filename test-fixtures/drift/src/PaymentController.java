package demo;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
class PaymentController {
    @PostMapping("/payments")
    public String createPayment(@RequestBody PaymentRequest payment) { return "created"; }

    @PutMapping("/users/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody UserUpdate update) { return "updated"; }
}

record PaymentRequest(String amount, String userId) {}
record UserUpdate(String email, String role) {}
