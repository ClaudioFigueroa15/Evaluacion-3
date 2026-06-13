package evaluacion3.subscription.repository;

import evaluacion3.subscription.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByUsuarioIdAndEstado(Long usuarioId, String estado);

    List<Subscription> findByUsuarioId(Long usuarioId);
}
