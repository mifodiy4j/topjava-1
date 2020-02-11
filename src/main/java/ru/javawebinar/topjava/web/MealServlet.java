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
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

public class MealServlet extends HttpServlet {
    private static final Logger log = getLogger(MealServlet.class);
    private static final int CALORIES_PER_DAY = 2000;
    private static final int DEFAULT_CALORIES = 1000;

    private MealRepository repository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        repository = new InMemoryMealRepository();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
            log.info("get all meals");
            Collection<Meal> meals = repository.getAll();
            List<MealTo> mealToList = MealsUtil.convert(meals, CALORIES_PER_DAY);
            request.setAttribute("mealToList", mealToList);
            request.getRequestDispatcher("/meals.jsp").forward(request, response);

        } else if (action.equals("delete")) {
            int id = getId(request);
            log.info("Delete meal id = {}", id);
            repository.delete(id);
            response.sendRedirect("meals");

        } else if (action.equals("create")) {
            log.info("Create meal");
            Meal meal = new Meal(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), "", DEFAULT_CALORIES);
            request.setAttribute("meal", meal);
            request.getRequestDispatcher("/mealForm.jsp").forward(request, response);

        } else if (action.equals("update")) {
            int id = getId(request);
            log.info("Update meal id = {}", id);
            Meal meal = repository.get(id);
            request.setAttribute("meal", meal);
            request.getRequestDispatcher("/mealForm.jsp").forward(request, response);

        }
    }

    private int getId(HttpServletRequest request) {
        String paramId = Objects.requireNonNull(request.getParameter("id"));
        return Integer.parseInt(paramId);
    }
}
