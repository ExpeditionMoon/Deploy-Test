package goorm.tricount.repository;

import goorm.tricount.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository{
    private final JdbcTemplate jdbcTemplate;

    // 멤버 추가
    @Override
    public Member save(Member member) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("member").usingGeneratedKeyColumns("id");

        Map<String, Object> params = new HashMap<>();
        params.put("login_id", member.getLoginId());
        params.put("password", member.getPassword());
        params.put("name", member.getName());

        Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
        member.setId(key.longValue());

        return member;
    }

    // 멤버 조회
    @Override
    public Optional<Member> findById(Long id) {
        List<Member> result = jdbcTemplate.query("SELECT * FROM member WHERE id = ?", memberRowMapper(), id);
        return result.stream().findAny();
    }

    @Override
    public Optional<Member> findByLoginId(String loginId) {
        List<Member> result = jdbcTemplate.query("SELECT * FROM member WHERE login_id = ?", memberRowMapper(), loginId);
        return result.stream().findAny();
    }

    private RowMapper<Member> memberRowMapper() {
        return ((rs, rowNum) -> {
            Member member = new Member();
            member.setId(rs.getLong("id"));
            member.setLoginId(rs.getString("login_id"));
            member.setName(rs.getString("name"));
            member.setPassword(rs.getString("password"));

            return member;
        });
    }

}
