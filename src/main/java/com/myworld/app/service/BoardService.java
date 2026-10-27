
package com.myworld.app.service;

import com.myworld.app.entity.Board;
import com.myworld.app.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Transactional
    public Board saveBoard(String title, String content) {
        Board board = new Board(title, content);
        return boardRepository.save(board);
    }

    @Transactional(readOnly = true)
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Board getBoard(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("게시글이 없습니다. ID: " + id));
    }
    @Transactional
    public void updateBoard(Long id, String title, String content) {

        Board board = boardRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("게시글이 없습니다. ID: " + id));

        board.update(title, content);
    }
    @Transactional
    public void deleteBoard(Long id) {

        Board board = boardRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("게시글이 없습니다. ID: " + id));

        boardRepository.delete(board);
    }
}
