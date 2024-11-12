package es.storeapp.business.repositories;

import es.storeapp.business.entities.User;

import java.sql.DriverManager;
import java.text.MessageFormat;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends AbstractRepository<User> {

    private static final String FIND_USER_BY_EMAIL_QUERY = "SELECT u FROM User u WHERE u.email = ''{0}''";
    private static final String COUNT_USER_BY_EMAIL_QUERY = "SELECT COUNT(*) FROM User u WHERE u.email = ''{0}''";
    // private static final String LOGIN_QUERY = "SELECT u FROM User u WHERE u.email
    // = ''{0}'' AND u.password = ''{1}''";
    /* vulnerabilidad - SQL-i */
    private static final String LOGIN_QUERY = "SELECT u FROM User u WHERE u.email = :email AND u.password = :password";

    public User findByEmail(String email) {
        try {
            Query query = entityManager.createQuery(MessageFormat.format(FIND_USER_BY_EMAIL_QUERY, email));
            return (User) query.getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean existsUser(String email) {
        Query query = entityManager.createQuery(MessageFormat.format(COUNT_USER_BY_EMAIL_QUERY, email));
        return ((Long) query.getSingleResult() > 0);
    }

    public User findByEmailAndPassword(String email, String password) {
        try {
            // PreparedStatement preparedStatement =
            // DriverManager.getConnection(url).preparedStatement(LOGIN_QUERY)

            /* vulnerabilidad - SQL-i */
            // Query query = entityManager.createQuery(MessageFormat.format(LOGIN_QUERY,
            // email, password));
            Query query = entityManager.createQuery(LOGIN_QUERY);
            query.setParameter("email", email);
            query.setParameter("password", password);
            return (User) query.getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
