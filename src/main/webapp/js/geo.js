$(document).ready(function(){
  var longitude=0.0
  var latitude=0.0
  function initialize() {
    if(navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(makeCoords);
    } else {
      alert("Błąd")
    }
  }
  function makeCoords(position) {
    latitude  = position.coords.latitude
    longitude = position.coords.longitude
    console.log(position.coords.latitude)
    console.log(position.coords.longitude)
    $("#autodetect input[name=latitude]").val(latitude)
    $("#autodetect input[name=longitude]").val(longitude)
  }
  initialize()
});
