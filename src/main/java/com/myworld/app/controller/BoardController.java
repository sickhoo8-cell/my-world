
package com.myworld.app.controller;

import com.myworld.app.dto.BoardDTO;
import com.myworld.app.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.myworld.app.entity.Board;

@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("boards", boardService.getAllBoards());
        return "board/list";
    }

    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("boardDTO", new BoardDTO());
        return "board/write";
    }

    @PostMapping("/write")
    public String write(
            @Valid @ModelAttribute("boardDTO") BoardDTO dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "board/write";
        }

        boardService.saveBoard(dto.getTitle(), dto.getContent());

        return "redirect:/board";
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        model.addAttribute("board", boardService.getBoard(id));

        return "board/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {

        Board board = boardService.getBoard(id);

        BoardDTO dto = new BoardDTO();
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());

        model.addAttribute("boardDTO", dto);
        model.addAttribute("boardId", id);

        return "board/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("boardDTO") BoardDTO dto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("boardId", id);
            return "board/edit";
        }

        boardService.updateBoard(id, dto.getTitle(), dto.getContent());

        return "redirect:/board/" + id;
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {

        boardService.deleteBoard(id);

        return "redirect:/board";
    }

}
