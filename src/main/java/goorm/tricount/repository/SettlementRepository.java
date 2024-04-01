package goorm.tricount.repository;

import goorm.tricount.model.Settlement;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository {
    Settlement create(String name);

    void addParticipantToSettlement(Long settlementId, Long memberId);

    Optional<Settlement> findById(Long id);

    List<Settlement> findAllByMemberId(Long memberId);
}
