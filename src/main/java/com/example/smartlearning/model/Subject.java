package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Integer subjectId;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Lob
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @OneToMany(
            mappedBy = "subject",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserSubject> userSubjects = new ArrayList<>();
}