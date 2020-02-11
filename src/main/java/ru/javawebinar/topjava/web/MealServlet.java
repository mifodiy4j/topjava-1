package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealTo;
import ru.javawebinar.topjava.repository.InMemoryMealRepository;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.MealsUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

public class MealServlet extends HttpServlet {
    private static final Logger log = getLogger(MealServlet.class);
    private static final int CALORIES_PER_DAY = 2000;

    private MealRepository repository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        repository = new InMemoryMealRepository();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String id = request.getParameter("id");
        Meal meal = new Meal(id.isEmpty() ? null : Integer.valueOf(id),
                LocalDateTime.parse(request.getParameter("dateTime")),
                request.getParameter("description"),
                Integer.parseInt(request.getParameter("calories"))
                );
        repository.save(meal);
        response.sendRedirect("meals");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("redirect to meals");

        String action = request.getParameter("action");

        if (action == null) {
            log.info("get all");
            Collection<Meal> meals = repository.getAll();
            List<MealTo> mealToList = MealsUtil.convert(meals, CALORIES_PER_DAY);
            request.setAttribute("mealToList", mealToList);
            request.getRequestDispatcher("/meals.jsp").forward(request, response);

        } else if (action.equals("delete")) {
            int id = getId(request);
            log.info("Delete {}", id);
            repository.delete(id);
            response.sendRedirect("meals");

        } else if (action.equals("crate")) {
            int id = getId(request);
            log.info("Create {}", id);
            Meal meal = new Meal(LocalDateTime.now(), "", 1000);
            request.setAttribute("meal", meal);
            request.getRequestDispatcher("/mealEdit.jsp").forward(request, response);

        }

//        List<Meal> meals = MealsStorage.getInstance().getAll();
//        List<MealTo> mealToList = MealsUtil.convert(meals, CALORIES_PER_DAY);
//        request.setAttribute("mealToList", mealToList);
//        request.getRequestDispatcher("/meals.jsp").forward(request, response);

//        request.getRequestDispatcher("/users.jsp").forward(request, response);
//        response.sendRedirect("meals.jsp");
    }

    private int getId(HttpServletRequest request) {
        String paramId = Objects.requireNonNull(request.getParameter("id"));
        return Integer.parseInt(paramId);
    }
}
