const username =document.getElementById("username");
const joinButton = document.getElementById("joinButton");
const statusLabel = document.getElementById("status");
const gameID = document.getElementById("gameid");
const curID = document.getElementById("curid");
const gameBoard = document.getElementById("board");
const flagRadio = document.getElementById("flagRadio");
const attendersList = document.getElementById("attenders-content");
const closeButton = document.getElementById("closeButton");
const webSocketURL = "ws://192.168.35.177:8080/minesweeper"
var row;
var column;
var gameConn;
var statusConn;
var serverMessage ={
    board : [],
    host : "",
    attenders : [],
    id : ""
};

var clientMessage={
    id : "",
    actions : [],
    host : ""
};

statusLabel.innerText = "closed";

closeButton.onclick = ()=>{
    //clientMessage.id = gameID.value;
    //clientMessage.host = username.value;
    //clientMessage.actions = clientMessage.actions = [{loc:-1, actionType:"DIG"}];;
    gameConn.close();
};

joinButton.onclick = ()=>{
    gameBoard.replaceChildren();
    if(gameConn==null||gameConn.readyState ==3)
        gameConn = new WebSocket(webSocketURL);
    else {
        gameConn.close();
        gameConn = new WebSocket(webSocketURL);
    }

    gameConn.onopen = ()=>{
        console.log("controlconn connected");
        statusLabel.innerText = "connected!";
        clientMessage.id = gameID.value;
        clientMessage.host = username.value;
        clientMessage.actions = null;
        gameConn.send(JSON.stringify(clientMessage));
    };

    gameConn.onclose = ()=>{
        statusLabel.innerText = statusLabel.innerText+"  & ConnClosed";
    }

    gameConn.onmessage = (ev)=>{
        //var statusLabel = document.getElementById("status");
        //var gameID =document.getElementById("gameid");
        if(ev.data=="init") {
            //console.log("init!");
            return;
        }
        var recv = JSON.parse(ev.data);

        setAttenders(recv.attenders);
        var flags = 0;
        for( let i=0; i<recv.board.length;++i) {
            if(recv.board[i]=="10") {
                ++flags;
            }
        }
        if(!isNaN(Number(recv.message))) {
            statusLabel.innerText = "mines : "+(Number(recv.message)-flags);
        }
        //statusLabel.innerText = "mines : "+recv.message;
        if(curID.value == recv.id && gameBoard.hasChildNodes()) {
            drawGame(recv.y,recv.x,recv.board);
        } else {
            curID.value = recv.id;
            curID.innerText = recv.id;
            gameID.value = recv.id;
            initGame(recv.y,recv.x,recv.board);
        }

        if(recv.message=="GAME OVER" || recv.message=="WIN"){
            gameConn.close();
        }
        //console.log(JSON.parse(ev.data));
    };
};


function setAttenders(attenders) {
    attendersList.replaceChildren();
    attenders.forEach(i=>{
        let attender = document.createElement("a");
        attender.innerText = i;
        attendersList.append(attender);
    });
}

function initGame(rows,columns,brd) {
    gameBoard.replaceChildren();
    gameBoard.style.width = (columns*18)+"px";
    gameBoard.style.height = (rows*(18+4))+"px";
    row=rows;
    column = columns;
    for (let r = 0; r < rows; r++) {
        let row = document.createElement("div");
        for (let c = 0; c < columns; c++) {
            let idx= r*columns+c;
            let tile = document.createElement("div");

            tile.id = idx;
            tile.addEventListener("click",onTileClick);
            row.append(tile);
            if(brd[idx]=="9") {
                tile.innerText = "";
            } else if(brd[idx]=="10") {
                tile.innerText = "🚩";
            } else if(brd[idx]=="0"){
                tile.classList.add("tile-clicked");
                tile.innerText = "";
            } else if(brd[idx]=="-1") {
                tile.classList.add("tile-clicked");
                tile.innerText = "💣";
            } else {
                tile.classList.add("tile-clicked");
                tile.innerText = brd[idx];
            }

        }
        gameBoard.append(row);
    }
}

function drawGame(rows,columns,brd) {

    for (let r = 0; r < rows; r++) {
        for (let c = 0; c < columns; c++) {
            let idx= r*columns+c;
            let tile = document.getElementById(idx);

            if(brd[idx]=="9") {
                tile.innerText = "";
            } else if(brd[idx]=="10") {
                tile.innerText = "🚩";
            } else if(brd[idx]=="0"){
                tile.classList.add("tile-clicked");
                tile.innerText = "";
            } else if(brd[idx]=="-1") {
                tile.classList.add("tile-clicked");
                tile.innerText = "💣";
            } else {
                tile.classList.add("tile-clicked");
                tile.innerText = brd[idx];
            }

        }
    }
}

function onTileClick() {
    console.log(this);
    actions = [];
    var idx = this.id;


    var action = "DIG";

    if(document.querySelector('input[id="flagmode"]').checked) {
        var y = Math.floor(idx/column);
        var x = Math.floor(idx-y*column);
        let flagCount =0;
        let tile = document.getElementById(idx).innerText;
        action = "FLAG";
        if(tile == "" || tile == "🚩") {
            actions.push({"actionType":action,"loc":idx});
        } else if(!isNaN(tile)){
            let dir = [[1,0],[0,1],[0,-1],[-1,0],[1,1],[-1,-1],[-1,1],[1,-1]];
            for(let i =0; i < 8;  ++i) {
                let dy = y+dir[i][0];
                let dx = x+dir[i][1];
                if(dy<0||dy>=row) continue;
                if(dx<0||dx>=column) continue
                let newIdx= dy*column+dx;
                if(document.getElementById(newIdx).innerText == "🚩") {
                    flagCount++;
                    continue;
                }
                actions.push({"actionType":"DIG","loc":newIdx});
            }
            if(flagCount!=parseInt(tile)) return;
        }

    } else {
        actions.push({"actionType":action,"loc":idx});
    }

    var obj = {
        "actions" :actions,
        "host" :document.getElementById("username").value,
        "id" :document.getElementById("gameid").value
    }

    gameConn.send(JSON.stringify(obj));
}