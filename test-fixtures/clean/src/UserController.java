package demo;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
class UserController {
    @GetMapping("/users")
    public String users(@RequestParam int page, @RequestParam int size) { return "users"; }
}
