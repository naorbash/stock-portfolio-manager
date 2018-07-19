# Stock-Portfolio-Manager
⭐️RESTful API server for creating and maintaining stocks portfolios

⭐️Spring Boot App

## Features

- **Allowing a user to create an accout and POST his portfolio** 
- **The generated portfolio can now be edited easy by the REST API** 
- **User can get his total portfolio value with one GET request** 
- **User can get recommendations on stocks such as - most stable stock\best stock in the last days** 

## Installation

After downloading the project and making sure you have an installed maven on your machine,

enter the command line, navigate to its directory and type:

```bash
mvn install 
```
Can be done also on any Spring supported IDE such as intellij


## Configurations

All configurations can be done in the "application.properties" file.

such as:
- Changing the database from H2 to anything you like working with
- Changing the database credentials
- Manage SSL configurations
- Manage the tomcat server configurations
- Manage session configurations 
- Manage the spring.security configurations
- and more...



## Routs and API


to write here

## Deploy

Here is an example on how to deploy to [Heroku](https://heroku.com) using [Heroku CLI](https://devcenter.heroku.com/articles/heroku-command-line):
```bash
# login to heroku
heroku login

# create a Git repository for the application 
git init
git add .
git commit -m "first commit"

# provision a new Heroku app
heroku create

# deploy your code
git push heroku master

# visit the app’s URL
heroku open

# You can view the logs for the application by running this command:
heroku logs --tail


#The second time you deploy, you just need to:
git add -A
git commit -m "Update code"
git push heroku master
```


PRs are welcome.

## Credits

FYBER


