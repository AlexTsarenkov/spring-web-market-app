package ru.yandex.praktikum.springwebmarketapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.Paging;
import ru.yandex.praktikum.springwebmarketapp.service.ItemService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping({"/items", "/"})
public class ItemsController {
    private final ItemService itemService;
    private Map<Long, Item> currentPageitems;

    @GetMapping
    public ModelAndView getItems(@RequestParam(name = "search", required = false) String searchString,
                                 @RequestParam(name = "sort", required = false, defaultValue = "NO") String sort,
                                 @RequestParam(name = "pageNumber", required = false, defaultValue = "1") int pageNum,
                                 @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
                                 HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Map<Long, Item> itemQuantityMap =
                (Map<Long, Item>) session.getAttribute("ITEM_QUANTITY_MAP");

        Page<Item> itemPage = itemService.findAll(searchString, sort, pageNum, pageSize);
        List<Item> items = itemPage.getContent();

        if (itemQuantityMap != null && !itemQuantityMap.isEmpty()) {
            items.stream().forEach(item -> {
                item.setCount(itemQuantityMap.containsKey(item.getId()) ?
                        itemQuantityMap.get(item.getId()).getCount() : 0);
            });
        }

        currentPageitems = items.stream()
                .collect(Collectors.toMap(item -> item.getId(), item -> item));

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

    @PostMapping
    public RedirectView changeItemQuantity(@RequestParam(name = "id") Long itemId,
                                           @RequestParam(name = "search", required = false) String searchString,
                                           @RequestParam(name = "sort", required = false, defaultValue = "NO") String sort,
                                           @RequestParam(name = "pageNumber", required = false, defaultValue = "1") int pageNum,
                                           @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
                                           @RequestParam(name = "action") String action,
                                           HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Map<Long, Item> itemQuantityMap =
                (Map<Long, Item>) session.getAttribute("ITEM_QUANTITY_MAP");
        if (itemQuantityMap == null) {
            itemQuantityMap = new HashMap<>();
        }
        if (itemQuantityMap.containsKey(itemId)) {
            int itemQuantity = itemQuantityMap.get(itemId).getCount();

            itemQuantity = switch (action) {
                case "PLUS" -> itemQuantity + 1;
                case "MINUS" -> itemQuantity >= 1 ? itemQuantity - 1 : 0;
                default -> itemQuantity;
            };

            itemQuantityMap.get(itemId).setCount(itemQuantity);

        } else {
            if (action.equals("PLUS")) {
                Item item = currentPageitems.get(itemId);
                item.setCount(item.getCount() + 1);
                itemQuantityMap.put(itemId, item);
            }
        }

        session.setAttribute("ITEM_QUANTITY_MAP", itemQuantityMap);

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
