package com.movieflix.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "streaming")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Streaming {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;
}
