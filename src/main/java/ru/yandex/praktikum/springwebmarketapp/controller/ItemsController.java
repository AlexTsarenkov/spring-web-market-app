package ru.yandex.praktikum.springwebmarketapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.Paging;
import ru.yandex.praktikum.springwebmarketapp.service.ItemService;
import ru.yandex.praktikum.springwebmarketapp.service.SessionService;
import ru.yandex.praktikum.springwebmarketapp.utill.ItemQuantityAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping({"/items", "/"})
public class ItemsController {
    private final ItemService itemService;
    private final SessionService sessionService;
    private Map<Long, Item> currentPageitems;

    @GetMapping
    public ModelAndView getItems(@RequestParam(name = "search", required = false) String searchString,
                                 @RequestParam(name = "sort", required = false, defaultValue = "NO") String sort,
                                 @RequestParam(name = "pageNumber", required = false, defaultValue = "1") int pageNum,
                                 @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
                                 HttpServletRequest request) {
        Page<Item> itemPage = itemService.findAll(searchString, sort, pageNum, pageSize);
        List<Item> items = itemPage.getContent();

        currentPageitems = items.stream()
                .collect(Collectors.toMap(item -> item.getId(), item -> item));

        Map<Long, Item> itemQuantityMap = sessionService.getItemQuantityMap(request).orElse(new HashMap<>());
        if (!itemQuantityMap.isEmpty()) {
            items.stream().forEach(item -> {
                item.setCount(itemQuantityMap.containsKey(item.getId()) ?
                        itemQuantityMap.get(item.getId()).getCount() : 0);
            });
        }


        ModelAndView modelAndView = new ModelAndView("items");
        modelAndView.addObject("items", itemService.prepareDataForModel(itemPage.getContent()));
        modelAndView.addObject("search", searchString);
        modelAndView.addObject("sort", sort);
        modelAndView.addObject("paging", Paging.builder()
                .pageSize(pageSize)
                .pageNumber(pageNum)
                .hasPrevious(!itemPage.isFirst())
                .hasNext(itemPage.hasNext())
                .build());

        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getItem(@PathVariable Long id, HttpServletRequest request) {
        Map<Long, Item> itemQuantityMap = sessionService.getItemQuantityMap(request).orElse(new HashMap<>());
        Item item = currentPageitems.get(id);
        if (itemQuantityMap.containsKey(item.getId())) {
            item.setCount(itemQuantityMap.get(item.getId()).getCount());
        }
        ModelAndView modelAndView = new ModelAndView("item");
        modelAndView.addObject("item", item);
        return modelAndView;
    }

    @PostMapping("/{id}")
    public RedirectView updateItem(@PathVariable Long id,
                                   @RequestParam(name = "action") String action,
                                   HttpServletRequest request) {
        Item item = currentPageitems.get(id);
        sessionService.changeItemQuantity(item, request, ItemQuantityAction.valueOf(action));
        RedirectView redirectView = new RedirectView(String.format("/items/%d", id));
        return redirectView;
    }

    @PostMapping
    public RedirectView changeItemQuantity(@RequestParam(name = "id") Long itemId,
                                           @RequestParam(name = "search", required = false) String searchString,
                                           @RequestParam(name = "sort", required = false, defaultValue = "NO") String sort,
                                           @RequestParam(name = "pageNumber", required = false, defaultValue = "1") int pageNum,
                                           @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
                                           @RequestParam(name = "action") String action,
                                           HttpServletRequest request) {
        Item item = currentPageitems.get(itemId);
        sessionService.changeItemQuantity(item, request, ItemQuantityAction.valueOf(action));

        StringBuilder path = new StringBuilder("/items");

        if (searchString != null && !searchString.isEmpty()) {
            path.append("?search=").append(searchString);
            path.append(searchString);
        }

        if (sort != null && !sort.isEmpty()) {
            if (path.toString().endsWith("items")) {
                path.append("?sort=");
            } else {
                path.append("&sort=");
            }
            path.append(sort);
        }

        if (searchString.toString().endsWith("items")) {
            path.append("?pageNumber=");
        } else {
            path.append("&pageNumber=");
        }
        path.append(pageNum);

        path.append("&pageSize=");
        path.append(pageSize);

        return new RedirectView(path.toString());
    }
}
