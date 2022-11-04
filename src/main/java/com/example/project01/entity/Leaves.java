package com.example.project01.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "leaves")
@Data
public class Leaves {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int lno;

    private int mno;

    private String use_date;

    private String type;

    private double leave_count;

    private int rno;

    @Builder
    public Leaves(int rno, int mno, String use_date, String type , double leave_count) {
        this.rno = rno;
        this.mno = mno;
        this.use_date = use_date;
        this.type = type;
        this.leave_count = leave_count;

    }

    public Leaves() {}
}
