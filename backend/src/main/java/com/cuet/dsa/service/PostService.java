package com.cuet.dsa.service;

import com.cuet.dsa.dto.request.PostRequest;
import com.cuet.dsa.dto.response.PagedResponse;
import com.cuet.dsa.dto.response.PostResponse;
import com.cuet.dsa.entity.Post;
import com.cuet.dsa.entity.User;
import com.cuet.dsa.exception.AccessDeniedException;
import com.cuet.dsa.exception.ResourceNotFoundException;
import com.cuet.dsa.repository.PostRepository;
import com.cuet.dsa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponse createPost(Long userId, PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = Post.builder()
                .postBody(request.getPostBody().trim())
                .user(user)
                .build();

        return toResponse(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        return toResponse(findPost(postId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getFeed(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postAt"));
        Page<PostResponse> posts = postRepository.findAll(pageable).map(this::toResponse);
        return PagedResponse.from(posts);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getUserPosts(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postRepository
                .findByUserIdOrderByPostAtDesc(userId, pageable)
                .map(this::toResponse);
        return PagedResponse.from(posts);
    }

    @Transactional
    public PostResponse updatePost(Long postId, Long userId, boolean admin, PostRequest request) {
        Post post = findPost(postId);
        ensureOwnerOrAdmin(post, userId, admin);
        post.setPostBody(request.getPostBody().trim());
        return toResponse(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId, boolean admin) {
        Post post = findPost(postId);
        ensureOwnerOrAdmin(post, userId, admin);
        postRepository.delete(post);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    private void ensureOwnerOrAdmin(Post post, Long userId, boolean admin) {
        if (!admin && !post.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only modify your own post");
        }
    }

    private PostResponse toResponse(Post post) {
        User user = post.getUser();
        return PostResponse.builder()
                .postId(post.getPostId())
                .postBody(post.getPostBody())
                .postAt(post.getPostAt())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .build();
    }
}
