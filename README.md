# 지뢰찾기

Spring Webflux의 Websocket을 활용한 지뢰찾기 게임  


## 환경

### redis stack  
https://github.com/redis-stack/redis-stack
#### docker
https://www.docker.com/
#### springboot 3.x  
#### ubuntu 20.04
#### redis-om-spring  
https://github.com/redis/redis-om-spring
## 흐름도

```mermaid
sequenceDiagram
    browser-->Spring Webflux: open websocket
    loop while sessionClose or GameEnd
        browser--)Spring Webflux: clientMessage(id, host:<userID>, actions:null)
        alt gameFound
            Spring Webflux--)Spring Webflux: updateGame
        else NotFound
            Spring Webflux--)Spring Webflux:createGame
        end
        loop Every 150ms
            Spring Webflux--)Spring Webflux:getGameStatus
            Spring Webflux--)browser: serverMessage(id, host, board, heigth, width, message)
        end
    end
```
### ServerMessage
서버로 부터 받는 메시지  
+ id : String : 게임 ID  
+ host : String : 게임을 만든 유저 ID  
+ height : int :게임 보드 높이  
+ width : int :게임 보드 넓이  
+ board : int[] :게임 보드 값  
+ message : String :추가 정보를 위한 메시지  
+ attendders : list : 게임에 참가중인 유저들  
### ClientMessage
브라우저에서 서버로 보내는 메시지
+ id : String : 게임 ID  
+ host : String : 메시지를 보내는 유저 ID
+ actions : list<Action> : 유저의 입력값  
    + action
        + loc : int : 유저가 누른 게임보드 인덱스
        + actionType : enum(DIG,FLAG) : 유저의 입력 타입, 깃발꽂기와 파기가 있다
