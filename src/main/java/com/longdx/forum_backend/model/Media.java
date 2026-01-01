package com.longdx.forum_backend.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Duration;
import java.time.OffsetDateTime;

@Entity
@Table(name = "media")
@Data
public class Media {

    @Id
    @Tsid
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id")
    private ForumThread thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(name = "media_type", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String mediaType; // IMAGE, VIDEO, GIF

    @Column(name = "media_url", nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String mediaUrl; // Media URL

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl; // Thumbnail URL

    @Column(name = "file_size")
    private Long fileSize; // File size (bytes)

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "duration")
    private Duration duration; // Duration (for video)

    @Column(name = "position", nullable = false)
    private Integer position = 0; // Display order

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0; // Display order

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}

