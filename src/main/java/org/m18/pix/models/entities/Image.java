package org.m18.pix.models.entities;

import lombok.Data;
import java.util.Date;
import javax.persistence.*;

/**
 * Represents an image
 */
@Data
@Entity
@Table(
    name = "images",
    indexes = {
        @Index(columnList = "sender"),
        @Index(columnList = "created_at")
    }
)
public class Image {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String hash;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String sender;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_email")
    private String senderEmail;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT NOW()", insertable = false, updatable = false)
    private Date createdAt;

}
