# Mapbox Alexa skill

A skill for Amazon Echo to interact with Mapbox services.

[![image](https://cloud.githubusercontent.com/assets/6964/26731813/423b47d2-4783-11e7-963b-6a46b3529ef2.png)](https://vimeo.com/220016659)

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

# Mapbox Conversation Action for the Google Assistant

A Conversation Action for Google Assistant users using API.AI to interact with Mapbox services.

[![Google Home](https://coedmagazine.files.wordpress.com/2016/10/google-home-lead.jpg?quality=88&w=750)](https://youtu.be/4cLFgJPnlz4)

### Examples

Get a route:

* You: `Hey Google! Mapbox.`
* Google Home: `Hi! I'm your Mapbox assistant. What can I do for you today?`
* You: `I'd like to go from Valencia to Barcelona.`
* Google Home: `The route from Valencia to Barcelona is 350756.4 meters long and it will take you around 197.88 minutes.`

### Dependencies

* [Mapbox Directions API](https://www.mapbox.com/api-documentation/#directions)
* [Mapbox Geocoding API](https://www.mapbox.com/api-documentation/#geocoding)
* [API.AI](https://api.ai/)
* The Conversation Action (`mapboxAssistant.js`) runs on AWS Lambda.

### Build your own Conversation Action

- API.AI

1) Create Your Conversational AI Assistant

2) Create A User Intent

- Amazon AWS

3) Create an Amazon IAM User

4) Create A Lambda Function

5) Code Your Lambda Function (e.g. `mapboxAssistant.js`)

6) Create an Endpoint in API Gateway

- API.AI

7) Use Your New API for Intent Fulfillment

8) Test On The Google Home
