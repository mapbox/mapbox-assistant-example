# Mapbox Alexa skill

A skill for Amazon Echo to interact with Mapbox services.

[![image](https://cloud.githubusercontent.com/assets/6964/22553109/896dcce6-e929-11e6-9b12-def4f5343395.png)](https://www.youtube.com/watch?v=dljDVTtMDTo)

### Examples

Set your home address:

* You: `Alexa, tell Mapbox that my address is <address>.`
* Alexa: `Thank you, home address set.`

Set your office address:

* You: `Alexa, tell Mapbox that my office address is <address>.`
* Alexa: `Thank you, office address set.`

Get traffic information:

* You: `Alexa, ask Mapbox how long is my commute.`
* Alexa: `Your commute is 3 kilometers long, a 9 minutes drive with current traffic.`

Use the Directions API:

* You: `Alexa, ask Mapbox how far is <address>.`
* Alexa: `That address is a 4 kilometers drive, 10 minutes with current traffic.`

Use the Geocoding API:

* You: `Alexa, ask Mapbox what's popular nearby.`
* Alexa: `Have you tried Dupont Circle Fountain, Washington, District of Columbia 20036, United States?`

Read the latest post on the blog:

* You: `Alexa, ask Mapbox what's new.`
* Alexa: `Over the last week we've published 9 stories, our latest is that David Rhodes published today Bringing real-world places into your game.`

### Dependencies

* [Mapbox Android Services](http://www.github.com/mapbox/mapbox-java).
* The skill runs on AWS Lambda, session state (user home/work addresses) is stored on AWS S3.

### Deploy to AWS Lambda

* Build the `.zip` deployment package: `make build`.
* Output is in `skill/build/distributions/alexa-skill-0.1.zip`.
* Upload to AWS Lambda.
