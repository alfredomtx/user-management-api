
# What is this project?
Most applications nowadays need some type of user management API, some service to add new users, authenticate login, 
manage passwords, recover and reset, etc. Most importantly, manage passwords and login process in a secure way. 

This project is a user management REST API that I intend to use in my own projects, and it's a way for me to practice 
what I study and learn about Java/Spring.

Currently, I'm using this API in my personal project [OldBot](https://oldbot.com.br), for the user authentication,
registration process(account activation email, reset password request) and more.


# Is the project finished?
The answer is no! Very far from that! There are still much work to do related to this project, and I will keep working 
on it as way to learn and improve my Java/Spring knowledge. 


# Project technologies, functionalities and more

## Security, Authentication and Authorization
Spring Security is responsible to manage Authentication and Authorization.
When logging in, the user is authenticated in the database and receives an `access` and `refresh` token.

### Auth0 and Json Web Token (JWT)
It uses Auth0 for Authorization, generating Json Web Tokens to be used in every request by the users.

All requests received are intercepted by Spring Security and validations are performed to check whether
the `access token` provided in the request is valid.


## Email Service
The email service uses Java Mail to send emails. It's needed to set up the SMTP settings in your
`application.properties` file.

### Asynchronous email sending with RabbitMQ
Since sending email is something that can take a few seconds and does not make much sense being synchronous
, it's recommended to send emails asynchronously, to achieve that, one of the best solutions is RabbitMQ
using messaging queues.

You can create your own instance of RabbitMQ using docker locally or, as I prefer, a cloud solution, and I recommend
https://www.cloudamqp.com. It has free plans for hobby/development, and it's very easy to use.


## Database
The project has 2 already configured databases, MySQL and PostgreSQL.

MySQL I use in my local machine, and Postgre, since my application is deployed in Heroku, it's used there.


## Tests
Most of the **User related** Controller and Service methods have unit testing.
More tests will be implemented as the project goes on.

Another important pending work to do is implement integration tests, which I still need to learn how to implement.


## Work to do, improvements, intentions
There are still many things I have in mind for this project, things to implement, things to learn how to do, things 
to finish, etc. Here is a list of some of them separated in topics, I will be updating this list as I finish any item 
or think of new things to add.

### General project
*️⃣ Implement integration tests.

### User
*️⃣ Implement `refresh token` functioning to renew the `access token`.

*️⃣ Finish/fix the implementation of some tests with `TODO` comment.

*️⃣ Implement change user email process, with emails confirmation, token, etc. 

### Email
*️⃣ Create a microservice for the Email service(detach from the User service).

*️⃣ Create a microservice for the Consumer/Subscriber of the Email queue.
It's simple to do but for now I'm keeping in the same application to save costs with new Dynos 🙂 (since my application has low traffic).

*️⃣ Implement unit tests.

### RabbitMQ (Email)
*️⃣ Learn how to save failed messages in database or other queue for further analysis and retries.

### Registration
*️⃣ Implement unit tests.
