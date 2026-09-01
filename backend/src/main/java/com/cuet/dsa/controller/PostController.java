package com.cuet.dsa.controller;

import com.cuet.dsa.dto.request.PostRequest;
import com.cuet.dsa.dto.response.ApiResponse;
import com.cuet.dsa.dto.response.PagedResponse;
import com.cuet.dsa.dto.response.PostResponse;
import com.cuet.dsa.security.SecurityContextHelper;
import com.cuet.dsa.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final SecurityContextHelper securityHelper;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        PostResponse response = postService.createPost(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Post created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(postService.getFeed(page, size)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.ok(postService.getPost(postId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(postService.getUserPosts(userId, page, size)));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        PostResponse response = postService.updatePost(
                postId,
                userId,
                securityHelper.isAdmin(),
                request
        );
        return ResponseEntity.ok(ApiResponse.ok("Post updated successfully", response));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        postService.deletePost(postId, userId, securityHelper.isAdmin());
        return ResponseEntity.ok(ApiResponse.ok("Post deleted successfully", null));
    }
}
