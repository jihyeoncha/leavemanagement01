package com.example.project01.repositories;

import com.example.project01.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice,Integer> {
    Notice findByBno(int bno);

    @Transactional
    //update 또는 delete 를 수행하게 하기 위해서는
    //@Transactional 어노테이션을 붙어야 한다.
    @Modifying
    @Query("UPDATE Notice SET views = views + 1 WHERE bno = :bno")
    void updateViews(@Param(value="bno")int bno);

    @Transactional
    @Modifying
    @Query("UPDATE Notice SET modify_date = current_timestamp WHERE bno = :bno")
    void updateTime(@Param(value="bno")int bno);

    @Transactional
    @Modifying
    @Query("UPDATE Notice SET title = :title , content = :content WHERE bno = :bno")
    void updateNotice(@Param(value="bno")int bno, @Param(value="title")String title, @Param(value="content")String content);

    @Transactional
    void deleteByBno(int bno);

    Page<Notice> findAll(Pageable pageable);
}
