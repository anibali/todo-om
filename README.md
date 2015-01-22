# todo-om

A silly little Todo list app I created to learn about
ClojureScript and Om.

![Screenshot](https://cloud.githubusercontent.com/assets/226707/5853834/74c923d6-a27b-11e4-9d19-513b0c6db40e.png)

## Development

Start a REPL (in a terminal: `lein repl`).

In the REPL do

```clojure
(run)
```

The call to `(run)` does two things, it starts the webserver at port
10555, and also the Figwheel server which takes care of live reloading
ClojureScript code and CSS. Give them some time to start.

When you see a new prompt appear, you're ready to go. Browse to
`http://localhost:10555` and enjoy.

## Deploying to Heroku

This assumes you have a
[Heroku account](https://signup.heroku.com/dc), have installed the
[Heroku toolbelt](https://toolbelt.heroku.com/), and have done a
`heroku login` before.

``` sh
git init
git add -A
git commit
heroku create
git push heroku master:master
heroku open
```

## Running with Foreman

Heroku uses [Foreman](http://ddollar.github.io/foreman/) to run your
app, which uses the `Procfile` in your repository to figure out which
server command to run. Heroku also compiles and runs your code with a
Leiningen "production" profile, instead of "dev". To locally simulate
what Heroku does you can do:

``` sh
lein with-profile -dev,+production uberjar && foreman start
```

Now your app is running at
[http://localhost:5000](http://localhost:5000) in production mode.
