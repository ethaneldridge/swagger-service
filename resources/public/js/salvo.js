function loadChats(chats) {

  var messages = "";
  for (var i = 0; i < chats.length; i++) {
    messages += chats[i].message + "\n";
  }

  $('#chats').html(messages);
}

function init() {

  var canvas = document.getElementById("canvasMap");
  var context = canvas.getContext('2d');
  var gamePieceImages = [];

  var serverState = function(gameState){

    var mapMain = gameState.salvoMaps[0];
    var mapImage = new Image();

    mapImage.onload = function() {
      $('#turn').html(gameState.salvoTurnTracker.turnDescription);

      canvas.width = mapImage.width;
      canvas.height = mapImage.height;
      context.drawImage(mapImage, 0, 0);
      drawGamePieces(context, gameState, gamePieceImages);
    }
    mapImage.src = "/images/" + mapMain.imageName;
  }

  $.ajax({url: "/salvo/gamestate", success: serverState});

  $.ajax({url: "/salvo/chats", success: loadChats});
}

function drawGamePieces(context, gameState, gamePieceImages) {
  var mapMain = gameState.salvoMaps[0];
  var imgCount = 0;

  for (var i = 0; i < mapMain.pieces.length; i++) {
    var piece = mapMain.pieces[i];
    var img = new Image();
    gamePieceImages.push(img);

    img.onload = function() {
      if (++imgCount>=mapMain.pieces.length) {
        drawUnits(context, gameState, gamePieceImages)
      }
    }
    img.src = "/images/" + piece.imageName;
  }
}

function drawUnits(context, gameState, gamePieceImages) {
  var mapMain = gameState.salvoMaps[0];
  var imgCount = 0;
  var stackName = ""
  var stacked = false;
  var stackXOffset = 0;
  var stackYOffset = 0;

  for (var i = 0; i < mapMain.pieces.length; i++) {

    var piece = mapMain.pieces[i];
    if (stackName == piece.stackName) {
      stacked = true;
      stackXOffset += 5;
      stackYOffset -= 5;
    } else {
      stackName = piece.stackName;
      stacked = false;
      stackXOffset = 0;
      stackYOffset = 0;
      
    }
    var x = piece.locationNew.salvoPoint.x - gamePieceImages[i].width / 2 + stackXOffset;
    var y = piece.locationNew.salvoPoint.y - gamePieceImages[i].height / 2 + stackYOffset;
    context.drawImage(gamePieceImages[i], x, y);
  }
}
