var turns = [["#", "#", "#"], ["#", "#", "#"], ["#", "#", "#"]];
var turn = "";
var gameOn = false;

function playerTurn(turn, id) {
    if (gameOn) {
        var spotTaken = $("#" + id).text();
        if (spotTaken !== "X" && spotTaken !== "O") {
            makeAMove(playerType, id.split("_")[0], id.split("_")[1]);
        }
    }
}

function makeAMove(type, xCoordinate, yCoordinate) {
    $.ajax({
        url: serverUrl + "/game/gameplay",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "type": type,
            "coordinateX": xCoordinate,
            "coordinateY": yCoordinate,
            "gameId": gameId
        }),
        success: function (data) {
            gameOn = false;
            displayResponse(data);
        },
        error: function (error) {
            //error.responseJSON
            console.log(error);
        }
    })
}

function displayResponse(data) {
    var s = "";
    let board = data.board;
    for (let i = 0; i < board.length; i++) {
        for (let j = 0; j < board[i].length; j++) {
            if (board[i][j] === 1) {
                s='X';
                //turns[i][j] = 'X'
            } else if (board[i][j] === 2) {
                s='O';
                //turns[i][j] = 'O';
            }else{
                s=' ';
            }
            let id = i + "_" + j;
            $("#" + id).text(s);
            //$("#" + id).text(turns[i][j]);
        }
    }
    if (data.winner != null) {
        alert("Winner is " + data.winnerName);
    }
    gameOn = true;
}

$(".tic").click(function () {
    var slot = $(this).attr('id');
    playerTurn(turn, slot);
});


$(".box").click(function () {
    var slot = $(this).attr('id');
    playerTurn(turn, slot);
});

function reset() {
    turns = [["#", "#", "#"], ["#", "#", "#"], ["#", "#", "#"]];
    //$(".tic").text("#");
    $(".box").text("");
    $(".tic").text("");
}

$("#reset").click(function () {
    reset();
});

function initializeBoard(cols){
    $(".game-board").html("");
    $(".game-board").removeAttr("style");
    $(".game-board").css("grid-template", "repeat("+cols+", 1fr) / repeat("+cols+", 1fr)");
    for (let i = 0; i < cols; i++) {
          for (let j = 0; j < cols; j++) {
            $(".game-board").append('<div class="box" id="'+i+'_'+j+'">&nbsp</div>');
          }
    }
    $(".box").click(function () {
        var slot = $(this).attr('id');
        playerTurn(turn, slot);
    });

    //var myEl = document.getElementById('myelement');

    //myEl.addEventListener('click', function() {
    //    alert('Hello world');
    //}, false);
}