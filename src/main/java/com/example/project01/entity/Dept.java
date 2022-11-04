package com.example.project01.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "dept")
@Data
public class Dept {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int dno;
    private String dname;

}
