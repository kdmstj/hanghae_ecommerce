package kr.hhplus.be.server.controller;

import kr.hhplus.be.server.dto.UserPointRequest;
import kr.hhplus.be.server.dto.UserPointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/points")
@RequiredArgsConstructor
public class UserPointController {

    @GetMapping
    public ResponseEntity<UserPointResponse> point(
            @PathVariable long userId
    ) {
        return ResponseEntity.ok(new UserPointResponse(userId, 10000));
    }

    @PatchMapping("/charge")
    public ResponseEntity<UserPointResponse> charge(
            @PathVariable long userId,
            @RequestBody UserPointRequest request
    ) {
        return ResponseEntity.ok(new UserPointResponse(userId, request.amount()));
    }
}
