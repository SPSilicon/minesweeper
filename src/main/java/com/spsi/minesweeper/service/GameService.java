package com.spsi.minesweeper.service;

import com.spsi.minesweeper.domain.Action;
import com.spsi.minesweeper.domain.Game;
import com.spsi.minesweeper.domain.GameRepository;
import com.spsi.minesweeper.domain.GameStatus;
import com.spsi.minesweeper.service.message.ServerMessage;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private static final int[][] dir = {{1,0},{0,1},{0,-1},{-1,0},{1,1},{-1,-1},{-1,1},{1,-1}};
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    
    public String restartGame(String roomId, String host) {
        Game game = gameRepository.findById(roomId).orElseThrow(()->new RuntimeException("game not found"));
        int[] board = game.getBoard();
        int width = game.getWidth();
        int height = game.getHeight();
        int mineCount = game.getMines().size();
        boolean[] revealed = game.getRevealed();
        boolean[] flag = game.getFlag();
        Set<String> attenders = game.getAttenders();
        List<Integer> mines = new ArrayList<>();
        LocalDateTime time = LocalDateTime.now();
        
        Random random = new Random(time.getDayOfYear()+time.getDayOfMonth()+time.getNano());

        Arrays.fill(board,0);
        Arrays.fill(revealed,false);
        Arrays.fill(flag,false);
        for(int i=0;i<mineCount;++i) {
            int y = random.nextInt(height);
            int x = random.nextInt(width);
            int cur = y*width+x;
            if(board[cur]==-1) continue;
            for(int j=0;j<dir.length;++j) {
                int dy = y+dir[j][0];
                int dx = x+dir[j][1];
                if(dy<0||dy>=height) continue;
                if(dx<0||dx>=width) continue;
                int idx = dy*width+dx;
                if(board[idx]>=0) ++board[idx];
            }
            board[cur] = -1;
            mines.add(cur);
        }

        attenders.add(host);
        game.setGameStatus(GameStatus.PLAYING);
        game = gameRepository.save(game);

        return game.getId();
    }

    public String updateGame(String roomId,String userId, List<Action> actionList) {
        Game game = gameRepository.findById(roomId).orElseThrow(()->new RuntimeException("game not found"));
        Queue<Integer> que = new LinkedList<>();
        int[] board = game.getBoard();
        boolean[] revealed = game.getRevealed();
        boolean[] flag = game.getFlag();
        int width = game.getWidth();
        int height = game.getHeight();
        Set<String> attenders = Optional.ofNullable(game.getAttenders()).orElse(new HashSet<>());
        game.setGameStatus(GameStatus.PLAYING);

        if(actionList!=null) {
            for(Action act : actionList) {
                switch(act.getActionType()) {
                    case DIG:
                        if(board[act.getLoc()]==-1) {
                            game.setGameStatus(GameStatus.ENDED);
                            break;
                        }
                        if(flag[act.getLoc()]) break;
                        if(!revealed[act.getLoc()]) {
                            revealed[act.getLoc()] = true;
                            if(board[act.getLoc()]==0)
                                que.add(act.getLoc());
                        }

                        break;
                    case FLAG:
                        flag[act.getLoc()] = !flag[act.getLoc()];
                        break;
                }

            }
        }

        while(!que.isEmpty()) {
            int cur = que.poll();
            
            int y = cur/width;
            int x = cur-y*width;
            for(int i=0;i<8;++i) {
                int dy = y+dir[i][0];
                int dx = x+dir[i][1];
                int idx = dy*width+dx;

                if(dy<0||dy>=height) continue;
                if(dx<0||dx>=width) continue;
                if(revealed[idx]) continue;
                if(board[idx]>8||board[idx]<0) continue;
                revealed[idx] = true;
                if(board[idx]==0)
                    que.add(idx);
            }
        }
        int count =0;
        for(boolean i : revealed) {
            if(i) ++count;
        }

        if(count==revealed.length-game.getMines().size()) {
            game.setGameStatus(GameStatus.WIN);
        }

        attenders.add(userId);
        game = gameRepository.save(game);

        return game.getId();
    }

    public String createGame(String host,int width,int height, int mine) {
        int[] board = new int[width*height];
        boolean[] revealed = new boolean[width*height];
        boolean[] flag = new boolean[width*height];
        Set<String> attenders = new HashSet<>();
        List<Integer> mines = new ArrayList<>();
        LocalDateTime time = LocalDateTime.now();
        Random random = new Random(time.getDayOfYear()+time.getDayOfMonth()+time.getNano());

        for(int i=0;i<mine;++i) {
            int y = random.nextInt(height);
            int x = random.nextInt(width);
            int cur = y*width+x;
            if(board[cur]==-1) {
                --i;
                continue;
            }
            for(int j=0;j<dir.length;++j) {
                int dy = y+dir[j][0];
                int dx = x+dir[j][1];
                if(dy<0||dy>=height) continue;
                if(dx<0||dx>=width) continue;
                int idx = dy*width+dx;
                if(board[idx]>=0) ++board[idx];
            }
            board[cur] = -1;
            mines.add(cur);
        }

        attenders.add(host);

        Game game = Game.of(board,width,height,revealed,flag,attenders,mines,GameStatus.READY);

        game = gameRepository.save(game);

        return game.getId();
    }

    public ServerMessage getServerMessage(String roomId) {
        Optional<Game> oGame = gameRepository.findById(roomId);
        if(oGame.isPresent()) {
            Game game = oGame.get();
            int[] board = game.getBoard();
            boolean[] revealed = game.getRevealed();
            boolean[] flag = game.getFlag();
            String GameMessage= "";
            for(int i=0;i<board.length;++i) {
                if(!revealed[i]) {
                    if(flag[i])
                        board[i]=10;
                    else
                        board[i]=9;
                }
            }
    
            if(game.getGameStatus() == GameStatus.PLAYING) {
                GameMessage = Integer.toString(game.getMines().size());
            } else if(game.getGameStatus() == GameStatus.ENDED) {
                for(int i : game.getMines()){
                    board[i] = -1;
                }
                GameMessage = "GAME OVER";
            } else if(game.getGameStatus()== GameStatus.WIN) {
                GameMessage = "WIN";
            }
            return new ServerMessage(game.getId(),game.getHost(),game.getHeight(),game.getWidth(), game.getAttenders(),board,GameMessage);
        }
        return new ServerMessage("","",1,1,new HashSet<>(),new int[1],"error");
    }
    
    public void leave(String roomID, String userID) {
        Game game = gameRepository.findById(roomID).orElse(null);
        game.getAttenders().remove(userID);
        if(game.getAttenders().isEmpty()) {
            deleteGame(game);
        } else {
            gameRepository.save(game);
        }
    }
   
    public void deleteGame(Game game) {
        gameRepository.delete(game);
    }
    
    public String findGameById(String roomId) {
        Game game = gameRepository.findById(roomId).orElse(null);
        if(game==null) return "";
        return game.getId();
    }
    
    public boolean existsGameById(String roomId) {
        return gameRepository.existsById(roomId);
    }


    public boolean isEnded(String roomId) {
        Game game = gameRepository.findById(roomId).orElse(null);
        if(game==null) return true;
        else return game.getGameStatus()==GameStatus.ENDED;
    }
    
}
