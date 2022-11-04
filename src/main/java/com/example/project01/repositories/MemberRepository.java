package com.example.project01.repositories;

import com.example.project01.entity.Member;
import com.example.project01.entity.Member;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Integer> {
    int findMemberByEmail(String email);
    Member findMemberByEmailAndPw(String email, String pw);
    Member findNameByMno(int mno);

    List findMemberByDno(int dno);

    @Query("SELECT MAX(mno) FROM Member ")
    int findMemberByMaxMno();

    @Transactional
    @Modifying
    @Query("UPDATE Member SET pw = :newPw WHERE mno = :mno")
    void updatePw(@Param(value="newPw")String newPw, @Param(value="mno")int mno);

    @Transactional
    @Modifying
    @Query("UPDATE Member SET activate = :activate WHERE mno = :mno")
    void updateActivate(@Param(value="activate")int activate, @Param(value="mno")int mno);
}
