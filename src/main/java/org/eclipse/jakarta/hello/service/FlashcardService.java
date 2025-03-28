package org.eclipse.jakarta.hello.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.jakarta.hello.model.Category;
import org.eclipse.jakarta.hello.model.Flashcard;
import org.eclipse.jakarta.hello.model.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FlashcardService {

    @PersistenceContext(unitName = "flashcardPU")
    private EntityManager em;

    @Transactional
    public void addFlashcard(String question, String answer, User user) {
        Flashcard card = new Flashcard(question, answer);
        card.setUser(user);
        em.persist(card);
    }

    @Transactional
    public void addFlashcard(String question, String answer, User user, Category category) {
    Flashcard card = new Flashcard(question, answer);
    card.setUser(user);
    card.setCategory(category);
    em.persist(card);
}

    @Transactional
    public void addFlashcard(String question, String answer) {
        Flashcard card = new Flashcard(question, answer);
        em.persist(card);
    }

    public List<Flashcard> getAllFlashcards() {
        return em.createQuery("SELECT f FROM Flashcard f", Flashcard.class).getResultList();
    }

    public Flashcard getFlashcardById(Long id) {
        return em.find(Flashcard.class, id);
    }

    public List<Flashcard> getFlashcardsByCategory(Category category) {
        return em.createQuery("SELECT f FROM Flashcard f WHERE f.category = :category", Flashcard.class)
                 .setParameter("category", category)
                 .getResultList();
    }

    public List<Flashcard> getDueFlashcards(User user) {
        return em.createQuery(
            "SELECT f FROM Flashcard f WHERE f.user = :user AND f.nextReview <= :now ORDER BY f.nextReview ASC",
            Flashcard.class)
            .setParameter("user", user)
            .setParameter("now", LocalDateTime.now())
            .getResultList();
    }

    @Transactional
    public void updateAfterReviewWithRating(Long id, String rating) {
        Flashcard card = em.find(Flashcard.class, id);
        if (card == null) return;
    
        int reps = card.getRepetitions();
        double ease = card.getEasiness() == 0.0 ? 2.5 : card.getEasiness();
        double interval = card.getReviewInterval();
    
        switch (rating) {
            case "again":
                reps = 0;
                ease = Math.max(1.3, ease - 0.2);
                interval = 0;
                break;
    
            case "hard":
                ease = Math.max(1.3, ease - 0.15);
                reps++;
                interval = Math.max(3, interval * 1.2);
                break;
    
            case "good":
                reps++;
                interval = (interval == 0) ? 10 : interval * ease;
                ease += 0.1;
                interval = Math.max(10, interval);
                break;
    
            case "easy":
                reps++;
                interval = (interval == 0) ? 20 : interval * ease * 1.5;
                ease += 0.15;
                interval = Math.max(20, interval);
                break;
        }
    
        card.setRepetitions(reps);
        card.setEasiness(ease);
        card.setReviewInterval(interval);
        card.setNextReview(LocalDateTime.now().plusSeconds((long) (interval * 60)));
    
        em.merge(card);
    }
@Transactional
public void updateFlashcard(Flashcard card) {
    em.merge(card);
}

@Transactional
public void deleteFlashcard(Long id) {
    Flashcard card = em.find(Flashcard.class, id);
    if (card != null) {
        em.remove(card);
    }
}

public List<Flashcard> getAllFlashcardsByUser(User user) {
    return em.createQuery("SELECT f FROM Flashcard f WHERE f.user = :user", Flashcard.class)
             .setParameter("user", user)
             .getResultList();
}

public static String timeUntilNextReview(LocalDateTime nextReview) {
    Duration duration = Duration.between(LocalDateTime.now(), nextReview);

    if (duration.isNegative() || duration.isZero()) {
        return "Jetzt fällig";
    } else if (duration.toSeconds() < 60) {
        return "In " + duration.toSeconds() + " Sekunden"; 
    } else if (duration.toMinutes() < 60) {
        return "In " + duration.toMinutes() + " Minuten";
    } else if (duration.toHours() < 24) {
        return "In " + duration.toHours() + " Stunden";
    } else {
        return "In " + duration.toDays() + " Tagen";
    }
}
}
