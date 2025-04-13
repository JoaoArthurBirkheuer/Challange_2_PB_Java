package br.com.compass.dao;

import java.util.List;

import br.com.compass.model.AccountInactivationRequest;
import br.com.compass.model.enums.RequestStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class InactivationRequestDAO implements AutoCloseable{

    private final EntityManager em;

    public InactivationRequestDAO(EntityManager em) {
        this.em = em;
    }

    public void save(AccountInactivationRequest request) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(request);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save inactivation request", e);
        }
    }

    public void update(AccountInactivationRequest request) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(request);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to update inactivation request", e);
        }
    }

    public AccountInactivationRequest findById(Long id) {
        return em.find(AccountInactivationRequest.class, id);
    }

    public List<AccountInactivationRequest> findByStatus(RequestStatus status) {
        return em.createQuery(
                "SELECT a FROM AccountInactivationRequest a WHERE a.status = :status", 
                AccountInactivationRequest.class)
            .setParameter("status", status)
            .getResultList();
    }

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}
}