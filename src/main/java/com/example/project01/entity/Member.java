package com.example.project01.entity;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;


@Entity
@Table(name = "member")
@Data
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int mno;
    private String email;
    private String pw;
    private String name;
    private String entry_date;
    private int dno;

    @ColumnDefault("0")
    private int activate;

    @Builder
    public Member(String email, String name, String entry_date, int dno) {
        this.email = email;
        this.pw = "0000";
        this.name = name;
        this.entry_date = entry_date;
        this.dno = dno;
    }

    public Member() {}

}
