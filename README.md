# Stock-Portfolio-Manager
⭐️RESTful API server for creating and maintaining stocks portfolios

⭐️Spring Boot App

## Features

- **_Allowing a user to create an accout and POST his portfolio_** 
- **_The generated portfolio can be edit easily by the REST API_** 
- **_User can get his total portfolio value with one GET request_** 
- **_User can GWT recommendations on stocks such as - most stable stock in the last days\most performing stock the last days \highest value stock_** 

## Compile :hammer:

After downloading the project and making sure you have an installed maven on your machine,

open the command line, navigate to the project's directory and type:

```bash
mvn install 
```
Can be done also on any Spring supported IDE such as intellij

## Configure :wrench:

**All supported stocks symbols are located in the "_supportedStocks.txt_" file**

**All of the stocks history is located in the "_stocks.csv_" file**

**All of the app configurations can be done in the "_application.properties_" file, such as:**

- Changing the database from H2 to anything you like working with
- Changing the database credentials
- Changing the app's port
- Manage the tomcat server configurations
- Manage session configurations 
- Manage the spring.security configurations
- and more...

## Run :arrow_forward:

Before running, make sure there is no process listening on your app's port(default is set to 8080).

After compiling, In the command line type:

```bash
mvn spring-boot:run
```
Can be done also on any Spring supported IDE such as intellij

**:beers: Congratulations, Your REST server is up and running :beers:**



## REST Routs and API  :tram:
### Creating a new user and portfolio:

- **URL**:
```
/api/portfolio
```
- Method: **POST**

- Body contant type: **JSON**

- Expected **body** will contain a list of all owned stocks symbols and thier amount like here:
```
[
    {
        "stockSymbol": "AMZN",
        "stockAmount": 2
    },
    {
        "stockSymbol": "FB",
        "stockAmount": 1
    },
    {
        "stockSymbol": "GOOGL",
        "stockAmount": 1
    }
]
```
- **Returns**:
The generated client's id with a HTTP status code of 200 :ok:


### Replacing the client's portfolio with a new one:
- **URL** will contain the client's id in the route:
```
/api/portfolio/replace/{clientId}
```
- Method: **PUT**

- Body contant type: **JSON**

- Expected **body** will contain a list of all stocks symbols and their amount like here:
```
[
    {
        "stockSymbol": "FBEN",
        "stockAmount": 4
    },
    {
        "stockSymbol": "MSFT",
        "stockAmount": 3
    }
]
```
- **Returns**:
HTTP status code of 200 :ok:

### Updating all\some of the client's stocks:
- **URL** will contain the client's id in the route:
```
/api/portfolio/update/{clientId}
```
- Method: **PUT**

- Body contant type: **JSON**

- Expected **body** will contain a list of stocks symbols that the user own and their new amount,
In case of amount 0 the stock will be deleted from the portfolio:
```
[
    {
        "stockSymbol": "AMZN",
        "stockAmount": 0
    }
]
```
- **Returns**:
HTTP status code of 200 :ok:

### Returning the client's total portfolio value:
- **URL** will contain the client's id in the route:
```
/api/portfolio/value/{clientId}
```
- Method: **GET**

- **Returns**:
The client's total portfolio value with a HTTP status code of 200 :ok:


### App's recommendation for the most performing client's stock in previous days:
- **URL** will contain the client's id in the route and past days will be entered in the query string, pastDays are limited to the amount supported by the history file:
```
/api/portfolio/performance/{clientId}?pastDays=5
```
- Method: **GET**

**Recommendation explanation**: Most performing stock is an owned stock that raised the most in value during the giving days entered.

- **Returns**:
the most performing client's stock in previous days with a HTTP status code of 200 :ok:


### App's recommendation for the most stable client's stock in previous days:
- **URL** will contain the client's id in the route and past days will be entered in the query string, pastDays are limited to the amount supported by the history file and must be at least 2:
```
/api/portfolio/stable/{clientId}?pastDays=5
```
- Method: **GET**

**Recommendation explanation**: Most stable stock is the one with least value fluctuation during the giving days entered.

- **Returns**:
the most stable client's stock in previous days with a HTTP status code of 200 :ok:


### App's recommendation for the best (not owned by the client) stock to buy.:
- **URL** will contain the client's id in the route
```
/api/portfolio/best/{clientId}
```
- Method: **GET**

**Recommendation explanation**: Best stock is the one that whose current value is the highest among all stocks.

- **Returns**:
the best (not owned by the client) stock to buy with a HTTP status code of 200 :ok:


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


Any queastions are welcome.

## Credits

FYBER


