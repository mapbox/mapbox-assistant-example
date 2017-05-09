'use strict';
let https = require('https');
let mapboxAPI = 'api.mapbox.com';

exports.handler = function(event, context, callback) {
  let routeOrigin = event.result.parameters['route-origin'] + '';
  let origin = routeOrigin.split(" ");
  let routeDestination = event.result.parameters['route-destination'] + '';
  let destination = routeDestination.split(" ");

  let optionsOrigin = searchDirectionsRequestOptions(origin[1]);
  let optionsDestination = searchDirectionsRequestOptions(destination[1]);

  makeRequest(optionsOrigin, function( data, error) {
    let originFeatures = data.features[0];

    if (originFeatures === null) {
        callback(null, {"speech": "I'm not sure!"});
    }
    else {
        let originLatLon = originFeatures.center[0] + "," + originFeatures.center[1];

        makeRequest(optionsDestination, function( data, error) {
            let destinationFeatures = data.features[0];

            if (destinationFeatures === null) {
                callback(null, {"speech": "I'm not sure!"});
            }
            else {
                let destinationLatLon = destinationFeatures.center[0] + "," + destinationFeatures.center[1];
                let optionsRoute = searchRouteRequestOptions(originLatLon, destinationLatLon);

                makeRequest(optionsRoute, function( data, error) {
                    let route = data.routes[0];

                    if (route === null) {
                        callback(null, {"speech": "I'm not sure!"});
                    }
                    else {
                        let distance = route.distance + " meters";
                        let duration = route.duration/60 + " minutes.";
                        let response = "The route from " + origin[1] + " to " + destination[1] + " is " + distance + " long and it will take you around " + duration;
                        callback(null, {"speech": response});
                    }
                });
            }
        });
    }
  });
};

function searchDirectionsRequestOptions(place) {
    return {
        host: mapboxAPI,
        path: '/geocoding/v5/mapbox.places/' + place + '.json?access_token=<YOUR_ACCESS_TOKEN_GOES_HERE>'
    };
}

function searchRouteRequestOptions(origin, destination) {
    return {
        host: mapboxAPI,
        path: '/directions/v5/mapbox/driving/' + origin + ';' + destination + '.json?access_token=<YOUR_ACCESS_TOKEN_GOES_HERE>'
    };
}

function makeRequest(options, callback) {
    var request = https.request(options,
    function(response) {
        var responseString = '';
        response.on('data', function(data) {
            responseString += data;
        });
        response.on('end', function() {
            var responseJSON = JSON.parse(responseString);
            callback(responseJSON, null);
        });
    });
    request.end();
}
