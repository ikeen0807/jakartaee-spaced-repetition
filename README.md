📚 Spaced Repetition Flashcard App
A web application for efficient learning with flashcards using the SM-2 spaced repetition algorithm, built with Jakarta EE, Thymeleaf, and PostgreSQL.

🚀 Features
User authentication and registration

Create, edit, and delete personal flashcards

Assign flashcards to categories

Filter flashcards by category

Learn flashcards using the SM-2 algorithm

Due-date tracking for each flashcard

Responsive UI with Thymeleaf and custom CSS

Category management (create & delete categories and associated cards)

🧠 Technology Stack
Backend: Jakarta EE 10 (Payara 6)

Frontend: Thymeleaf template engine

Database: PostgreSQL

Persistence: JPA (EclipseLink)

Build Tool: Maven

⚙️ Setup Instructions
Prerequisites
Java 17+

Maven

Payara Server 6+

PostgreSQL (with a configured user/database)

1. Database Configuration
Set up your PostgreSQL database and ensure the JNDI resource is available in glassfish-resources.xml:

```
<jdbc-resource pool-name="PostgresPool" jndi-name="jdbc/__default"/>
<jdbc-connection-pool name="PostgresPool"
                      res-type="javax.sql.DataSource"
                      datasource-classname="org.postgresql.ds.PGSimpleDataSource"
                      ... />
```
Ensure you define username, password, and database in the pool properties.

2. Deployment
```
mvn clean package
```
Deploy the .war file to your Payara server (e.g. via Admin Console or asadmin).
The app will be available at:
```
http://localhost:8080/jakartaee-spaced-repetition/
```
3. Default Setup
On first start, if no flashcards exist, two demo cards will be inserted for user ID 1.

You can register a new user and manage your own cards and categories securely.
📂 Project Structure

```
├── src/main/java
│   └── org.eclipse.jakarta.hello
│       ├── controller
│       ├── model
│       ├── service
│       └── util
├── src/main/resources
│   └── META-INF
│       ├── persistence.xml
│       └── glassfish-resources.xml
├── src/main/webapp
│   ├── css
│   ├── templates (Thymeleaf)
│   └── WEB-INF
│       └── web.xml
```

📈 Algorithm: SM-2
This app uses the SuperMemo-2 (SM-2) algorithm, the same core logic behind Anki:

Ratings: Again, Hard, Good, Easy

Adjusts easiness factor, interval, and repetition count

Re-schedules the card based on your performance

📸 Screenshots (Optional)
Add screenshots here to showcase flashcards, learning mode, category management, etc.

📚 License
MIT – free to use, modify, and distribute.
