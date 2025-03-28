package org.eclipse.jakarta.hello.util;

import org.eclipse.jakarta.hello.model.Flashcard;
import org.eclipse.jakarta.hello.model.User;
import org.eclipse.jakarta.hello.service.UserService;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Startup
@Singleton
public class DbInit {

    @PersistenceContext(unitName = "flashcardPU")
    private EntityManager em;
    @Inject
    private UserService userService;

    @PostConstruct
    @Transactional
    public void init() {
        try {
            long count = em.createQuery("SELECT COUNT(f) FROM Flashcard f", Long.class).getSingleResult();
            long userCount = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
            if (userCount == 0) {
                em.persist(new User("admin", "123"));

                System.out.println("Admin erfolgreich eingefügt!");
            }
           
            if (count == 0) {
                em.persist(new Flashcard("Testfrage?", "Testantwort"));
                 User user =  userService.findById(1);
                em.persist(new Flashcard("Was ist Jakarta EE?", "Eine moderne Java Plattform für Webanwendungen.", user));

                System.out.println("📚 Testdaten erfolgreich eingefügt!");
            }

        } catch (Exception e) {
            System.out.println("⚠️ Initialisierung fehlgeschlagen: " + e.getMessage());
        }
    }
}
