package com.example.project01.entity;

import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;

@Entity
@Table(name = "notice")
//lombok의 get+set+toString 등 설정 어노테이션
@Data
//아래 @Column에서 설정해 준 default값 자동 적용하게 하는 어노테이션
@DynamicInsert
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int bno;

    private int mno;

    private String writer;

    private String title;

    private String content;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private String reg_date;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private String modify_date;

    @ColumnDefault("0")
    private int views;

    @Builder
    public Notice(int mno, String writer,String title, String content) {
        this.mno = mno;
        this.writer = writer;
        this.title = title;
        this.content = content;
    }

    public Notice() {}
}
