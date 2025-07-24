package kr.hhplus.be.server.point.presentation.controller;

import jakarta.validation.Valid;
import kr.hhplus.be.server.point.application.service.PointService;
import kr.hhplus.be.server.point.presentation.dto.UserPointRequest;
import kr.hhplus.be.server.point.presentation.dto.UserPointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/points")
@RequiredArgsConstructor
public class UserPointController {

    private final PointService pointService;

    @GetMapping
    public ResponseEntity<UserPointResponse> get(
            @PathVariable long userId
    ) {
        UserPointResponse response = UserPointResponse.from(pointService.get(userId));

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/charge")
    public ResponseEntity<UserPointResponse> charge(
            @PathVariable long userId,
            @RequestBody @Valid UserPointRequest request
    ) {
        UserPointResponse response = UserPointResponse.from(pointService.charge(request.toCommand(userId)));

        return ResponseEntity.ok(response);
    }
}
