package com.spsi.minesweeper.service;

import com.spsi.minesweeper.domain.Game;
import com.spsi.minesweeper.domain.GameRepository;
import com.spsi.minesweeper.domain.GameStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private static final int[][] dir = {{1,0},{0,1},{0,-1},{-1,0},{1,1},{-1,-1},{-1,1},{1,-1}};
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game createGame(String host,String RoomID,int width,int height, int mine) {
        int[] board = new int[width*height];
        boolean[] revealed = new boolean[width*height];
        boolean[] flag = new boolean[width*height];
        Set<String> attenders = new HashSet<>();
        List<Integer> mines = new ArrayList<>();
        LocalDateTime time = LocalDateTime.now();
        Random random = new Random(time.getDayOfYear()+time.getDayOfMonth()+time.getDayOfMonth());

        for(int i=0;i<mine;++i) {
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

        Game game = Game.of(board,width,height,revealed,flag,attenders,mines,GameStatus.READY);

        return gameRepository.save(game);
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

    public Optional<Game> findGameById(String gameID) {
        return gameRepository.findById(gameID);
    }

    public boolean existsGameById(String gameID) {
        return gameRepository.existsById(gameID);
    }

    public Game updateGame(Game game,String userID, List<Action> actionList) {
        Queue<Integer> que = new LinkedList<>();
        int[] board = game.getBoard();
        boolean[] revealed = game.getRevealed();
        boolean[] flag = game.getFlag();
        int width = game.getWidth();
        int height = game.getHeight();
        Set<String> attenders = Optional.ofNullable(game.getAttenders()).orElse(new HashSet<>());
        GameStatus gameStatus = GameStatus.PLAYING;

        if(actionList!=null) {
            for(Action act : actionList) {
                switch(act.getActionType()) {
                    case DIG:
                        if(board[act.getLoc()]==-1) {
                            gameStatus = GameStatus.ENDED;
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
            for(int i=0;i<4;++i) {
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
            gameStatus =GameStatus.WIN;
        }

        attenders.add(userID);
        return gameRepository.save(Game.of(board,width,height,revealed,flag,attenders,game.getMines(),gameStatus));
    }
}
