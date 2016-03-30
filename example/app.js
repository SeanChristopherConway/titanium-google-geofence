var win = Ti.UI.createWindow({

	backgroundColor : 'white'

});

var label = Ti.UI.createLabel();

win.add(label);

win.open();

// TODO: write your module tests here

var geoFence = require('google.geofence');

geoFence.addEventListener('enterregions', function(e) {
	var regions = JSON.parse(e.regions);
	Ti.API.info("Entered geofence: " + regions.identifier);
	alert("Entered geofence: " + regions.identifier);
});

geoFence.addEventListener('exitregions', function(e) {
	var regions = JSON.parse(e.regions);
	Ti.API.info("Exited geofence: " + regions.identifier);
	alert("Exiting geofence: " + regions.identifier);
});

Geofence.addEventListener('error', function(e) {
	Ti.API.info(JSON.stringify(e));
});


geoFence.addEventListener('monitorregions', function(e) {
	Ti.API.info("Monitoring regions: " + JSON.stringify(e));
});

var fences = [{

	"center" : {

		latitude : 55.62509823, //change these coordinates for testing and or use a location spoofer app to test locations outside of your area.

		longitude : -111.87053167

	},

	identifier : "test",

	radius : 50

}];

geoFence.stopMonitoringAllRegions();
geoFence.startMonitoringForRegions(JSON.stringify(fences));

