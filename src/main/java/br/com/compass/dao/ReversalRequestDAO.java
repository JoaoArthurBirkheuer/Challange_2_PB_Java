package br.com.compass.dao;

import java.util.List;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Client;
import br.com.compass.model.Manager;
import br.com.compass.model.ReversalRequest;
import br.com.compass.model.enums.RequestStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class ReversalRequestDAO {

    public void save(ReversalRequest request) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(request);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void update(ReversalRequest request) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(request);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public ReversalRequest findById(Long id) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            return em.find(ReversalRequest.class, id);
        } finally {
            em.close();
        }
    }

    public List<ReversalRequest> findPendingRequests() {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            TypedQuery<ReversalRequest> query = em.createQuery(
                "SELECT r FROM ReversalRequest r WHERE r.status = :status", ReversalRequest.class);
            query.setParameter("status", RequestStatus.PENDING);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<ReversalRequest> findByClient(Client client) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            TypedQuery<ReversalRequest> query = em.createQuery(
                "SELECT r FROM ReversalRequest r WHERE r.requester = :client", ReversalRequest.class);
            query.setParameter("client", client);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<ReversalRequest> findByManager(Manager manager) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            TypedQuery<ReversalRequest> query = em.createQuery(
                "SELECT r FROM ReversalRequest r WHERE r.resolver = :manager", ReversalRequest.class);
            query.setParameter("manager", manager);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
