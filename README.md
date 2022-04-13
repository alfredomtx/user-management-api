
# What is this project?
User API aplication

## Security, Authentication and Authorization
Spring Security is responsible to manage Authentication and Authorization.
When logging in, the user is authenticated in the database and receives an `access` and `refresh` token.

### Json Web Token (JWT) for Authorization
Every request received is intercepted by Spring Security and validations are performed to check whether
the `access token` provided in the request is valid.


## Tests
Most of the Controller and Service methods have unit testing.



## Email Services


### Send emails using RabbitMQ
You can create your own instance of RabbitMQ using docker locally or, as I prefer, a cloud solution, and I recommend
https://www.cloudamqp.com/, it has free plans for hobby/development and it's very easy to use.