package com.example.project01.entity;


import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;

@Entity
@Table(name = "request")
@Data
@DynamicInsert
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int rno;

    private int mno;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private String req_date;

    private double leave_count;

    @ColumnDefault("-")
    private String approval;

    private String type;

    private String reason;

    private String leave_date;

    private String title;

    private String writer;


    @Builder
    public Request(int mno, String title, String writer, double leave_count, String type, String reason, String leave_date) {
        this.mno = mno;
        this.title = title;
        this.writer = writer;
        this.leave_count = leave_count;
        this.type = type;
        this.reason = reason;
        this.leave_date = leave_date;
    }

    public Request() {}

}
