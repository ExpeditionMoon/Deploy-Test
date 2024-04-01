package goorm.tricount.repository;

import goorm.tricount.model.Member;
import goorm.tricount.model.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class SettlementRepositoryImpl implements SettlementRepository{

    private final JdbcTemplate jdbcTemplate;

    // 정산 추가
    @Override
    public Settlement create(String name) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("settlement").usingGeneratedKeyColumns("id");

        Map<String, Object> params = new HashMap<>();
        params.put("name", name);

        Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));

        Settlement settlement = new Settlement();
        settlement.setId(key.longValue());
        settlement.setName(name);

        return settlement;
    }

    // 정산 참가자(중간 테이블) 추가
    @Override
    public void addParticipantToSettlement(Long settlementId, Long memberId) {
        jdbcTemplate.update("INSERT INTO settlement_participants (settlement_id, member_id) VALUES (?, ?)", settlementId, memberId);
    }

    // 특정 정산 조회(정산 + 정산 참가자 자세한 정보)
    @Override
    public Optional<Settlement> findById(Long id) {
        List<Settlement> result = jdbcTemplate.query("SELECT * FROM settlement\n" +
                "JOIN settlement_participant ON settlement.id = settlement_participant.settlement_id\n" +
                "JOIN member on settlement_participant.member_id = member.id\n" +
                "WHERE settlement.id = ?", settlementParticipantRowMapper(), id);
        return result.stream().findAny();
    }

    // TODO 특정 멤버의 모든 정산 목록 조회(나중에 구현)
    @Override
    public List<Settlement> findAllByMemberId(Long memberId) {
        return null;
    }

    private RowMapper<Settlement> settlementParticipantRowMapper() {
        return ((rs, rowNum) -> {
            Settlement settlement = new Settlement();
            settlement.setId(rs.getLong("settlement.id"));
            settlement.setName(rs.getString("settlement.name"));

            List<Member> participants = new ArrayList<>();
            do {
                Member participant = new Member(
                        rs.getLong("member.id"),
                        rs.getString("member.login_id"),
                        rs.getString("member.name"),
                        rs.getString("member.password")
                );
            } while (rs.next());

            settlement.setParticipants(participants);
            return settlement;
        });
    }
}
