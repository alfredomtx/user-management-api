
# ☕ What is this project?
> Most applications need some type of user management API, a service to add new users, authenticate login, 
manage passwords, recover and reset, etc. So I'm **building my own** 🙂

This project is a User Management REST API built with `Spring Boot 2.6.4` and `Java 11`. I intend to use it in my own
projects, and it's a way for me to practice what I study and learn about Java/Spring.

Currently, I'm using this API in my personal project [OldBot](https://oldbot.com.br).


## Is the project finished?
The answer is no! Very far from that! There are still much work to do related to this project, and I will keep working 
on it as way to learn and improve my Java/Spring knowledge. 


## Project technologies, functionalities and more
<details><summary><b>💻 Security, Authentication and Authorization</b></summary>

Spring Security is responsible to manage Authentication and Authorization.
When logging in, the user is authenticated in the database and receives an `access token`.

### 🔴 Auth0 and Json Web Token (JWT)
It uses `Auth0` for Authorization, generating **Json Web Tokens** to be used in every request by the users.

All requests received are intercepted by Spring Security and validations are performed to check whether
the `access token` provided in the request is valid.

The token configurations, such as <u>expiration time</u>, are set in the `JWTAuthenticationFilter.java` file.

### 👮‍♂ Spring Security and Roles
All the access for the API and it's routes are set in the `SecurityConfiguration.java` file.  

Currently, there are only `2` main roles used in the project, they are:
- `USER`: simple user, allowed to access only login and registration related routes.
- `ADMIN`: has access and is allowed for everything.

</details>


<details><summary><b>📮 Email Service</b></summary>

The email service uses Java Mail to send emails. The SMTP settings must be set in your
`application.properties` file.

You can use [MailDev](https://github.com/maildev/maildev) to easily test email service in your local machine.

### 🐇 Asynchronous email sending with RabbitMQ
Since sending email is something that can take a few seconds and does not make much sense being synchronous
, it's recommended to send emails asynchronously, to achieve that, one of the best solutions is RabbitMQ
using messaging queues.

You can create your own instance of [RabbitMQ](https://www.rabbitmq.com/) using docker locally or, as I prefer, a
 cloud solution, and I recommend [CloudAMQP](https://www.cloudamqp.com). It has free plans for hobby/development
, and it's very easy to use.

- `RabbitMQService.java` is the Publisher which sends the messages to the queue.
- `EmailConsumer.java` is the Subscriber which receives the queue messages and call **EmailService** to send the emails.

</details>


<details><summary><b>📦 Database</b></summary>

The project has 2 configured databases by default, `MySQL` and `PostgreSQL`.

MySQL I use in my local machine, and Postgre, since my application is deployed in [Heroku](https://heroku.com/)
, it's used there.

</details>

<details><summary><b>✅Tests</b></summary>

Most of the `User` related Controller and Service methods have unit testing.
More tests will be implemented as the project goes on.

Another important pending work to do is implement **integration** tests, which I still need to learn how to implement.

</details>


## 🚀 Work to do, improvements, intentions
There are still many things I have in mind for this project, things to implement, things to learn how to do, things 
to finish, etc. **Here is a list** of some of them separated in topics, I will be updating this list as 
I finish any item or think of new things to add.

<details><summary><b>General stuff</b></summary>

- *️⃣ Implement integration tests.
- *️⃣ Implement API call limitations with Bucket4J.

</details>


<details><summary><b>User</b></summary>

- *️⃣ Finish/fix the implementation of some tests with `TODO` comment.
- *️⃣ Implement change user email process, with emails confirmation, token, etc. 

</details>


<details><summary><b>Email</b></summary>

- *️⃣ Create a microservice for the Email service(detach from the User service).
- *️⃣ Create a microservice for the Consumer/Subscriber of the Email queue.
It's simple to do but for now I'm keeping in the same application to save costs with new Dynos 🙂 (since my application
 has low traffic).
- *️⃣ Implement unit tests.

</details>


<details><summary><b>Registration</b></summary>

- *️⃣ Implement unit tests.

</details>

<details><summary><b>Security</b></summary>

- *️⃣ Implement unit tests for Spring Security.

</details>